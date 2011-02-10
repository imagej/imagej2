package imagej.imglib.process;

import imagej.Dimensions;
import imagej.data.Type;
import imagej.function.unary.CopyUnaryFunction;
import imagej.imglib.ImageUtils;
import imagej.imglib.TypeManager;
import imagej.imglib.process.operation.BinaryAssignOperation;
import imagej.imglib.process.operation.GetPlaneOperation;
import imagej.process.Index;
import mpicbg.imglib.container.basictypecontainer.PlanarAccess;
import mpicbg.imglib.container.basictypecontainer.array.ArrayDataAccess;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.logic.BitType;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.ByteType;
import mpicbg.imglib.type.numeric.integer.IntType;
import mpicbg.imglib.type.numeric.integer.LongType;
import mpicbg.imglib.type.numeric.integer.ShortType;
import mpicbg.imglib.type.numeric.integer.Unsigned12BitType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import mpicbg.imglib.type.numeric.integer.UnsignedIntType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;
import mpicbg.imglib.type.numeric.real.DoubleType;
import mpicbg.imglib.type.numeric.real.FloatType;

/** this class designed to hold functionality that could be migrated to imglib */
public class OldImageUtils
{
	public static final String X = "X";
	public static final String Y = "Y";
	public static final String Z = "Z";
	public static final String TIME = "Time";
	public static final String CHANNEL = "Channel";

	// ***************** public methods  **************************************************

	/** returns the total number of samples in an imglib image. Notice its of long type unlike
		imglib's image.getNumPixels()
	*/
	public static long getTotalSamples(Image<?> image)
	{
		return Dimensions.getTotalSamples(image.getDimensions());
	}

	/** copies a plane of data of specified size from an imglib image to an output array of doubles
	 *
	 * @param image - the image we want to pull the data from
	 * @param w - the desired width
	 * @param h - the desire height
	 * @param planePos - the position of the plane within the imglib image
	 * @return the sample data as an array of doubles
	 */
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

		final PlanarAccess<ArrayDataAccess<?>> planarAccess = ImageUtils.getPlanarAccess(im);
		if (planarAccess == null) {
			return getPlaneCopy(im, planePos);
		}

		// TODO: Add utility method for this to Index class.
		final int[] lengths = new int[dims.length - 2];
		for (int i=2; i<dims.length; i++) lengths[i - 2] = dims[i];
		final int no = Index.positionToRaster(lengths, planePos);
		return planarAccess.getPlane(no).getCurrentStorageArray();
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
	public static void setPlane(Image<?> im, int[] planePos, Object plane) {
		// obtain dimensional lengths
		final int[] dims = im.getDimensions();
		if (dims.length < 2) {
			throw new IllegalArgumentException("Too few dimensions: " + dims.length);
		}

		final PlanarAccess planarAccess = ImageUtils.getPlanarAccess(im);
		if (planarAccess == null) {
			// TODO
			throw new RuntimeException("Unimplemented");
		}

		// TODO: Add utility method for this to Index class.
		final int[] lengths = new int[dims.length - 2];
		for (int i=2; i<dims.length; i++) lengths[i - 2] = dims[i];
		final int no = Index.positionToRaster(lengths, planePos);
		// TODO: move ImageOpener.makeArray somewhere more suitable.
		planarAccess.setPlane(no, ImageUtils.makeArray(plane));
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

	public static int getWidth(final Image<?> img) {
		return getDimSize(img, X, 0);
	}

	public static int getHeight(final Image<?> img) {
		return getDimSize(img, Y, 1);
	}

	public static int getNChannels(final Image<?> img) {
		return getDimSize(img, CHANNEL, 2);
	}

	public static int getNSlices(final Image<?> img) {
		return getDimSize(img, Z, 3);
	}

	public static int getNFrames(final Image<?> img) {
		return getDimSize(img, TIME, 4);
	}

	public static int getDimSize(final Image<?> img, final String dimType) {
		return getDimSize(img, dimType, -1);
	}

	// ***************** private methods  **************************************************

	/**
	 * get a plane of data from an imglib image
	 * @param im - the imglib image we will be pulling plane from
	 * @param planePos - the position of the plane within the image
	 * @return an array of primitive type matching the imglib image's type
	 */
	@SuppressWarnings({"unchecked"})
	private static Object getPlaneCopy(Image<? extends RealType<?>> im, int[] planePos)
	{
		RealType<?> imglibType = ImageUtils.getType(im);
		
		Type ijType = TypeManager.getIJType(imglibType); 

		if (imglibType instanceof BitType)
			return GetPlaneOperation.getPlaneAs((Image<ByteType>)im, planePos, ijType);

		if (imglibType instanceof ByteType)
			return GetPlaneOperation.getPlaneAs((Image<ByteType>)im, planePos, ijType);

		if (imglibType instanceof UnsignedByteType)
			return GetPlaneOperation.getPlaneAs((Image<UnsignedByteType>)im, planePos, ijType);

		if (imglibType instanceof Unsigned12BitType)
			return GetPlaneOperation.getPlaneAs((Image<Unsigned12BitType>)im, planePos, ijType);

		if (imglibType instanceof ShortType)
			return GetPlaneOperation.getPlaneAs((Image<ShortType>)im, planePos, ijType);

		if (imglibType instanceof UnsignedShortType)
			return GetPlaneOperation.getPlaneAs((Image<UnsignedShortType>)im, planePos, ijType);

		if (imglibType instanceof IntType)
			return GetPlaneOperation.getPlaneAs((Image<IntType>)im, planePos, ijType);

		if (imglibType instanceof UnsignedIntType)
			return GetPlaneOperation.getPlaneAs((Image<UnsignedIntType>)im, planePos, ijType);

		if (imglibType instanceof LongType)
			return GetPlaneOperation.getPlaneAs((Image<LongType>)im, planePos, ijType);

		if (imglibType instanceof FloatType)
			return GetPlaneOperation.getPlaneAs((Image<FloatType>)im, planePos, ijType);

		if (imglibType instanceof DoubleType)
			return GetPlaneOperation.getPlaneAs((Image<DoubleType>)im, planePos, ijType);

		throw new IllegalArgumentException("getPlaneCopy(): unsupported type - "+imglibType.getClass());
	}

	/** Converts the given image name back to a list of dimensional axis types. */
	private static String[] decodeTypes(String name) {
		final int lBracket = name.lastIndexOf(" [");
		if (lBracket < 0) return new String[0];
		final int rBracket = name.lastIndexOf("]");
		if (rBracket < lBracket) return new String[0];
		return name.substring(lBracket + 2, rBracket).split(" ");
	}

	private static int getDimSize(final Image<?> img, final String dimType, final int defaultIndex)
	{
		final String imgName = img.getName();
		final int[] dimensions = img.getDimensions();
		final String[] dimTypes = decodeTypes(imgName);
		int size = 1;
		if (dimTypes.length == dimensions.length) {
			for (int i = 0; i < dimTypes.length; i++) {
				if (dimType.equals(dimTypes[i])) size *= dimensions[i];
			}
		}
		else {
			// assume default ordering
			if (defaultIndex >= 0 && defaultIndex < dimensions.length) {
				size = dimensions[defaultIndex];
			}
		}
		return size;
	}

}
