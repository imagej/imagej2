package imagej.legacy;

import net.imglib2.RandomAccess;
import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.ImgPlus;
import net.imglib2.img.basictypeaccess.PlanarAccess;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import imagej.data.Dataset;
import imagej.util.Index;
import imagej.util.Log;


/**
 * Collection of static methods used by the various ImageTranslators and the
 * DatasetHarmonizer. Kept together so that bidirectional translation code is
 * in one place and thus more easily maintained.
 * */
public class LegacyUtils {

	// -- constructor --
	
	private LegacyUtils() {
		// utility class: do not instantiate
	}

	// -- public interface --
	
	public static Dataset makeExactDataset(ImagePlus imp) {
		final int x = imp.getWidth();
		final int y = imp.getHeight();
		final int c = imp.getNChannels();
		final int z = imp.getNSlices();
		final int t = imp.getNFrames();
		final long[] dims = new long[] { x, y, c, z, t };
		final String name = imp.getTitle();
		final Axis[] axes = { Axes.X, Axes.Y, Axes.CHANNEL, Axes.Z, Axes.TIME };
		final int bitsPerPixel = imp.getBitDepth();
		final boolean signed = isSigned(imp);
		final boolean floating = isFloating(imp);
		final Dataset ds =
			Dataset.create(dims, name, axes, bitsPerPixel, signed, floating);

		setDatasetPlanes(ds, imp);
		
		return ds;
	}
	
	
	public static Dataset makeColorDataset(ImagePlus imp) {
		final int x = imp.getWidth();
		final int y = imp.getHeight();
		final int c = imp.getNChannels();
		final int z = imp.getNSlices();
		final int t = imp.getNFrames();
		
		if (c != 1)
			throw new IllegalArgumentException(
				"can't make a color Dataset from a multichannel ColorProcessor stack");
		
		if (imp.getType() != ImagePlus.COLOR_RGB)
			throw new IllegalArgumentException(
				"can't make a color Dataset from a nonRGB ImagePlus");
		
		final long[] dims = new long[] { x, y, 3, z, t };
		final String name = imp.getTitle();
		final Axis[] axes = { Axes.X, Axes.Y, Axes.CHANNEL, Axes.Z, Axes.TIME };
		final int bitsPerPixel = imp.getBitDepth();
		final boolean signed = isSigned(imp);
		final boolean floating = isFloating(imp);
		final Dataset ds =
			Dataset.create(dims, name, axes, bitsPerPixel, signed, floating);
		
		ds.setRGBMerged(true);
		
		return ds;
	}

	// TODO - check that Dataset can be represented exactly
	public static ImagePlus makeExactImagePlus(Dataset ds) {
		int[] dimIndices = new int[5];
		int[] dimValues = new int[5];
		getImagePlusDims(ds, dimIndices, dimValues);

		final int cIndex = dimIndices[2];
		final int zIndex = dimIndices[3];
		final int tIndex = dimIndices[4];
		final int c = cIndex < 0 ? 1 : dimValues[cIndex];
		final int z = zIndex < 0 ? 1 : dimValues[zIndex];
		final int t = tIndex < 0 ? 1 : dimValues[tIndex];

		ImagePlus imp = makeImagePlus(ds, getPlaneMaker(ds));
		
		setImagePlusPlanes(ds, imp);

		imp.setDimensions(c, z, t);
		
		return imp;
	}

	
	public static ImagePlus makeNearestTypeGrayImagePlus(Dataset ds) {
		PlaneMaker planeMaker = getPlaneMaker(ds);
		return makeImagePlus(ds, planeMaker);
	}

	public static ImagePlus makeColorImagePlus(Dataset ds) {
		if ( ! isColorCompatible(ds) )
			throw new IllegalArgumentException("Dataset is not color compatible");
		
		int[] dimIndices = new int[5];
		int[] dimValues = new int[5];
		getColorImagePlusDims(ds, dimIndices, dimValues);
		int w = dimValues[0];
		int h = dimValues[1];
		int z = dimValues[3];
		int t = dimValues[4];
		
		ImageStack stack = new ImageStack(w, h, z*t);
		
		for (int i = 0; i < z*t; i++)
			stack.setPixels(new int[w*h], i+1);
		
		ImagePlus imp = new ImagePlus(ds.getName(), stack);
		
		imp.setDimensions(1, z, t);

		return imp;
	}

	// assumes ds & imp are correct dimensions and not directly mapped
	public static void setImagePlusGrayData(Dataset ds, ImagePlus imp) {
		boolean bitData = ds.getType() instanceof BitType;
		int x = imp.getWidth();
		int y = imp.getHeight();
		int zIndex = ds.getAxisIndex(Axes.Z);
		int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		int tIndex = ds.getAxisIndex(Axes.TIME);
		int z = (int) ( (zIndex < 0) ? 1 : ds.getImgPlus().dimension(zIndex) );
		int c = (int) ( (cIndex < 0) ? 1 : ds.getImgPlus().dimension(cIndex) );
		int t = (int) ( (tIndex < 0) ? 1 : ds.getImgPlus().dimension(tIndex) );
		int imagejPlaneNumber = 1;
		RandomAccess<? extends RealType<?>> accessor = ds.getImgPlus().randomAccess();
		for (int ti = 0; ti < t; ti++) {
			if (tIndex >= 0) accessor.setPosition(ti, tIndex);
			for (int zi = 0; zi < z; zi++) {
				if (zIndex >= 0) accessor.setPosition(zi, zIndex);
				for (int ci = 0; ci < c; ci++) {
					if (cIndex >= 0) accessor.setPosition(ci, cIndex);
					ImageProcessor proc = imp.getStack().getProcessor(imagejPlaneNumber++);
					for (int yi = 0; yi < y; yi++) {
						accessor.setPosition(yi, 1);
						for (int xi = 0; xi < x; xi++) {
							accessor.setPosition(xi, 0);
							double value = accessor.get().getRealDouble();
							if (bitData)
								if (value > 0)
									value = 255;
							proc.setf(xi, yi, (float)value);
						}
					}
				}
			}
		}
	}

	// assumes ds & imp are correct dimensions and not directly mapped
	public static void setImagePlusColorData(Dataset ds, ImagePlus imp) {
		int x = imp.getWidth();
		int y = imp.getHeight();
		int zIndex = ds.getAxisIndex(Axes.Z);
		int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		int tIndex = ds.getAxisIndex(Axes.TIME);
		int z = (int) ( (zIndex < 0) ? 1 : ds.getImgPlus().dimension(zIndex) );
		int t = (int) ( (tIndex < 0) ? 1 : ds.getImgPlus().dimension(tIndex) );
		int imagejPlaneNumber = 1;
		RandomAccess<? extends RealType<?>> accessor = ds.getImgPlus().randomAccess();
		for (int ti = 0; ti < t; ti++) {
			if (tIndex >= 0) accessor.setPosition(ti, tIndex);
			for (int zi = 0; zi < z; zi++) {
				if (zIndex >= 0) accessor.setPosition(zi, zIndex);
				ImageProcessor proc = imp.getStack().getProcessor(imagejPlaneNumber++);
				for (int yi = 0; yi < y; yi++) {
					accessor.setPosition(yi, 1);
					for (int xi = 0; xi < x; xi++) {
						accessor.setPosition(xi, 0);
						
						accessor.setPosition(0, cIndex);
						int rValue = ((int) accessor.get().getRealDouble()) & 0xff;
						
						accessor.setPosition(1, cIndex);
						int gValue = ((int) accessor.get().getRealDouble()) & 0xff;
						
						accessor.setPosition(2, cIndex);
						int bValue = ((int) accessor.get().getRealDouble()) & 0xff;
						
						int intValue = (0xff << 24) | (rValue << 16) | (gValue << 8) | (bValue);
						
						proc.set(xi, yi, intValue);
					}
				}
			}
		}
	}

	// assumes ds & imp are correct dimensions and not directly mapped
	public static void setDatasetGrayData(Dataset ds, ImagePlus imp) {
		RealType<?> type = ds.getType();
		double typeMin = type.getMinValue();
		double typeMax = type.getMaxValue();
		int x = imp.getWidth();
		int y = imp.getHeight();
		int zIndex = ds.getAxisIndex(Axes.Z);
		int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		int tIndex = ds.getAxisIndex(Axes.TIME);
		int z = (int) ( (zIndex < 0) ? 1 : ds.getImgPlus().dimension(zIndex) );
		int c = (int) ( (cIndex < 0) ? 1 : ds.getImgPlus().dimension(cIndex) );
		int t = (int) ( (tIndex < 0) ? 1 : ds.getImgPlus().dimension(tIndex) );
		int imagejPlaneNumber = 1;
		RandomAccess<? extends RealType<?>> accessor = ds.getImgPlus().randomAccess();
		for (int ti = 0; ti < t; ti++) {
			if (tIndex >= 0) accessor.setPosition(ti, tIndex);
			for (int zi = 0; zi < z; zi++) {
				if (zIndex >= 0) accessor.setPosition(zi, zIndex);
				for (int ci = 0; ci < c; ci++) {
					if (cIndex >= 0) accessor.setPosition(ci, cIndex);
					ImageProcessor proc = imp.getStack().getProcessor(imagejPlaneNumber++);
					for (int yi = 0; yi < y; yi++) {
						accessor.setPosition(yi, 1);
						for (int xi = 0; xi < x; xi++) {
							accessor.setPosition(xi, 0);
							double value = proc.getf(xi, yi);
							// NB - always clamp! a little unnecessary work sometimes
							if (value < typeMin) value = typeMin;
							if (value > typeMax) value = typeMax;
							accessor.get().setReal(value);
						}
					}
				}
			}
		}
		ds.update();
	}

	// assumes ds & imp are correct dimensions and not directly mapped
	public static void setDatasetColorData(Dataset ds, ImagePlus imp) {
		int x = imp.getWidth();
		int y = imp.getHeight();
		int zIndex = ds.getAxisIndex(Axes.Z);
		int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		int tIndex = ds.getAxisIndex(Axes.TIME);
		int z = (int) ( (zIndex < 0) ? 1 : ds.getImgPlus().dimension(zIndex) );
		int t = (int) ( (tIndex < 0) ? 1 : ds.getImgPlus().dimension(tIndex) );
		int imagejPlaneNumber = 1;
		RandomAccess<? extends RealType<?>> accessor = ds.getImgPlus().randomAccess();
		for (int ti = 0; ti < t; ti++) {
			if (tIndex >= 0) accessor.setPosition(ti, tIndex);
			for (int zi = 0; zi < z; zi++) {
				if (zIndex >= 0) accessor.setPosition(zi, zIndex);
				ImageProcessor proc = imp.getStack().getProcessor(imagejPlaneNumber++);
				for (int yi = 0; yi < y; yi++) {
					accessor.setPosition(yi, 1);
					for (int xi = 0; xi < x; xi++) {
						accessor.setPosition(xi, 0);
						int value = proc.get(xi, yi);
						int rValue = (value >> 16) & 0xff;
						int gValue = (value >> 8) & 0xff;
						int bValue = (value >> 0) & 0xff;
						accessor.setPosition(0, cIndex);
						accessor.get().setReal(rValue);
						accessor.setPosition(1, cIndex);
						accessor.get().setReal(gValue);
						accessor.setPosition(2, cIndex);
						accessor.get().setReal(bValue);
					}
				}
			}
		}
		ds.update();
	}
	
	public static void setImagePlusPlanes(Dataset ds, ImagePlus imp) {
		int[] dimIndices = new int[5];
		int[] dimValues = new int[5];
		getImagePlusDims(ds, dimIndices, dimValues);

		final int cIndex = dimIndices[2];
		final int zIndex = dimIndices[3];
		final int tIndex = dimIndices[4];
		final int cCount = cIndex < 0 ? 1 : dimValues[cIndex];
		final int zCount = zIndex < 0 ? 1 : dimValues[zIndex];
		final int tCount = tIndex < 0 ? 1 : dimValues[tIndex];

		ImageStack stack = imp.getStack();

		long[] dims = ds.getDims();
		final long[] planeDims = new long[dims.length - 2];
		for (int i = 0; i < planeDims.length; i++)
			planeDims[i] = dims[i + 2];
		final long[] planePos = new long[planeDims.length];

		for (int t = 0; t < tCount; t++) {
			if (tIndex >= 0) planePos[tIndex - 2] = t;
			for (int z = 0; z < zCount; z++) {
				if (zIndex >= 0) planePos[zIndex - 2] = z;
				for (int c = 0; c < cCount; c++) {
					if (cIndex >= 0) planePos[cIndex - 2] = c;
					// NB - getImagePlusDims() removes need to check planeNum range
					final int planeNum = (int) Index.indexNDto1D(planeDims, planePos);
					final Object plane = ds.getPlane(planeNum);
					if (plane == null) {
						Log.error(message("Couldn't extract plane from Dataset ", c, z, t));
					}
					stack.setPixels(plane, planeNum+1);
				}
			}
		}
	}
	
	public static void setDatasetPlanes(Dataset ds, ImagePlus imp) {
		final int c = imp.getNChannels();
		final int z = imp.getNSlices();
		final int t = imp.getNFrames();

		// copy planes by reference
		final long planeCount = c*z*t;
		for (int p = 0; p < planeCount; p++) {
			final Object plane = imp.getStack().getPixels(p + 1);
			if (plane == null) {
				Log.error("Could not extract plane from ImageStack: " + p);
			}
			ds.setPlane(p, plane);
		}
		// no need to call ds.update() - setPlane() tracks it
	}

	@SuppressWarnings({"rawtypes","unchecked"})
	// assumes the data type of the given Dataset is fine as is
	public static void reshapeDataset(Dataset ds, ImagePlus imp) {
		long[] newDims = ds.getDims();
		double[] cal = new double[newDims.length];
		ds.calibration(cal);
		int xIndex = ds.getAxisIndex(Axes.X);
		int yIndex = ds.getAxisIndex(Axes.Y);
		int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		int zIndex = ds.getAxisIndex(Axes.Z);
		int tIndex = ds.getAxisIndex(Axes.TIME);
		if (xIndex >= 0) newDims[xIndex] = imp.getWidth();
		if (yIndex >= 0) newDims[yIndex] = imp.getHeight();
		if (cIndex >= 0) {
			if (imp.getType() == ImagePlus.COLOR_RGB)
				newDims[cIndex] = 3;
			else
				newDims[cIndex] = imp.getNChannels();
		}
		if (zIndex >= 0) newDims[zIndex] = imp.getNSlices();
		if (tIndex >= 0) newDims[tIndex] = imp.getNFrames();
		ImgFactory factory = ds.getImgPlus().factory();
		Img<?> img = factory.create(newDims, ds.getType());
		ImgPlus<?> imgPlus = new ImgPlus(img, ds.getName(), ds.getAxes(), cal);
		ds.setImgPlus((ImgPlus<? extends RealType<?>>) imgPlus);
	}
	
	public static void setDatasetMetadata(Dataset ds, ImagePlus imp) {
		ds.setName(imp.getTitle());
		// copy calibration info where possible
		int xIndex = ds.getAxisIndex(Axes.X);
		int yIndex = ds.getAxisIndex(Axes.Y);
		int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		int zIndex = ds.getAxisIndex(Axes.Z);
		int tIndex = ds.getAxisIndex(Axes.TIME);
		Calibration cal = imp.getCalibration();
		if (xIndex >= 0)
			ds.setCalibration(cal.pixelWidth, xIndex);
		if (yIndex >= 0)
			ds.setCalibration(cal.pixelHeight, yIndex);
		if (cIndex >= 0)
			ds.setCalibration(1, cIndex);
		if (zIndex >= 0)
			ds.setCalibration(cal.pixelDepth, zIndex);
		if (tIndex >= 0)
			ds.setCalibration(cal.frameInterval, tIndex);
		// no need to ds.update() - these calls should track that themselves
	}
	
	public static void setImagePlusMetadata(Dataset ds, ImagePlus imp) {
		imp.setTitle(ds.getName());
		// copy calibration info where possible
		Calibration cal = imp.getCalibration();
		int xIndex = ds.getAxisIndex(Axes.X);
		int yIndex = ds.getAxisIndex(Axes.Y);
		int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		int zIndex = ds.getAxisIndex(Axes.Z);
		int tIndex = ds.getAxisIndex(Axes.TIME);
		if (xIndex >= 0)
			cal.pixelWidth = ds.calibration(xIndex);
		if (yIndex >= 0)
			cal.pixelHeight = ds.calibration(yIndex);
		if (cIndex >= 0) {
			// nothing to set on IJ1 side
		}
		if (zIndex >= 0)
			cal.pixelDepth = ds.calibration(zIndex);
		if (tIndex >= 0)
			cal.frameInterval = ds.calibration(tIndex);
	}

	public static boolean datasetIsIJ1Compatible(Dataset ds) {
		return ij1StorageCompatible(ds) && ij1TypeCompatible(ds);
	}
	
	public static boolean imagePlusIsNearestType(Dataset ds, ImagePlus imp) {
		int impType = imp.getType();
		
		if (impType == ImagePlus.COLOR_RGB) {
			if (isColorCompatible(ds))
				return true;
		}
		
		RealType<?> dsType = ds.getType();
		boolean isSigned = ds.isSigned();
		boolean isInteger = ds.isInteger();
		int bitsPerPix = dsType.getBitsPerPixel();

		if ((isSigned) && (!isInteger))
			return impType == ImagePlus.GRAY32;
		
		if ((!isSigned) && (isInteger) && (bitsPerPix <= 8))
			return impType == ImagePlus.GRAY8;
			
		if ((!isSigned) && (isInteger) && (bitsPerPix <= 16))
			return impType == ImagePlus.GRAY16;

		return impType == ImagePlus.GRAY32;
	}
	
	public static boolean hasNonIJ1Axes(Axis[] axes) {
		for (Axis axis : axes) {
			if (axis == Axes.X) continue;
			if (axis == Axes.Y) continue;
			if (axis == Axes.CHANNEL) continue;
			if (axis == Axes.Z) continue;
			if (axis == Axes.TIME) continue;
			return true;
		}
		return false;
	}

	// -- private helpers --

	private static boolean isColorCompatible(Dataset ds) {
		if ( ! ds.isRGBMerged() ) return false;
		if ( ! (ds.getType() instanceof UnsignedByteType) ) return false;
		int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		if (cIndex < 0) return false;
		if (ds.getImgPlus().dimension(cIndex) != 3) return false;
		return true;
	}

		private interface PlaneMaker {
		Object makePlane(int w, int h);
	}
	
	private static class BytePlaneMaker implements PlaneMaker {
		public BytePlaneMaker() {
			// nothing to do
		}
		@Override
		public Object makePlane(int w, int h) {
			return new byte[w * h];
		}
	}
	
	private static class ShortPlaneMaker implements PlaneMaker {
		public ShortPlaneMaker() {
			// nothing to do
		}
		@Override
		public Object makePlane(int w, int h) {
			return new short[w * h];
		}
	}
	
	private static class FloatPlaneMaker implements PlaneMaker {
		public FloatPlaneMaker() {
			// nothing to do
		}
		@Override
		public Object makePlane(int w, int h) {
			return new float[w * h];
		}
	}
	
	private static ImagePlus makeImagePlus(Dataset ds, PlaneMaker planeMaker) {

		int[] dimIndices = new int[5];
		int[] dimValues = new int[5];
		getImagePlusDims(ds, dimIndices, dimValues);
		
		final int cIndex = dimIndices[2];
		final int zIndex = dimIndices[3];
		final int tIndex = dimIndices[4];
		final int cCount = dimValues[2];
		final int zCount = dimValues[3];
		final int tCount = dimValues[4];
		
		final ImageStack stack = new ImageStack(dimValues[0], dimValues[1]);

		final long[] planeDims = new long[ds.getImgPlus().numDimensions()];
		for (int i = 0; i < planeDims.length; i++)
			planeDims[i] = ds.getImgPlus().dimension(i+2);
		final long[] planePos = new long[planeDims.length];

		for (long t = 0; t < tCount; t++) {
			if (tIndex >= 0) planePos[tIndex - 2] = t;
			for (long z = 0; z < zCount; z++) {
				if (zIndex >= 0) planePos[zIndex - 2] = z;
				for (long c = 0; c < cCount; c++) {
					if (cIndex >= 0) planePos[cIndex - 2] = c;
					Object plane = planeMaker.makePlane(dimValues[0], dimValues[1]);
					stack.addSlice(null, plane);
				}
			}
		}

		ImagePlus imp = new ImagePlus(ds.getName(), stack);
		
		imp.setDimensions(cCount, zCount, tCount);
		
		return imp;
	}
	
	private static PlaneMaker getPlaneMaker(Dataset ds) {
		boolean signed = ds.isSigned();
		int bitsPerPixel = ds.getType().getBitsPerPixel();
		if (!signed && bitsPerPixel <= 8)
			return new BytePlaneMaker();
		if (!signed && bitsPerPixel <= 16)
				return new ShortPlaneMaker();
		return new FloatPlaneMaker();
	}
	
	private static void getColorImagePlusDims(Dataset dataset,
		int[] outputIndices, int[] outputDims)
	{
		getImagePlusDims(dataset, outputIndices, outputDims);
		if (outputDims[2] != 3)
			throw new IllegalArgumentException("dataset must have three channels");
	}
	
	private static void getImagePlusDims(Dataset dataset,
		int[] outputIndices, int[] outputDims)
	{
		// make sure there are not any other axis types present
		Axis[] axes = dataset.getAxes();
		if (hasNonIJ1Axes(axes))
			throw new IllegalArgumentException(
				"Dataset has one or more axes that can not be classified as"+
				" X, Y, Z, C, or T");

		final long[] dims = dataset.getDims();

		// check width
		final int xIndex = dataset.getAxisIndex(Axes.X);
		if (xIndex != 0) {
			throw new IllegalArgumentException("Expected X as dimension #0");
		}
		if (dims[xIndex] > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Width out of range: " + dims[xIndex]);
		}

		// check height
		final int yIndex = dataset.getAxisIndex(Axes.Y);
		if (yIndex != 1) {
			throw new IllegalArgumentException("Expected Y as dimension #1");
		}
		if (dims[yIndex] > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Height out of range: " + dims[yIndex]);
		}

		if ((dims[xIndex]*dims[yIndex]) > Integer.MAX_VALUE)
			throw new IllegalArgumentException("plane size has too many elements (" +
				(dims[xIndex]*dims[yIndex])+") : max ("+Integer.MAX_VALUE+")");
		
		// check channels, slices and frames
		final int cIndex = dataset.getAxisIndex(Axes.CHANNEL);
		final int zIndex = dataset.getAxisIndex(Axes.Z);
		final int tIndex = dataset.getAxisIndex(Axes.TIME);
		final long cCount = cIndex < 0 ? 1 : dims[cIndex];
		final long zCount = zIndex < 0 ? 1 : dims[zIndex];
		final long tCount = tIndex < 0 ? 1 : dims[tIndex];
		if (cCount * zCount * tCount > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("too many planes (" +
				(cCount*zCount*tCount)+") : max ("+Integer.MAX_VALUE+")");
		}
		outputIndices[0] = xIndex;
		outputDims[0] = (int) dims[xIndex];
		outputIndices[1] = yIndex;
		outputDims[1] = (int) dims[yIndex];
		outputIndices[2] = cIndex;
		outputDims[2] = (int) dims[cIndex];
		outputIndices[3] = zIndex;
		outputDims[3] = (int) dims[zIndex];
		outputIndices[4] = tIndex;
		outputDims[4] = (int) dims[tIndex];
	}
	
	private static boolean ij1StorageCompatible(Dataset ds) {
		return ds.getImgPlus().getImg() instanceof PlanarAccess;
	}
	
	private static boolean ij1TypeCompatible(Dataset ds) {
		// TODO - rather than direct type comparisons should we reason
		// here on bitPerPix, sign. integer flags? Which is correct
		// both now and in the long run?
		RealType<?> type = ds.getType();
		if (type instanceof UnsignedByteType) return true;
		if (type instanceof UnsignedShortType) return true;
		if (type instanceof FloatType) return true;
		return false;
	}

	private static boolean isGray32(final ImagePlus imp) {
		final int type = imp.getType();
		return type == ImagePlus.GRAY32;
	}
	
	private static boolean isSigned(final ImagePlus imp) {
	  // TODO - ignores IJ1's support of signed 16 bit. OK?
		return isGray32(imp);
	}

	private static boolean isFloating(final ImagePlus imp) {
		return isGray32(imp);
	}

	private static String message(final String message, final long c, final long z,
		final long t)
	{
		return message + ": c=" + c + ", z=" + z + ", t=" + t;
	}

}
