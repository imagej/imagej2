package imagej.process;

import java.io.File;

import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileInfo;
import ij.process.ImageProcessor;
import imagej.SampleInfo.ValueType;
import imagej.io.ImageOpener;
import imagej.process.function.CopyUnaryFunction;
import imagej.process.operation.BinaryAssignOperation;
import imagej.process.operation.GetPlaneOperation;
import loci.formats.FormatTools;
import mpicbg.imglib.container.Container;
import mpicbg.imglib.container.ContainerFactory;
import mpicbg.imglib.container.array.Array;
import mpicbg.imglib.container.array.ArrayContainerFactory;
import mpicbg.imglib.container.basictypecontainer.DataAccess;
import mpicbg.imglib.container.basictypecontainer.array.PlanarAccess;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImageFactory;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.ByteType;
import mpicbg.imglib.type.numeric.integer.IntType;
import mpicbg.imglib.type.numeric.integer.LongType;
import mpicbg.imglib.type.numeric.integer.ShortType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import mpicbg.imglib.type.numeric.integer.UnsignedIntType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;
import mpicbg.imglib.type.numeric.real.DoubleType;
import mpicbg.imglib.type.numeric.real.FloatType;

// TODO
//   createImagePlus() calls imp.setDimensions(z,c,t). But we may have other dims too. Change when we can call ImagePlus::setDimensions(int[] dims)
//   Split this class into a separate project, imglib-utils, to avoid ij dependencies with other project (e.g., bf-imglib).

public class ImageUtils
{
	
	// ***************** public methods  **************************************************
	
	public static int[] getDimsBeyondXY(int[] fullDims)
	{
		if (fullDims.length < 2)
			throw new IllegalArgumentException("Image must be at least 2-D");
		
		int[] extraDims = new int[fullDims.length-2];
		
		for (int i = 0; i < extraDims.length; i++)
			extraDims[i] = fullDims[i+2];
		
		return extraDims;
	}
	
	public static long getTotalSamples(int[] dimensions)
	{
		int numDims = dimensions.length;
		
		if (numDims == 0)
			return 0;
		
		long totalSamples = 1;
		
		for (int i = 0; i < numDims; i++)
			totalSamples *= dimensions[i];
		
		return totalSamples;
	}
	
	public static long getTotalPlanes(int[] dimensions)
	{
		int numDims = dimensions.length;
		
		if (numDims < 2)
			return 0;
	
		if (numDims == 2)
			return 1;
		
		// else numDims > 2
		
		int[] sampleSpace = getDimsBeyondXY(dimensions);
		
		return getTotalSamples(sampleSpace);
	}

	public static long getTotalSamples(Image<?> image)
	{
		return getTotalSamples(image.getDimensions());
	}
	
	public static RealType<?> getType(Image<?> image)
	{
		Cursor<?> cursor = image.createCursor();
		RealType<?> type = (RealType<?>) cursor.getType();
		cursor.close();
		return type;
	}
	
	@SuppressWarnings({"rawtypes"})
	public static double[] getPlaneData(Image<? extends RealType> image, int w, int h, int[] planePos) {
		  // TODO - use LocalizablePlaneCursor
			// example in ImageJVirtualStack.extractSliceFloat
			final double[] data = new double[w * h];
			final LocalizableByDimCursor<? extends RealType> cursor = image.createLocalizableByDimCursor();
			final int[] pos = Index.create(0,0,planePos);
			int index = 0;
			for (int y=0; y<h; y++) {
				pos[1] = y;
				for (int x=0; x<w; x++) {
					pos[0] = x;
					cursor.setPosition(pos);
					// TODO: better handling of complex types
					data[index++] = cursor.getType().getRealDouble();
				}
			}
			return data;
		}
		
	// TODO: Can we extract these arrays without case logic? Seems difficult...

	/** Obtains planar access instance backing the given image, if any. */
	public static PlanarAccess<?> getPlanarAccess(Image<?> im) {
		PlanarAccess<?> planarAccess = null;
		Container<?> container = im.getContainer();
		if (container instanceof Array) {
			Array<?, ?> array = (Array<?, ?>) container;
			final DataAccess dataAccess = array.update(null);
			if (dataAccess instanceof PlanarAccess) {
				// NB: This is the #2 container type mentioned above.
				planarAccess = (PlanarAccess<?>) dataAccess;
			}
		}
		return planarAccess;
	}

	/**
	 * Gets the plane at the given position from the specified image,
	 * by reference if possible.
	 *
	 * @param <T> Type of image.
	 * @param im Image from which to extract the plane.
	 * @param planePos Dimension position of the plane in question.
	 */
	public static <T extends RealType<T>> Object getPlane(Image<T> im, int[] planePos) {
		// obtain dimensional lengths
		final int[] dims = im.getDimensions();
		if (dims.length < 2) {
			throw new IllegalArgumentException("Too few dimensions: " + dims.length);
		}

		final PlanarAccess<?> planarAccess = getPlanarAccess(im);
		if (planarAccess == null) {
			return getPlaneCopy(im, planePos);
		}

		// TODO: Add utility method for this to Index class.
		final int[] lengths = new int[dims.length - 2];
		for (int i=2; i<dims.length; i++) lengths[i - 2] = dims[i];
		final int no = Index.positionToRaster(lengths, planePos);
		return planarAccess.getPlane(no);
	}

	/**
	 * Sets the plane at the given position for the specified image,
	 * by reference if possible.
	 *
	 * @param <T> Type of image.
	 * @param im Image from which to extract the plane.
	 * @param planePos Dimension position of the plane in question.
	 * @param plane The plane data to assign.
	 * @throws ClassCastException if the plane is incompatible with the image.
	 * @throws RuntimeException if the plane cannot be set by reference.
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static <T extends RealType<T>> void setPlane(Image<T> im, int[] planePos, Object plane) {
		// obtain dimensional lengths
		final int[] dims = im.getDimensions();
		if (dims.length < 2) {
			throw new IllegalArgumentException("Too few dimensions: " + dims.length);
		}

		final PlanarAccess planarAccess = getPlanarAccess(im);
		if (planarAccess == null) {
			// TODO
			throw new RuntimeException("Unimplemented");
		}

		// TODO: Add utility method for this to Index class.
		final int[] lengths = new int[dims.length - 2];
		for (int i=2; i<dims.length; i++) lengths[i - 2] = dims[i];
		final int no = Index.positionToRaster(lengths, planePos);
		planarAccess.setPlane(no, plane);
	}

	/** throws an exception if the combination of origins and spans is outside an image's dimensions */
	public static void verifyDimensions(int[] imageDimensions, int[] origin, int[] span)
	{
		// span dims should match origin dims
		if (origin.length != span.length)
			throw new IllegalArgumentException("verifyDimensions() : origin (dim="+origin.length+") and span (dim="+span.length+") arrays are of differing sizes");

		// origin/span dimensions should match image dimensions
		if (origin.length != imageDimensions.length)
			throw new IllegalArgumentException("verifyDimensions() : origin/span (dim="+origin.length+") different size than input image (dim="+imageDimensions.length+")");
		
		// make sure origin in a valid range : within bounds of source image
		for (int i = 0; i < origin.length; i++)
			if ((origin[i] < 0) || (origin[i] >= imageDimensions[i]))
				throw new IllegalArgumentException("verifyDimensions() : origin outside bounds of input image at index " + i);

		// make sure span in a valid range : >= 1
		for (int i = 0; i < span.length; i++)
			if (span[i] < 1)
				throw new IllegalArgumentException("verifyDimensions() : span size < 1 at index " + i);

		// make sure origin + span within the bounds of the input image
		for (int i = 0; i < span.length; i++)
			if ( (origin[i] + span[i]) > imageDimensions[i] )
				throw new IllegalArgumentException("verifyDimensions() : span range (origin+span) beyond input image boundaries at index " + i);
	}
	

	/** copies data from one image to another given origins and dimensional spans */
	public static <K extends RealType<K>>
		void copyFromImageToImage(Image<K> srcImage, int[] srcOrigin, int[] srcSpan,
									Image<K> dstImage, int[] dstOrigin, int[] dstSpan)
	{
		CopyUnaryFunction copyFunc = new CopyUnaryFunction();
		
		BinaryAssignOperation<K> copier =
			new BinaryAssignOperation<K>(dstImage, dstOrigin, dstSpan, srcImage, srcOrigin, srcSpan, copyFunc);

		copier.execute();
	}
	
	public static ImgLibProcessor<?> createProcessor(int width, int height, Object pixels, boolean unsigned)
	{
		ArrayContainerFactory containerFactory = new ArrayContainerFactory();
		containerFactory.setPlanar(true);
		
		ImgLibProcessor<?> proc = null;
		
		int[] dimensions = new int[]{width, height, 1};
		
		if (pixels instanceof byte[])
		{
			if (unsigned)
			{
				ImageFactory<UnsignedByteType> factory = new ImageFactory<UnsignedByteType>(new UnsignedByteType(),containerFactory);
				Image<UnsignedByteType> hatchedImage = factory.createImage(dimensions);
				proc = new ImgLibProcessor<UnsignedByteType>(hatchedImage, 0);
			}
			else
			{
				ImageFactory<ByteType> factory = new ImageFactory<ByteType>(new ByteType(),containerFactory);
				Image<ByteType> hatchedImage = factory.createImage(dimensions);
				proc = new ImgLibProcessor<ByteType>(hatchedImage, 0);
			}
		}
		else if (pixels instanceof short[])
		{
			if (unsigned)
			{
				ImageFactory<UnsignedShortType> factory = new ImageFactory<UnsignedShortType>(new UnsignedShortType(),containerFactory);
				Image<UnsignedShortType> hatchedImage = factory.createImage(dimensions);
				proc = new ImgLibProcessor<UnsignedShortType>(hatchedImage, 0);
			}
			else
			{
				ImageFactory<ShortType> factory = new ImageFactory<ShortType>(new ShortType(),containerFactory);
				Image<ShortType> hatchedImage = factory.createImage(dimensions);
				proc = new ImgLibProcessor<ShortType>(hatchedImage, 0);
			}
		}
		else if (pixels instanceof int[])
		{
			if (unsigned)
			{
				ImageFactory<UnsignedIntType> factory = new ImageFactory<UnsignedIntType>(new UnsignedIntType(),containerFactory);
				Image<UnsignedIntType> hatchedImage = factory.createImage(dimensions);
				proc = new ImgLibProcessor<UnsignedIntType>(hatchedImage, 0);
			}
			else
			{
				ImageFactory<IntType> factory = new ImageFactory<IntType>(new IntType(),containerFactory);
				Image<IntType> hatchedImage = factory.createImage(dimensions);
				proc = new ImgLibProcessor<IntType>(hatchedImage, 0);
			}
		}
		else if (pixels instanceof long[])
		{
			if (unsigned)
			{
				throw new IllegalArgumentException("createProcessor(): unsigned long is not a supported pixel type");
			}
			else
			{
				ImageFactory<LongType> factory = new ImageFactory<LongType>(new LongType(),containerFactory);
				Image<LongType> hatchedImage = factory.createImage(dimensions);
				proc = new ImgLibProcessor<LongType>(hatchedImage, 0);
			}
		}
		else if (pixels instanceof float[])
		{
			ImageFactory<FloatType> factory = new ImageFactory<FloatType>(new FloatType(),containerFactory);
			Image<FloatType> hatchedImage = factory.createImage(dimensions);
			proc = new ImgLibProcessor<FloatType>(hatchedImage, 0);
		}
		else if (pixels instanceof double[])
		{
			ImageFactory<DoubleType> factory = new ImageFactory<DoubleType>(new DoubleType(),containerFactory);
			Image<DoubleType> hatchedImage = factory.createImage(dimensions);
			proc = new ImgLibProcessor<DoubleType>(hatchedImage, 0);
		}
		else
			throw new IllegalArgumentException("createProcessor(): passed unknown type of pixels - "+pixels.getClass());
		
		proc.setPixels(pixels);
		
		return proc;
	}
	

	public static ImagePlus createImagePlus(final Image<?> img)
	{
		return createImagePlus(img, null);
	}

	public static ImagePlus createImagePlus(final Image<?> img, final String id)
	{
		final int sizeX = getWidth(img);
		final int sizeY = getHeight(img);
		final int sizeC = getNChannels(img);
		final int sizeZ = getNSlices(img);
		final int sizeT = getNFrames(img);

		

		final String imgName = img.getName();
		final int[] dimensions = img.getDimensions();
		final String[] dimTypes = ImageOpener.decodeTypes(imgName);

		final ImageStack stack = new ImageStack(img);
		final ImagePlus imp = new ImagePlus(img.getName(), stack);
		if (id != null) {
			final FileInfo fi = new FileInfo();
			fi.width = sizeX;
			fi.height = sizeY;
			final File file = new File(id);
			if (file.exists()) {
				fi.fileName = file.getName();
				fi.directory = file.getParent();
				imp.setTitle(fi.fileName);
			}
			else fi.url = id;
			imp.setFileInfo(fi);
		}

		// let ImageJ know what dimensions we have
		imp.setDimensions(sizeC, sizeZ, sizeT);

		return imp;
	}
	
	@SuppressWarnings({"unchecked"})
	public static <K extends RealType<K>> Image<K> createImage(RealType<K> type, ContainerFactory cFact, int[] dimensions)
	{
		ImageFactory<K> factory = new ImageFactory<K>((K)type, cFact);
		return factory.createImage(dimensions);
	}

	public static int getWidth(final Image<?> img) {
		return getDimSize(img, ImageOpener.X, 0);
	}

	public static int getHeight(final Image<?> img) {
		return getDimSize(img, ImageOpener.Y, 1);
	}

	public static int getNChannels(final Image<?> img) {
		return getDimSize(img, FormatTools.CHANNEL, 2);
	}

	public static int getNSlices(final Image<?> img) {
		return getDimSize(img, ImageOpener.Z, 3);
	}

	public static int getNFrames(final Image<?> img) {
		return getDimSize(img, ImageOpener.TIME, 4);
	}
	
	// ***************** private methods  **************************************************
	
	@SuppressWarnings({"unchecked"})
	private static Object getPlaneCopy(Image<? extends RealType<?>> im, int[] planePos)
	{
		RealType<?> type = getType(im);

		if (type instanceof ByteType)
			return GetPlaneOperation.getPlaneAs((Image<ByteType>)im, planePos, ValueType.BYTE);

		if (type instanceof UnsignedByteType)
			return GetPlaneOperation.getPlaneAs((Image<UnsignedByteType>)im, planePos, ValueType.UBYTE);

		if (type instanceof ShortType)
			return GetPlaneOperation.getPlaneAs((Image<ShortType>)im, planePos, ValueType.SHORT);

		if (type instanceof UnsignedShortType)
			return GetPlaneOperation.getPlaneAs((Image<UnsignedShortType>)im, planePos, ValueType.USHORT);

		if (type instanceof IntType)
			return GetPlaneOperation.getPlaneAs((Image<IntType>)im, planePos, ValueType.INT);

		if (type instanceof UnsignedIntType)
			return GetPlaneOperation.getPlaneAs((Image<UnsignedIntType>)im, planePos, ValueType.UINT);

		if (type instanceof LongType)
			return GetPlaneOperation.getPlaneAs((Image<LongType>)im, planePos, ValueType.LONG);

		if (type instanceof FloatType)
			return GetPlaneOperation.getPlaneAs((Image<FloatType>)im, planePos, ValueType.FLOAT);

		if (type instanceof DoubleType)
			return GetPlaneOperation.getPlaneAs((Image<DoubleType>)im, planePos, ValueType.DOUBLE);

		throw new IllegalArgumentException("getPlaneCopy(): unsupported type - "+type.getClass());
	}

	private static int getDimSize(final Image<?> img, final String dimType, final int defaultIndex) {
		final String imgName = img.getName();
		final int[] dimensions = img.getDimensions();
		final String[] dimTypes = ImageOpener.decodeTypes(imgName);
		int size = 1;
		if (dimTypes.length == dimensions.length) {
			for (int i = 0; i < dimTypes.length; i++) {
				if (dimType.equals(dimTypes[i])) size *= dimensions[i];
			}
		}
		else {
			// assume default ordering
			if (dimensions.length > defaultIndex) size = dimensions[defaultIndex];
		}
		return size;
	}

}