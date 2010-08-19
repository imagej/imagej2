package ij.process;

import ij.Prefs;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;

import loci.common.DataTools;

import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.cursor.special.RegionOfInterestCursor;
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
//   Make sure that resetMinAndMax() and/or findMinAndMax() are called at appropriate times. Maybe base class does this for us?
//   I have not yet mirrored ImageJ's signed 16 bit hacks. Will need to test Image<ShortType> and see if things work versus an ImagePlus.
//   Review imglib's various cursors and perhaps change which ones I'm using.
//   Nearly all methods below broken for ComplexType and and LongType
//   All methods below assume x and y first two dimensions and that the Image<T> consists of XY planes
//     In createImagePlus we rely on image to be 5d or less. We should modify ImageJ to have a setDimensions(int[] dimension) and integrate
//     its use throughout the application.
//   Rename ImgLibProcessor to GenericProcessor????
//   Rename TypeManager to TypeUtils
//   Rename Image<> to something else like Dataset<> or NumericDataset<>
//   Move some things to ImageUtils and/or their own classes: Operations, applyOperations()'s
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
	private static float divideByZeroValue;
	
	static {
		divideByZeroValue = (float)Prefs.getDouble(Prefs.DIV_BY_ZERO_VALUE, Float.POSITIVE_INFINITY);
		if (divideByZeroValue==Float.MAX_VALUE)
			divideByZeroValue = Float.POSITIVE_INFINITY;
	}
	

	private static enum PixelType {BYTE,SHORT,INT,FLOAT,DOUBLE,LONG};

	//****************** Instance variables *******************************************************
	
	private final Image<T> imageData;

	// TODO: How can we use generics here without breaking javac?
	private final RealType<?> type;
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
	
	public ImgLibProcessor(Image<T> img, T type, int[] thisPlanePosition)
	{
		final int[] dims = img.getDimensions();
		
		if (dims.length < 2)
			throw new IllegalArgumentException("Image must be at least 2-D");

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

	public ImgLibProcessor(Image<T> img, T type, long planeNumber)
	{
		this(img, type, ImageUtils.getPlanePosition(img.getDimensions(), planeNumber));
	}

	//****************** Operations methods *******************************************************

	/*  idea to abstract the visiting of the image via applying Operations.
	 *  To expand: flipVertical() challenges these designs.
	*/
	
	private abstract class SingleCursorRoiOperation
	{
		Image<T> image;
		int[] origin, span;
		
		protected SingleCursorRoiOperation(Image<T> image, int[] origin, int[] span)
		{
			this.image = image;
			this.origin = origin.clone();
			this.span = span.clone();
		}
		
		public Image<T> getImage() { return image; }
		public int[] getDimsOrigin() { return origin; }
		public int[] getDimsSpan() { return span; }

		public abstract void beforeIteration(RealType<?> type);
		public abstract void insideIteration(RealType<?> sample);
		public abstract void afterIteration();
	}
	
	private abstract class DualCursorRoiOperation
	{
		Image<T> img1, img2;
		int[] origin1, span1, origin2, span2;
		
		protected DualCursorRoiOperation(Image<T> img1, int[] origin1, int[] span1, Image<T> img2, int[] origin2, int[] span2)
		{
			this.img1 = img1;
			this.origin1 = origin1.clone();
			this.span1 = span1.clone();

			this.img2 = img2;
			this.origin2 = origin2.clone();
			this.span2 = span2.clone();
		}
		
		public Image<T> getImage1()   { return img1; }
		public int[] getDimsOrigin1() { return origin1; }
		public int[] getDimsSpan1()   { return span1; }
		
		public Image<T> getImage2()   { return img2; }
		public int[] getDimsOrigin2() { return origin2; }
		public int[] getDimsSpan2()   { return span2; }

		public abstract void beforeIteration(RealType<?> type1, RealType<?> type2);
		public abstract void insideIteration(RealType<?> sample1, RealType<?> sample2);
		public abstract void afterIteration();
	}
	
	private abstract class PositionalOperation
	{
		Image<T> inputImage;
		ImageProcessor outputProc;
		
		PositionalOperation(Image<T> image, ImageProcessor proc)
		{
			this.inputImage = image;
			this.outputProc = proc;
		}
		
		public Image<T> getImage() { return inputImage; }
		public ImageProcessor getProcessor() { return outputProc; }
		
		public abstract void beforeIteration(RealType<?> type);
		public abstract void insideIteration(int[] position, RealType<?> sample);
		public abstract void afterIteration();
	}
	
	private void applyOperation(SingleCursorRoiOperation op)
	{
		final LocalizableByDimCursor<T> imageCursor = op.getImage().createLocalizableByDimCursor();
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
	
	private void applyOperation(DualCursorRoiOperation op)
	{
		LocalizableByDimCursor<T> image1Cursor = op.getImage1().createLocalizableByDimCursor();
		LocalizableByDimCursor<T> image2Cursor = op.getImage2().createLocalizableByDimCursor();

		RegionOfInterestCursor<T> image1RoiCursor = new RegionOfInterestCursor<T>(image1Cursor, op.getDimsOrigin1(), op.getDimsSpan1());
		RegionOfInterestCursor<T> image2RoiCursor = new RegionOfInterestCursor<T>(image2Cursor, op.getDimsOrigin2(), op.getDimsSpan2());
		
		op.beforeIteration(image1Cursor.getType(),image2Cursor.getType());
		
		while (image1RoiCursor.hasNext() && image2RoiCursor.hasNext())
		{
			image1RoiCursor.fwd();
			image2RoiCursor.fwd();
			
			op.insideIteration(image1Cursor.getType(),image2Cursor.getType());
		}
		
		op.afterIteration();
		
		image1RoiCursor.close();
		image2RoiCursor.close();
		image1Cursor.close();
		image2Cursor.close();
	}
	
	private void applyOperation(PositionalOperation op)
	{
		LocalizableByDimCursor<T> cursor = op.getImage().createLocalizableByDimCursor();
		
		int[] position = Index.create(0, 0, getPlanePosition());
		
		op.beforeIteration(this.type);
		
		int height = getHeight();
		int width = getWidth();
		
		for (int y = 0; y < height; y++) {
			
			position[1] = y;
			
			for (int x = 0; x < width; x++) {
				
				position[0] = x;
				
				cursor.setPosition(position);
				
				op.insideIteration(position,cursor.getType());
			}
		}
		
		op.afterIteration();

		cursor.close();	
	}
	
	private class ApplyLutOperation extends SingleCursorRoiOperation
	{
		int[] lut;
		
		ApplyLutOperation(Image<T> image, int[] origin, int[] span, int[] lut)
		{
			super(image,origin,span);
		
			this.lut = lut;
		}
		
		@Override
		public void beforeIteration(RealType<?> type) {
		}

		@Override
		public void insideIteration(RealType<?> sample) {
			int value = this.lut[(int)sample.getRealDouble()];
			sample.setReal(value);
		}
		
		@Override
		public void afterIteration() {
		}

	}
	
	private class BlitterOperation extends DualCursorRoiOperation
	{
		private static final double TOL = 0.00000001;

		int mode;
		boolean isIntegral;
		double transparentValue;
		ImgLibProcessor<T> ip;
		
		double min, max;
		boolean useDBZValue = !Float.isInfinite(divideByZeroValue);
		
		long numPixels;
		long pixelsSoFar;
		long numPixelsInTwentyRows;
		
		BlitterOperation(ImgLibProcessor<T> ip, ImgLibProcessor<T> other, int xloc, int yloc, int mode, double tranVal)
		{
			super(other.imageData,
					Index.create(2),
					Span.singlePlane(other.getWidth(), other.getHeight(), 2),
					ip.imageData,
					Index.create(xloc, yloc, ip.getPlanePosition()),
					Span.singlePlane(other.getWidth(), other.getHeight(), ip.imageData.getNumDimensions()));
			this.ip = ip;
			this.mode = mode;
			this.transparentValue = tranVal;
			this.numPixels = other.getNumPixels();
			this.numPixelsInTwentyRows = (this.numPixels * 20) / other.getHeight();
		}
		
		@Override
		public void beforeIteration(RealType<?> type1, RealType<?> type2) {
			this.isIntegral = TypeManager.isIntegralType(type1);
			this.pixelsSoFar= 0;
			this.min = type1.getMinValue();
			this.max = type1.getMaxValue();
		}

		@Override
		public void insideIteration(RealType<?> sample1, RealType<?> sample2) {
			double value;
			switch (this.mode)
			{
				/** dst=src */
				case Blitter.COPY:
					sample2.setReal( sample1.getRealDouble() );
					break;
				
				/** dst=255-src (8-bits and RGB) */
				case Blitter.COPY_INVERTED:
					if (this.isIntegral)
						sample2.setReal( this.max - sample1.getRealDouble() );
					else
						sample2.setReal( sample1.getRealDouble() );
					break;
				
				/** Copies with white pixels transparent. */
				case Blitter.COPY_TRANSPARENT:
					if (this.isIntegral)
					{
						if ( Math.abs( sample1.getRealDouble() - this.transparentValue ) > TOL )
							sample2.setReal( sample1.getRealDouble() );
					}
					break;
				
				/** dst=dst+src */
				case Blitter.ADD:
					value = sample2.getRealDouble() + sample1.getRealDouble();
					if ((this.isIntegral) && (value > this.max))
						value = this.max;
					sample2.setReal( value );
					break;
				
				/** dst=dst-src */
				case Blitter.SUBTRACT:
					value = sample2.getRealDouble() - sample1.getRealDouble();
					if ((this.isIntegral) && (value < this.min))
						value = this.min;
					sample2.setReal( value );
					break;
					
				/** dst=src*src */
				case Blitter.MULTIPLY:
					value = sample2.getRealDouble() * sample1.getRealDouble();
					if ((this.isIntegral) && (value > this.max))
						value = this.max;
					sample2.setReal( value );
					break;
				
				/** dst=dst/src */
				case Blitter.DIVIDE:
					value = sample1.getRealDouble();
					if (value == 0)
					{
						if (this.isIntegral)
							value = this.max;
						else if (this.useDBZValue)
							value = divideByZeroValue;
						else
							value = sample2.getRealDouble() / value;
					}
					else
						value = sample2.getRealDouble() / value;
					if (this.isIntegral)
						value = Math.floor(value);
					sample2.setReal( value );
					break;
				
				/** dst=(dst+src)/2 */
				case Blitter.AVERAGE:
					if (this.isIntegral)
						sample2.setReal( ((int)sample2.getRealDouble() + (int)sample1.getRealDouble()) / 2 );
					else
						sample2.setReal( (sample2.getRealDouble() + sample1.getRealDouble()) / 2.0 );
					break;
				
				/** dst=abs(dst-src) */
				case Blitter.DIFFERENCE:
					sample2.setReal( Math.abs(sample2.getRealDouble() - sample1.getRealDouble()) );
					break;
				
				/** dst=dst AND src */
				case Blitter.AND:
					sample2.setReal( ((int)sample2.getRealDouble()) & ((int)(sample1.getRealDouble())) );
					break;
				
				/** dst=dst OR src */
				case Blitter.OR:
					sample2.setReal( ((int)sample2.getRealDouble()) | ((int)(sample1.getRealDouble())) );
					break;
				
				/** dst=dst XOR src */
				case Blitter.XOR:
					sample2.setReal( ((int)sample2.getRealDouble()) ^ ((int)(sample1.getRealDouble())) );
					break;
				
				/** dst=min(dst,src) */
				case Blitter.MIN:
					if (sample1.getRealDouble() < sample2.getRealDouble())
						sample2.setReal( sample1.getRealDouble() );
					break;
				
				/** dst=max(dst,src) */
				case Blitter.MAX:
					if (sample1.getRealDouble() > sample2.getRealDouble())
						sample2.setReal( sample1.getRealDouble() );
					break;
				
				/** Copies with zero pixels transparent. */
				case Blitter.COPY_ZERO_TRANSPARENT:
					if ( sample1.getRealDouble() != 0 )
						sample2.setReal( sample1.getRealDouble() );
					break;
					
				default:
					throw new IllegalArgumentException("GeneralBlitter::copyBits(): unknown blitter mode - "+this.mode);
			}
			
			this.pixelsSoFar++;

			if (( this.pixelsSoFar % this.numPixelsInTwentyRows) == 0) 
				showProgress( (double)this.pixelsSoFar / this.numPixels );
		}

		@Override
		public void afterIteration() {
			this.ip.showProgress(1.0);
		}

	}

	class DuplicateOperation extends PositionalOperation
	{
		DuplicateOperation(Image<T> image, ImageProcessor proc)
		{
			super(image,proc);
		}

		@Override
		public void beforeIteration(RealType<?> type) {
		}

		@Override
		public void insideIteration(int[] position, RealType<?> sample) {
			float floatVal = sample.getRealFloat();
			getProcessor().setf(position[0], position[1], floatVal);
		}

		@Override
		public void afterIteration() {
		}
	}
	
	private class HistogramOperation extends SingleCursorRoiOperation
	{
		ImageProcessor mask;
		int[] histogram;
		int pixIndex;
		
		HistogramOperation(Image<T> image, int[] origin, int[] span, ImageProcessor mask, int lutSize)
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
		public void beforeIteration(RealType<?> type) {
			this.pixIndex = 0;
		}

		@Override
		public void insideIteration(RealType<?> sample) {
			if ((this.mask == null) || (this.mask.get(pixIndex) > 0))
				this.histogram[(int)sample.getRealDouble()]++;
			pixIndex++;
		}
		
		@Override
		public void afterIteration() {
		}

	}

	class SetToFloatOperation extends PositionalOperation
	{
		SetToFloatOperation(Image<T> image, ImageProcessor proc)
		{
			super(image,proc);
		}

		@Override
		public void beforeIteration(RealType<?> type) {
		}

		@Override
		public void insideIteration(int[] position, RealType<?> sample) {
			getProcessor().setf(position[0], position[1], sample.getRealFloat());
		}

		@Override
		public void afterIteration() {
		}
	}
	

	private class MinMaxOperation extends SingleCursorRoiOperation
	{
		double min, max;
		
		MinMaxOperation(Image<T> image, int[] origin, int[] span)
		{
			super(image,origin,span);
		}
		
		public double getMax() { return this.max; }
		public double getMin() { return this.min; }
		
		@Override
		public void beforeIteration(RealType<?> type)
		{
			this.min = type.getMaxValue();
			this.max = type.getMinValue();
		}
		
		@Override
		public void insideIteration(RealType<?> sample)
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

	private class ResetUsingMaskOperation extends DualCursorRoiOperation
	{
		byte[] maskPixels;
		int pixNum;
		
		ResetUsingMaskOperation(Image<T> img1, int[] origin1, int[] span1, Image<T> img2, int[] origin2, int[] span2, ImageProcessor mask)
		{
			super(img1,origin1,span1,img2,origin2,span2);
			
			this.maskPixels = (byte[])mask.getPixels();
		}
		
		@Override
		public void beforeIteration(RealType<?> type1, RealType<?> type2) {
			pixNum = 0;
		}

		@Override
		public void insideIteration(RealType<?> sample1, RealType<?> sample2) {
			if (maskPixels[pixNum++] == 0)
			{
				double pix = sample1.getRealDouble();
				sample2.setReal(pix);
			}
		}
		
		@Override
		public void afterIteration() {
		}

	}
	
	private class ThresholdOperation extends SingleCursorRoiOperation
	{
		double threshold, min, max;
		
		ThresholdOperation(Image<T> image, int[] origin, int[] span, double threshold)
		{
			super(image,origin,span);
			
			this.threshold = threshold;
		}
		
		@Override
		public void beforeIteration(RealType<?> type)
		{
			this.threshold = TypeManager.boundValueToType(type, this.threshold);
			this.min = type.getMinValue();
			this.max = type.getMaxValue();
		}
		
		@Override
		public void insideIteration(RealType<?> sample)
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
	
	private class GenericBlitter  // TODO - purposely not extending Blitter as it uses ImageProcessors rather than ImgLibProcessors: rethink?
	{
		ImgLibProcessor<T> ip;
		double transparentColor;
		
		GenericBlitter(ImgLibProcessor<T> ip)
		{
			this.ip = ip;
			if (TypeManager.isIntegralType(this.ip.getType()))
				this.transparentColor = this.ip.getMaxAllowedValue();
		}
		
		public void copyBits(ImgLibProcessor<T> other, int xloc, int yloc, int mode)
		{
			BlitterOperation blitOp = new BlitterOperation(this.ip, other, xloc, yloc, mode, this.transparentColor);
			
			//if (mode == Blitter.DIVIDE)
			//	System.out.println("Here is my breakpoint anchor");
				
			this.ip.applyOperation(blitOp);
		}
		
		public void setTransparentColor(Color color)
		{
			if (TypeManager.isIntegralType(this.ip.getType()))
				this.transparentColor = this.ip.getBestIndex(color);
		}
		
	}
	
	private ImgLibProcessor<T> makeImageOfMyType(ImageProcessor ip)
	{
		// if ip's type matches me
		//   just return ip
		if (ip instanceof ImgLibProcessor<?>)
		{
			// TODO
		}
		
		// otherwise
		//   create a processor of my type with size matching ip's dimensions
		//   populate the pixels
		//   return it

		Image<T> image = imageData.createNewImage(new int[]{ ip.getWidth(), ip.getHeight() } );
		
		ImgLibProcessor<T> newProc = new ImgLibProcessor<T>(image, (T)type, 0);  // cast required!
		
		newProc.setPixels(ip.getPixels());
		
		return newProc;
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
		int[] imageDimensionsOffset = Index.create(0, 0, getPlanePosition());
		int[] imageDimensionsSize = Span.singlePlane(getWidth(), getHeight(), this.imageData.getNumDimensions());

		MinMaxOperation mmOp = new MinMaxOperation(this.imageData,imageDimensionsOffset,imageDimensionsSize);
		
		applyOperation(mmOp);
		
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

	/*
	 * since setPlane relies on position array being passed in (since used to set pixel data and snapshot data) I will need to rework
	 * the foundations of positional operation before I can use this code. Also applyOperation() always refers to this.imageData. But
	 * could make it apply to Image<T> and put in ImageUtils (yes, do this).

	private class SetPlaneOperation extends PositionalOperation
	{
		// set in constructor
		int[] position;
		Object pixels;
		PixelType pixType;
		
		// set before iteration
		int pixNum;
		RealType type;
		boolean isIntegral;
		
		SetPlaneOperation(Image<?> theImage, int[] position, Object pixels, PixelType inputType)
		{
			super(theImage,null);
			this.position = position;
			this.pixels = pixels;
			this.pixType = inputType;
		}
		
		@Override
		public void beforeIteration(RealType type) {
			this.pixNum = 0;
			this.type = type;
			this.isIntegral = TypeManager.isIntegralType(type);
		}

		@Override
		public void insideIteration(int[] position, RealType sample) {

			double inputPixValue = getPixValue(pixels, pixType, isUnsigned, this.pixNum++);
			
			if (this.isIntegral)
				inputPixValue = TypeManager.boundValueToType(this.type, inputPixValue);
			
			sample.setReal(inputPixValue);
		}

		@Override
		public void afterIteration() {
		}
	}

	private void setPlane(Image<T> theImage, int[] position, Object pixels, PixelType inputType, long numPixels)
	{
		if (numPixels != getNumPixels(theImage))
			throw new IllegalArgumentException("setPlane() error: input image does not have same dimensions as passed in pixels");

		boolean isUnsigned = TypeManager.isUnsignedType(this.type);
		
		SetPlaneOperation setOp = new SetPlaneOperation(withCorrectParams);  // somehow include position array as it stores planePosition
		
		applyOperation(correctImage);  // this shows its broken since we have Image<T> and not ImgLibProcessors
	}
	
	*/
	
	private void setPlane(Image<T> theImage, int[] position, Object pixels, PixelType inputType, long numPixels)
	{
		if (numPixels != getNumPixels(theImage))
			throw new IllegalArgumentException("setPlane() error: input image does not have same dimensions as passed in pixels");
		
		boolean isUnsigned = TypeManager.isUnsignedType(this.type);
		
		LocalizableByDimCursor<T> cursor = theImage.createLocalizableByDimCursor();  // cannot use cached cursor here
		
		int pixNum = 0;
		
		int height = getHeight();
		int width = getWidth();
		
		for (int y = 0; y < height; y++) {
			
			position[1] = y;
			
			for (int x = 0; x < width; x++) {
				
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

		ApplyLutOperation lutOp = new ApplyLutOperation(this.imageData,index,span,lut);
		
		applyOperation(lutOp);
	}

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
		// TODO - a possibly very expensive operation
		ImgLibProcessor<T> otherProc = makeImageOfMyType(ip);
		
		new GenericBlitter(this).copyBits(otherProc, xloc, yloc, mode);
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
		
		ImageUtils.copyFromImageToImage(this.imageData, newImage, imageOrigin, newImageOrigin, imageSpan);
		
		return new ImgLibProcessor<T>(newImage, (T)this.type, 0);  // cast required!
	}
	
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
		ImageProcessor proc = createProcessor(getWidth(), getHeight());
		
		DuplicateOperation duplicator = new DuplicateOperation(this.imageData, proc);
		
		applyOperation(duplicator);
		
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
	
			HistogramOperation histOp = new HistogramOperation(this.imageData,origin,span,getMask(),lutSize);
			
			applyOperation(histOp);
			
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

		
		ResetUsingMaskOperation resetOp = new ResetUsingMaskOperation(snapData,snapOrigin,snapSpan,this.imageData,imageOrigin,imageSpan,mask);
		
		applyOperation(resetOp);
	}
	
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
		int[] position = Index.create(0, 0, getPlanePosition());
		
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
		
		ThresholdOperation threshOp = new ThresholdOperation(this.imageData,origin,span,thresholdLevel);
		
		applyOperation(threshOp);
	}
	
	@Override
	public FloatProcessor toFloat(int channelNumber, FloatProcessor fp)
	{
		int width = getWidth();
		int height = getHeight();
		
		long size = getNumPixels(this.imageData);
		
		if (fp == null || fp.getWidth()!=width || fp.getHeight()!=height)
			fp = new FloatProcessor(width, height, new float[(int)size], super.cm);
		
		SetToFloatOperation floatOp = new SetToFloatOperation(this.imageData, fp);
		
		applyOperation(floatOp);
		
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
