package ij.process;

import ij.ImagePlus;
import ij.ImageStack;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;

import loci.common.DataTools;

import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.cursor.special.RegionOfInterestCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.Type;
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
//   For copyBits() I may need to implement convertToFloat(), convertToByte(), and convertToShort()
//   Make sure that resetMinAndMax() and/or findMinAndMax() are called at appropriate times. Maybe base class does this for us?
//   I have not yet mirrored ImageJ's signed 16 bit hacks. Will need to test Image<ShortType> and see if things work versus an ImagePlus.
//   Review imglib's various cursors and perhaps change which ones I'm using.
//   Nearly all methods below broken for ComplexType and and LongType
//   All methods below assume x and y first two dimensions and that the Image<T> consists of XY planes
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

	private static enum PixelType {BYTE,SHORT,INT,FLOAT,DOUBLE,LONG};

	//****************** Instance variables *******************************************************
	
	private final Image<T> imageData;

	// TODO: How can we use generics here without breaking javac?
	@SuppressWarnings("rawtypes")
	private final RealType type;
	private boolean isIntegral;
	private byte[] pixels8;
	private Snapshot<T> snapshot;
	private ImageProperties<T> imageProperties;
	// TODO - move some of these next ones to imageProperties
	private double min, max;
	private double fillColor;
	
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
	
	public ImgLibProcessor(Image<T> img, T type, long planeNumber )
	{
		final int[] dims = img.getDimensions();
		
		if (dims.length < 2)
			throw new IllegalArgumentException("Image must be at least 2-D");

		int[] thisPlanePosition = ImageUtils.getPlanePosition(dims, planeNumber);
		
		this.imageData = img;
		this.type = type;
		
		//assign the properties object for the image
		this.imageProperties = new ImageProperties< T >( );
		
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

	//****************** Operations methods *******************************************************

	/*  idea to abstract the visiting of the image via applying Operations. Only defined for RoiOperations at the moment.
	 *  To expand: do a PositionalOperation class for LocalizableByDim loops. crop(), and flipVertical() challenge the RoiOperation design.
	*/
	
	private abstract class Operation
	{
		public abstract void beforeIteration(RealType type);
		public abstract void insideIteration(RealType sample);
		public abstract void afterIteration();
	}
	
	private abstract class RoiOperation extends Operation
	{
		Image<?> image;
		int[] origin, span;
		
		protected RoiOperation(Image<?> image, int[] origin, int[] span)
		{
			this.image = image;
			this.origin = origin.clone();
			this.span = span.clone();
		}
		
		public int[] getDimsOrigin() { return origin; }
		public int[] getDimsSpan() { return span; }
	}
	
	private void applyRoiOperation(RoiOperation op)
	{
		final LocalizableByDimCursor<T> imageCursor = this.imageData.createLocalizableByDimCursor();
		final RegionOfInterestCursor<T> imageRoiCursor = new RegionOfInterestCursor< T >( imageCursor, op.getDimsOrigin(), op.getDimsSpan() );
		
		op.beforeIteration(imageRoiCursor.getType());
		
		//iterate over all the pixels, of the selected image plane
		for (T sample : imageRoiCursor)
		{
			op.insideIteration(sample);
		}
		
		op.afterIteration();
		
		imageRoiCursor.close();
		imageCursor.close();
	}
	
	private abstract class DualRoiOperation
	{
		Image<?> img1, img2;
		int[] origin1, span1, origin2, span2;
		
		protected DualRoiOperation(Image<?> img1, int[] origin1, int[] span1, Image<?> img2, int[] origin2, int[] span2)
		{
			this.img1 = img1;
			this.origin1 = origin1.clone();
			this.span1 = span1.clone();

			this.img2 = img2;
			this.origin2 = origin2.clone();
			this.span2 = span2.clone();
		}
		
		public Image<?> getImage1()   { return img1; }
		public int[] getDimsOrigin1() { return origin1; }
		public int[] getDimsSpan1()   { return span1; }
		
		public Image<?> getImage2()   { return img2; }
		public int[] getDimsOrigin2() { return origin2; }
		public int[] getDimsSpan2()   { return span2; }

		public abstract void beforeIteration(RealType type1, RealType type2);
		public abstract void insideIteration(RealType sample1, RealType sample2);
		public abstract void afterIteration();
	}
	
	private void applyDualRoiOperation(DualRoiOperation op)
	{
		
		op.getImage2().createLocalizableByDimCursor();

		LocalizableByDimCursor<T> image1Cursor = (LocalizableByDimCursor<T>) op.getImage1().createLocalizableByDimCursor();
		LocalizableByDimCursor<T> image2Cursor = (LocalizableByDimCursor<T>) op.getImage2().createLocalizableByDimCursor();

		RegionOfInterestCursor<T> image1RoiCursor = new RegionOfInterestCursor<T>(image1Cursor, op.getDimsOrigin1(), op.getDimsSpan1());
		RegionOfInterestCursor<T> image2RoiCursor = new RegionOfInterestCursor<T>(image2Cursor, op.getDimsOrigin2(), op.getDimsSpan2());
		
		op.beforeIteration(image1Cursor.getType(),image2Cursor.getType());
		
		while (image1RoiCursor.hasNext() && image2RoiCursor.hasNext())
		{
			image1RoiCursor.fwd();
			image2RoiCursor.fwd();
			
			op.insideIteration(image1Cursor.getType(),image2Cursor.getType());
		}
		
		image1RoiCursor.close();
		image2RoiCursor.close();
		image1Cursor.close();
		image2Cursor.close();
	}
	
	private class ApplyLutOperation extends RoiOperation
	{
		int[] lut;
		
		ApplyLutOperation(Image<?> image, int[] origin, int[] span, int[] lut)
		{
			super(image,origin,span);
		
			this.lut = lut;
		}
		
		@Override
		public void beforeIteration(RealType type) {
		}

		@Override
		public void afterIteration() {
		}

		@Override
		public void insideIteration(RealType sample) {
			int value = this.lut[(int)sample.getRealDouble()];
			sample.setReal(value);
		}
		
	}
	
	private class CropOperation extends DualRoiOperation
	{
		CropOperation(Image<?> img1, int[] origin1, int[] span1, Image<?> img2, int[] origin2, int[] span2)
		{
			super(img1,origin1,span1,img2,origin2,span2);
		}
		
		@Override
		public void afterIteration() {
		}

		@Override
		public void beforeIteration(RealType type1, RealType type2) {
		}

		@Override
		public void insideIteration(RealType sample1, RealType sample2) {
			double value = sample1.getRealDouble();
			sample2.setReal(value);
		}
		
	}
	
	private class HistogramOperation extends RoiOperation
	{
		ImageProcessor mask;
		int[] histogram;
		int pixIndex;
		
		HistogramOperation(Image<?> image, int[] origin, int[] span, ImageProcessor mask, int lutSize)
		{
			super(image,origin,span);
		
			this.mask = mask;
			
			this.histogram = new int[lutSize];
		}
		
		public int[] getHistogram()
		{
			return this.histogram;
		}
		
		@Override
		public void beforeIteration(RealType type) {
			this.pixIndex = 0;
		}

		@Override
		public void afterIteration() {
		}

		@Override
		public void insideIteration(RealType sample) {
			if ((this.mask == null) || (this.mask.get(pixIndex) > 0))
				this.histogram[(int)sample.getRealDouble()]++;
			pixIndex++;
		}
		
	}

	private class MinMaxOperation extends RoiOperation
	{
		double min, max;
		
		MinMaxOperation(Image<?> image, int[] origin, int[] span)
		{
			super(image,origin,span);
		}
		
		public double getMax() { return this.max; }
		public double getMin() { return this.min; }
		
		@Override
		public void beforeIteration(RealType type)
		{
			this.min = type.getMaxValue();
			this.max = type.getMinValue();
		}
		
		@Override
		public void insideIteration(RealType sample)
		{
			double value = sample.getRealDouble();
			
			if ( value > this.max )
				this.max = value;

			if ( value < this.min )
				this.min = value;
		}
		
		@Override
		public void afterIteration()
		{
		}
	}

	private class ResetUsingMaskOperation extends DualRoiOperation
	{
		byte[] maskPixels;
		int pixNum;
		
		ResetUsingMaskOperation(Image<?> img1, int[] origin1, int[] span1, Image<?> img2, int[] origin2, int[] span2, ImageProcessor mask)
		{
			super(img1,origin1,span1,img2,origin2,span2);
			
			this.maskPixels = (byte[])mask.getPixels();
		}
		
		@Override
		public void afterIteration() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void beforeIteration(RealType type1, RealType type2) {
			pixNum = 0;
		}

		@Override
		public void insideIteration(RealType sample1, RealType sample2) {
			if (maskPixels[pixNum++] == 0)
			{
				double pix = sample1.getRealDouble();
				sample2.setReal(pix);
			}
		}
		
	}
	
	private class ThresholdOperation extends RoiOperation
	{
		double threshold, min, max;
		
		ThresholdOperation(Image<?> image, int[] origin, int[] span, double threshold)
		{
			super(image,origin,span);
			
			this.threshold = threshold;
		}
		
		@Override
		public void beforeIteration(RealType type)
		{
			this.threshold = TypeManager.boundValueToType(type, this.threshold);
			this.min = type.getMinValue();
			this.max = type.getMaxValue();
		}
		
		@Override
		public void insideIteration(RealType sample)
		{
			if (sample.getRealDouble() <= this.threshold)
				sample.setReal(this.min);
			else
				sample.setReal(this.max);
		}
		
		@Override
		public void afterIteration()
		{
		}

	}
	
	//****************** Helper methods *******************************************************

	private long getNumPixels()
	{
		return ImageUtils.getTotalSamples(this.imageData.getDimensions());
	}

	private void findMinAndMax()
	{
		// TODO - should do something different for UnsignedByte (involving LUT) if we mirror ByteProcessor

		//get the current image data
		int[] imageDimensionsOffset = Index.create(0, 0, this.imageProperties.getPlanePosition());
		int[] imageDimensionsSize = Span.singlePlane(super.width, super.height, this.imageData.getNumDimensions());

		MinMaxOperation mmOp = new MinMaxOperation(this.imageData,imageDimensionsOffset,imageDimensionsSize);
		
		applyRoiOperation(mmOp);
		
		setMinAndMaxOnly(mmOp.getMin(), mmOp.getMax());
		
		showProgress(1.0);
	}
	
	/*  OLD WAY
	private void findMinAndMax()
	{
		// TODO - should do something different for UnsignedByte (involving LUT) if we mirror ByteProcessor

		//get the current image data
		int[] imageDimensionsOffset = Index.create(0, 0, this.imageProperties.getPlanePosition());
		int[] imageDimensionsSize = Span.singlePlane(super.width, super.height, this.imageData.getNumDimensions());
		
		//Get a cursor
		final LocalizableByDimCursor<T> imageCursor = this.cachedCursor.get();
		final RegionOfInterestCursor<T> imageROICursor = new RegionOfInterestCursor< T >( imageCursor, imageDimensionsOffset, imageDimensionsSize );
				
		//assign crazy values
		this.max = imageCursor.getType().getMinValue();
		this.min = imageCursor.getType().getMaxValue();
 
		//iterate over all the pixels, of the selected image plane
		for (T sample : imageROICursor)
		{
			double value = sample.getRealDouble();
			
			if ( value > this.max )
				this.max = value;

			if ( value < this.min )
				this.min = value;
		}
		
		//close the cursor
		imageROICursor.close( );
		
		showProgress(1.0);
	}
	*/
	
	/*
	 * Throws an exception if the LUT length is wrong for the pixel layout type
	 */
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
	
	private Object getCopyOfPixelsFromImage(Image<T> image, RealType type, int[] planePos)
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
	
	private double getPixValue(Object pixels, PixelType inputType, boolean unsigned, int pixNum)
	{
		switch (inputType) {
			case BYTE:
				byte b = ((byte[])pixels)[pixNum];
				if ((unsigned) && (b < 0))
					return 256.0 + b;
				else
					return b;
			case SHORT:
				short s = ((short[])pixels)[pixNum];
				if ((unsigned) && (s < 0))
					return 65536.0 + s;
				else
					return s;
			case INT:
				int i = ((int[])pixels)[pixNum];
				if ((unsigned) && (i < 0))
					return 4294967296.0 + i;
				else
					return i;
			case FLOAT:
				return ((float[])pixels)[pixNum];
			case DOUBLE:
				return ((double[])pixels)[pixNum];
			case LONG:
				return ((long[])pixels)[pixNum];  // TODO : possible precision loss here
			default:
				throw new IllegalArgumentException("unknown pixel type");
		}
	}

	private void setPlane(Image<T> theImage, int[] position, Object pixels, PixelType inputType, long numPixels)
	{
		if (numPixels != getNumPixels())
			throw new IllegalArgumentException("setPlane() error: input data does not have same dimensions as internal storage");
		
		boolean isUnsigned = TypeManager.isUnsignedType(this.type);
		
		LocalizableByDimCursor<T> cursor = theImage.createLocalizableByDimCursor();  // cannot use cached cursor here
		
		int pixNum = 0;
		
		for (int y = 0; y < super.height; y++) {
			
			position[1] = y;
			
			for (int x = 0; x < super.width; x++) {
				
				position[0] = x;
				
				cursor.setPosition(position);
				
				T pixRef = cursor.getType();
				
				double inputPixValue = getPixValue(pixels, inputType, isUnsigned, pixNum++);
				
				if (this.isIntegral)
					inputPixValue = TypeManager.boundValueToType(this.type, inputPixValue);
				
				pixRef.setReal(inputPixValue);
			}
		}
		
		cursor.close();  // since a local cursor close it
	}
	
	private Object getPixelsArray() {
		return getCopyOfPixelsFromImage(this.imageData, this.type, this.imageProperties.getPlanePosition());
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
		
		switch (op)
		{
			case FILL:
				throw new RuntimeException("Unimplemented");
			case ADD:
				throw new RuntimeException("Unimplemented");
			case MULT:
				throw new RuntimeException("Unimplemented");
			case AND:
				if (!this.isIntegral)
					return; 
				throw new RuntimeException("Unimplemented");
			case OR:
				if (!this.isIntegral)
					return; 
				throw new RuntimeException("Unimplemented");
			case XOR:
				if (!this.isIntegral)
					return; 
				throw new RuntimeException("Unimplemented");
			case GAMMA:
				throw new RuntimeException("Unimplemented");
			case LOG:
				throw new RuntimeException("Unimplemented");
			case EXP:
				throw new RuntimeException("Unimplemented");
			case SQR:
				throw new RuntimeException("Unimplemented");
			case SQRT:
				throw new RuntimeException("Unimplemented");
			case ABS:
				throw new RuntimeException("Unimplemented");
			case MINIMUM:
				throw new RuntimeException("Unimplemented");
			case MAXIMUM:
				throw new RuntimeException("Unimplemented");
			default:
				throw new IllegalArgumentException("doProcess() error: passed an unknown operation " + op);
		}
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
		
		int[] index = Index.create(super.roiX, super.roiY, this.imageProperties.getPlanePosition());
		int[] span = Span.singlePlane(super.roiWidth, super.roiHeight, this.imageData.getNumDimensions());

		ApplyLutOperation lutOp = new ApplyLutOperation(this.imageData,index,span,lut);
		
		applyRoiOperation(lutOp);
	}

	/* OLD WAY
	@Override
	public void applyTable(int[] lut) 
	{
		if (!this.isIntegral)
			return;

		verifyLutLengthOkay(lut);
		
		int[] index = Index.create(super.roiX, super.roiY, this.imageProperties.getPlanePosition());
		int[] span = Span.singlePlane(super.roiWidth, super.roiHeight, this.imageData.getNumDimensions());
		
		//Fill the image with data - first get a cursor
		final LocalizableByDimCursor<T> imageCursor = this.cachedCursor.get();
		RegionOfInterestCursor<T> imageRegionOfInterestCursor = new RegionOfInterestCursor< T >( imageCursor, index, span );
		
		for (final T pixel:imageRegionOfInterestCursor)
		{
			pixel.setReal( lut[ (int) pixel.getRealDouble() ] );
		}
		
		//close the roi cursor
		imageRegionOfInterestCursor.close( );
	}
	 */
	
	@Override
	public void convolve(float[] kernel, int kernelWidth, int kernelHeight)
	{
		throw new RuntimeException("Unimplemented");

	}

	@Override
	public void convolve3x3(int[] kernel)
	{
		throw new RuntimeException("Unimplemented");

	}

	@Override
	public void copyBits(ImageProcessor ip, int xloc, int yloc, int mode)
	{
		FloatProcessor proc = ip.toFloat(0,null); 
		new FloatBlitter(proc).copyBits(ip, xloc, yloc, mode);
	}

	//TODO ask about changing name of Image to avoid conflict with java.awt
	@Override
	public java.awt.Image createImage()
	{
		boolean firstTime = this.pixels8==null;
		if (firstTime || !super.lutAnimation)
			create8BitImage();
		if (super.cm==null)
			makeDefaultColorModel();
		if (super.source==null) {
			super.source = new MemoryImageSource(super.width, super.height, super.cm, this.pixels8, 0, super.width);
			super.source.setAnimated(true);
			super.source.setFullBufferUpdates(true);
			super.img = Toolkit.getDefaultToolkit().createImage(super.source);
		} else if (super.newPixels) {
			super.source.newPixels(this.pixels8, super.cm, 0, super.width);
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
		ImageProcessor ip2 = new ImgLibProcessor<T>(image, (T)this.type, 0);
		ip2.setColorModel(getColorModel());
		// TODO - ByteProcessor does this conditionally. Do we mirror here?
		ip2.setMinAndMax(getMin(), getMax());
		ip2.setInterpolationMethod(super.interpolationMethod);
		return ip2;
	}

	@Override
	public ImageProcessor crop()
	{	
		int[] imageOrigin = Index.create(super.roiX, super.roiY, this.imageProperties.getPlanePosition());
		int[] imageSpan = Span.singlePlane(super.roiWidth, super.roiHeight, this.imageData.getNumDimensions());
		
		int[] newImageOrigin = Index.create(2);
		int[] newImageSpan = Span.singlePlane(super.roiWidth, super.roiHeight, 2);

		Image<T> newImage = this.imageData.createNewImage(newImageSpan);
		
		CropOperation cropOp = new CropOperation(this.imageData, imageOrigin, imageSpan, newImage, newImageOrigin, newImageSpan);
		
		applyDualRoiOperation(cropOp);

		return new ImgLibProcessor<T>(newImage, (T)this.type, 0);
	}
	
	/* OLD WAY
	@Override
	public ImageProcessor crop()
	{	
		int[] originInImage = Index.create(super.roiX, super.roiY, this.imageProperties.getPlanePosition());
		int[] extentsInImage = Span.singlePlane(super.roiWidth, super.roiHeight, this.imageData.getNumDimensions());
		
		// TODO - fine as is? pass all dims with some extent of 1? or even crop all planes of Image?
		int[] originInNewImage = Index.create(2);
		int[] extentsInNewImage = Span.singlePlane(super.roiWidth, super.roiHeight, 2);

		Image<T> newImage = this.imageData.createNewImage(extentsInNewImage);
		
		LocalizableByDimCursor<T> imageDimCursor = this.cachedCursor.get();
		LocalizableByDimCursor<T> newImageDimCursor = newImage.createLocalizableByDimCursor();
		
		RegionOfInterestCursor<T> imageRoiCursor = new RegionOfInterestCursor<T>(imageDimCursor, originInImage, extentsInImage);
		RegionOfInterestCursor<T> newImageRoiCursor = new RegionOfInterestCursor<T>(newImageDimCursor, originInNewImage, extentsInNewImage);
		
		while (imageRoiCursor.hasNext() && newImageRoiCursor.hasNext())
		{
			imageRoiCursor.fwd();
			newImageRoiCursor.fwd();
			double value = imageRoiCursor.getType().getRealDouble(); 
			newImageRoiCursor.getType().setReal(value); 
		}

		// close the relevant cursors
		imageRoiCursor.close();
		newImageRoiCursor.close();
		newImageDimCursor.close();

		return new ImgLibProcessor<T>(newImage, (T)this.type, 0);
	}
	*/
	
	@Override
	public void dilate()
	{
		if (this.type instanceof UnsignedByteType)
		{
			// TODO
			throw new RuntimeException("Unimplemented");
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
		ImageProcessor proc = createProcessor(super.width, super.height);

		LocalizableByDimCursor<T> cursor = this.cachedCursor.get();
		
		int[] position = Index.create(0, 0, this.imageProperties.getPlanePosition());
		
		for (int x = 0; x < super.width; x++) {
			position[0] = x;
			for (int y = 0; y < super.height; y++) {
				position[1] = y;
				cursor.setPosition(position);
				float floatVal = cursor.getType().getRealFloat();
				proc.setf(x, y, floatVal);
			}
		}
		
		return proc;
	}

	@Override
	public void erode()
	{
		if (this.type instanceof UnsignedByteType)
		{
			// TODO
			throw new RuntimeException("Unimplemented");
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
		
		int roiWidth = super.roiWidth, roiHeight = super.roiHeight;
		int roiX = super.roiX, roiY = super.roiY;
		
		if (mask.getWidth()!=roiWidth || mask.getHeight()!=roiHeight)
			return;
		
		byte[] mpixels = (byte[])mask.getPixels();
		
		for (int y=roiY, my=0; y<(roiY+roiHeight); y++, my++) {
			int i = y * super.width + roiX;
			int mi = my * roiWidth;
			for (int x=roiX; x<(roiX+roiWidth); x++) {
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

	@Override
	public void filter(int type)
	{
		throw new RuntimeException("Unimplemented");
		// TODO - make special calls to erode() and dilate() I think when ByteType. Copy ByteProcessor. See other processors too.
	}

	/** swap the rows of an image about its central row */
	@Override
	public void flipVertical()
	{
		// create suitable cursors - noncached
		final LocalizableByDimCursor<T> cursor1 = this.imageData.createLocalizableByDimCursor( );
		final LocalizableByDimCursor<T> cursor2 = this.imageData.createLocalizableByDimCursor( );
		
		// allocate arrays that will hold position variables
		final int[] position1 = Index.create(0, 0, this.imageProperties.getPlanePosition());
		final int[] position2 = Index.create(0, 0, this.imageProperties.getPlanePosition());
		
		// calc some useful variables in regards to our region of interest.
		final int minX = super.roiX;
		final int minY = super.roiY;
		final int maxX = minX + super.roiWidth - 1;
		final int maxY = minY + super.roiHeight - 1;
		
		// calc half height - we will only need to swap the top half of the rows with the bottom half
		final int halfRoiHeight = super.roiHeight / 2;
		
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
		
		//final LocalizableByDimCursor<T> cursor = this.imageData.createLocalizableByDimCursor();
		cachedCursor.get().setPosition( Index.create(x, y, this.imageProperties.getPlanePosition()) );
		
		value = (int)( cachedCursor.get().getType().getRealDouble() );
		
		//cursor.close( );
		
		return value;
	}

	@Override
	public int get(int index)
	{
		int x = index % super.width;
		int y = index / super.width;
		return get( x, y) ;
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
			int[] origin = Index.create(0,0,this.imageProperties.getPlanePosition());
			
			int[] span = Span.singlePlane(super.width, super.height, this.imageData.getNumDimensions());
			
			int lutSize = (int) (this.getMaxAllowedValue() + 1);
	
			HistogramOperation histOp = new HistogramOperation(this.imageData,origin,span,super.mask,lutSize);
			
			applyRoiOperation(histOp);
			
			return histOp.getHistogram();
		}
		
		return null;
	}
	
	/* OLD WAY
	@Override
	public int[] getHistogram()
	{
		if ((type instanceof UnsignedByteType) || (type instanceof UnsignedShortType))
		{
			int tableSize = ((int) getMaxAllowedValue()) + 1;
			
			int[] hist = new int[tableSize];

			int[] origin = Index.create(0,0,this.imageProperties.getPlanePosition());
			
			int[] span = Span.singlePlane(super.width, super.height, this.imageData.getNumDimensions());
			
			LocalizableByDimCursor<T> cursor = this.cachedCursor.get();
			RegionOfInterestCursor<T> roiCursor = new RegionOfInterestCursor<T>(cursor, origin, span);
			
			int pixIndex = 0;
			for (T sample : roiCursor)
			{
				if ((super.mask == null) || (super.mask.get(pixIndex) > 0))
					hist[(int)sample.getRealDouble()]++;
				pixIndex++;
			}
			
			roiCursor.close();
			
			return hist;
		}
		
		return null;
	}
	*/

	@Override
	public double getInterpolatedPixel(double x, double y)
	{
		throw new RuntimeException("Unimplemented");
	}

	@Override
	public double getMax() 
	{
		return this.max;
	}

	public double getMaxAllowedValue() 
	{
		return this.cachedCursor.get().getType().getMaxValue();
	}
	
	@Override
	public double getMin() 
	{
		return this.min;
	}

	public double getMinAllowedValue() 
	{
		return this.cachedCursor.get().getType().getMinValue();
	}
	
	@Override
	public int getPixel(int x, int y)
	{
		return get(x, y);
	}

	@Override
	public int getPixelInterpolated(double x, double y)
	{
		throw new RuntimeException("Unimplemented");
	}

	@Override
	public float getPixelValue(int x, int y)
	{
		// make sure its in bounds
		if ((x >= 0) && (x < super.width) && (y >= 0) && (y < super.height))
			return getf(x, y);
		
		return 0f;
	}

	@Override
	public Object getPixels()
	{
		// TODO: could add a special case for single-image 8-bit array-backed data
		// TODO: special case for new container
		return getPixelsArray();
	}

	@Override
	public Object getPixelsCopy()
	{
		if (this.snapshot!=null && super.snapshotCopyMode)
		{
			super.snapshotCopyMode = false;
			
			Image<T> snapStorage = this.snapshot.getStorage();
			
			int[] planePosOfZero = Index.create(imageProperties.getPlanePosition().length);
			
			return getCopyOfPixelsFromImage(snapStorage, this.type, planePosOfZero); 
		}
		else
		{
			return getPixelsArray();
		}
	}

	@Override
	public float getf(int x, int y) 
	{
		float value;
		
		cachedCursor.get().setPosition( Index.create(x, y, this.imageProperties.getPlanePosition()) );
		
		value =  ( float ) cachedCursor.get().getType().getRealDouble();
		
		return value;
	}

	@Override
	public float getf(int index)
	{
		int x = index % super.width;
		int y = index / super.width;
		return getf( x, y) ;
	}

	public double[] getPlaneData()
	{
		return ImageUtils.getPlaneData(this.imageData, super.width, super.height, this.imageProperties.getPlanePosition());
	}

	@Override
	public Object getSnapshotPixels()
	{
		if (this.snapshot == null)
			return null;
		
		Image<T> snapStorage = this.snapshot.getStorage();
		
		int[] planePosOfZero = Index.create(this.imageProperties.getPlanePosition().length);

		return getCopyOfPixelsFromImage(snapStorage, this.type, planePosOfZero);
	}

	@Override
	public void invert()
	{
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
			// TODO
			throw new RuntimeException("Unimplemented");
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
		throw new RuntimeException("Unimplemented");

	}

	@Override
	public void or(int value)
	{
		doProcess(OR,value);
	}
	
	@Override
	public void putPixel(int x, int y, int value)
	{
		if (x>=0 && x<super.width && y>=0 && y<super.height)
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
		if (x>=0 && x<super.width && y>=0 && y<super.height)
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

	@SuppressWarnings( "unchecked" )
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
		
		if ((mask.getWidth() != super.roiWidth) || (mask.getHeight() != super.roiHeight))
			throw new IllegalArgumentException(maskSizeError(mask));

		Image<T> snapData = this.snapshot.getStorage();
		
		int[] snapOrigin = Index.create(super.roiX, super.roiY, new int[snapData.getNumDimensions()-2]);
		int[] snapSpan = Span.singlePlane(super.roiWidth, super.roiHeight, snapData.getNumDimensions());

		int[] imageOrigin = Index.create(super.roiX, super.roiY, this.imageProperties.getPlanePosition());
		int[] imageSpan = Span.singlePlane(super.roiWidth, super.roiHeight, this.imageData.getNumDimensions());

		
		ResetUsingMaskOperation resetOp = new ResetUsingMaskOperation(snapData,snapOrigin,snapSpan,this.imageData,imageOrigin,imageSpan,mask);
		
		applyDualRoiOperation(resetOp);
	}
	
	/*  OLD WAY
	@Override
	public void reset(ImageProcessor mask)
	{
		if (mask==null || this.snapshot==null)
			return;
		
		if ((mask.getWidth() != super.roiWidth) || (mask.getHeight() != super.roiHeight))
			throw new IllegalArgumentException(maskSizeError(mask));

		Image<T> snapData = this.snapshot.getStorage();
		
		int[] originInImage = Index.create(super.roiX, super.roiY, this.imageProperties.getPlanePosition());
		int[] originInSnapshot = Index.create(super.roiX, super.roiY, new int[snapData.getNumDimensions()-2]);

		int[] spanInImage = Span.singlePlane(super.roiWidth, super.roiHeight, this.imageData.getNumDimensions());
		int[] spanInSnapshot = Span.singlePlane(super.roiWidth, super.roiHeight, snapData.getNumDimensions());
		
		LocalizableByDimCursor<T> imageCursor = this.cachedCursor.get();
		LocalizableByDimCursor<T> snapshotCursor = snapData.createLocalizableByDimCursor();

		RegionOfInterestCursor<T> imageRoiCursor = new RegionOfInterestCursor<T>(imageCursor, originInImage, spanInImage);
		RegionOfInterestCursor<T> snapRoiCursor = new RegionOfInterestCursor<T>(snapshotCursor, originInSnapshot, spanInSnapshot);
		
		byte[] maskPixels = (byte[])mask.getPixels();
		
		int i = 0;
		while (imageRoiCursor.hasNext() && snapRoiCursor.hasNext())
		{
			imageRoiCursor.fwd();
			snapRoiCursor.fwd();
			
			if (maskPixels[i++] == 0)
			{
				double pix = snapRoiCursor.getType().getRealDouble();
				imageRoiCursor.getType().setReal(pix);
			}
		}
		
		snapRoiCursor.close();
		imageRoiCursor.close();
		snapshotCursor.close();
		
		findMinAndMax();
	}
	*/
	
	@Override
	public ImageProcessor resize(int dstWidth, int dstHeight)
	{
		throw new RuntimeException("Unimplemented");
	}

	@Override
	public void rotate(double angle)
	{
		throw new RuntimeException("Unimplemented");

	}

	@Override
	public void scale(double xScale, double yScale)
	{
		throw new RuntimeException("Unimplemented");

	}

	@Override
	public void set(int x, int y, int value) 
	{
		cachedCursor.get().setPosition( Index.create(x, y, this.imageProperties.getPlanePosition()) );
		cachedCursor.get().getType().setReal( value );
	}

	@Override
	public void set(int index, int value) 
	{
		int x = index % super.width;
		int y = index / super.width;
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
					fillColor = bestIndex;
				
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
		int x = index % super.width;
		int y = index / super.width;
		setd( x, y, value);
	}

	public void setd(int x, int y, double value)
	{
		cachedCursor.get().setPosition( Index.create(x, y, this.imageProperties.getPlanePosition()) );
		
		RealType pixRef = cachedCursor.get().getType();

		// TODO - verify the following implementation is what we want to do:
		// NOTE - for an integer type backed data store imglib rounds float values. ImageJ has always truncated float values.
		//   I need to detect beforehand and do my truncation if an integer type.
		
		if (this.isIntegral)
			value = (double)Math.floor(value);
		
		pixRef.setReal( value ); 
	}

	@Override
	public void setf(int index, float value)
	{
		int x = index % super.width;
		int y = index / super.width;
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
		int[] position = Index.create(0, 0, this.imageProperties.getPlanePosition());
		
		if (pixels instanceof byte[])
			
			setPlane(this.imageData, position, pixels, PixelType.BYTE, ((byte[])pixels).length);
		
		else if (pixels instanceof short[])
			
			setPlane(this.imageData, position, pixels, PixelType.SHORT, ((short[])pixels).length);
		
		else if (pixels instanceof int[])
			
			setPlane(this.imageData, position, pixels, PixelType.INT, ((int[])pixels).length);
		
		else if (pixels instanceof float[])
			
			setPlane(this.imageData, position, pixels, PixelType.FLOAT, ((float[])pixels).length);
		
		else if (pixels instanceof double[])
			
			setPlane(this.imageData, position, pixels, PixelType.DOUBLE, ((double[])pixels).length);
		
		else if (pixels instanceof long[])
			
			setPlane(this.imageData, position, pixels, PixelType.LONG, ((long[])pixels).length);
		
		else
			throw new IllegalArgumentException("unknown object passed to ImgLibProcessor::setPixels() - "+ pixels.getClass());
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
		
		int[] position = Index.create(snapStorage.getNumDimensions());
		
		if (pixels instanceof byte[])
			
			setPlane(snapStorage, position, pixels, PixelType.BYTE, ((byte[])pixels).length);
		
		else if (pixels instanceof short[])
			
			setPlane(snapStorage, position, pixels, PixelType.SHORT, ((short[])pixels).length);
		
		else if (pixels instanceof int[])
			
			setPlane(snapStorage, position, pixels, PixelType.INT, ((int[])pixels).length);
		
		else if (pixels instanceof float[])
			
			setPlane(snapStorage, position, pixels, PixelType.FLOAT, ((float[])pixels).length);
		
		else if (pixels instanceof double[])
			
			setPlane(snapStorage, position, pixels, PixelType.DOUBLE, ((double[])pixels).length);
		
		else if (pixels instanceof long[])
			
			setPlane(snapStorage, position, pixels, PixelType.LONG, ((long[])pixels).length);
		
		else
			throw new IllegalArgumentException("unknown object passed to ImgLibProcessor::setSnapshotPixels() - "+ pixels.getClass());
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
		int[] origins = Index.create(0, 0, this.imageProperties.getPlanePosition());

		int[] spans = Span.singlePlane(super.width, super.height, this.imageData.getNumDimensions());
		
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

		int[] origin = Index.create(0, 0, this.imageProperties.getPlanePosition());
		int[] span = Span.singlePlane(super.width, super.height, this.imageData.getNumDimensions());
		
		ThresholdOperation threshOp = new ThresholdOperation(this.imageData,origin,span,thresholdLevel);
		
		applyRoiOperation(threshOp);
	}
	
	/* OLD WAY
	@Override
	public void threshold(int thresholdLevel) 
	{
		if (!this.isIntegral)
			return;
		
		//ensure level is OK for underlying type & convert to double
		double thresholdLevelAsDouble = TypeManager.boundValueToType(this.type, thresholdLevel);
		
		//Get a cursor
		int[] origin = Index.create(0, 0, this.imageProperties.getPlanePosition());
		int[] span = Span.singlePlane(super.width, super.height, this.imageData.getNumDimensions());
		
		// must use a RoiCursor to stay on our single plane
		final LocalizableByDimCursor<T> imageCursor = this.cachedCursor.get();
		final RegionOfInterestCursor<T> roiCursor = new RegionOfInterestCursor<T>(imageCursor, origin, span);

		double minPossible = roiCursor.getType().getMinValue();
		double maxPossible = roiCursor.getType().getMaxValue();
		
		for (final T pixel : roiCursor)
		{
			pixel.setReal( pixel.getRealDouble() <= thresholdLevelAsDouble ? minPossible : maxPossible );
		}
		
		//close the cursor
		roiCursor.close();
	}
	*/
	
	@Override
	public FloatProcessor toFloat(int channelNumber, FloatProcessor fp)
	{
		long size = getNumPixels();
		
		if (fp == null || fp.getWidth()!=super.width || fp.getHeight()!=super.height)
			fp = new FloatProcessor(super.width, super.height, new float[(int)size], super.cm);
		
		float[] fPixels = (float[])fp.getPixels();

		LocalizableByDimCursor<T> cursor = this.cachedCursor.get();
		
		int[] position = Index.create(0, 0, this.imageProperties.getPlanePosition());
		
		int pixNum = 0;
		for (int y = 0; y < super.height; y++) {
			position[1] = y;
			for (int x = 0; x < super.width; x++) {
				position[0] = x;
				cursor.setPosition(position);
				fPixels[pixNum++] = cursor.getType().getRealFloat();
			}
		}

		fp.setRoi(getRoi());
		fp.setMask(super.mask);
		fp.setMinAndMax(this.min, this.max);
		fp.setThreshold(super.minThreshold, super.maxThreshold, ImageProcessor.NO_LUT_UPDATE);

		return fp;
	}
	
	@Override
	public void xor(int value)
	{
		doProcess(XOR,value);
	}

	// TODO - belongs elsewhere???

	public static ImagePlus createImagePlus(final Image<?> img)
	{
		Cursor<?> cursor = img.createCursor();
		
		Type runtimeT = cursor.getType();
		
		cursor.close();
		
		int[] dimensions = img.getDimensions();
		
		long numPlanes = ImageUtils.getTotalPlanes(dimensions);

		ImageStack stack = new ImageStack(img.getDimension(0), img.getDimension(1));
		
		for (long plane = 0; plane < numPlanes; plane++)
		{
			ImageProcessor processor = null;
			
			if (runtimeT instanceof UnsignedByteType)
			{
				processor = new ImgLibProcessor<UnsignedByteType>((Image<UnsignedByteType>)img, new UnsignedByteType(), plane);
			}
				
			if (runtimeT instanceof ByteType)
			{
				processor = new ImgLibProcessor<ByteType>((Image<ByteType>)img, new ByteType(), plane);
			}
				
			if (runtimeT instanceof UnsignedShortType)
			{
				processor = new ImgLibProcessor<UnsignedShortType>((Image<UnsignedShortType>)img, new UnsignedShortType(), plane);
			}
				
			if (runtimeT instanceof ShortType)
			{
				processor = new ImgLibProcessor<ShortType>((Image<ShortType>)img, new ShortType(), plane);
			}
				
			if (runtimeT instanceof UnsignedIntType)
			{
				processor = new ImgLibProcessor<UnsignedIntType>((Image<UnsignedIntType>)img, new UnsignedIntType(), plane);
			}
				
			if (runtimeT instanceof IntType)
			{
				processor = new ImgLibProcessor<IntType>((Image<IntType>)img, new IntType(), plane);
			}
				
			if (runtimeT instanceof FloatType)
			{
				processor = new ImgLibProcessor<FloatType>((Image<FloatType>)img, new FloatType(), plane);
			}
				
			if (runtimeT instanceof DoubleType)
			{
				processor = new ImgLibProcessor<DoubleType>((Image<DoubleType>)img, new DoubleType(), plane);
			}
				
			if (runtimeT instanceof LongType)
			{
				processor = new ImgLibProcessor<LongType>((Image<LongType>)img, new LongType(), plane);
			}
				
			if (processor == null)
				throw new IllegalArgumentException("no processor type was matched");
			
			stack.addSlice(""+plane, processor);
		}
		
		ImagePlus imp = new ImagePlus(img.getName(), stack);
		
		// TODO - next calc only works for images with 5 or fewer dimensions and requires default ordering of xyzct
		//   We should modify ImageJ to have a setDimensions(int[] dimension) and integrate its use throughout.
		
		// let ImageJ know about dimensions
		
		int slices = 1;
		if (dimensions.length > 2)
			slices = dimensions[2];
		
		int channels = 1;
		if (dimensions.length > 3)
			channels = dimensions[3];

		int frames = 1;
		if (dimensions.length > 4)
			frames = dimensions[4];

		imp.setDimensions(channels, slices, frames);
		
		return imp;
	}
}
