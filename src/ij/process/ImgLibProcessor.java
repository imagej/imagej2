package ij.process;

import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;
import java.io.File;

import javax.swing.JFileChooser;

import loci.common.DataTools;
import mpicbg.imglib.container.ContainerFactory;
import mpicbg.imglib.container.array.ArrayContainerFactory;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.cursor.special.RegionOfInterestCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.io.LOCI;
import mpicbg.imglib.type.Type;
import mpicbg.imglib.type.numeric.ComplexType;
import mpicbg.imglib.type.numeric.IntegerType;
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

// BDZ TODO / NOTES
//   We should break out the static classes here as public ones and write tests for them
//   Make sure that resetMinAndMax() and/or findMinAndMax() are called at appropriate times. Maybe base class does this for us?
//   Nearly all methods below broken for ComplexType and and LongType 
//   Improvements to ImgLib
//     Rename LocalizableByDimCursor to PositionCursor. Be able to say posCursor.setPosition(int[] pos) and posCursor.setPosition(long sampIndex).
//       Another possibility: just call it a Cursor. And then cursor.get() or cursor.get(int[] pos) or cursor.get(long sampleNumber) 
//     Create ROICursors directly from an Image<T> and have that ctor setup its own LocalizableByDimCursor for you.
//     Allow new Image<ByteType>(rows,cols). Have a default factory and a default container and have other constructors that you use in the cases
//       where you want to specify them. Also allow new Image<? extends RealType>(rows,cols,pixels).
//     Have a few static ContainerFactories that we can just refer to rather than newing them all the time. Maybe do so also for Types so that
//       we're not always having to pass a new UnsignedByteType() but rather a static one and if a new one needed the ctor can create.
//     In general come up with much shorter names to make use less cumbersome.
//     Put in our code somewhere for totalSamples(), totalPlanes(), the getPlane...() routines, the SnapShot copytoimagefromimage stuff, and the
//       setpixels kinds of code. Also the index and span stuff.

public class ImgLibProcessor<T extends RealType<T>> extends ImageProcessor implements java.lang.Cloneable {

	private static enum PixelType {BYTE,SHORT,INT,FLOAT,DOUBLE,LONG};
	

	//****************** Instance variables *******************************************************
	
	private final Image<T> imageData;

	// TODO: How can we use generics here without breaking javac?
	@SuppressWarnings("rawtypes")
	private final RealType type;

	private byte[] pixels8;
	private Snapshot<T> snapshot;
	private ImageProperties<T> imageProperties;
	// TODO - move these next two to imageProperties
	private double min, max;
	private double fillColor;

	//****************** Constructors *************************************************************
	
	public ImgLibProcessor(Image<T> img, T type, long planeNumber ) {

		final int[] dims = img.getDimensions();
		
		if (dims.length < 2)
			throw new IllegalArgumentException("Image must be at least 2-D");

		int[] thisPlanePosition = ImageUtils.getPlanePosition(dims,planeNumber);
		
		this.imageData = img;
		this.type = type;
		
		//assign the properties object for the image
		this.imageProperties = new ImageProperties< T >( );
		
		this.imageProperties.setPlanePosition( thisPlanePosition );

		super.width = dims[0]; // TODO: Dimensional labels are safer way to find X
		super.height = dims[1]; // TODO: Dimensional labels are safer way to find Y
	
		this.fillColor = 0;
		
		if (this.type instanceof UnsignedByteType)
			this.imageProperties.setBackgroundValue(255);

		resetRoi();
		
		findMinAndMax();
	}

	//****************** Helper methods *******************************************************

	private void findMinAndMax()
	{
		// TODO - should do something different for UnsignedByte (involving LUT) if we mirror ByteProcessor

		//get the current image data
		int[] imageDimensionsOffset = Index.create(0,0,this.imageProperties.getPlanePosition());
		int[] imageDimensionsSize = Span.singlePlane(super.width,super.height,this.imageData.getNumDimensions());
		
		//Get a cursor
		final LocalizableByDimCursor<T> imageCursor = this.imageData.createLocalizableByDimCursor( );
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
		imageCursor.close( );
	}
	
	/*
	 * Throws an exception if the LUT length is wrong for the pixel layout type
	 */
	private void testLUTLength( int[] lut )
	{
		if ( this.type instanceof GenericByteType< ? > )
		{
			if (lut.length!=256)
				throw new IllegalArgumentException("lut.length != expected length for type " + type );
		} else if( this.type instanceof GenericShortType< ? > )
		{
			if (lut.length!=65536)
				throw new IllegalArgumentException("lut.length != expected length for type " + type );
		} else {
			throw new IllegalArgumentException("LUT NA for type " + type ); 
		}
	}
	
	private int[] getMultiDimensionalPositionArray( int x, int y )
	{
		return Index.create(x,y,this.imageProperties.getPlanePosition());
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
	
	private void setSnapshotPlane(Object pixels, PixelType inputType, long numPixels)
	{
		// must create snapshot data structures if they don't exist. we'll overwrite data
		if (this.snapshot == null)
			snapshot();
		
		Image<T> snapStorage = this.snapshot.getStorage();

		// data.getNumPixels() returns an int!!! calc our own for now
		long totalSamples = ImageUtils.getTotalSamples(snapStorage.getDimensions());
		
		if (numPixels != totalSamples)
			throw new IllegalArgumentException("snapshot size does not match number of pixels passed in");
		
		int[] position = Index.create(snapStorage.getDimensions().length);
		
		final LocalizableByDimCursor<T> snapCursor = snapStorage.createLocalizableByDimCursor( );
		
		boolean destImageIsUnsigned = TypeManager.isUnsignedType(snapCursor.getType());

		int pixIndex = 0;
		for (int y = 0; y < super.height; y++) {
			position[1] = y; 
			for (int x = 0; x < super.width; x++) {
				double newValue = getPixValue(pixels,inputType,destImageIsUnsigned,pixIndex);
				position[0] = x;
				snapCursor.setPosition(position);
				snapCursor.getType().setReal(newValue);
				pixIndex++;
			}
		}
		
		//close the cursors
		snapCursor.close();
	}

	private Object getPixelsArray() {
		return getCopyOfPixelsFromImage(this.imageData, this.type, this.imageProperties.getPlanePosition());
	}

	//****************** public methods *******************************************************

	/* Unnecessary?????
	protected ImageProperties<T> getImageProperties() {
		return imageProperties;
	}
	*/
	
	@Override
	public void applyTable(int[] lut) 
	{
		testLUTLength(lut);
		
		int[] index = Index.create(super.roiX,super.roiY,this.imageProperties.getPlanePosition());
		int[] span = Span.singlePlane(super.roiWidth, super.roiHeight, this.imageData.getNumDimensions());
		
		//Fill the image with data - first get a cursor
		final LocalizableByDimCursor<T> imageCursor = this.imageData.createLocalizableByDimCursor( );
		RegionOfInterestCursor<T> imageRegionOfInterestCursor = new RegionOfInterestCursor< T >( imageCursor, index, span );
		
		for (final T pixel:imageRegionOfInterestCursor)
		{
			pixel.setReal( lut[ (int) pixel.getRealDouble() ] );
		}
		
		//close the cursors
		imageRegionOfInterestCursor.close( );
		imageCursor.close( );
	}

	@Override
	public void convolve(float[] kernel, int kernelWidth, int kernelHeight) {
		throw new RuntimeException("Unimplemented");

	}

	@Override
	public void convolve3x3(int[] kernel) {
		throw new RuntimeException("Unimplemented");

	}

	@Override
	public void copyBits(ImageProcessor ip, int xloc, int yloc, int mode) {
		throw new RuntimeException("Unimplemented");

	}

	//TODO ask about changing name of Image to avoid conflict with java.awt
	@Override
	public java.awt.Image createImage() {
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
	public ImageProcessor createProcessor(int width, int height) {
		Image<T> image = this.imageData.createNewImage(new int[]{width,height});
		ImageProcessor ip2 = new ImgLibProcessor<T>(image, (T)this.type, 0);
		ip2.setColorModel(getColorModel());
		// TODO - ByteProcessor does this conditionally. Do we mirror here?
		ip2.setMinAndMax(getMin(), getMax());
		ip2.setInterpolationMethod(super.interpolationMethod);
		return ip2;
	}

	@Override
	public ImageProcessor crop() {
		
		int[] originInImage = Index.create(super.roiX,super.roiY,this.imageProperties.getPlanePosition());
		int[] extentsInImage = Span.singlePlane(super.roiWidth, super.roiHeight, this.imageData.getNumDimensions());
		
		// TODO - fine as is? pass all dims with some extent of 1? or even crop all planes of Image?
		int[] originInNewImage = Index.create(2);
		int[] extentsInNewImage = Span.singlePlane(super.roiWidth, super.roiHeight, 2);

		Image<T> newImage = this.imageData.createNewImage(extentsInNewImage);
		
		LocalizableByDimCursor<T> imageDimCursor = this.imageData.createLocalizableByDimCursor();
		LocalizableByDimCursor<T> newImageDimCursor = newImage.createLocalizableByDimCursor();
		
		RegionOfInterestCursor<T> imageRoiCursor = new RegionOfInterestCursor<T>(imageDimCursor,originInImage,extentsInImage);
		RegionOfInterestCursor<T> newImageRoiCursor = new RegionOfInterestCursor<T>(newImageDimCursor,originInNewImage,extentsInNewImage);
		
		while (imageRoiCursor.hasNext() && newImageRoiCursor.hasNext())
		{
			imageRoiCursor.fwd();
			newImageRoiCursor.fwd();
			double value = imageRoiCursor.getType().getRealDouble(); 
			newImageRoiCursor.getType().setReal(value); 
		}
		
		imageRoiCursor.close();
		newImageRoiCursor.close();
		imageDimCursor.close();
		newImageDimCursor.close();

		return new ImgLibProcessor<T>(newImage, (T)this.type, 0);
	}

	@Override
	public void dilate() {
		// only applicable to integral types (or is it just for lut backed types?)
		if (TypeManager.isIntegralType(this.type))
		{
			// TODO
		}
		throw new RuntimeException("Unimplemented");
	}

	@Override
	public void drawPixel(int x, int y) {
		throw new RuntimeException("Unimplemented");

	}

	@Override
	public ImageProcessor duplicate() {
		ImageProcessor proc = createProcessor(super.width, super.height);

		LocalizableByDimCursor<T> cursor = this.imageData.createLocalizableByDimCursor();
		
		int[] position = Index.create(0, 0, this.imageProperties.getPlanePosition());
		
		for (int x = 0; x < super.width; x++) {
			position[0] = x;
			for (int y = 0; y < super.height; y++) {
				position[1] = y;
				cursor.setPosition(position);
				float floatVal = cursor.getType().getRealFloat();
				proc.setf(x,y,floatVal);
			}
		}
		
		return proc;
	}

	@Override
	public void erode() {
		// only applicable to integral types (or is it just for lut backed types?)
		if (TypeManager.isIntegralType(this.type))
		{
			// TODO
		}
		throw new RuntimeException("Unimplemented");
	}

	@Override
	public void fill(ImageProcessor mask) {
		throw new RuntimeException("Unimplemented");

	}

	@Override
	public void filter(int type) {
		throw new RuntimeException("Unimplemented");
		// TODO - make special calls to erode() and dilate() I think when ByteType. Copy ByteProcessor. See other processors too.
	}

	/** swap the rows of an image about its central row */
	@Override
	public void flipVertical()
	{
		// create suitable cursor
		final LocalizableByDimCursor<T> cursor1 = this.imageData.createLocalizableByDimCursor( );
		final LocalizableByDimCursor<T> cursor2 = this.imageData.createLocalizableByDimCursor( );
		
		// allocate arrays that will hold position variables
		final int[] position1 = Index.create(0,0,this.imageProperties.getPlanePosition());
		final int[] position2 = Index.create(0,0,this.imageProperties.getPlanePosition());
		
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
			
			// for each col in this row
			for (int x=minX; x<=maxX; x++) {
				
				// setup position index for cursor 1
				position1[0] = x;
				position1[1] = y1;

				// setup position index for cursor 2
				position2[0] = x;
				position2[1] = y2;

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
		
		// close the cursors when done with them
		cursor1.close();
		cursor2.close();
	}

	@Override
	public int get(int x, int y) 
	{	
		int value;
		
		final LocalizableByDimCursor<T> cursor = this.imageData.createLocalizableByDimCursor();
		cursor.setPosition( getMultiDimensionalPositionArray( x, y ) );
		
		value = (int)( cursor.getType().getRealDouble() );
		
		cursor.close( );
		
		return value;
	}

	@Override
	public int get(int index) {
		//imageData
		int x = index % super.width;
		int y = index / super.width;
		return get( x, y) ;
	}

	@Override
	public double getBackgroundValue() {
		return this.imageProperties.getBackgroundValue();
	}

	@Override
	public int[] getHistogram() {
		/* TODO - work from here
		if ((type instanceof UnsignedByteType) || (type instanceof UnsignedShortType))
		{
			if (super.mask != null)
				return maskedHistogram();
			else
				return regularHistogram();
		}
		*/
		throw new RuntimeException("Unimplemented");
	}

	@Override
	public double getInterpolatedPixel(double x, double y) {
		throw new RuntimeException("Unimplemented");
	}

	@Override
	public double getMax() 
	{
		return this.max;
	}

	@Override
	public double getMin() 
	{
		return this.min;
	}

	@Override
	public int getPixel(int x, int y) {
		return get(x,y);
	}

	@Override
	public int getPixelInterpolated(double x, double y) {
		throw new RuntimeException("Unimplemented");
	}

	@Override
	public float getPixelValue(int x, int y) {
		return getf(x,y);
	}

	@Override
	public Object getPixels() {
		// TODO: could add a special case for single-image 8-bit array-backed data
		// TODO: special case for new container
		return getPixelsArray();
	}

	@Override
	public Object getPixelsCopy() {
		
		if (this.snapshot!=null && super.snapshotCopyMode)
		{
			super.snapshotCopyMode = false;
			Image<T> snapStorage = this.snapshot.getStorage();
			int[] planePosOfZero = Index.create(imageProperties.getPlanePosition().length);
			return getCopyOfPixelsFromImage(snapStorage,this.type,planePosOfZero); 
		}
		else
		{
			return getPixelsArray();
		}
	}

	@Override
	public Object getSnapshotPixels() {
		
		if (this.snapshot == null)
			return null;
		
		Image<T> snapStorage = this.snapshot.getStorage();
		
		int[] planePosOfZero = Index.create(this.imageProperties.getPlanePosition().length);

		return getCopyOfPixelsFromImage(snapStorage, this.type, planePosOfZero);
	}

	@Override
	public float getf(int x, int y) 
	{
		float value;
		
		final LocalizableByDimCursor<T> cursor = this.imageData.createLocalizableByDimCursor();
				
		cursor.setPosition( getMultiDimensionalPositionArray(x, y) );
		
		value =  ( float ) cursor.getType().getRealDouble();
		
		cursor.close( );
		
		return value;
	}

	@Override
	public float getf(int index) {
		int x = index % super.width;
		int y = index / super.width;
		return getf( x, y) ;
	}

	@Override
	public void medianFilter() {
		throw new RuntimeException("Unimplemented");
	}

	@Override
	public void noise(double range) {
		throw new RuntimeException("Unimplemented");

	}

	@Override
	public void putPixel(int x, int y, int value) {
		throw new RuntimeException("Unimplemented");

	}

	@Override
	public void putPixelValue(int x, int y, double value) {
		throw new RuntimeException("Unimplemented");

	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void reset() {
		
		if (this.snapshot!=null)
		{
			this.snapshot.pasteIntoImage(this.imageData);
			findMinAndMax();
		}
		// TODO - ShortProcessor kept track of max and min here. Might need to do so also. But imglib or Rick may do too.
	}

	@Override
	public void reset(ImageProcessor mask) {
		
		if (mask==null || this.snapshot==null)
			return;
		
		if (mask.getWidth()!=super.roiWidth||mask.getHeight()!=super.roiHeight)
			throw new IllegalArgumentException(maskSizeError(mask));

		Image<T> snapData = this.snapshot.getStorage();
		
		LocalizableByDimCursor<T> imageCursor = this.imageData.createLocalizableByDimCursor();
		LocalizableByDimCursor<T> snapshotCursor = snapData.createLocalizableByDimCursor();

		int[] originInImage = Index.create(super.roiX,super.roiY,this.imageProperties.getPlanePosition());
		int[] originInSnapshot = Index.create(snapData.getNumDimensions());
		originInSnapshot[0] = super.roiX;
		originInSnapshot[1] = super.roiY;

		int[] spanInImage = Span.singlePlane(super.roiWidth,super.roiHeight,this.imageData.getNumDimensions());
		int[] spanInSnapshot = Span.singlePlane(super.roiWidth,super.roiHeight,snapData.getNumDimensions());
		
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
		imageCursor.close();
		
		findMinAndMax();
	}

	@Override
	public ImageProcessor resize(int dstWidth, int dstHeight) {
		throw new RuntimeException("Unimplemented");
	}

	@Override
	public void rotate(double angle) {
		throw new RuntimeException("Unimplemented");

	}

	@Override
	public void scale(double xScale, double yScale) {
		throw new RuntimeException("Unimplemented");

	}

	@Override
	public void set(int x, int y, int value) 
	{
		final LocalizableByDimCursor<T> cursor = this.imageData.createLocalizableByDimCursor();
		
		cursor.setPosition( getMultiDimensionalPositionArray( x, y ) );
		cursor.getType().setReal( value );
		
		cursor.close();
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
	public void setColor(Color color) {
		throw new RuntimeException("Unimplemented");

	}

	@Override
	public void setMinAndMax(double min, double max) {
	
		if (min==0.0 && max==0.0)
		{
			resetMinAndMax();
			return;
		}
	
		this.min = min;
		this.max = max;
		
		// From FLoatProc - huh? fixedScale = true;
		
		resetThreshold();
	}

	@Override
	public void resetMinAndMax() {
		
		// from FloatProc : fixedScale = false;
		
		findMinAndMax();
		
		resetThreshold();
	}

	@Override
	public void setPixels(Object pixels) {
		throw new RuntimeException("Unimplemented");

	}

	@Override
	public void setPixels(int channelNumber, FloatProcessor fp) {
		throw new RuntimeException("Unimplemented");

	}

	@Override
	public void setSnapshotPixels(Object pixels)
	{
		if (pixels instanceof byte[])
			setSnapshotPlane(pixels,PixelType.BYTE,((byte[])pixels).length);
		else if (pixels instanceof short[])
			setSnapshotPlane(pixels,PixelType.SHORT,((short[])pixels).length);
		else if (pixels instanceof int[])
			setSnapshotPlane(pixels,PixelType.INT,((int[])pixels).length);
		else if (pixels instanceof float[])
			setSnapshotPlane(pixels,PixelType.FLOAT,((float[])pixels).length);
		else if (pixels instanceof double[])
			setSnapshotPlane(pixels,PixelType.DOUBLE,((double[])pixels).length);
		else if (pixels instanceof long[])
			setSnapshotPlane(pixels,PixelType.LONG,((double[])pixels).length);
		else
			throw new IllegalArgumentException("unknow object passed to ImgLibProcessor::setSnapshotPixels() - "+ pixels.getClass());
	}

	@Override
	public void setValue(double value) 
	{
		this.fillColor = value;
		if (TypeManager.isIntegralType(this.type))
		{
			super.fgColor = TypeManager.boundIntValueToType(this.type,(int)this.fillColor);
		}
	}

	@Override
	public void setf(int x, int y, float value) {

		// TODO - verify this is what we want to do
		// NOTE - for an integer type backed data store imglib rounds float values. imagej has always truncated float values.
		//   I need to detect beforehand and do my truncation if an integer type.
		
		final LocalizableByDimCursor<T> cursor = this.imageData.createLocalizableByDimCursor();

		cursor.setPosition( getMultiDimensionalPositionArray( x, y ) );
		
		RealType pixRef = cursor.getType();

		if (TypeManager.isIntegralType(pixRef))
			value = (float)Math.floor(value);
		
		pixRef.setReal( value ); 

		cursor.close();
	}

	@Override
	public void setf(int index, float value) {
		int x = index % super.width;
		int y = index / super.width;
		setf( x, y, value);
	}

	@Override
	public void snapshot() 
	{
		int[] origins = Index.create(0,0,this.imageProperties.getPlanePosition());

		int[] spans = Span.singlePlane(super.width, super.height, this.imageData.getNumDimensions());
		
		this.snapshot = new Snapshot<T>(this.imageData, origins, spans);
		
		// TODO - ShortProcessor kept track of max and min here. Might need to do so also. But imglib or Rick may do too.
	}

	@Override
	public void threshold(int thresholdLevel) 
	{
		if (!TypeManager.isIntegralType(this.type))
			return;
		
		//ensure level is OK for underlying type & convert to double
		double thresholdLevelAsDouble = TypeManager.boundIntValueToType(this.type,thresholdLevel);
		
		//Get a cursor
		int[] origin = Index.create(0,0,this.imageProperties.getPlanePosition());
		int[] span = Span.singlePlane(super.width, super.height, this.imageData.getNumDimensions());
		
		// must use a RoiCursor to stay on our single plane
		final LocalizableByDimCursor<T> imageCursor = this.imageData.createLocalizableByDimCursor( );
		final RegionOfInterestCursor<T> roiCursor = new RegionOfInterestCursor<T>(imageCursor,origin,span);

		double minPossible = roiCursor.getType().getMinValue();
		double maxPossible = roiCursor.getType().getMaxValue();
		
		for (final T pixel : roiCursor)
		{
			pixel.setReal( pixel.getRealDouble() <= thresholdLevelAsDouble ? minPossible : maxPossible );
		}
		
		//close the cursors
		roiCursor.close();
		imageCursor.close();
	}

	@Override
	public FloatProcessor toFloat(int channelNumber, FloatProcessor fp) {
		
		int size = super.width * super.height;
		
		if (fp == null || fp.getWidth()!=super.width || fp.getHeight()!=super.height)
			fp = new FloatProcessor(super.width, super.height, new float[size], super.cm);
		
		float[] fPixels = (float[])fp.getPixels();

		LocalizableByDimCursor<T> cursor = this.imageData.createLocalizableByDimCursor();
		
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

	public double[] getPlaneData()
	{
		return ImageUtils.getPlaneData(this.imageData, super.width, super.height, this.imageProperties.getPlanePosition());
	}
	
	// TODO - belongs elsewhere???

	public static ImagePlus createImagePlus(final Image<?> img)
	{
		Type runtimeT = img.createCursor().getType();
		
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
		
		ImagePlus imp = new ImagePlus(img.getName(),stack);
		
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
		
		// TODO - prev calc only works for 5-d images and less with default ordering
		
		return imp;
	}
	
	public static void main(String[] args) {
		final JFileChooser chooser = new JFileChooser();
		int rval = chooser.showOpenDialog(null);
		if (rval != JFileChooser.APPROVE_OPTION) return;
		final File file = chooser.getSelectedFile();
		final String fileName = file.getAbsolutePath();
		final ContainerFactory containerFactory = new ArrayContainerFactory();
		//Image<?> image = LOCI.openLOCI( fileName, containerFactory );
		final Image<UnsignedByteType> image = LOCI.openLOCIUnsignedByteType(fileName, containerFactory);
		
		// make our image plus from inglib image
		final ImagePlus imp = createImagePlus(image);
		
		// methods to test to make sure they work

		/*
		// invert() : works
		
		imp.getProcessor().invert();
		*/
		
		// applyTable() : works
		//
		int[] lut = new int[256];
		for (int i = 0; i < 256; i++)
		lut[i] = 255-i;
		imp.getProcessor().applyTable(lut);
		
		/*
		// get(x,y)
		imp.getProcessor().set( 20, 20,175 );
		System.out.println( imp.getProcessor().get(20,20) );
		*/
		
		/*
		//Start the timer
		long a = System.currentTimeMillis( );
		for(int x = 0;x<1000;x++)
			imp.getProcessor().flipVertical();
		
		//stop the timer
		long b = System.currentTimeMillis( );
		
		System.out.println("Took imglib " + (b-a) + " milliseconds or " + ((b-a)/1000) + " seconds to do an image flip.");
		*/

		new ImageJ();
		imp.show();
	}
}
