package ij.process;

import ij.Prefs;
import ij.process.SetPlaneOperation.PixelType;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;
import java.util.Random;

import loci.common.DataTools;

import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.ByteType;
import mpicbg.imglib.type.numeric.integer.GenericByteType;
import mpicbg.imglib.type.numeric.integer.GenericShortType;
import mpicbg.imglib.type.numeric.integer.IntType;
import mpicbg.imglib.type.numeric.integer.LongType;
import mpicbg.imglib.type.numeric.integer.ShortType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import mpicbg.imglib.type.numeric.integer.UnsignedIntType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;
import mpicbg.imglib.type.numeric.real.DoubleType;
import mpicbg.imglib.type.numeric.real.FloatType;

// NOTES
// Image may change to Img to avoid name conflict with java.awt.Image.
//
// TODO
// 1. Add a new container that uses array-backed data of the proper primitive
//    type, plane by plane.
// 2. Then we can return the data with getPixels by reference in that case
//    (and use the current cursor approach in other cases).
//
// For create8BitImage, we can call imageData.getDisplay().get8Bit* to extract
// displayable image data as bytes [was LocalizableByPlaneCursor relevant here
// as well? can't remember].
//
// For getPlane* methods, we can use a LocalizablePlaneCursor (see
// ImageJVirtualStack.extractSliceFloat for an example) to grab the data
// plane-by-plane; this way the container knows the optimal way to traverse.

// More TODO / NOTES
//   Recently pulled a lot of code in for filters/convolve/etc. This could should be refactored to use multiple classes to implement these
//     features. Waiting to do this once I've got all tests passing for byte/short/floats.
//   Make sure that resetMinAndMax() and/or findMinAndMax() are called at appropriate times. Maybe base class does this for us?
//   I have not yet mirrored ImageJ's signed 16 bit hacks. Will need to test Image<ShortType> and see if things work versus an ImagePlus.
//   Review imglib's various cursors and perhaps change which ones I'm using.
//   Nearly all methods below broken for ComplexType and and LongType
//   All methods below assume x and y first two dimensions and that the Image<T> consists of XY planes
//     In createImagePlus we rely on image to be 5d or less. We should modify ImageJ to have a setDimensions(int[] dimension) and integrate
//     its use throughout the application.
//   Just realized the constructor that takes plane number may not make a ton of sense as implemented. We assume X & Y are the first 2 dimensions.
//     This ideally means that its broken into planes in the last two dimensions. This may just be a convention we use but it may break things too.
//     Must investigate.
//   Rename ImgLibProcessor to GenericProcessor????
//   Rename TypeManager to TypeUtils
//   Rename Image<> to something else like Dataset<> or NumericDataset<>
//   Improvements to ImgLib
//     Rename LocalizableByDimCursor to PositionCursor. Be able to say posCursor.setPosition(int[] pos) and posCursor.setPosition(long sampIndex).
//       Another possibility: just call it a Cursor. And then cursor.get() or cursor.get(int[] pos) or cursor.get(long sampleNumber) 
//     Create ROICursors directly from an Image<T> and have that ctor setup its own LocalizableByDimCursor for you.
//     Allow new Image<ByteType>(rows,cols). Have a default factory and a default container and have other constructors that you use in the cases
//       where you want to specify them. Also allow new Image<T extends RealType<T>>(rows,cols,pixels).
//     Have a few static ContainerFactories that we can just refer to rather than newing them all the time. Maybe do so also for Types so that
//       we're not always having to pass a new UnsignedByteType() but rather a static one and if a new one needed the ctor can clone.
//     In general come up with much shorter names to make use less cumbersome.
//     It would be good to specify axis order of a cursor's traversal : new Cursor(image,"zxtcy") and then just call cursor.get() as needed.
//       Also could do cursor.fwd("t" or some enum T) which would iterate forward in the (here T) plane of the image skipping large groups of
//       samples at a time. 
//     Put our ImageUtils class code somewhere in Imglib. Also maybe include the Index and Span classes too. Also TypeManager class.

public class ImgLibProcessor<T extends RealType<T>> extends ImageProcessor implements java.lang.Cloneable
{
	// copied from various processors
	public static final int BLUR_MORE=0, FIND_EDGES=1, MEDIAN_FILTER=2, MIN=3, MAX=4, CONVOLVE=5, ERODE=10, DILATE=11;

	// safer way
	public static enum FilterType {BLUR_MORE, FIND_EDGES, MEDIAN_FILTER, MIN, MAX, CONVOLVE, ERODE, DILATE};
	
	// TODO later: define a FilterOperation class that gets applied. Create various filters from it.
	
	//****************** Instance variables *******************************************************
	
	private final Image<T> imageData;

	// TODO: How can we use generics here without breaking javac?
	private final RealType<?> type;
	private boolean isIntegral;
	private byte[] pixels8;
	private Snapshot<T> snapshot;
	private ImageProperties imageProperties;
	// TODO - move some of these next ones to imageProperties
	private double min, max;
	private double fillColor;
	private int binaryCount, binaryBackground;
	
	private ThreadLocal<LocalizableByDimCursor<T>> cachedCursor =
		new ThreadLocal<LocalizableByDimCursor<T>>()
		{
			@Override
			protected LocalizableByDimCursor<T> initialValue() {
				return imageData.createLocalizableByDimCursor();
			}

			@Override
			protected void finalize() throws Throwable {
				try {
					cachedCursor.get().close();
					//System.out.println("closing cursor at "+System.nanoTime());
				} finally {
					super.finalize();
				}
			}
		};

	//****************** Constructors *************************************************************
	
	public ImgLibProcessor(Image<T> img, T type, int[] thisPlanePosition)
	{
		final int[] dims = img.getDimensions();
		
		if (dims.length < 2)
			throw new IllegalArgumentException("Image must be at least 2-D");

		this.imageData = img;
		this.type = type;
		
		//assign the properties object for the image
		this.imageProperties = new ImageProperties();
		
		this.imageProperties.setPlanePosition( thisPlanePosition );

		super.width = dims[0]; // TODO: Dimensional labels are safer way to find X
		super.height = dims[1]; // TODO: Dimensional labels are safer way to find Y
	
		this.fillColor = this.type.getMaxValue();
		
		this.isIntegral = TypeManager.isIntegralType(this.type);
		
		if (this.type instanceof UnsignedByteType)
			this.imageProperties.setBackgroundValue(255);

		resetRoi();
		
		findMinAndMax();
	}

	public ImgLibProcessor(Image<T> img, T type, long planeNumber)
	{
		this(img, type, ImageUtils.getPlanePosition(img.getDimensions(), planeNumber));
	}

	
	//****************** Helper methods *******************************************************

	private long getNumPixels(Image<T> image)
	{
		return ImageUtils.getTotalSamples(image.getDimensions());
	}

	private void findMinAndMax()
	{
		// TODO - should do something different for UnsignedByte (involving LUT) if we mirror ByteProcessor

		//get the current image data
		int[] imageOrigin = Index.create(0, 0, getPlanePosition());
		int[] imageSpan = Span.singlePlane(getWidth(), getHeight(), this.imageData.getNumDimensions());

		MinMaxOperation<T> mmOp = new MinMaxOperation<T>(this.imageData,imageOrigin,imageSpan);
		
		Operation.apply(mmOp);
		
		setMinAndMaxOnly(mmOp.getMin(), mmOp.getMax());
		
		showProgress(1.0);
	}
	
	
	// Throws an exception if the LUT length is wrong for the pixel layout type
	private void verifyLutLengthOkay( int[] lut )
	{
		if ( this.type instanceof GenericByteType< ? > ) {
			
			if (lut.length!=256)
				throw new IllegalArgumentException("lut.length != expected length for type " + type );
		}
		else if( this.type instanceof GenericShortType< ? > ) {
			
			if (lut.length!=65536)
				throw new IllegalArgumentException("lut.length != expected length for type " + type );
		}
		else {
			
			throw new IllegalArgumentException("LUT NA for type " + type ); 
		}
	}
	
	// NOTE - eliminating bad cast warnings in this method by explicit casts causes Hudson tests to fail
	
	private Object getCopyOfPixelsFromImage(Image<T> image, RealType<?> type, int[] planePos)
	{
		int w = image.getDimension(0);
		int h = image.getDimension(1);
		
		if (type instanceof ByteType) {
			Image<ByteType> im = (Image) image;
			return ImageUtils.getPlaneBytes(im, w, h, planePos);
		}
		if (type instanceof UnsignedByteType) {
			Image<UnsignedByteType> im = (Image) image;
			return ImageUtils.getPlaneUnsignedBytes(im, w, h, planePos);
		}
		if (type instanceof ShortType) {
			Image<ShortType> im = (Image) image;
			return ImageUtils.getPlaneShorts(im, w, h, planePos );
		}
		if (type instanceof UnsignedShortType) {
			Image<UnsignedShortType> im = (Image) image;
			return ImageUtils.getPlaneUnsignedShorts(im, w, h, planePos);
		}
		if (type instanceof IntType) {
			Image<IntType> im = (Image) image;
			return ImageUtils.getPlaneInts(im, w, h, planePos);
		}
		if (type instanceof UnsignedIntType) {
			Image<UnsignedIntType> im = (Image) image;
			return ImageUtils.getPlaneUnsignedInts(im, w, h, planePos);
		}
		if (type instanceof LongType) {
			Image<LongType> im = (Image) image;
			return ImageUtils.getPlaneLongs(im, w, h, planePos);
		}
		if (type instanceof FloatType) {
			Image<FloatType> im = (Image) image;
			return ImageUtils.getPlaneFloats(im, w, h, planePos);
		}
		if (type instanceof DoubleType) {
			Image<DoubleType> im = (Image) image;
			return ImageUtils.getPlaneDoubles(im, w, h, planePos);
		}
		if (type instanceof LongType) {
			Image<LongType> im = (Image) image;
			return ImageUtils.getPlaneLongs(im, w, h, planePos);
		}
		return ImageUtils.getPlaneData(image, w, h, planePos);
	}
	
	private void setPlane(Image<T> theImage, int[] origin, Object pixels, PixelType inputType, long numPixels)
	{
		if (numPixels != getNumPixels(theImage))
			throw new IllegalArgumentException("setPlane() error: input image does not have same dimensions as passed in pixels");

		boolean isUnsigned = TypeManager.isUnsignedType(this.type);
		
		SetPlaneOperation<T> setOp = new SetPlaneOperation<T>(theImage, origin, pixels, inputType, isUnsigned);
		
		Operation.apply(setOp);
	}
	
	private void setImagePlanePixels(Image<T> image, int[] planePosition, Object pixels)
	{
		int[] position = Index.create(0,0,planePosition);
		
		if (pixels instanceof byte[])
			
			setPlane(image, position, pixels, PixelType.BYTE, ((byte[])pixels).length);
		
		else if (pixels instanceof short[])
			
			setPlane(image, position, pixels, PixelType.SHORT, ((short[])pixels).length);
		
		else if (pixels instanceof int[])
			
			setPlane(image, position, pixels, PixelType.INT, ((int[])pixels).length);
		
		else if (pixels instanceof float[])
			
			setPlane(image, position, pixels, PixelType.FLOAT, ((float[])pixels).length);
		
		else if (pixels instanceof double[])
			
			setPlane(image, position, pixels, PixelType.DOUBLE, ((double[])pixels).length);
		
		else if (pixels instanceof long[])
			
			setPlane(image, position, pixels, PixelType.LONG, ((long[])pixels).length);
		
		else
			throw new IllegalArgumentException("setImagePlanePixels(): unknown object passed as pixels - "+ pixels.getClass());
	}
	
	@Override
	byte[] create8BitImage()
	{
		// TODO: use imageData.getDisplay().get8Bit* methods
		Object pixels = getPixels();

		if (pixels instanceof byte[])
		{
			this.pixels8 = (byte[]) pixels;
		}
		else if (pixels instanceof short[])
		{
			short[] pix = (short[]) pixels;
			this.pixels8 = DataTools.shortsToBytes(pix, false);
		}
		else if (pixels instanceof int[])
		{
			int[] pix = (int[]) pixels;
			this.pixels8 = DataTools.intsToBytes(pix, false);
		}
		else if (pixels instanceof float[])
		{
			float[] pix = (float[]) pixels;
			this.pixels8 = DataTools.floatsToBytes(pix, false);
		}
		else if (pixels instanceof double[])
		{
			double[] pix = (double[]) pixels;
			this.pixels8 = DataTools.doublesToBytes(pix, false);
		}
		else if (pixels instanceof long[])
		{
			long[] pix = (long[]) pixels;
			this.pixels8 = DataTools.longsToBytes(pix, false);
		}

		return this.pixels8;
	}

	private void doLutProcess(int op, double value)
	{
		double SCALE = 255.0/Math.log(255.0);
		int v;
		
		int[] lut = new int[256];
		for (int i=0; i<256; i++) {
			switch(op) {
				case INVERT:
					v = 255 - i;
					break;
				case FILL:
					v = fgColor;
					break;
				case ADD:
					v = i + (int)value;
					break;
				case MULT:
					v = (int)Math.round(i * value);
					break;
				case AND:
					v = i & (int)value;
					break;
				case OR:
					v = i | (int)value;
					break;
				case XOR:
					v = i ^ (int)value;
					break;
				case GAMMA:
					v = (int)(Math.exp(Math.log(i/255.0)*value)*255.0);
					break;
				case LOG:
					if (i==0)
						v = 0;
					else
						v = (int)(Math.log(i) * SCALE);
					break;
				case EXP:
					v = (int)(Math.exp(i/SCALE));
					break;
				case SQR:
						v = i*i;
					break;
				case SQRT:
						v = (int)Math.sqrt(i);
					break;
				case MINIMUM:
					if (i<value)
						v = (int)value;
					else
						v = i;
					break;
				case MAXIMUM:
					if (i>value)
						v = (int)value;
					else
						v = i;
					break;
				 default:
				 	v = i;
			}
			if (v < 0)
				v = 0;
			if (v > 255)
				v = 255;
			lut[i] = v;
		}
		applyTable(lut);
	}

	
	private void doProcess(int op, double value)
	{
		if (this.type instanceof UnsignedByteType)
		{
			doLutProcess(op,value);
			return;
		}

		// TODO - this code pulled from FloatProcessor. ShortProcessor is slightly different. Also this code may fail for signed 16-bit data.
		
		double range = max - min;
		double c, v1, v2;
		boolean resetMinMax = roiWidth==width && roiHeight==height && !(op==FILL);
		c = value;
		for (int y=roiY; y<(roiY+roiHeight); y++) {
			int i = y * width + roiX;
			for (int x=roiX; x<(roiX+roiWidth); x++) {
				v1 = getd(i);
				switch(op) {
					case INVERT:
						v2 = max - (v1 - min);
						break;
					case FILL:
						v2 = fillColor;
						break;
					case ADD:
						v2 = v1 + c;
						break;
					case MULT:
						v2 = v1 * c;
						break;
					case AND:
						if (this.isIntegral)
							v2 = (int)v1 & (int)value;
						else
							v2 = v1;
						break;
					case OR:
						if (this.isIntegral)
							v2 = (int)v1 | (int)value;
						else
							v2 = v1;
						break;
					case XOR:
						if (this.isIntegral)
							v2 = (int)v1 ^ (int)value;
						else
							v2 = v1;
						break;
					case GAMMA:
						if (this.isIntegral)
						{
							if (range<=0.0 || v1 <= min)
								v2 = v1;
							else					
								v2 = (int)(Math.exp(value*Math.log((v1-min)/range))*range+min);
						}
						else // float
						{
							if (v1 <= 0)
								v2 = 0f;
							else
								v2 = Math.exp(c*Math.log(v1));
						}
						break;
					case LOG:
						if (this.isIntegral)
						{
							if (v1<=0)
								v2 = 0;
							else 
								v2 = (int)(Math.log(v1)*(max/Math.log(max)));
						}
						else // float
						{
							if (v1<=0f)
								v2 = 0f;
							else
								v2 = Math.log(v1);
						}
						break;
					case EXP:
						// TODO - this does not match ShortProc for signed data
						//		- also int type falls thru to float. Since IJ has no true int type don't know best behavior here. 
						if (this.type instanceof GenericShortType<?>)
						{
							v2 = (int)(Math.exp(v1*(Math.log(this.max)/this.max)));
						}
						else // a float or int type
							v2 = Math.exp(v1);
						break;
					case SQR:
							v2 = v1*v1;
						break;
					case SQRT:
						if (v1<=0f)
							v2 = 0f;
						else
							v2 = Math.sqrt(v1);
						break;
					case ABS:
							v2 = Math.abs(v1);
						break;
					case MINIMUM:
						if (v1<value)
							v2 = value;
						else
							v2 = v1;
						break;
					case MAXIMUM:
						if (v1>value)
							v2 = value;
						else
							v2 = v1;
						break;
					 default:
					 	v2 = v1;
				}
				setd(i++, v2);
			}
		}
		if (resetMinMax)
			findMinAndMax();
	}
	
	/** Uses bilinear interpolation to find the pixel value at real coordinates (x,y). */
	private double calcBilinearPixel(double x, double y)
	{
		int xbase = (int)x;
		int ybase = (int)y;
		double xFraction = x - xbase;
		double yFraction = y - ybase;
		double lowerLeft = getd(xbase,ybase);
		double lowerRight = getd(xbase+1,ybase);
		double upperRight = getd(xbase+1,ybase+1);
		double upperLeft = getd(xbase,ybase+1);
		double upperAverage = upperLeft + xFraction * (upperRight - upperLeft);
		double lowerAverage = lowerLeft + xFraction * (lowerRight - lowerLeft);
		return lowerAverage + yFraction * (upperAverage - lowerAverage);
	}

	// copied from ByteProcessor - really belongs elsewhere
	private int findMedian(int[] values)
	{
		//Finds the 5th largest of 9 values
		for (int i = 1; i <= 4; i++) {
			int max = 0;
			int mj = 1;
			for (int j = 1; j <= 9; j++)
				if (values[j] > max) {
					max = values[j];
					mj = j;
				}
			values[mj] = 0;
		}
		int max = 0;
		for (int j = 1; j <= 9; j++)
			if (values[j] > max)
				max = values[j];
		return max;
	}

	private int getEdgePixel(byte[] pixels2, int x, int y)
	{
		if (x<=0) x = 0;
		if (x>=width) x = width-1;
		if (y<=0) y = 0;
		if (y>=height) y = height-1;
		return pixels2[x+y*width]&255;
	}

	private int getEdgePixel0(byte[] pixels2, int background, int x, int y)
	{
		if (x<0 || x>width-1 || y<0 || y>height-1)
			return background;
		else
			return pixels2[x+y*width]&255;
	}

	private int getEdgePixel1(byte[] pixels2, int foreground, int x, int y)
	{
		if (x<0 || x>width-1 || y<0 || y>height-1)
			return foreground;
		else
			return pixels2[x+y*width]&255;
	}

	private void filterEdge(FilterType type, byte[] pixels2, int n, int x, int y, int xinc, int yinc)
	{
		int p1, p2, p3, p4, p5, p6, p7, p8, p9;
		int sum=0, sum1, sum2;
		int count;
		int binaryForeground = 255 - binaryBackground;
		int bg = binaryBackground;
		int fg = binaryForeground;
		
		for (int i=0; i<n; i++)
		{
			if ((!Prefs.padEdges && type==FilterType.ERODE) || type==FilterType.DILATE)
			{
				p1=getEdgePixel0(pixels2,bg,x-1,y-1); p2=getEdgePixel0(pixels2,bg,x,y-1); p3=getEdgePixel0(pixels2,bg,x+1,y-1);
				p4=getEdgePixel0(pixels2,bg,x-1,y); p5=getEdgePixel0(pixels2,bg,x,y); p6=getEdgePixel0(pixels2,bg,x+1,y);
				p7=getEdgePixel0(pixels2,bg,x-1,y+1); p8=getEdgePixel0(pixels2,bg,x,y+1); p9=getEdgePixel0(pixels2,bg,x+1,y+1);
			}
			else if (Prefs.padEdges && type==FilterType.ERODE)
			{
				p1=getEdgePixel1(pixels2,fg, x-1,y-1); p2=getEdgePixel1(pixels2,fg,x,y-1); p3=getEdgePixel1(pixels2,fg,x+1,y-1);
				p4=getEdgePixel1(pixels2,fg, x-1,y); p5=getEdgePixel1(pixels2,fg,x,y); p6=getEdgePixel1(pixels2,fg,x+1,y);
				p7=getEdgePixel1(pixels2,fg,x-1,y+1); p8=getEdgePixel1(pixels2,fg,x,y+1); p9=getEdgePixel1(pixels2,fg,x+1,y+1);
			}
			else
			{
				p1=getEdgePixel(pixels2,x-1,y-1); p2=getEdgePixel(pixels2,x,y-1); p3=getEdgePixel(pixels2,x+1,y-1);
				p4=getEdgePixel(pixels2,x-1,y); p5=getEdgePixel(pixels2,x,y); p6=getEdgePixel(pixels2,x+1,y);
				p7=getEdgePixel(pixels2,x-1,y+1); p8=getEdgePixel(pixels2,x,y+1); p9=getEdgePixel(pixels2,x+1,y+1);
			}
			switch (type) {
				case BLUR_MORE:
					sum = (p1+p2+p3+p4+p5+p6+p7+p8+p9)/9;
					break;
				case FIND_EDGES: // 3x3 Sobel filter
					sum1 = p1 + 2*p2 + p3 - p7 - 2*p8 - p9;
					sum2 = p1 + 2*p4 + p7 - p3 - 2*p6 - p9;
					sum = (int)Math.sqrt(sum1*sum1 + sum2*sum2);
					if (sum> 255) sum = 255;
					break;
				case MIN:
					sum = p5;
					if (p1<sum) sum = p1;
					if (p2<sum) sum = p2;
					if (p3<sum) sum = p3;
					if (p4<sum) sum = p4;
					if (p6<sum) sum = p6;
					if (p7<sum) sum = p7;
					if (p8<sum) sum = p8;
					if (p9<sum) sum = p9;
					break;
				case MAX:
					sum = p5;
					if (p1>sum) sum = p1;
					if (p2>sum) sum = p2;
					if (p3>sum) sum = p3;
					if (p4>sum) sum = p4;
					if (p6>sum) sum = p6;
					if (p7>sum) sum = p7;
					if (p8>sum) sum = p8;
					if (p9>sum) sum = p9;
					break;
				case ERODE:
					if (p5==binaryBackground)
						sum = binaryBackground;
					else {
						count = 0;
						if (p1==binaryBackground) count++;
						if (p2==binaryBackground) count++;
						if (p3==binaryBackground) count++;
						if (p4==binaryBackground) count++;
						if (p6==binaryBackground) count++;
						if (p7==binaryBackground) count++;
						if (p8==binaryBackground) count++;
						if (p9==binaryBackground) count++;							
						if (count>=binaryCount)
							sum = binaryBackground;
						else
							sum = binaryForeground;
					}
					break;
				case DILATE:
					if (p5==binaryForeground)
						sum = binaryForeground;
					else {
						count = 0;
						if (p1==binaryForeground) count++;
						if (p2==binaryForeground) count++;
						if (p3==binaryForeground) count++;
						if (p4==binaryForeground) count++;
						if (p6==binaryForeground) count++;
						if (p7==binaryForeground) count++;
						if (p8==binaryForeground) count++;
						if (p9==binaryForeground) count++;							
						if (count>=binaryCount)
							sum = binaryForeground;
						else
							sum = binaryBackground;
					}
					break;
			}
			setd(x, y, (byte)sum); // we know its an unsigned byte type so cast is correct
			x+=xinc;
			y+=yinc;
		}
	}

	private void filterUnsignedByte(FilterType type)
	{
		int p1, p2, p3, p4, p5, p6, p7, p8, p9;
		int inc = roiHeight/25;
		if (inc<1) inc = 1;
		
		byte[] pixels2 = (byte[])getPixelsCopy();
		if (width==1) {
			filterEdge(type, pixels2, roiHeight, roiX, roiY, 0, 1);
			return;
		}
		int offset, sum1, sum2=0, sum=0;
		int[] values = new int[10];
		int rowOffset = width;
		int count;
		int binaryForeground = 255 - binaryBackground;
		for (int y=yMin; y<=yMax; y++) {
			offset = xMin + y * width;
			p2 = pixels2[offset-rowOffset-1]&0xff;
			p3 = pixels2[offset-rowOffset]&0xff;
			p5 = pixels2[offset-1]&0xff;
			p6 = pixels2[offset]&0xff;
			p8 = pixels2[offset+rowOffset-1]&0xff;
			p9 = pixels2[offset+rowOffset]&0xff;

			for (int x=xMin; x<=xMax; x++) {
				p1 = p2; p2 = p3;
				p3 = pixels2[offset-rowOffset+1]&0xff;
				p4 = p5; p5 = p6;
				p6 = pixels2[offset+1]&0xff;
				p7 = p8; p8 = p9;
				p9 = pixels2[offset+rowOffset+1]&0xff;

				switch (type) {
					case BLUR_MORE:
						sum = (p1+p2+p3+p4+p5+p6+p7+p8+p9)/9;
						break;
					case FIND_EDGES: // 3x3 Sobel filter
						sum1 = p1 + 2*p2 + p3 - p7 - 2*p8 - p9;
						sum2 = p1  + 2*p4 + p7 - p3 - 2*p6 - p9;
						sum = (int)Math.sqrt(sum1*sum1 + sum2*sum2);
						if (sum> 255) sum = 255;
						break;
					case MEDIAN_FILTER:
						values[1]=p1; values[2]=p2; values[3]=p3; values[4]=p4; values[5]=p5;
						values[6]=p6; values[7]=p7; values[8]=p8; values[9]=p9;
						sum = findMedian(values);
						break;
					case MIN:
						sum = p5;
						if (p1<sum) sum = p1;
						if (p2<sum) sum = p2;
						if (p3<sum) sum = p3;
						if (p4<sum) sum = p4;
						if (p6<sum) sum = p6;
						if (p7<sum) sum = p7;
						if (p8<sum) sum = p8;
						if (p9<sum) sum = p9;
						break;
					case MAX:
						sum = p5;
						if (p1>sum) sum = p1;
						if (p2>sum) sum = p2;
						if (p3>sum) sum = p3;
						if (p4>sum) sum = p4;
						if (p6>sum) sum = p6;
						if (p7>sum) sum = p7;
						if (p8>sum) sum = p8;
						if (p9>sum) sum = p9;
						break;
					case ERODE:
						if (p5==binaryBackground)
							sum = binaryBackground;
						else {
							count = 0;
							if (p1==binaryBackground) count++;
							if (p2==binaryBackground) count++;
							if (p3==binaryBackground) count++;
							if (p4==binaryBackground) count++;
							if (p6==binaryBackground) count++;
							if (p7==binaryBackground) count++;
							if (p8==binaryBackground) count++;
							if (p9==binaryBackground) count++;							
							if (count>=binaryCount)
								sum = binaryBackground;
							else
							sum = binaryForeground;
						}
						break;
					case DILATE:
						if (p5==binaryForeground)
							sum = binaryForeground;
						else {
							count = 0;
							if (p1==binaryForeground) count++;
							if (p2==binaryForeground) count++;
							if (p3==binaryForeground) count++;
							if (p4==binaryForeground) count++;
							if (p6==binaryForeground) count++;
							if (p7==binaryForeground) count++;
							if (p8==binaryForeground) count++;
							if (p9==binaryForeground) count++;							
							if (count>=binaryCount)
								sum = binaryForeground;
							else
								sum = binaryBackground;
						}
						break;
				}
				
				setd(offset++, (byte)sum); // we know its unsigned byte type so cast is correct
			}
			//if (y%inc==0)
			//	showProgress((double)(y-roiY)/roiHeight);
		}
		if (xMin==1) filterEdge(type, pixels2, roiHeight, roiX, roiY, 0, 1);
		if (yMin==1) filterEdge(type, pixels2, roiWidth, roiX, roiY, 1, 0);
		if (xMax==width-2) filterEdge(type, pixels2, roiHeight, width-1, roiY, 0, 1);
		if (yMax==height-2) filterEdge(type, pixels2, roiWidth, roiX, height-1, 1, 0);
		//showProgress(1.0);
	}
	
	private void filterGeneralType(FilterType type, int[] kernel)
	{
		double v1, v2, v3;           //input pixel values around the current pixel
		double v4, v5, v6;
		double v7, v8, v9;
		double k1=0, k2=0, k3=0;  //kernel values (used for CONVOLVE only)
		double k4=0, k5=0, k6=0;
		double k7=0, k8=0, k9=0;
		double scale = 0;
		if (type==FilterType.CONVOLVE) {
			if (kernel == null)
				throw new IllegalArgumentException("filterGeneralType(): convolve asked for but no kernel given");
			k1=kernel[0]; k2=kernel[1]; k3=kernel[2];
			k4=kernel[3]; k5=kernel[4]; k6=kernel[5];
			k7=kernel[6]; k8=kernel[7]; k9=kernel[8];
			for (int i=0; i<kernel.length; i++)
				scale += kernel[i];
			if (scale==0) scale = 1;
			scale = 1/scale; //multiplication factor (multiply is faster than divide)
		}
		int inc = roiHeight/25;
		if (inc<1) inc = 1;
		
		double[] pixels2 = ImageUtils.getPlaneData(this.imageData, getWidth(), getHeight(), getPlanePosition());
		int xEnd = roiX + roiWidth;
		int yEnd = roiY + roiHeight;
		for (int y=roiY; y<yEnd; y++) {
			int p = roiX + y*width;            //points to current pixel
			int p6 = p - (roiX>0 ? 1 : 0);      //will point to v6, currently lower
			int p3 = p6 - (y>0 ? width : 0);    //will point to v3, currently lower
			int p9 = p6 + (y<height-1 ? width : 0); // ...  to v9, currently lower
			v2 = pixels2[p3];
			v5 = pixels2[p6];
			v8 = pixels2[p9];
			if (roiX>0) { p3++; p6++; p9++; }
			v3 = pixels2[p3];
			v6 = pixels2[p6];
			v9 = pixels2[p9];

			switch (type) {
				case BLUR_MORE:
					for (int x=roiX; x<xEnd; x++,p++)
					{
						if (x<width-1) { p3++; p6++; p9++; }
						v1 = v2; v2 = v3;
						v3 = pixels2[p3];
						v4 = v5; v5 = v6;
						v6 = pixels2[p6];
						v7 = v8; v8 = v9;
						v9 = pixels2[p9];
						double val = v1+v2+v3+v4+v5+v6+v7+v8+v9;
						if (this.type instanceof GenericShortType<?>)
						{
							val += 4;
							val /= 9;
							val = (short) val;
						}
						else
							val *= 0.11111111; //0.111... = 1/9
						setd(p, val);
					}
					break;
				case FIND_EDGES:
					for (int x=roiX; x<xEnd; x++,p++) {
						if (x<width-1) { p3++; p6++; p9++; }
						v1 = v2; v2 = v3;
						v3 = pixels2[p3];
						v4 = v5; v5 = v6;
						v6 = pixels2[p6];
						v7 = v8; v8 = v9;
						v9 = pixels2[p9];
						double sum1 = (v1 + 2*v2 + v3 - v7 - 2*v8 - v9);
						double sum2 = (v1 + 2*v4 + v7 - v3 - 2*v6 - v9);
						double offset = 0;
						if (this.isIntegral)
							offset = 0.5;
						setd(p, Math.sqrt(sum1*sum1 + sum2*sum2) + offset);
					}
					break;
				case CONVOLVE:
					for (int x=roiX; x<xEnd; x++,p++) {
						if (x<width-1) { p3++; p6++; p9++; }
						v1 = v2; v2 = v3;
						v3 = pixels2[p3];
						v4 = v5; v5 = v6;
						v6 = pixels2[p6];
						v7 = v8; v8 = v9;
						v9 = pixels2[p9];
						double sum = k1*v1 + k2*v2 + k3*v3
									+ k4*v4 + k5*v5 + k6*v6
									+ k7*v7 + k8*v8 + k9*v9;
						sum *= scale;
						if (this.isIntegral)
							sum = TypeManager.boundValueToType(this.type, sum);
						setd(p, sum);
					}
					break;
			}
			if (y%inc==0)
				showProgress((double)(y-roiY)/roiHeight);
		}
		showProgress(1.0);
	}
	
	private void convolve3x3UnsignedByte(int[] kernel)
	{
		int p1, p2, p3,
			p4, p5, p6,
			p7, p8, p9;
		int k1=kernel[0], k2=kernel[1], k3=kernel[2],
			k4=kernel[3], k5=kernel[4], k6=kernel[5],
			k7=kernel[6], k8=kernel[7], k9=kernel[8];
	
		int scale = 0;
		for (int i=0; i<kernel.length; i++)
			scale += kernel[i];
		if (scale==0) scale = 1;
		int inc = roiHeight/25;
		if (inc<1) inc = 1;
		
		byte[] pixels2 = (byte[])getPixelsCopy();
		int offset, sum;
		int rowOffset = width;
		for (int y=yMin; y<=yMax; y++) {
			offset = xMin + y * width;
			p1 = 0;
			p2 = pixels2[offset-rowOffset-1]&0xff;
			p3 = pixels2[offset-rowOffset]&0xff;
			p4 = 0;
			p5 = pixels2[offset-1]&0xff;
			p6 = pixels2[offset]&0xff;
			p7 = 0;
			p8 = pixels2[offset+rowOffset-1]&0xff;
			p9 = pixels2[offset+rowOffset]&0xff;
	
			for (int x=xMin; x<=xMax; x++) {
				p1 = p2; p2 = p3;
				p3 = pixels2[offset-rowOffset+1]&0xff;
				p4 = p5; p5 = p6;
				p6 = pixels2[offset+1]&0xff;
				p7 = p8; p8 = p9;
				p9 = pixels2[offset+rowOffset+1]&0xff;
	
				sum = k1*p1 + k2*p2 + k3*p3
					+ k4*p4 + k5*p5 + k6*p6
					+ k7*p7 + k8*p8 + k9*p9;
				sum /= scale;
	
				if(sum>255) sum= 255;
				if(sum<0) sum= 0;
	
				setd(offset++, (byte)sum);
			}
			if (y%inc==0)
				showProgress((double)(y-roiY)/roiHeight);
		}
		showProgress(1.0);
	}
	
	private void convolve3x3GeneralType(int[] kernel)
	{
		filterGeneralType(FilterType.CONVOLVE, kernel);
	}
	
	//****************** public methods *******************************************************

	@Override
	public void abs()
	{
		doProcess(ABS, 0.0);
	}

	@Override
	public void add(int value)
	{
		doProcess(ADD, value);
	}
	
	@Override
	public void add(double value)
	{
		doProcess(ADD, value);
	}
	
	@Override
	public void and(int value)
	{
		doProcess(AND,value);
	}

	@Override
	public void autoThreshold()
	{
		if (this.isIntegral)
			super.autoThreshold();
	}
	
	@Override
	public void applyTable(int[] lut) 
	{
		if (!this.isIntegral)
			return;

		verifyLutLengthOkay(lut);
		
		Rectangle roi = getRoi();
		
		int[] index = Index.create(roi.x, roi.y, getPlanePosition());
		int[] span = Span.singlePlane(roi.width, roi.height, this.imageData.getNumDimensions());

		ApplyLutOperation<T> lutOp = new ApplyLutOperation<T>(this.imageData,index,span,lut);
		
		Operation.apply(lutOp);
	}

	public ImgLibProcessor<T> getImgLibProcThatMatchesMyType(ImageProcessor inputProc)
	{
		// if inputProc's type matches me
		//   just return inputProc
		
		if (inputProc instanceof ImgLibProcessor<?>)
		{
			ImgLibProcessor<?> imglibProc = (ImgLibProcessor<?>) inputProc;

			if (TypeManager.sameKind(this.type,imglibProc.getType()))
				return (ImgLibProcessor<T>) imglibProc;
		}
		
		// otherwise
		//   create a processor of my type with size matching ip's dimensions
		//   populate the pixels
		//   return it

		Image<T> image = imageData.createNewImage(new int[]{ inputProc.getWidth(), inputProc.getHeight() } );
		
		ImgLibProcessor<T> newProc = new ImgLibProcessor<T>(image, (T)type, 0);  // cast required!
		
		newProc.setPixels(inputProc.getPixels());
		
		return newProc;
	}
	
	@Override
	public void convolve(float[] kernel, int kernelWidth, int kernelHeight)
	{
		ImageProcessor ip2 = toFloat(0,null);
		ip2.setRoi(getRoi());
		new ij.plugin.filter.Convolver().convolve(ip2, kernel, kernelWidth, kernelHeight);
		setPixels(ip2.getPixels());
	}

	@Override
	public void convolve3x3(int[] kernel)
	{
		if (this.type instanceof UnsignedByteType)
			convolve3x3UnsignedByte(kernel);
		else
			convolve3x3GeneralType(kernel);
	}

	@Override
	public void copyBits(ImageProcessor ip, int xloc, int yloc, int mode)
	{
		ImgLibProcessor<T> otherProc = getImgLibProcThatMatchesMyType(ip);
		
		new GenericBlitter<T>(this).copyBits(otherProc, xloc, yloc, mode);
	}

	//TODO ask about changing name of Image to avoid conflict with java.awt
	@Override
	public java.awt.Image createImage()
	{
		int width = getWidth();
		int height = getHeight();
		boolean firstTime = this.pixels8==null;
		if (firstTime || !super.lutAnimation)
			create8BitImage();
		if (super.cm==null)
			makeDefaultColorModel();
		if (super.source==null) {
			super.source = new MemoryImageSource(width, height, super.cm, this.pixels8, 0, width);
			super.source.setAnimated(true);
			super.source.setFullBufferUpdates(true);
			super.img = Toolkit.getDefaultToolkit().createImage(super.source);
		} else if (super.newPixels) {
			super.source.newPixels(this.pixels8, super.cm, 0, width);
			super.newPixels = false;
		} else
			super.source.newPixels();

		super.lutAnimation = false;
		return super.img;
	}

	@Override
	public ImageProcessor createProcessor(int width, int height)
	{
		Image<T> image = this.imageData.createNewImage(new int[]{width,height});
		ImageProcessor ip2 = new ImgLibProcessor<T>(image, (T)this.type, 0);  // cast required!
		ip2.setColorModel(getColorModel());
		// TODO - ByteProcessor does this conditionally. Do we mirror here?
		ip2.setMinAndMax(getMin(), getMax());
		ip2.setInterpolationMethod(super.interpolationMethod);
		return ip2;
	}

	@Override
	public ImageProcessor crop()
	{	
		Rectangle roi = getRoi();

		int[] imageOrigin = Index.create(roi.x, roi.y, getPlanePosition());
		int[] imageSpan = Span.singlePlane(roi.width, roi.height, this.imageData.getNumDimensions());
		
		int[] newImageOrigin = Index.create(2);
		int[] newImageSpan = Span.singlePlane(roi.width, roi.height, 2);

		Image<T> newImage = this.imageData.createNewImage(newImageSpan);
		
		ImageUtils.copyFromImageToImage(this.imageData, imageOrigin, newImage, newImageOrigin, imageSpan);
		
		return new ImgLibProcessor<T>(newImage, (T)this.type, 0);  // cast required!
	}
	
	@Override
	public void dilate()
	{
		if (this.type instanceof UnsignedByteType)
		{
			if (isInvertedLut())
				filter(FilterType.MAX);
			else
				filter(FilterType.MIN);
		}
	}

	public void dilate(int count, int background)
	{
		if (this.type instanceof UnsignedByteType)
		{
			binaryCount = count;
			binaryBackground = background;
			filter(FilterType.DILATE);
		}
	}

	@Override
	public void drawPixel(int x, int y)
	{
		if (x>=super.clipXMin && x<=super.clipXMax && y>=super.clipYMin && y<=super.clipYMax)
		{
			if (this.isIntegral)
				putPixel(x, y, fgColor);
			else
				putPixel(x, y, Float.floatToIntBits((float)fillColor));
		}
	}

	@Override
	public ImageProcessor duplicate()
	{
		int width = getWidth();
		int height = getHeight();
		
		ImageProcessor proc = createProcessor(width, height);

		int[] origin = Index.create(0, 0, getPlanePosition());
		
		int[] span = Span.singlePlane(width, height, this.imageData.getNumDimensions());
		
		SetFloatValuesOperation<T> floatOp = new SetFloatValuesOperation<T>(this.imageData, origin, span, proc);

		Operation.apply(floatOp);
		
		return proc;
	}
	
	@Override
	public void erode()
	{
		if (this.type instanceof UnsignedByteType)
		{
			if (isInvertedLut())
				filter(FilterType.MIN);
			else
				filter(FilterType.MAX);
		}
	}

	public void erode(int count, int background)
	{
		if (this.type instanceof UnsignedByteType)
		{
			binaryCount = count;
			binaryBackground = background;
			filter(FilterType.ERODE);
		}
	}

	@Override
	public void exp()
	{
		doProcess(EXP, 0.0);
	}
	
	@Override
	public void fill()
	{
		doProcess(FILL,0.0);
	}

	@Override
	public void fill(ImageProcessor mask)
	{
		if (mask==null) {
			fill();
			return;
		}

		Rectangle roi = getRoi();

		if (mask.getWidth()!=roi.width || mask.getHeight()!=roi.height)
			return;
		
		byte[] mpixels = (byte[])mask.getPixels();
		
		int width = getWidth();
		
		for (int y=roi.y, my=0; y<(roi.y+roi.height); y++, my++) {
			int i = y * width + roi.x;
			int mi = my * roi.width;
			for (int x=roi.x; x<(roi.x+roi.width); x++) {
				if (mpixels[mi++]!=0)
				{
					if (this.isIntegral)
						setf(i, super.fgColor);
					else
						setd(i, this.fillColor);
				}
				i++;
			}
		}
	}

	public void filter(FilterType type)
	{
		if (this.type instanceof UnsignedByteType)
			filterUnsignedByte(type);
		else
			filterGeneralType(type,null);
	}

	@Deprecated
	@Override
	public void filter(int type)
	{
		switch (type)
		{
			case BLUR_MORE:		filter(FilterType.BLUR_MORE);		break;
			case FIND_EDGES:	filter(FilterType.FIND_EDGES);		break;
			case MEDIAN_FILTER:	filter(FilterType.MEDIAN_FILTER);	break;
			case MIN:			filter(FilterType.MIN);				break;
			case MAX:			filter(FilterType.MAX);				break;
			case CONVOLVE:		filter(FilterType.CONVOLVE);		break;
			case ERODE:			filter(FilterType.ERODE);			break;
			case DILATE:		filter(FilterType.DILATE);			break;
			default:
				throw new IllegalArgumentException("filter(): unknown filter type requested - "+type);
		}
	}

	/** swap the rows of an image about its central row */
	@Override
	public void flipVertical()
	{
		// create suitable cursors - noncached
		final LocalizableByDimCursor<T> cursor1 = this.imageData.createLocalizableByDimCursor( );
		final LocalizableByDimCursor<T> cursor2 = this.imageData.createLocalizableByDimCursor( );
		
		// allocate arrays that will hold position variables
		final int[] position1 = Index.create(0, 0, getPlanePosition());
		final int[] position2 = Index.create(0, 0, getPlanePosition());
		
		// calc some useful variables in regards to our region of interest.
		Rectangle roi = getRoi();
		final int minX = roi.x;
		final int minY = roi.y;
		final int maxX = minX + roi.width - 1;
		final int maxY = minY + roi.height - 1;
		
		// calc half height - we will only need to swap the top half of the rows with the bottom half
		final int halfRoiHeight = roi.height / 2;
		
		// the half the rows
		for (int yoff = 0; yoff < halfRoiHeight; yoff++) {
			
			// calc locations of the two rows to be swapped
			final int y1 = minY + yoff;
			final int y2 = maxY - yoff;
			
			// setup y position index for cursor 1
			position1[1] = y1;

			// setup y position index for cursor 2
			position2[1] = y2;

			// for each col in this row
			for (int x=minX; x<=maxX; x++) {
				
				// setup x position index for cursor 1
				position1[0] = x;

				// setup x position index for cursor 2
				position2[0] = x;

				// move to position1 and save the current value
				cursor1.setPosition(position1);
				final double pixVal1 = cursor1.getType().getRealDouble();
				
				// move to position2 and save the current value
				cursor2.setPosition(position2);
				final double pixVal2 = cursor2.getType().getRealDouble();
		
				// write the values back in swapped order
				cursor2.getType().setReal(pixVal1);
				cursor1.getType().setReal(pixVal2);
			}
		}
		
		// close the noncached cursors when done with them
		cursor1.close();
		cursor2.close();
	}

	@Override
	public void gamma(double value)
	{
		doProcess(GAMMA, value);
	}
	
	@Override
	public int get(int x, int y) 
	{	
		int value;
		
		int[] position = Index.create(x, y, getPlanePosition());
		
		final LocalizableByDimCursor<T> cursor = this.cachedCursor.get();
		
		cursor.setPosition( position );
		
		value = (int)( cursor.getType().getRealDouble() );
		
		// do not close cursor - using cached one
		
		return value;
	}

	@Override
	public int get(int index)
	{
		int width = getWidth();
		int x = index % width;
		int y = index / width;
		return get(x, y) ;
	}

	public double getd(int x, int y)
	{
		int[] position = Index.create(x, y, getPlanePosition());
		
		LocalizableByDimCursor<T> cursor = this.cachedCursor.get();
		
		cursor.setPosition(position);
		
		RealType<?> pixRef = cursor.getType();

		return pixRef.getRealDouble(); 

		// do not close cursor - using cached one
	}

	public double getd(int index)
	{
		int width = getWidth();
		int x = index % width;
		int y = index / width;
		return getd(x, y);
	}

	@Override
	public float getf(int x, int y) 
	{
		float value;
		
		int[] position = Index.create(x, y, getPlanePosition());
		
		LocalizableByDimCursor<T> cursor = this.cachedCursor.get();
		
		cursor.setPosition(position);
		
		value =  ( float ) cursor.getType().getRealDouble();
		
		// do not close cursor - using cached one
		
		return value;
	}

	@Override
	public float getf(int index)
	{
		int width = getWidth();
		int x = index % width;
		int y = index / width;
		return getf(x, y) ;
	}

	@Override
	public double getBackgroundValue()
	{
		return this.imageProperties.getBackgroundValue();
	}

	public int[] getHistogram()
	{
		if ((type instanceof UnsignedByteType) || (type instanceof UnsignedShortType))
		{
			int[] origin = Index.create(0, 0, getPlanePosition());
			
			int[] span = Span.singlePlane(getWidth(), getHeight(), this.imageData.getNumDimensions());
			
			int lutSize = (int) (this.getMaxAllowedValue() + 1);
	
			HistogramOperation<T> histOp = new HistogramOperation<T>(this.imageData,origin,span,getMask(),lutSize);
			
			Operation.apply(histOp);
			
			return histOp.getHistogram();
		}
		
		return null;
	}
	
	public Image<T> getImage()
	{
		return this.imageData;
	}
	
	@Override
	public double getInterpolatedPixel(double x, double y)
	{
		if (super.interpolationMethod == BICUBIC)
			return getBicubicInterpolatedPixel(x, y, this);
		else {
			if (x<0.0) x = 0.0;
			if (x>=width-1.0) x = width-1.001;
			if (y<0.0) y = 0.0;
			if (y>=height-1.0) y = height-1.001;
			return calcBilinearPixel(x, y);
		}
	}

	@Override
	public double getMax() 
	{
		return this.max;
	}

	public double getMaxAllowedValue() 
	{
		return this.type.getMaxValue();
	}
	
	@Override
	public double getMin() 
	{
		return this.min;
	}

	public double getMinAllowedValue() 
	{
		return this.type.getMinValue();
	}
	
	public long getNumPixels()
	{
		return getNumPixels(this.imageData);
	}
	
	@Override
	public int getPixel(int x, int y)
	{
		if ((x >= 0) && (x < super.width) && (y >= 0) && (y < super.height))
			return get(x, y);
		return 0;
	}

	@Override
	public int getPixelInterpolated(double x, double y)
	{
		if (super.interpolationMethod == BILINEAR)
		{
			if (x<0.0 || y<0.0 || x>=width-1 || y>=height-1)
				return 0;
			else if (this.isIntegral)
				return (int) Math.round(getInterpolatedPixel(x, y));
			else
				return Float.floatToIntBits((float)getInterpolatedPixel(x, y));
		}
		else if (interpolationMethod==BICUBIC)
		{
			if (this.isIntegral)
			{
				double value = getInterpolatedPixel(x, y) + 0.5;
				value = TypeManager.boundValueToType(this.type, value);
				return (int)value;
			}
			return Float.floatToIntBits((float)getBicubicInterpolatedPixel(x, y, this));
		}
		else
			return getPixel((int)(x+0.5), (int)(y+0.5));
	}

	@Override
	public float getPixelValue(int x, int y)
	{
		int width = getWidth();
		int height = getHeight();
		
		// make sure its in bounds
		if ((x >= 0) && (x < width) && (y >= 0) && (y < height))
			return getf(x, y);
		
		return 0f;
	}

	@Override
	public Object getPixels()
	{
		// TODO: could add a special case for single-image 8-bit array-backed data
		// TODO: special case for new container
		return getCopyOfPixelsFromImage(this.imageData, this.type, getPlanePosition());
	}

	@Override
	public Object getPixelsCopy()
	{
		if (this.snapshot!=null && super.snapshotCopyMode)
		{
			super.snapshotCopyMode = false;
			
			Image<T> snapStorage = this.snapshot.getStorage();
			
			int[] planePosOfZero = Index.create(getPlanePosition().length);  // this is correct!
			
			return getCopyOfPixelsFromImage(snapStorage, this.type, planePosOfZero); 
		}
		else
		{
			return getCopyOfPixelsFromImage(this.imageData, this.type, getPlanePosition());
		}
	}

	public double[] getPlaneData()
	{
		return ImageUtils.getPlaneData(this.imageData, getWidth(), getHeight(), getPlanePosition());
	}

	public int[] getPlanePosition()
	{
		return this.imageProperties.getPlanePosition();
	}
	
	@Override
	public Object getSnapshotPixels()
	{
		if (this.snapshot == null)
			return null;
		
		Image<T> snapStorage = this.snapshot.getStorage();
		
		int[] planePosOfZero = Index.create(getPlanePosition().length);  // this is correct!

		return getCopyOfPixelsFromImage(snapStorage, this.type, planePosOfZero);
	}

	public RealType<?> getType()
	{
		return this.type;
	}
	
	@Override
	public void invert()
	{
		if (this.isIntegral)
			resetMinAndMax();
		
		doProcess(INVERT, 0.0);
	}
	
	@Override
	public void log()
	{
		doProcess(LOG, 0.0);
	}
	
	@Override
	public void max(double value)
	{
		doProcess(MAXIMUM, value);
	}

	@Override
	public void medianFilter()
	{
		if (this.type instanceof UnsignedByteType)
		{
			filter(FilterType.MEDIAN_FILTER);
		}
	}

	@Override
	public void min(double value)
	{
		doProcess(MINIMUM, value);
	}
	
	@Override
	public void multiply(double value)
	{
		doProcess(MULT, value);
	}
	
	@Override
	public void noise(double range)
	{
		Random rnd=new Random();
		double v, ran;
		boolean inRange;
		for (int y=roiY; y<(roiY+roiHeight); y++) {
			for (int x=roiX; x<(roiX+roiWidth); x++) {
				inRange = false;
				do {
					ran = Math.round(rnd.nextGaussian()*range);
					v = getd(x, y) + ran;
					inRange = TypeManager.validValue(this.type, v);
					if (inRange)
					{
						if (this.isIntegral)
							v = TypeManager.boundValueToType(this.type, v);
						setd(x, y, v);
					}
				} while (!inRange);
			}
			if (y%20==0)
				showProgress((double)(y-roiY)/roiHeight);
		}
		showProgress(1.0);
	}

	@Override
	public void or(int value)
	{
		doProcess(OR,value);
	}
	
	@Override
	public void putPixel(int x, int y, int value)
	{
		if (x>=0 && x<getWidth() && y>=0 && y<getHeight())
		{
			if (this.isIntegral)
			{
				value = (int)TypeManager.boundValueToType(this.type, value);
				set(x, y, value);
			}
			else
				setf(x, y, Float.intBitsToFloat(value));
		}
	}

	@Override
	public void putPixelValue(int x, int y, double value)
	{
		if (x>=0 && x<getWidth() && y>=0 && y<getHeight())
		{
			if (this.isIntegral)
			{
				value = TypeManager.boundValueToType(this.type, value);
				set(x, y, (int)(value+0.5));
			}
			else
				setd(x, y, value);
		}
	}

	@Override
	public void reset()
	{
		if (this.snapshot!=null)
		{
			this.snapshot.pasteIntoImage(this.imageData);
			findMinAndMax();
		}
	}

	@Override
	public void reset(ImageProcessor mask)
	{
		if (mask==null || this.snapshot==null)
			return;
		
		Rectangle roi = getRoi();

		if ((mask.getWidth() != roi.width) || (mask.getHeight() != roi.height))
			throw new IllegalArgumentException(maskSizeError(mask));

		Image<T> snapData = this.snapshot.getStorage();
		
		int[] snapOrigin = Index.create(roi.x, roi.y, new int[snapData.getNumDimensions()-2]);
		int[] snapSpan = Span.singlePlane(roi.width, roi.height, snapData.getNumDimensions());

		int[] imageOrigin = Index.create(roi.x, roi.y, getPlanePosition());
		int[] imageSpan = Span.singlePlane(roi.width, roi.height, this.imageData.getNumDimensions());

		
		ResetUsingMaskOperation<T> resetOp = new ResetUsingMaskOperation<T>(snapData,snapOrigin,snapSpan,this.imageData,imageOrigin,imageSpan,mask);
		
		Operation.apply(resetOp);
	}
	
	@Override
	public ImageProcessor resize(int dstWidth, int dstHeight)
	{
		if (roiWidth==dstWidth && roiHeight==dstHeight)
			if (this.type instanceof UnsignedByteType)
				return crop();

		double value;
		double srcCenterX = roiX + roiWidth/2.0;
		double srcCenterY = roiY + roiHeight/2.0;
		double dstCenterX = dstWidth/2.0;
		double dstCenterY = dstHeight/2.0;
		double xScale = (double)dstWidth/roiWidth;
		double yScale = (double)dstHeight/roiHeight;
		if (interpolationMethod!=NONE) {
			dstCenterX += xScale/2.0;
			dstCenterY += yScale/2.0;
		}
		ImgLibProcessor<?> ip2 = (ImgLibProcessor<?>)createProcessor(dstWidth, dstHeight);
		double xs, ys;
		if (interpolationMethod==BICUBIC) {
			for (int y=0; y<=dstHeight-1; y++) {
				ys = (y-dstCenterY)/yScale + srcCenterY;
				int index = y*dstWidth;
				for (int x=0; x<=dstWidth-1; x++) {
					xs = (x-dstCenterX)/xScale + srcCenterX;
					value = getBicubicInterpolatedPixel(xs, ys, this);
					if (this.isIntegral)
					{
						value += 0.5;
						value = TypeManager.boundValueToType(this.type, value);
					}
					ip2.setd(index++, value);
				}
				if (y%30==0) showProgress((double)y/dstHeight);
			}
		} else {  // not BICUBIC
			double xlimit = width-1.0, xlimit2 = width-1.001;
			double ylimit = height-1.0, ylimit2 = height-1.001;
			int index1, index2;
			for (int y=0; y<=dstHeight-1; y++) {
				ys = (y-dstCenterY)/yScale + srcCenterY;
				if (interpolationMethod==BILINEAR) {
					if (ys<0.0) ys = 0.0;
					if (ys>=ylimit) ys = ylimit2;
				}
				index1 = width*(int)ys;
				index2 = y*dstWidth;
				for (int x=0; x<=dstWidth-1; x++) {
					xs = (x-dstCenterX)/xScale + srcCenterX;
					if (interpolationMethod==BILINEAR)
					{
						if (xs<0.0) xs = 0.0;
						if (xs>=xlimit) xs = xlimit2;
						value = getInterpolatedPixel(xs, ys);
						if (this.isIntegral)
						{
							value += 0.5;
							//value = ((byte) value) & ((byte)this.type.getMaxValue());
							//value = (byte)(((byte) value) & (255));  // 128 goes to -128
							//value = ((int) value) & ((int)this.type.getMaxValue());
							value = TypeManager.boundValueToType(this.type, value);  // TODO - is this correct?????
						}
						ip2.setd(index2++, value);
					}
					else  // interp == NONE
					{
						value = getd(index1+(int)xs);
						ip2.setd(index2++, value);
					}
				}
				if (y%30==0) showProgress((double)y/dstHeight);
			}
		}
		showProgress(1.0);
		return ip2;
	}

	@Override
	public void rotate(double angle)
	{
		// TODO - for some reason Float and Short don't check for rotate by 0.0 and their results are different than doing nothing!!!!!
		if ((angle%360==0) && (this.type instanceof UnsignedByteType))
			return;
		Object pixels2 = getPixelsCopy();
		ImgLibProcessor<?> ip2 = ImageUtils.createProcessor(getWidth(), getHeight(), pixels2, TypeManager.isUnsignedType(this.type));
		if (interpolationMethod==BICUBIC) {
			ip2.setBackgroundValue(getBackgroundValue());
		}
		double centerX = roiX + (roiWidth-1)/2.0;
		double centerY = roiY + (roiHeight-1)/2.0;
		int xMax = roiX + this.roiWidth - 1;
		
		// TODO in original ByteProcessor code here:
		// if (!bgColorSet && isInvertedLut()) bgColor = 0;
		
		double angleRadians = -angle/(180.0/Math.PI);
		double ca = Math.cos(angleRadians);
		double sa = Math.sin(angleRadians);
		double tmp1 = centerY*sa-centerX*ca;
		double tmp2 = -centerX*sa-centerY*ca;
		double tmp3, tmp4, xs, ys;
		int index, ixs, iys;
		double dwidth=width, dheight=height;
		double xlimit = width-1.0, xlimit2 = width-1.001;
		double ylimit = height-1.0, ylimit2 = height-1.001;
		
		if (interpolationMethod==BICUBIC) {
			for (int y=roiY; y<(roiY + roiHeight); y++) {
				index = y*width + roiX;
				tmp3 = tmp1 - y*sa + centerX;
				tmp4 = tmp2 + y*ca + centerY;
				for (int x=roiX; x<=xMax; x++) {
					xs = x*ca + tmp3;
					ys = x*sa + tmp4;
					double value = getBicubicInterpolatedPixel(xs, ys, ip2);
					if (this.isIntegral)
					{
						value = (int) (value+0.5);
						value = TypeManager.boundValueToType(this.type, value);
					}
					setd(index++,value);
				}
				if (y%30==0) showProgress((double)(y-roiY)/roiHeight);
			}
		} else {
			for (int y=roiY; y<(roiY + roiHeight); y++) {
				index = y*width + roiX;
				tmp3 = tmp1 - y*sa + centerX;
				tmp4 = tmp2 + y*ca + centerY;
				for (int x=roiX; x<=xMax; x++) {
					xs = x*ca + tmp3;
					ys = x*sa + tmp4;
					if ((xs>=-0.01) && (xs<dwidth) && (ys>=-0.01) && (ys<dheight))
					{
						if (interpolationMethod==BILINEAR) {
							if (xs<0.0) xs = 0.0;
							if (xs>=xlimit) xs = xlimit2;
							if (ys<0.0) ys = 0.0;			
							if (ys>=ylimit) ys = ylimit2;
							double value = ip2.getInterpolatedPixel(xs, ys);
							if (this.isIntegral)
							{
								value = (int) (value+0.5);
								value = TypeManager.boundValueToType(this.type, value);
							}
							setd(index++, value);
						} else {
							ixs = (int)(xs+0.5);
							iys = (int)(ys+0.5);
							if (ixs>=width) ixs = width - 1;
							if (iys>=height) iys = height -1;
							setd(index++, ip2.getd(ixs,iys));
						}
					}
					else
					{
						double value = 0;
						if (this.isIntegral)
							value = this.getBackgroundValue();
						setd(index++,value);
					}
				}
				if (y%30==0)
					showProgress((double)(y-roiY)/roiHeight);
			}
		}
		showProgress(1.0);
	}

	@Override
	public void scale(double xScale, double yScale)
	{
		// TODO - in original ByteProcessor code right here
		// if (!bgColorSet && isInvertedLut()) bgColor = 0;
		
		double xCenter = roiX + roiWidth/2.0;
		double yCenter = roiY + roiHeight/2.0;
		int xmin, xmax, ymin, ymax;
		if ((xScale>1.0) && (yScale>1.0)) {
			//expand roi
			xmin = (int)(xCenter-(xCenter-roiX)*xScale);
			if (xmin<0) xmin = 0;
			xmax = xmin + (int)(roiWidth*xScale) - 1;
			if (xmax>=width) xmax = width - 1;
			ymin = (int)(yCenter-(yCenter-roiY)*yScale);
			if (ymin<0) ymin = 0;
			ymax = ymin + (int)(roiHeight*yScale) - 1;
			if (ymax>=height) ymax = height - 1;
		} else {
			xmin = roiX;
			xmax = roiX + roiWidth - 1;
			ymin = roiY;
			ymax = roiY + roiHeight - 1;
		}
		Object pixels2 = getPixelsCopy();
		ImgLibProcessor<?> ip2 = ImageUtils.createProcessor(getWidth(), getHeight(), pixels2, TypeManager.isUnsignedType(this.type));
		boolean checkCoordinates = (xScale < 1.0) || (yScale < 1.0);
		int index1, index2, xsi, ysi;
		double ys, xs;
		if (interpolationMethod==BICUBIC) {
			for (int y=ymin; y<=ymax; y++) {
				ys = (y-yCenter)/yScale + yCenter;
				int index = y*width + xmin;
				for (int x=xmin; x<=xmax; x++) {
					xs = (x-xCenter)/xScale + xCenter;
					double value = getBicubicInterpolatedPixel(xs, ys, ip2);
					if (this.isIntegral)
					{
						value = (int) (value+0.5);
						value = TypeManager.boundValueToType(this.type, value);
					}
					setd(index++,value);
				}
				if (y%30==0) showProgress((double)(y-ymin)/height);
			}
		} else {
			double xlimit = width-1.0, xlimit2 = width-1.001;
			double ylimit = height-1.0, ylimit2 = height-1.001;
			for (int y=ymin; y<=ymax; y++) {
				ys = (y-yCenter)/yScale + yCenter;
				ysi = (int)ys;
				if (ys<0.0) ys = 0.0;			
				if (ys>=ylimit) ys = ylimit2;
				index1 = y*width + xmin;
				index2 = width*(int)ys;
				for (int x=xmin; x<=xmax; x++) {
					xs = (x-xCenter)/xScale + xCenter;
					xsi = (int)xs;
					if (checkCoordinates && ((xsi<xmin) || (xsi>xmax) || (ysi<ymin) || (ysi>ymax)))
					{
						if (this.type instanceof UnsignedByteType)
							setd(index1++, this.getBackgroundValue());
						else
							setd(index1++, this.min);
					}
					else  // interpolated pixel within bounds of image
					{
						if (interpolationMethod==BILINEAR) {
							if (xs<0.0) xs = 0.0;
							if (xs>=xlimit) xs = xlimit2;
							double value = ip2.getInterpolatedPixel(xs, ys);
							if (this.isIntegral)
							{
								value = (int) (value+0.5);
								value = TypeManager.boundValueToType(this.type, value);
							}
							setd(index1++, value);
						}
						else  // interpolation type of NONE
						{
							setd(index1++, ip2.getd(index2+xsi));
						}
					}
				}
				if (y%30==0) showProgress((double)(y-ymin)/height);
			}
		}
		showProgress(1.0);
	}

	@Override
	public void set(int x, int y, int value) 
	{
		int[] position = Index.create(x, y, getPlanePosition());
		
		LocalizableByDimCursor<T> cursor = this.cachedCursor.get();
		
		cursor.setPosition(position);
		
		cursor.getType().setReal( value );
		
		// do not close cursor - using cached one
	}

	@Override
	public void set(int index, int value) 
	{
		int width = getWidth();
		int x = index % width;
		int y = index / width;
		set( x, y, value) ;
	}

	@Override
	public void setBackgroundValue(double value) 
	{
		// only set for unsigned byte type like ImageJ (maybe need to extend to integral types and check min/max pixel ranges
		// see ImageProperties.setBackground() for some additional notes.
		if (this.type instanceof UnsignedByteType)
		{
			if (value < 0) value = 0;
			if (value > 255) value = 255;
			value = (int) value;
			this.imageProperties.setBackgroundValue(value);
		}
	}

	@Override
	public void setColor(Color color)
	{
		int bestIndex = getBestIndex(color);
		
		if (this.type instanceof GenericByteType<?>) {

			super.drawingColor = color;
			super.fgColor = bestIndex;
		}
		else { // not a Byte type

			double min = getMin();
			double max = getMax();
			
			if ((bestIndex>0) && (min == 0) && (max == 0)) {
				
				if (this.isIntegral)
					setValue(bestIndex);
				else  // floating type
					this.fillColor = bestIndex;
				
				setMinAndMax(0,255);
			}
			else if ((bestIndex == 0) && (min > 0) && ((color.getRGB()&0xffffff) == 0)) {
				
				if (!this.isIntegral)
					this.fillColor = 0;
				else { // integral data that is not byte
					
					if (TypeManager.isUnsignedType(this.type))  // TODO - this logic is different than original code. Test!
						setValue(0.0);
					else
						setValue(this.type.getMaxValue());
				}
			}
			else {
				
				double value = (min + (max-min)*(bestIndex/255.0));
				
				if (this.isIntegral)
					super.fgColor = (int)value;
				else
					this.fillColor = value;
			}
		}
	}

	// not an override. helper method that has no side effects.
	public void setMinAndMaxOnly(double min, double max)
	{
		this.min = min;
		this.max = max;
		
		// TODO : do I want the this.isIntegral code from next method in here too?
	}
	
	@Override
	public void setMinAndMax(double min, double max)
	{
		if (min==0.0 && max==0.0)
		{
			resetMinAndMax();
			return;
		}
	
		this.min = min;
		this.max = max;
		
		if (this.isIntegral)
		{
			this.min = (int) this.min;
			this.max = (int) this.max;
		}
		
		// TODO - From FloatProc - there is code for setting the fixedScale boolean. May need it.
		
		resetThreshold();
	}

	@Override
	public void resetMinAndMax()
	{
		// TODO - From FloatProc - there is code for setting the fixedScale boolean. May need it.
		
		findMinAndMax();
		
		resetThreshold();
	}

	public void setd(int index, double value)
	{
		int width = getWidth();
		int x = index % width;
		int y = index / width;
		setd( x, y, value);
	}

	public void setd(int x, int y, double value)
	{
		int[] position = Index.create(x, y, getPlanePosition());
		
		LocalizableByDimCursor<T> cursor = this.cachedCursor.get();
		
		cursor.setPosition(position);
		
		RealType<?> pixRef = cursor.getType();

		// TODO - verify the following implementation is what we want to do:
		// NOTE - for an integer type backed data store imglib rounds float values. ImageJ has always truncated float values.
		//   I need to detect beforehand and do my truncation if an integer type.
		
		if (this.isIntegral)
			value = (double)Math.floor(value);
		
		pixRef.setReal( value ); 

		// do not close cursor - using cached one
	}

	@Override
	public void setf(int index, float value)
	{
		int width = getWidth();
		int x = index % width;
		int y = index / width;
		setf( x, y, value);
	}

	@Override
	public void setf(int x, int y, float value)
	{
		setd(x, y, (double)value);
	}

	@Override
	public void setPixels(Object pixels)
	{
		setImagePlanePixels(this.imageData, getPlanePosition(), pixels);
	}

	@Override
	public void setPixels(int channelNumber, FloatProcessor fp)
	{
		// like ByteProcessor ignore channel number

		setPixels(fp.getPixels());
	}

	@Override
	public void setSnapshotPixels(Object pixels)
	{
		// must create snapshot data structures if they don't exist. we'll overwrite it's data soon.
		if (this.snapshot == null)
			snapshot();
		
		Image<T> snapStorage = this.snapshot.getStorage();
		
		int[] planePosition = Index.create(snapStorage.getNumDimensions()-2);
	
		setImagePlanePixels(snapStorage, planePosition, pixels);
	}

	@Override
	public void setValue(double value) 
	{
		this.fillColor = value;
		if (this.isIntegral)
		{
			super.fgColor = (int) TypeManager.boundValueToType(this.type, this.fillColor);
		}
	}

	@Override
	public void snapshot() 
	{
		int[] origins = Index.create(0, 0, getPlanePosition());

		int[] spans = Span.singlePlane(getWidth(), getHeight(), this.imageData.getNumDimensions());
		
		this.snapshot = new Snapshot<T>(this.imageData, origins, spans);
	}

	@Override
	public void sqr()
	{
		doProcess(SQR, 0.0);
	}
	
	@Override
	public void sqrt()
	{
		doProcess(SQRT, 0.0);
	}

	@Override
	public void threshold(int thresholdLevel) 
	{
		if (!this.isIntegral)
			return;

		int[] origin = Index.create(0, 0, getPlanePosition());
		int[] span = Span.singlePlane(getWidth(), getHeight(), this.imageData.getNumDimensions());
		
		ThresholdOperation<T> threshOp = new ThresholdOperation<T>(this.imageData,origin,span,thresholdLevel);
		
		Operation.apply(threshOp);
	}
	
	@Override
	public FloatProcessor toFloat(int channelNumber, FloatProcessor fp)
	{
		int width = getWidth();
		int height = getHeight();
		
		long size = getNumPixels(this.imageData);
		
		if (fp == null || fp.getWidth()!=width || fp.getHeight()!=height)
			fp = new FloatProcessor(width, height, new float[(int)size], super.cm);
		
		int[] origin = Index.create(0, 0, getPlanePosition());
		
		int[] span = Span.singlePlane(width, height, this.imageData.getNumDimensions());
		
		SetFloatValuesOperation<T> floatOp = new SetFloatValuesOperation<T>(this.imageData, origin, span, fp);

		Operation.apply(floatOp);
		
		fp.setRoi(getRoi());
		fp.setMask(getMask());
		fp.setMinAndMax(this.min, this.max);
		fp.setThreshold(getMinThreshold(), getMaxThreshold(), ImageProcessor.NO_LUT_UPDATE);

		return fp;
	}
	
	@Override
	public void xor(int value)
	{
		doProcess(XOR,value);
	}
}
