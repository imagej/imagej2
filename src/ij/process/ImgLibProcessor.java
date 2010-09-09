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
//     features.
//     Possible refactorings
//       create a FilterType class that inherits from Operation(?) and have convolve, blur_more, find_edges, etc. inherit from it.
//         Pass FilterAlgo to this Operation.
//       create some kind of algorithm classes for the various doProcess type ops. They would take two samples and combine them in some way.
//         then things like doProcess could be passed an algo and be very simplified. the various routines (like fill() for instance)
//         could then new a FillAlgo and pass it to doProcess.
//       turn doProcess into a Operation of some type. Maybe even modify BlitterOperation.
//   Make sure that resetMinAndMax() and/or findMinAndMax() are called at appropriate times. Maybe base class does this for us?
//   I have not yet mirrored ImageJ's signed 16 bit hacks. Will need to test Image<ShortType> and see if things work versus an ImagePlus.
//   Review imglib's various cursors and perhaps change which ones I'm using.
//   Nearly all methods below broken for ComplexType and and LongType
//   All methods below assume x and y first two dimensions and that the Image<T> consists of XY planes
//     In createImagePlus we rely on image to be 5d or less. We should modify ImageJ to have a setDimensions(int[] dimension) and integrate
//     its use throughout the application.
//   Just realized the constructor that takes plane number may not make a ton of sense as implemented. We assume X & Y are the first 2 dimensions.
//     This ideally means that its broken into planes in the last two dimensions. This may just be a convention we use but it may break things too.
//     Must investigate. (as a workaround for now we are incrementing indexes from left to right)
//   Rename ImgLibProcessor to GenericProcessor????
//   Rename TypeManager to TypeUtils
//   Rename Image<> to something else like Dataset<> or NumericDataset<>
//   Improvements to ImgLib
//     Rename LocalizableByDimCursor to PositionCursor. Be able to say posCursor.setPosition(int[] pos) and posCursor.setPosition(long sampIndex).
//       Another possibility: just call it a Cursor. And then cursor.get() or cursor.get(int[] pos) or cursor.get(long sampleNumber) 
//     Create ROICursors directly from an Image<T> and have that ctor setup its own LocalizableByDimCursor for you.
//     Allow new Image<ByteType>(rows,cols). Have a default factory and a default container and have other constructors that you use in the cases
//       where you want to specify them. Also allow new Image<UnsignedByte>(rows,cols,pixels).
//     Have a few static ContainerFactories that we can just refer to rather than newing them all the time. Maybe do so also for Types so that
//       we're not always having to pass a new UnsignedByteType() but rather a static one and if a new one needed the ctor can clone.
//     In general come up with much shorter names to make use less cumbersome.
//     It would be good to specify axis order of a cursor's traversal : new Cursor(image,"zxtcy") and then just call cursor.get() as needed.
//       Also could do cursor.fwd("t" or some enum value) which would iterate forward in the (here t) plane of the image skipping large groups of
//       samples at a time. 
//     Put our ImageUtils class code somewhere in Imglib. Also maybe include the Index and Span classes too. Also TypeManager class.

public class ImgLibProcessor<T extends RealType<T>> extends ImageProcessor implements java.lang.Cloneable
{
	/** filter numbers: copied from various processors */
	public static final int BLUR_MORE=0, FIND_EDGES=1, MEDIAN_FILTER=2, MIN=3, MAX=4, CONVOLVE=5, ERODE=10, DILATE=11;

	/** filter types: to replace filter numbers */
	public static enum FilterType {BLUR_MORE, FIND_EDGES, MEDIAN_FILTER, MIN, MAX, CONVOLVE, ERODE, DILATE};
	
	// TODO later: define a FilterOperation class that gets applied. Create various filters from it.
	
	//****************** Instance variables *******************************************************

	/** the ImgLib image that contains the plane of data this processor refers to */
	private final Image<T> imageData;
	
	/** the image coordinates of the plane within the ImgLib image. Example: 5d image - this xy plane where z=1,c=2,t=3 - planePosition = {1,2,3} */
	private final int[] planePosition;

	/** the underlying ImgLib type of the image data */
	private final RealType<?> type;
	
	/** flag for determining if we are working with integral data (or float otherwise) */
	private final boolean isIntegral;
	
	/** flag for determining if we are working with unsigned byte data */
	private final boolean isUnsignedByte;
	
	/** flag for determining if we are working with unsigned short data */
	private final boolean isUnsignedShort;
	
	/** flag for determining if we are working with float data */
	private final boolean isFloat;
	
	/** the 8 bit image created in create8bitImage() - the method and this variable probably belong in base class */
	private byte[] pixels8;
	
	/** the snapshot undo buffer associated with this processor */
	private Snapshot<T> snapshot;
	
	/** value of background. used for various fill operations. only applies to UnsignedByte Type data. */
	private double backgroundValue;
	
	/** the smallest value actually present in the image */
	private double min;

	/** the largest value actually present in the image */
	private double max;
	
	/** used by some methods that fill in default values. only applies to float data */
	private double fillColor;

	/** used by erode() and dilate(). for UnsignedByteType data only. */
	private int binaryCount;
	
	/** used by erode() and dilate(). for UnsignedByteType data only. */
	private int binaryBackground;
	
	/** a per thread variable. a cursor used to facilitate fast access to the ImgLib image. */
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

	/** constructor: takes an ImgLib image and the position of the plane within the image */
	public ImgLibProcessor(Image<T> image, int[] thisPlanePosition)
	{
		final int[] dims = image.getDimensions();
		
		if (dims.length < 2)
			throw new IllegalArgumentException("Image must be at least 2-D.");
		
		if (dims.length != (thisPlanePosition.length+2))
			throw new IllegalArgumentException("Image dimensions and input plane position are not compatible.");

		this.imageData = image;
		this.type = ImageUtils.getType(image);
		
		this.planePosition = thisPlanePosition.clone();

		super.width = dims[0]; // TODO: Dimensional labels are safer way to find X
		super.height = dims[1]; // TODO: Dimensional labels are safer way to find Y
	
		this.fillColor = this.type.getMaxValue();
		
		this.isIntegral = TypeManager.isIntegralType(this.type);
		
		this.isUnsignedByte = this.type instanceof UnsignedByteType;
		
		this.isUnsignedShort = this.type instanceof UnsignedShortType;
		
		this.isFloat = this.type instanceof FloatType;
		
		if (this.isUnsignedByte)
			this.setBackgroundValue(255);

		resetRoi();
		
		findMinAndMax();
	}

	/** constructor: takes an ImgLib image and the integer position of the plane within the image */
	public ImgLibProcessor(Image<T> img, long planeNumber)
	{
		this(img, ImageUtils.getPlanePosition(img.getDimensions(), planeNumber));
	}

	//****************** Helper methods *******************************************************

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

	/** required method. used in createImage(). creates an 8bit image from our image data of another type. */
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
	
	/** do a point operation to the current ROI plane using the passed in UnaryFunction */
	private void doPointOperation(UnaryFunction function)
	{
		int[] origin = originOfRoi();
		
		int[] span = spanOfRoiPlane();
			
		PointOperation<T> pointOp = new PointOperation<T>(this.imageData, origin, span, function);
		
		pointOp.execute();
		
		boolean resetMinMax = super.roiWidth==super.width && super.roiHeight==super.height;
		
		if (resetMinMax)
			findMinAndMax();
	}
	
	// TODO - refactor
	/** called by filterUnsignedByte(). */
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
					sum = p1+p2+p3+p4+p5+p6+p7+p8+p9;
					if (this.isIntegral)
						sum += 4;
					sum /= 9;
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

	// TODO - refactor
	/** applies the various filter operations to UnsignedByteType data*/
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
						sum = p1+p2+p3+p4+p5+p6+p7+p8+p9;
						if (this.isIntegral)
							sum += 4;
						sum /= 9;
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
						sum = find5thOf9(values);
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
	
	/** find the median value of a list of exactly 9 values stored in a 10 element array from 1..9. copied from ByteProcessor::findMedian() */
	private int find5thOf9(int[] values)
	{
		if (values.length != 10)
			throw new IllegalArgumentException("find5thOf9(): input array length length must be 10");

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

	/** find the minimum and maximum values present in this plane of the image data */
	private void findMinAndMax()
	{
		// TODO - should do something different for UnsignedByte (involving LUT) if we mirror ByteProcessor

		//get the current image data
		int[] imageOrigin = originOfImage();
		int[] imageSpan = spanOfImagePlane();

		MinMaxOperation<T> mmOp = new MinMaxOperation<T>(this.imageData,imageOrigin,imageSpan);
		
		mmOp.execute();
		
		setMinAndMaxOnly(mmOp.getMin(), mmOp.getMax());
	}
	
	// NOTE - eliminating bad cast warnings in this method by explicit casts causes Hudson tests to fail
	/** returns a copy of our pixels as an array in the specified type. specified type probably has to match image's type */
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
	
	/** called by filterEdge(). returns the pixel at x,y. if x,y out of bounds returns nearest edge pixel. */
	private int getEdgePixel(byte[] pixels2, int x, int y)
	{
		if (x<=0) x = 0;
		if (x>=width) x = width-1;
		if (y<=0) y = 0;
		if (y>=height) y = height-1;
		return pixels2[x+y*width]&255;
	}

	/** called by filterEdge(). returns the pixel at x,y. if x,y out of bounds returns the background color. */
	private int getEdgePixel0(byte[] pixels2, int background, int x, int y)
	{
		if (x<0 || x>width-1 || y<0 || y>height-1)
			return background;
		else
			return pixels2[x+y*width]&255;
	}

	/** called by filterEdge(). returns the pixel at x,y. if x,y out of bounds returns the foreground color. */
	private int getEdgePixel1(byte[] pixels2, int foreground, int x, int y)
	{
		if (x<0 || x>width-1 || y<0 || y>height-1)
			return foreground;
		else
			return pixels2[x+y*width]&255;
	}

	/** returns the coordinates of the image origin as an int[] */
	private int[] originOfImage()
	{
		return Index.create(0, 0, this.planePosition);
	}
	
	/** returns the coordinates of the ROI origin as an int[] */
	private int[] originOfRoi()
	{
		return Index.create(super.roiX, super.roiY, this.planePosition);
	}
	
	/** sets the pixels for the specified image and plane position to the passed in array of pixels */
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
	
	/** sets the min and max variables associated with this processor and does nothing else! */
	private void setMinAndMaxOnly(double min, double max)
	{
		this.min = min;
		this.max = max;
		
		// TODO : do I want the this.isIntegral code from setMinAndMax() in here too?
	}
	
	/** sets the pixels of a plane in an image. called by setImagePlanePixels(). input pixels' type info must have been determined earlier. */
	private void setPlane(Image<T> theImage, int[] origin, Object pixels, PixelType inputType, long numPixels)
	{
		if (numPixels != getTotalSamples())
			throw new IllegalArgumentException("setPlane() error: input image does not have same dimensions as passed in pixels");

		boolean isUnsigned = TypeManager.isUnsignedType(this.type);
		
		SetPlaneOperation<T> setOp = new SetPlaneOperation<T>(theImage, origin, pixels, inputType, isUnsigned);
		
		setOp.execute();
	}
	
	/** returns the span of the image plane as an int[] */
	private int[] spanOfImagePlane()
	{
		return Span.singlePlane(super.width, super.height, this.imageData.getNumDimensions());
	}
	
	/** returns the span of the ROI plane as an int[] */
	private int[] spanOfRoiPlane()
	{
		return Span.singlePlane(super.roiWidth, super.roiHeight, this.imageData.getNumDimensions());
	}
	
	/** verifies that the passed in lut is compatible with the current data type. Throws an exception if the lut length is wrong
	 *  for the pixel layout type.
	 */
	private void verifyLutLengthOkay( int[] lut )
	{
		if ( this.type instanceof GenericByteType< ? > )
		{
			if (lut.length!=256)
				throw new IllegalArgumentException("lut.length != expected length for type " + this.type );
		}
		else if( this.type instanceof GenericShortType< ? > )
		{
			if (lut.length!=65536)
				throw new IllegalArgumentException("lut.length != expected length for type " + this.type );
		}
		else
		{
			throw new IllegalArgumentException("lut not applicable for type " + this.type ); 
		}
	}
	
	//****************** public methods *******************************************************

	/** apply the ABS point operation over the current ROI area of the current plane of data */
	@Override
	public void abs()
	{
		AbsUnaryFunction function = new AbsUnaryFunction();
		
		doPointOperation(function);
	}

	/** apply the ADD point operation over the current ROI area of the current plane of data */
	@Override
	public void add(int value)
	{
		add((double) value);
	}
	
	/** apply the ADD point operation over the current ROI area of the current plane of data */
	@Override
	public void add(double value)
	{
		AddUnaryFunction func = new AddUnaryFunction(this.type, value);
		
		doPointOperation(func);
	}
	
	/** apply the AND point operation over the current ROI area of the current plane of data */
	@Override
	public void and(int value)
	{
		AndUnaryFunction func = new AndUnaryFunction(this.type, value);
		
		doPointOperation(func);
	}

	/** runs super class autoThreshold() for integral data */
	@Override
	public void autoThreshold()
	{
		if (this.isIntegral)
			super.autoThreshold();
	}
	
	/** does a lut substitution on current ROIO area image data. applies only to integral data. */
	@Override
	public void applyTable(int[] lut) 
	{
		if (!this.isIntegral)
			return;

		verifyLutLengthOkay(lut);
		
		Rectangle roi = getRoi();
		
		int[] index = originOfRoi();
		int[] span = spanOfRoiPlane();

		ApplyLutOperation<T> lutOp = new ApplyLutOperation<T>(this.imageData,index,span,lut);
		
		lutOp.execute();
	}

	// this method is kind of kludgy
	// not an override
	/** sometimes it is useful to work with two images of the exact same type. this method will take any ImageProcessor and return an
	 *  ImageLibProcessor of the exact same type as itself. if the input image matches already it is simply returned. otherwise a new
	 *  processor is created and its pixels are populated from this ImgLibProcessor.
	 * */
	public ImgLibProcessor<T> getImgLibProcThatMatchesMyType(ImageProcessor inputProc)
	{
		// if inputProc's type matches me
		//   just return inputProc
		
		if (inputProc instanceof ImgLibProcessor<?>)
		{
			ImgLibProcessor<?> imglibProc = (ImgLibProcessor<?>) inputProc;

			if (TypeManager.sameKind(this.type, imglibProc.getType()))
				return (ImgLibProcessor<T>) imglibProc;
		}
		
		// otherwise
		//   create a processor of my type with size matching ip's dimensions
		//   populate the pixels
		//   return it
		
		Image<T> image = imageData.createNewImage(new int[]{ inputProc.getWidth(), inputProc.getHeight() } );
		
		ImgLibProcessor<T> newProc = new ImgLibProcessor<T>(image, 0);
		
		boolean oldIntegralData = false;
		if ((inputProc instanceof ByteProcessor) || (inputProc instanceof ShortProcessor))
			oldIntegralData = true;

		if (!oldIntegralData)
		{
			newProc.setPixels(inputProc.getPixels());
		}
		else  // funky issue where Java's signedness and IJ's unsignedness complicate setting the pixels
		{
			int w = newProc.getWidth();
			int h = newProc.getHeight();
			
			for (int x = 0; x < w; x++)
				for (int y = 0; y < h; y++)
					newProc.setd( x, y, (inputProc.get(x, y) & 0xffff) );  // Must turn into int data and remove signedness
		}
		
		return newProc;
	}
	
	/** convolves the current image plane data with the provided kernel */
	@Override
	public void convolve(float[] kernel, int kernelWidth, int kernelHeight)
	{
		ImageProcessor ip2 = toFloat(0,null);
		ip2.setRoi(getRoi());
		new ij.plugin.filter.Convolver().convolve(ip2, kernel, kernelWidth, kernelHeight);
		setPixels(ip2.getPixels());
	}

	// TODO - refactor/replace with Wayne's latest code
	/** convolves the current ROI area data with the provided 3x3 kernel */
	@Override
	public void convolve3x3(int[] kernel)
	{
		/*  Could do this but Wayne says its 3X slower than his special case way
		float[] floatKernel = new float[9];
		for (int i = 0; i < 9; i++)
			floatKernel[i] = kernel[i];
		convolve(floatKernel,3,3);
		*/

		// our special case way
		int[] origin = originOfRoi();
		int[] span = spanOfRoiPlane();
		Convolve3x3FilterOperation<T> convolveOp = new Convolve3x3FilterOperation<T>(this.imageData, origin, span, this, kernel);
		convolveOp.execute();
	}

	// not an override
	/** uses a blitter to copy pixels to xloc,yloc from ImageProcessor ip using the given function */
	public void copyBits(ImageProcessor ip, int xloc, int yloc, BinaryFunction function)
	{
		ImgLibProcessor<T> otherProc = getImgLibProcThatMatchesMyType(ip);
		
		new GenericBlitter<T>(this).copyBits(otherProc, xloc, yloc, function);
	}
	
	/** uses a blitter to copy pixels to xloc,yloc from ImageProcessor ip using the given mode */
	@Deprecated
	@Override
	public void copyBits(ImageProcessor ip, int xloc, int yloc, int mode)
	{
		BinaryFunction function;
		switch (mode)
		{
			case Blitter.COPY:
				function = new CopyInput1BinaryFunction();
				break;
			case Blitter.COPY_INVERTED:
				function = new CopyInput1InvertedBinaryFunction(this.type.getMaxValue());
				break;
			case Blitter.COPY_TRANSPARENT:
				function = new CopyInput1TransparentBinaryFunction(this, this.isIntegral, this.type.getMaxValue());
				break;
			case Blitter.ADD:
				function = new AddBinaryFunction(this.isIntegral, this.type.getMaxValue());
				break;
			case Blitter.SUBTRACT:
				function = new SubtractBinaryFunction(this.isIntegral, this.type.getMinValue());
				break;
			case Blitter.MULTIPLY:
				function = new MultiplyBinaryFunction(this.isIntegral, this.type.getMaxValue());
				break;
			case Blitter.DIVIDE:
				function = new DivideBinaryFunction(this.isIntegral, this.type.getMaxValue());
				break;
			case Blitter.AVERAGE:
				function = new AverageBinaryFunction(this.isIntegral);
				break;
			case Blitter.DIFFERENCE:
				function = new DifferenceBinaryFunction();
				break;
			case Blitter.AND:
				function = new AndBinaryFunction();
				break;
			case Blitter.OR:
				function = new OrBinaryFunction();
				break;
			case Blitter.XOR:
				function = new XorBinaryFunction();
				break;
			case Blitter.MIN:
				function = new MinBinaryFunction();
				break;
			case Blitter.MAX:
				function = new MaxBinaryFunction();
				break;
			case Blitter.COPY_ZERO_TRANSPARENT:
				function = new CopyInput1ZeroTransparentBinaryFunction();
				break;
			default:
				throw new IllegalArgumentException("copyBits(mode): unknown mode "+mode);
		}
		copyBits(ip,xloc,yloc,function);
	}

	//TODO ask about changing name of Image to avoid conflict with java.awt
	/** creates a java.awt.Image from super class variables */
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

	/** creates a new ImgLibProcessor of desired height and width. copies some state from this processor. as a side effect it creates
	 *  a new 2D ImgLib Image that is owned by the ImgLibProcessor and is available via proc.getImage().
	*/
	@Override
	public ImageProcessor createProcessor(int width, int height)
	{
		Image<T> image = this.imageData.createNewImage(new int[]{width,height});
		ImageProcessor ip2 = new ImgLibProcessor<T>(image, 0);
		ip2.setColorModel(getColorModel());
		// TODO - ByteProcessor does this conditionally. Do we mirror here?
		ip2.setMinAndMax(getMin(), getMax());
		ip2.setInterpolationMethod(super.interpolationMethod);
		return ip2;
	}

	/** creates an ImgLibProcessor on a new image whose size and contents match the current ROI area. */
	@Override
	public ImageProcessor crop()
	{	
		Rectangle roi = getRoi();

		int[] imageOrigin = originOfRoi();
		int[] imageSpan = spanOfRoiPlane();
		
		int[] newImageOrigin = Index.create(2);
		int[] newImageSpan = Span.singlePlane(roi.width, roi.height, 2);

		Image<T> newImage = this.imageData.createNewImage(newImageSpan);
		
		ImageUtils.copyFromImageToImage(this.imageData, imageOrigin, newImage, newImageOrigin, imageSpan);
		
		return new ImgLibProcessor<T>(newImage, 0);
	}
	
	/** does a filter operation vs. min or max as appropriate. applies to UnsignedByte data only.*/
	@Override
	public void dilate()
	{
		if (this.isUnsignedByte)
		{
			if (isInvertedLut())
				filter(FilterType.MAX);
			else
				filter(FilterType.MIN);
		}
	}

	// not an override : mirrors code in ByteProcessor
	/** does a dilate filter operation. applies to UnsignedByte data only.*/
	public void dilate(int count, int background)
	{
		if (this.isUnsignedByte)
		{
			binaryCount = count;
			binaryBackground = background;
			filter(FilterType.DILATE);
		}
	}

	/** set the pixel at x,y to the fill/foreground value. if x,y outside clip region does nothing. */
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

	/** creates a processor of the same size and sets its pixel values to this processor's current plane data */
	@Override
	public ImageProcessor duplicate()
	{
		int width = getWidth();
		int height = getHeight();
		
		ImageProcessor proc = createProcessor(width, height);

		int[] origin = originOfImage();
		
		int[] span = spanOfImagePlane();
		
		SetFloatValuesOperation<T> floatOp = new SetFloatValuesOperation<T>(this.imageData, origin, span, proc);

		floatOp.execute();
		
		return proc;
	}
	
	/** does a filter operation vs. min or max as appropriate. applies to UnsignedByte data only.*/
	@Override
	public void erode()
	{
		if (this.isUnsignedByte)
		{
			if (isInvertedLut())
				filter(FilterType.MIN);
			else
				filter(FilterType.MAX);
		}
	}

	// not an override : mirrors code in ByteProcessor
	/** does an erode filter operation. applies to UnsignedByte data only.*/
	public void erode(int count, int background)
	{
		if (this.isUnsignedByte)
		{
			binaryCount = count;
			binaryBackground = background;
			filter(FilterType.ERODE);
		}
	}

	/** apply the EXP point operation over the current ROI area of the current plane of data */
	@Override
	public void exp()
	{
		ExpUnaryFunction function = new ExpUnaryFunction(this.type, this.min, this.max);
		
		doPointOperation(function);
	}
	
	/** apply the FILL point operation over the current ROI area of the current plane of data */
	@Override
	public void fill()
	{
		double fillValue = this.fillColor;
		if (this.isIntegral)
			fillValue = super.fgColor;
		
		FillUnaryFunction func = new FillUnaryFunction(fillValue);
		
		doPointOperation(func);
	}

	/** fills the current ROI area of the current plane of data with the fill color wherever the input mask is nonzero */
	@Override
	public void fill(ImageProcessor mask)
	{
		if (mask==null) {
			fill();
			return;
		}

		final Rectangle roi = getRoi();

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

	// not an override : way to phase out passing in filter numbers
	/** run specified filter (a FilterType) on current ROI area of current plane data */
	public void filter(FilterType type)
	{
		if (this.isUnsignedByte)
			filterUnsignedByte(type);
		else
		{
			int[] origin = originOfRoi();
			int[] span = spanOfRoiPlane();
			
			Filter3x3Operation<T> filter;
			
			switch (type)
			{
				case BLUR_MORE:
					filter = new BlurFilterOperation<T>(this.imageData,origin,span,this);
					break;
					
				case FIND_EDGES:
					filter = new FindEdgesFilterOperation<T>(this.imageData,origin,span,this);
					break;

				case MEDIAN_FILTER:
				case MIN:
				case MAX:
				case ERODE:
				case DILATE:
					// do nothing for these filters when not unsigned byte
					return;
					
				default:
					throw new IllegalArgumentException("filter(FilterTye): invalid filter type specified - "+type);
			}
			
			filter.execute();
		}
	}

	/** run specified filter (an int) on current ROI area of current plane data */
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

	/** swap the rows of the current ROI area about its central row */
	@Override
	public void flipVertical()
	{
		int x,y,mirrorY;
		int halfY = super.roiHeight/2;
		for (int yOff = 0; yOff < halfY; yOff++)
		{
			y = super.roiY + yOff;
			mirrorY = super.roiY + super.roiHeight - yOff - 1;
			for (int xOff = 0; xOff < super.roiWidth; xOff++)
			{
				x = super.roiX + xOff;
				double tmp = getd(x, y);
				setd(x, y, getd(x, mirrorY));
				setd(x, mirrorY, tmp);
			}
		}
	}
	
	/** apply the GAMMA point operation over the current ROI area of the current plane of data */
	@Override
	public void gamma(double value)
	{
		GammaUnaryFunction function = new GammaUnaryFunction(this.type, this.min, this.max, value);
		
		doPointOperation(function);
	}
	
	/** get the pixel value at x,y as an int. for float data it returns float encoded into int bits */
	@Override
	public int get(int x, int y) 
	{	
		double value;
		
		int[] position = Index.create(x, y, this.planePosition);
		
		final LocalizableByDimCursor<T> cursor = this.cachedCursor.get();
		
		cursor.setPosition( position );
		
		value = cursor.getType().getRealDouble();
		
		// do not close cursor - using cached one

		if (this.isIntegral)
			return (int) value;
		
		// else fall through to default float behavior in IJ
		return Float.floatToIntBits((float)value);
	}

	/** get the pixel value at index as an int. for float data it returns float encoded into int bits */
	@Override
	public int get(int index)
	{
		int width = getWidth();
		int x = index % width;
		int y = index / width;
		return get(x, y) ;
	}

	/** This method is from Chapter 16 of "Digital Image Processing:
	An Algorithmic Introduction Using Java" by Burger and Burge
	(http://www.imagingbook.com/). */
	@Override
	public double getBicubicInterpolatedPixel(double x0, double y0, ImageProcessor ip2)
	{
		int u0 = (int) Math.floor(x0);	//use floor to handle negative coordinates too
		int v0 = (int) Math.floor(y0);
		if (u0<=0 || u0>=super.width-2 || v0<=0 || v0>=super.height-2)
			return ip2.getBilinearInterpolatedPixel(x0, y0);
		double q = 0;
		for (int j = 0; j <= 3; j++)
		{
			int v = v0 - 1 + j;
			double p = 0;
			for (int i = 0; i <= 3; i++)
			{
				int u = u0 - 1 + i;
				p = p + ip2.getf(u,v) * cubic(x0 - u);  // TODO - orig code uses getf(). could we transition to getd()?
			}
			q = q + p * cubic(y0 - v);
		}
		return q;
	}
	
	// not an override
	/** get the pixel value at x,y as a double. */
	public double getd(int x, int y)
	{
		int[] position = Index.create(x, y, this.planePosition);
		
		LocalizableByDimCursor<T> cursor = this.cachedCursor.get();
		
		cursor.setPosition(position);
		
		RealType<?> pixRef = cursor.getType();

		return pixRef.getRealDouble(); 

		// do not close cursor - using cached one
	}

	// not an override
	/** get the pixel value at index as a double. */
	public double getd(int index)
	{
		int width = getWidth();
		int x = index % width;
		int y = index / width;
		return getd(x, y);
	}

	/** get the pixel value at x,y as a float. */
	@Override
	public float getf(int x, int y) 
	{
		float value;
		
		int[] position = Index.create(x, y, this.planePosition);
		
		LocalizableByDimCursor<T> cursor = this.cachedCursor.get();
		
		cursor.setPosition(position);
		
		value =  ( float ) cursor.getType().getRealDouble();
		
		// do not close cursor - using cached one
		
		return value;
	}

	/** get the pixel value at index as a float. */
	@Override
	public float getf(int index)
	{
		int width = getWidth();
		int x = index % width;
		int y = index / width;
		return getf(x, y) ;
	}

	/** get the current background value. always 0 if not UnsignedByte data */ 
	@Override
	public double getBackgroundValue()
	{
		if (this.isUnsignedByte)
			return this.backgroundValue;
		
		return 0.0;
	}

	/** calculate the histogram of the current ROI area of the current plane. */
	@Override
	public int[] getHistogram()
	{
		if ((this.isUnsignedByte) || (this.isUnsignedShort))
		{
			int[] origin = originOfRoi();
			
			int[] span = spanOfRoiPlane();
			
			int lutSize = (int) (this.getMaxAllowedValue() + 1);
	
			HistogramOperation<T> histOp = new HistogramOperation<T>(this.imageData,origin,span,getMask(),lutSize);
			
			histOp.execute();
			
			return histOp.getHistogram();
		}
		
		return null;
	}
	
	// not an override
	/** returns the ImgLib image this processor is associated with */
	public Image<T> getImage()
	{
		return this.imageData;
	}
	
	/** returns an interpolated pixel value from double coordinates using current interpolation method */
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

	/** returns the maximum data value currently present in the image plane */
	@Override
	public double getMax() 
	{
		return this.max;
	}

	// not an override
	/** returns the theoretical maximum possible sample value for this type of processor */ 
	public double getMaxAllowedValue() 
	{
		return this.type.getMaxValue();
	}
	
	/** returns the maximum data value currently present in the image plane */
	@Override
	public double getMin() 
	{
		return this.min;
	}

	// not an override
	/** returns the theoretical minimum possible sample value for this type of processor */ 
	public double getMinAllowedValue() 
	{
		return this.type.getMinValue();
	}
	
	// not an override
	/** returns the number of samples in my plane */ 
	public long getTotalSamples()
	{
		return ((long) super.width) * super.height;
	}
	
	/** returns the pixel at x,y. if coords are out of bounds returns 0. */
	@Override
	public int getPixel(int x, int y)
	{
		if ((x >= 0) && (x < super.width) && (y >= 0) && (y < super.height))
			return get(x, y);
		return 0;
	}

	/** given x,y, double coordinates this returns an interpolated pixel using the current interpolations methods. unlike getInterpolatedPixel()
	 *  which assumes you're doing some kind of interpolation, this method will return nearest neighbors when no interpolation selected. note
	 *  that it returns as an int so float values are encoded as int bits.
	 */
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

	/** returns the pixel value at x,y as a float. if x,y, out of bounds returns 0. */
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

	/** returns a copy of the current plane data pixels as an array of the appropriate type */
	@Override
	public Object getPixels()
	{
		// TODO: could add a special case for single-image 8-bit array-backed data
		// TODO: special case for new container
		return getCopyOfPixelsFromImage(this.imageData, this.type, this.planePosition);
	}

	/** returns a copy of some pixels as an array of the appropriate type. depending upon super class variable "snapshotCopyMode"
	 *  it will either copy current plane data or it will copy current snapshot data.
	 */
	@Override
	public Object getPixelsCopy()
	{
		if (this.snapshot!=null && super.snapshotCopyMode)
		{
			super.snapshotCopyMode = false;
			
			Image<T> snapStorage = this.snapshot.getStorage();
			
			int[] planePosOfZero = Index.create(this.planePosition.length);  // this is correct!
			
			return getCopyOfPixelsFromImage(snapStorage, this.type, planePosOfZero); 
		}
		else
		{
			return getCopyOfPixelsFromImage(this.imageData, this.type, this.planePosition);
		}
	}

	// not an override
	/** return the current plane data pixels as an array of doubles. converts types as needed. */
	public double[] getPlaneData()
	{
		return ImageUtils.getPlaneData(this.imageData, getWidth(), getHeight(), this.planePosition);
	}

	// not an override
	/** return the plane position of this processor within its parent ImgLib image */
	public int[] getPlanePosition()
	{
		return this.planePosition.clone();
	}
	
	/** returns a copy of the pixels in the current snapshot */
	@Override
	public Object getSnapshotPixels()
	{
		if (this.snapshot == null)
			return null;
		
		Image<T> snapStorage = this.snapshot.getStorage();
		
		int[] planePosOfZero = Index.create(this.planePosition.length);  // this is correct!

		return getCopyOfPixelsFromImage(snapStorage, this.type, planePosOfZero);
	}

	// not an override
	/** return the underlying ImgLib type of this processor */ 
	public RealType<?> getType()
	{
		return this.type;
	}
	
	/** apply the INVERT point operation over the current ROI area of the current plane of data */
	@Override
	public void invert()
	{
		if (this.isIntegral)
			resetMinAndMax();
		
		InvertUnaryFunction function = new InvertUnaryFunction(this.type, this.min, this.max);
		
		doPointOperation(function );
	}
	
	/** apply the LOG point operation over the current ROI area of the current plane of data */
	@Override
	public void log()
	{
		LogUnaryFunction function = new LogUnaryFunction(this.type, this.min, this.max);
		
		doPointOperation(function);
	}
	
	/** apply the MAXIMUM point operation over the current ROI area of the current plane of data */
	@Override
	public void max(double value)
	{
		MaxUnaryFunction function = new MaxUnaryFunction(value);
		
		doPointOperation(function);
	}

	
	/** run the MEDIAN_FILTER on current ROI area of current plane data. only applies to UnsignedByte data */
	@Override
	public void medianFilter()
	{
		if (this.isUnsignedByte)
		{
			filter(FilterType.MEDIAN_FILTER);
		}
	}

	/** apply the MINIMUM point operation over the current ROI area of the current plane of data */
	@Override
	public void min(double value)
	{
		MinUnaryFunction function = new MinUnaryFunction(value);
		
		doPointOperation(function);
	}
	
	/** apply the MULT point operation over the current ROI area of the current plane of data */
	@Override
	public void multiply(double value)
	{
		MultiplyUnaryFunction function = new MultiplyUnaryFunction(this.type, value);
		
		doPointOperation(function);
	}
	
	/** add noise to the current ROI area of current plane data. */
	@Override
	public void noise(double range)
	{
		ProgressTracker tracker = new ProgressTracker(this, roiWidth*roiHeight, 20*roiWidth);
		
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
				tracker.didOneMore();
			}
		}
		tracker.done();
	}

	/** apply the OR point operation over the current ROI area of the current plane of data */
	@Override
	public void or(int value)
	{
		OrUnaryFunction func = new OrUnaryFunction(this.type, value);
		
		doPointOperation(func);
	}
	
	/** set the pixel at x,y to the given int value. if float data value is a float encoded as an int. if x,y, out of bounds do nothing. */
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

	/** set the pixel at x,y to the given double value. if integral data the value is biased by 0.5 do force rounding.
	 *  if x,y, out of bounds do nothing.
	 */
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

	/** sets the current plane data to that stored in the snapshot */
	@Override
	public void reset()
	{
		if (this.snapshot!=null)
		{
			this.snapshot.pasteIntoImage(this.imageData);
			findMinAndMax();
		}
	}

	/** sets the current ROI area data to that stored in the snapshot wherever the mask is nonzero */
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

		int[] imageOrigin = originOfRoi();
		int[] imageSpan = spanOfRoiPlane();

		ResetUsingMaskOperation<T> resetOp = new ResetUsingMaskOperation<T>(snapData,snapOrigin,snapSpan,this.imageData,imageOrigin,imageSpan,mask);
		
		resetOp.execute();
	}

	// TODO - refactor
	/** create a new processor that has specified dimensions. populate it's data with interpolated pixels from this processor's ROI area data. */
	@Override
	public ImageProcessor resize(int dstWidth, int dstHeight)
	{
		if (roiWidth==dstWidth && roiHeight==dstHeight)
			if (this.isUnsignedByte)
				return crop();

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
		if (interpolationMethod==BICUBIC)
		{
			for (int y=0; y<=dstHeight-1; y++)
			{
				ys = (y-dstCenterY)/yScale + srcCenterY;
				int index = y*dstWidth;
				for (int x=0; x<=dstWidth-1; x++)
				{
					xs = (x-dstCenterX)/xScale + srcCenterX;
					double value = getBicubicInterpolatedPixel(xs, ys, this);
					if (this.isIntegral)
					{
						value += 0.5;
						value = TypeManager.boundValueToType(this.type, value);
					}
					ip2.setd(index++, value);
				}
				if (y%30==0) showProgress((double)y/dstHeight);
			}
		}
		else
		{  // not BICUBIC
			double xlimit = width-1.0, xlimit2 = width-1.001;
			double ylimit = height-1.0, ylimit2 = height-1.001;
			int index1, index2;
			for (int y=0; y<=dstHeight-1; y++)
			{
				ys = (y-dstCenterY)/yScale + srcCenterY;
				if (interpolationMethod==BILINEAR)
				{
					if (ys<0.0) ys = 0.0;
					if (ys>=ylimit) ys = ylimit2;
				}
				index1 = width*(int)ys;
				index2 = y*dstWidth;
				for (int x=0; x<=dstWidth-1; x++)
				{
					xs = (x-dstCenterX)/xScale + srcCenterX;
					if (interpolationMethod==BILINEAR)
					{
						if (xs<0.0) xs = 0.0;
						if (xs>=xlimit) xs = xlimit2;
						double value = getInterpolatedPixel(xs, ys);
						if (this.isIntegral)
						{
							value += 0.5;
							value = TypeManager.boundValueToType(this.type, value);
						}
						ip2.setd(index2++, value);
					}
					else  // interp == NONE
					{
						double value = getd(index1+(int)xs);
						ip2.setd(index2++, value);
					}
				}
				if (y%30==0) showProgress((double)y/dstHeight);
			}
		}
		showProgress(1.0);
		return ip2;
	}

	// TODO - refactor
	/** rotates current ROI area pixels by given angle. destructive. interpolates pixel values as needed. */
	@Override
	public void rotate(double angle)
	{
		// TODO - for some reason Float and Short don't check for rotate by 0.0 and their results are different than doing nothing!!!!!
		if ((angle%360==0) && (this.isUnsignedByte))
			return;
		
		Object pixels2 = getPixelsCopy();
		
		ImgLibProcessor<?> ip2 = ImageUtils.createProcessor(getWidth(), getHeight(), pixels2, TypeManager.isUnsignedType(this.type));
		
		if (interpolationMethod==BICUBIC)
			ip2.setBackgroundValue(getBackgroundValue());

		double centerX = roiX + (roiWidth-1)/2.0;
		double centerY = roiY + (roiHeight-1)/2.0;
		int xMax = roiX + roiWidth - 1;
		
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
		
		if (interpolationMethod==BICUBIC)
		{
			for (int y=roiY; y<(roiY + roiHeight); y++)
			{
				index = y*width + roiX;
				tmp3 = tmp1 - y*sa + centerX;
				tmp4 = tmp2 + y*ca + centerY;
				for (int x=roiX; x<=xMax; x++)
				{
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
		}
		else
		{
			for (int y=roiY; y<(roiY + roiHeight); y++)
			{
				index = y*width + roiX;
				tmp3 = tmp1 - y*sa + centerX;
				tmp4 = tmp2 + y*ca + centerY;
				for (int x=roiX; x<=xMax; x++)
				{
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

	// TODO - refactor
	/** scale current ROI area in x and y by given factors. destructive. calculates interpolated pixels as needed */
	@Override
	public void scale(double xScale, double yScale)
	{
		// TODO - in original ByteProcessor code right here
		// if (!bgColorSet && isInvertedLut()) bgColor = 0;
		
		double xCenter = roiX + roiWidth/2.0;
		double yCenter = roiY + roiHeight/2.0;
		
		int xmin, xmax, ymin, ymax;
		if ((xScale>1.0) && (yScale>1.0))
		{
			//expand roi
			xmin = (int)(xCenter-(xCenter-roiX)*xScale);
			if (xmin<0) xmin = 0;
			
			xmax = xmin + (int)(roiWidth*xScale) - 1;
			if (xmax>=width) xmax = width - 1;
			
			ymin = (int)(yCenter-(yCenter-roiY)*yScale);
			if (ymin<0) ymin = 0;
			
			ymax = ymin + (int)(roiHeight*yScale) - 1;
			if (ymax>=height) ymax = height - 1;
		}
		else
		{
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
		
		if (interpolationMethod==BICUBIC)
		{
			for (int y=ymin; y<=ymax; y++)
			{
				ys = (y-yCenter)/yScale + yCenter;
				int index = y*width + xmin;
				for (int x=xmin; x<=xmax; x++)
				{
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
		}
		else
		{
			double xlimit = width-1.0, xlimit2 = width-1.001;
			double ylimit = height-1.0, ylimit2 = height-1.001;
			for (int y=ymin; y<=ymax; y++)
			{
				ys = (y-yCenter)/yScale + yCenter;
				ysi = (int)ys;
				if (ys<0.0) ys = 0.0;			
				if (ys>=ylimit) ys = ylimit2;
				index1 = y*width + xmin;
				index2 = width*(int)ys;
				for (int x=xmin; x<=xmax; x++)
				{
					xs = (x-xCenter)/xScale + xCenter;
					xsi = (int)xs;
					if (checkCoordinates && ((xsi<xmin) || (xsi>xmax) || (ysi<ymin) || (ysi>ymax)))
					{
						if (this.isUnsignedByte)
							setd(index1++, this.getBackgroundValue());
						else
							setd(index1++, this.min);
					}
					else  // interpolated pixel within bounds of image
					{
						if (interpolationMethod==BILINEAR)
						{
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

	/** set the pixel at x,y, to provided int value. if float data then value is a float encoded as an int */
	@Override
	public void set(int x, int y, int value) 
	{
		int[] position = Index.create(x, y, this.planePosition);
		
		LocalizableByDimCursor<T> cursor = this.cachedCursor.get();
		
		cursor.setPosition(position);
		
		double dVal = value;

		if (!this.isIntegral)
			dVal = Float.intBitsToFloat(value);

		cursor.getType().setReal( dVal );
		
		// do not close cursor - using cached one
	}

	/** set the pixel at index to provided int value. if float data then value is a float encoded as an int */
	@Override
	public void set(int index, int value) 
	{
		int width = getWidth();
		int x = index % width;
		int y = index / width;
		set( x, y, value) ;
	}

	/** set the current background value. only applies to UnsignedByte data */ 
	@Override
	public void setBackgroundValue(double value) 
	{
		// only set for unsigned byte type like ImageJ (maybe need to extend to integral types and check min/max pixel ranges)
		if (this.isUnsignedByte)
		{
			if (value < 0) value = 0;
			if (value > 255) value = 255;
			value = (int) value;
			this.backgroundValue = value;
		}
	}

	/** set the current foreground value. */ 
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

	// not an override
	/** set the pixel at x,y to the provided double value. truncates values for integral types. */
	public void setd(int x, int y, double value)
	{
		int[] position = Index.create(x, y, this.planePosition);
		
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

	// not an override
	/** set the pixel at index to the provided double value. truncates values for integral types. */
	public void setd(int index, double value)
	{
		int width = getWidth();
		int x = index % width;
		int y = index / width;
		setd( x, y, value);
	}

	/** set the pixel at x,y to the given float value. truncates values for integral types. */
	@Override
	public void setf(int x, int y, float value)
	{
		setd(x, y, (double)value);
	}

	/** set the pixel at index to the given float value. truncates values for integral types. */
	@Override
	public void setf(int index, float value)
	{
		int width = getWidth();
		int x = index % width;
		int y = index / width;
		setf( x, y, value);
	}

	/** set min and max values to provided values. resets the threshold as needed. if passed (0,0) it will calculate actual min and max 1st. */
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

	/** set the current image plane data to the provided pixel values */
	@Override
	public void setPixels(Object pixels)
	{
		setImagePlanePixels(this.imageData, this.planePosition, pixels);
	}

	/** set the current image plane data to the pixel values of the provided FloatProcessor. channelNumber is ignored. */
	@Override
	public void setPixels(int channelNumber, FloatProcessor fp)
	{
		// like ByteProcessor ignore channel number

		setPixels(fp.getPixels());
	}

	/** sets the current snapshot pixels to the provided pixels. it copies data rather than changing snapshot reference. */
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

	/** sets the current fill/foreground value */
	@Override
	public void setValue(double value) 
	{
		this.fillColor = value;
		if (this.isIntegral)
		{
			super.fgColor = (int) TypeManager.boundValueToType(this.type, this.fillColor);
		}
	}

	/** copy the current pixels to the snapshot array */
	@Override
	public void snapshot() 
	{
		int[] origins = originOfImage();

		int[] spans = spanOfImagePlane();
		
		this.snapshot = new Snapshot<T>(this.imageData, origins, spans);
	}

	/** apply the SQR point operation over the current ROI area of the current plane of data */
	@Override
	public void sqr()
	{
		SqrUnaryFunction function = new SqrUnaryFunction(this.type, this.getMaxAllowedValue());
		
		doPointOperation(function);
	}
	
	/** apply the SQRT point operation over the current ROI area of the current plane of data */
	@Override
	public void sqrt()
	{
		SqrtUnaryFunction function = new SqrtUnaryFunction(this.type);
		
		doPointOperation(function);
	}

	/** calculates actual min and max values present and resets the threshold */
	@Override
	public void resetMinAndMax()
	{
		// TODO - From FloatProc - there is code for setting the fixedScale boolean. May need it.
		
		findMinAndMax();
		
		resetThreshold();
	}

	/** makes the image binary (values of 0 and 255) splitting the pixels based on their relationship to the threshold level.
	 *  only applies to integral data types.
	 */
	@Override
	public void threshold(int thresholdLevel) 
	{
		if (!this.isIntegral)
			return;

		int[] origin = originOfImage();
		
		int[] span = spanOfImagePlane();
		
		ThresholdOperation<T> threshOp = new ThresholdOperation<T>(this.imageData,origin,span,thresholdLevel);
		
		threshOp.execute();
	}

	/** creates a FloatProcessor whose pixel values are set to those of this processor. */
	@Override
	public FloatProcessor toFloat(int channelNumber, FloatProcessor fp)
	{
		int width = getWidth();
		int height = getHeight();
		
		long size = getTotalSamples();
		
		if (size > Integer.MAX_VALUE)
			throw new IllegalArgumentException("desired size of pixel array is too large (> "+Integer.MAX_VALUE+" entries)");
		
		int allocatedSize = (int) size;
		
		if (fp == null || fp.getWidth()!=width || fp.getHeight()!=height)
			fp = new FloatProcessor(width, height, new float[allocatedSize], super.cm);
		
		int[] origin = originOfImage();
		
		int[] span = spanOfImagePlane();
		
		SetFloatValuesOperation<T> floatOp = new SetFloatValuesOperation<T>(this.imageData, origin, span, fp);

		floatOp.execute();
		
		fp.setRoi(getRoi());
		fp.setMask(getMask());
		fp.setMinAndMax(this.min, this.max);
		fp.setThreshold(getMinThreshold(), getMaxThreshold(), ImageProcessor.NO_LUT_UPDATE);

		return fp;
	}
	
	/** apply the XOR point operation over the current ROI area of the current plane of data */
	@Override
	public void xor(int value)
	{
		XorUnaryFunction func = new XorUnaryFunction(this.type, value);
		
		doPointOperation(func);
	}
}
