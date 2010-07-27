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
import mpicbg.imglib.image.Image;
import mpicbg.imglib.io.LOCI;
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

public class ImgLibProcessor<T extends RealType<T>> extends ImageProcessor {

	private final Image<T> imageData;

	// TODO: How can we use generics here without breaking javac?
	@SuppressWarnings("rawtypes")
	private final RealType type;

	/**
   * Dimensional coordinates for this processor's plane.
   * Array length equals <code>imageData.getDimensions().length - 2</code>,
   * since the first two dimensions are assumed to be X and Y, respectively.
   */
	private final int[] coords;

	private byte[] pixels8;

	public ImgLibProcessor(Image<T> img, T type, int[] coords) {
		this.imageData = img;
		this.type = type;
		final int[] dims = img.getDimensions();
		if (dims.length < 2) {
			throw new IllegalArgumentException("Image must be at least 2-D");
		}
		this.width = dims[0]; // TODO: Dimensional labels are safer way to find X
		this.height = dims[1]; // TODO: Dimensional labels are safer way to find Y
		if (coords == null) {
			this.coords = new int[dims.length - 2];
		}
		else if (coords.length == dims.length - 2) {
			this.coords = coords.clone();
		}
		else {
			throw new IllegalArgumentException("Dimensional mismatch: image is " +
				dims.length + "-D but coords array length is " + coords.length);
		}
	}

	@Override
	public void applyTable(int[] lut) {
    throw new RuntimeException("Unimplemented");

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
		boolean firstTime = pixels8==null;
		if (firstTime || !lutAnimation)
			create8BitImage();
		if (cm==null)
			makeDefaultColorModel();
		if (source==null) {
			source = new MemoryImageSource(width, height, cm, pixels8, 0, width);
			source.setAnimated(true);
			source.setFullBufferUpdates(true);
			img = Toolkit.getDefaultToolkit().createImage(source);
		} else if (newPixels) {
			source.newPixels(pixels8, cm, 0, width);
			newPixels = false;
		} else
			source.newPixels();

		lutAnimation = false;
	  return img;
	}

	@Override
	public ImageProcessor createProcessor(int width, int height) {
    throw new RuntimeException("Unimplemented");
	}

	@Override
	public ImageProcessor crop() {
    throw new RuntimeException("Unimplemented");
	}

	@Override
	public void dilate() {
    throw new RuntimeException("Unimplemented");

	}

	@Override
	public void drawPixel(int x, int y) {
    throw new RuntimeException("Unimplemented");

	}

	@Override
	public ImageProcessor duplicate() {
    throw new RuntimeException("Unimplemented");
	}

	@Override
	public void erode() {
    throw new RuntimeException("Unimplemented");

	}

	@Override
	public void fill(ImageProcessor mask) {
    throw new RuntimeException("Unimplemented");

	}

	@Override
	public void filter(int type) {
    throw new RuntimeException("Unimplemented");

	}

	@Override
	public void flipVertical() {
    throw new RuntimeException("Unimplemented");

	}

	@Override
	public int get(int x, int y) {
    throw new RuntimeException("Unimplemented");
	}

	@Override
	public int get(int index) {
    throw new RuntimeException("Unimplemented");
	}

	@Override
	public double getBackgroundValue() {
    throw new RuntimeException("Unimplemented");
	}

	@Override
	public int[] getHistogram() {
    throw new RuntimeException("Unimplemented");
	}

	@Override
	public double getInterpolatedPixel(double x, double y) {
    throw new RuntimeException("Unimplemented");
	}

	@Override
	public double getMax() {
		return 255; // Unimplemented
	}

	@Override
	public double getMin() {
		return 0; // Unimplemented
	}

	@Override
	public int getPixel(int x, int y) {
    throw new RuntimeException("Unimplemented");
	}

	@Override
	public int getPixelInterpolated(double x, double y) {
    throw new RuntimeException("Unimplemented");
	}

	@Override
	public float getPixelValue(int x, int y) {
    throw new RuntimeException("Unimplemented");
	}

	@Override
	public Object getPixels() {
		// TODO: could add a special case for single-image 8-bit array-backed data
		// TODO: special case for new container
		return getPixelsArray();
	}

	@Override
	public Object getPixelsCopy() {
    throw new RuntimeException("Unimplemented");
	}

	@Override
	public Object getSnapshotPixels() {
    throw new RuntimeException("Unimplemented");
	}

	@Override
	public float getf(int x, int y) {
    throw new RuntimeException("Unimplemented");
	}

	@Override
	public float getf(int index) {
    throw new RuntimeException("Unimplemented");
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

	@Override
	public void reset() {
    throw new RuntimeException("Unimplemented");

	}

	@Override
	public void reset(ImageProcessor mask) {
    throw new RuntimeException("Unimplemented");

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
	public void set(int x, int y, int value) {
    throw new RuntimeException("Unimplemented");

	}

	@Override
	public void set(int index, int value) {
    throw new RuntimeException("Unimplemented");

	}

	@Override
	public void setBackgroundValue(double value) {
    throw new RuntimeException("Unimplemented");

	}

	@Override
	public void setColor(Color color) {
    throw new RuntimeException("Unimplemented");

	}

	@Override
	public void setMinAndMax(double min, double max) {
    throw new RuntimeException("Unimplemented");

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
	public void setSnapshotPixels(Object pixels) {
    throw new RuntimeException("Unimplemented");

	}

	@Override
	public void setValue(double value) {
    throw new RuntimeException("Unimplemented");

	}

	@Override
	public void setf(int x, int y, float value) {
    throw new RuntimeException("Unimplemented");

	}

	@Override
	public void setf(int index, float value) {
    throw new RuntimeException("Unimplemented");

	}

	@Override
	public void snapshot() {
    throw new RuntimeException("Unimplemented");

	}

	@Override
	public void threshold(int level) {
    throw new RuntimeException("Unimplemented");

	}

	@Override
	public FloatProcessor toFloat(int channelNumber, FloatProcessor fp) {
    throw new RuntimeException("Unimplemented");
	}

	@Override
	public byte[] create8BitImage()
	{
		// TODO: use imageData.getDisplay().get8Bit* methods
		Object pixels = getPixels();

		if (pixels instanceof byte[])
		{
			pixels8 = (byte[]) pixels;
		}
		else if (pixels instanceof short[])
		{
			short[] pix = (short[]) pixels;
			pixels8 = DataTools.shortsToBytes(pix, false);
		}
		else if (pixels instanceof int[])
		{
			int[] pix = (int[]) pixels;
			pixels8 = DataTools.intsToBytes(pix, false);
		}
		else if (pixels instanceof float[])
		{
			float[] pix = (float[]) pixels;
			pixels8 = DataTools.floatsToBytes(pix, false);
		}
		else if (pixels instanceof double[])
		{
			double[] pix = (double[]) pixels;
			pixels8 = DataTools.doublesToBytes(pix, false);
		}
		else if (pixels instanceof long[])
		{
			long[] pix = (long[]) pixels;
			pixels8 = DataTools.longsToBytes(pix, false);
		}

		return pixels8;
	}

//	@SuppressWarnings("unchecked")
//	private ArrayDataAccess<?> getAccess(mpicbg.imglib.image.Image<T> img) {
//		final Container<?> container = img.getContainer();
//		if (!(container instanceof Array<?, ?>)) return null;
//		final Array<T, ?> array = (Array<T, ?>) img.getContainer();
//		ArrayDataAccess<?> access = (ArrayDataAccess<?>) array.update(null);
//		return access;
//	}

	// TODO is there a better way? ask.

//	private boolean isSignedType(T t) {
//		return !(
//			(t instanceof UnsignedByteType) ||
//			(t instanceof UnsignedIntType) ||
//			(t instanceof UnsignedShortType)
//		);
//	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public Object getPixelsArray() {
		if (type instanceof ByteType) {
			Image<ByteType> im = (Image) imageData;
			return getPlaneBytes(im, width, height, coords);
		}
		if (type instanceof UnsignedByteType) {
			Image<UnsignedByteType> im = (Image) imageData;
			return getPlaneUnsignedBytes(im, width, height, coords);
		}
		if (type instanceof ShortType) {
			Image<ShortType> im = (Image) imageData;
			return getPlaneShorts(im, width, height, coords);
		}
		if (type instanceof UnsignedShortType) {
			Image<UnsignedShortType> im = (Image) imageData;
			return getPlaneUnsignedShorts(im, width, height, coords);
		}
		if (type instanceof IntType) {
			Image<IntType> im = (Image) imageData;
			return getPlaneInts(im, width, height, coords);
		}
		if (type instanceof UnsignedIntType) {
			Image<UnsignedIntType> im = (Image) imageData;
			return getPlaneUnsignedInts(im, width, height, coords);
		}
		if (type instanceof LongType) {
			Image<LongType> im = (Image) imageData;
			return getPlaneLongs(im, width, height, coords);
		}
		if (type instanceof FloatType) {
			Image<FloatType> im = (Image) imageData;
			return getPlaneFloats(im, width, height, coords);
		}
		if (type instanceof DoubleType) {
			Image<DoubleType> im = (Image) imageData;
			return getPlaneDoubles(im, width, height, coords);
		}
		return getPlaneData();
	}

	public double[] getPlaneData() {
	  // TODO - use LocalizablePlaneCursor
		// example in ImageJVirtualStack.extractSliceFloat
		final double[] data = new double[width * height];
		final LocalizableByDimCursor<T> cursor =
			imageData.createLocalizableByDimCursor();
		final int[] pos = makePosArray(coords);
		int index = 0;
		for (int y=0; y<height; y++) {
			pos[1] = y;
			for (int x=0; x<width; x++) {
				pos[0] = x;
				cursor.setPosition(pos);
				// TODO: better handling of complex types
				data[index++] = cursor.getType().getRealDouble();
			}
		}
		return data;
	}

	// TODO: Can we extract these arrays without case logic? Seems difficult...

	public static byte[] getPlaneBytes(Image<ByteType> im,
		int w, int h, int[] coords)
	{
		final byte[] data = new byte[w * h];
		final LocalizableByDimCursor<ByteType> cursor =
			im.createLocalizableByDimCursor();
		final int[] pos = makePosArray(coords);
		int index = 0;
		for (int y=0; y<h; y++) {
			pos[1] = y;
			for (int x=0; x<w; x++) {
				pos[0] = x;
				cursor.setPosition(pos);
				data[index++] = cursor.getType().get();
			}
		}
		return data;
	}

	public static byte[] getPlaneUnsignedBytes(Image<UnsignedByteType> im,
		int w, int h, int[] coords)
	{
		final byte[] data = new byte[w * h];
		final LocalizableByDimCursor<UnsignedByteType> cursor =
			im.createLocalizableByDimCursor();
		final int[] pos = makePosArray(coords);
		int index = 0;
		for (int y=0; y<h; y++) {
			pos[1] = y;
			for (int x=0; x<w; x++) {
				pos[0] = x;
				cursor.setPosition(pos);
				data[index++] = (byte) cursor.getType().get();
			}
		}
		return data;
	}

	public static short[] getPlaneShorts(Image<ShortType> im,
		int w, int h, int[] coords)
	{
		final short[] data = new short[w * h];
		final LocalizableByDimCursor<ShortType> cursor =
			im.createLocalizableByDimCursor();
		final int[] pos = makePosArray(coords);
		int index = 0;
		for (int y=0; y<h; y++) {
			pos[1] = y;
			for (int x=0; x<w; x++) {
				pos[0] = x;
				cursor.setPosition(pos);
				data[index++] = cursor.getType().get();
			}
		}
		return data;
	}

	public static short[] getPlaneUnsignedShorts(Image<UnsignedShortType> im,
		int w, int h, int[] coords)
	{
		final short[] data = new short[w * h];
		final LocalizableByDimCursor<UnsignedShortType> cursor =
			im.createLocalizableByDimCursor();
		final int[] pos = makePosArray(coords);
		int index = 0;
		for (int y=0; y<h; y++) {
			pos[1] = y;
			for (int x=0; x<w; x++) {
				pos[0] = x;
				cursor.setPosition(pos);
				data[index++] = (short) cursor.getType().get();
			}
		}
		return data;
	}

	public static int[] getPlaneInts(Image<IntType> im,
		int w, int h, int[] coords)
	{
		final int[] data = new int[w * h];
		final LocalizableByDimCursor<IntType> cursor =
			im.createLocalizableByDimCursor();
		final int[] pos = makePosArray(coords);
		int index = 0;
		for (int y=0; y<h; y++) {
			pos[1] = y;
			for (int x=0; x<w; x++) {
				pos[0] = x;
				cursor.setPosition(pos);
				data[index++] = cursor.getType().get();
			}
		}
		return data;
	}

	public static int[] getPlaneUnsignedInts(Image<UnsignedIntType> im,
		int w, int h, int[] coords)
	{
		final int[] data = new int[w * h];
		final LocalizableByDimCursor<UnsignedIntType> cursor =
			im.createLocalizableByDimCursor();
		final int[] pos = makePosArray(coords);
		int index = 0;
		for (int y=0; y<h; y++) {
			pos[1] = y;
			for (int x=0; x<w; x++) {
				pos[0] = x;
				cursor.setPosition(pos);
				data[index++] = (int) cursor.getType().get();
			}
		}
		return data;
	}

	public static long[] getPlaneLongs(Image<LongType> im,
		int w, int h, int[] coords)
	{
		final long[] data = new long[w * h];
		final LocalizableByDimCursor<LongType> cursor =
			im.createLocalizableByDimCursor();
		final int[] pos = makePosArray(coords);
		int index = 0;
		for (int y=0; y<h; y++) {
			pos[1] = y;
			for (int x=0; x<w; x++) {
				pos[0] = x;
				cursor.setPosition(pos);
				data[index++] = cursor.getType().get();
			}
		}
		return data;
	}

	public static float[] getPlaneFloats(Image<FloatType> im,
		int w, int h, int[] coords)
	{
		final float[] data = new float[w * h];
		final LocalizableByDimCursor<FloatType> cursor =
			im.createLocalizableByDimCursor();
		final int[] pos = makePosArray(coords);
		int index = 0;
		for (int y=0; y<h; y++) {
			pos[1] = y;
			for (int x=0; x<w; x++) {
				pos[0] = x;
				cursor.setPosition(pos);
				data[index++] = cursor.getType().get();
			}
		}
		return data;
	}

	public static double[] getPlaneDoubles(Image<DoubleType> im,
		int w, int h, int[] coords)
	{
		final double[] data = new double[w * h];
		final LocalizableByDimCursor<DoubleType> cursor =
			im.createLocalizableByDimCursor();
		final int[] pos = makePosArray(coords);
		int index = 0;
		for (int y=0; y<h; y++) {
			pos[1] = y;
			for (int x=0; x<w; x++) {
				pos[0] = x;
				cursor.setPosition(pos);
				data[index++] = cursor.getType().get();
			}
		}
		return data;
	}

	public static int[] makePosArray(int[] coords) {
		int[] pos = new int[2 + coords.length];
		for (int i=0; i<coords.length; i++) pos[i + 2] = coords[i];
		return pos;
	}

	public static void main(String[] args) {
		final JFileChooser chooser = new JFileChooser();
		int rval = chooser.showOpenDialog(null);
		if (rval != JFileChooser.APPROVE_OPTION) return;
		final File file = chooser.getSelectedFile();
		final String fileName = file.getAbsolutePath();
		final ContainerFactory containerFactory = new ArrayContainerFactory();
		final Image<UnsignedByteType> image =
			LOCI.openLOCIUnsignedByteType(fileName, containerFactory);
		final int[] dims = image.getDimensions();
		final int width = dims[0];
		final int height = dims[1];
		final int sizeZ = dims[2];
		final int[] coords = new int[1];
		final ImageStack imageStack = new ImageStack(width, height);
		for (int i =0; i<sizeZ; i++) {
			coords[0] = i;
			ImageProcessor ip = new ImgLibProcessor<UnsignedByteType>(image,
				new UnsignedByteType(), coords);
			imageStack.addSlice("" + (i + 1), ip);
		}
		ImagePlus imp = new ImagePlus(fileName, imageStack);
    new ImageJ();
    imp.show();
	}

}
