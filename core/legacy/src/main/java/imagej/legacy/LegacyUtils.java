//
// LegacyUtils.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

// TODO - the methods are not entirely consistent on whose responsibility
// it is to ensure the input data is correct. Either make them all check
// or all not check. Since the methods are package access we should be
// able to relax error checking if needed.

package imagej.legacy;

import ij.CompositeImage;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.ImageWindow;
import ij.macro.Interpreter;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import ij.process.LUT;
import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.Extents;
import imagej.data.Position;
import imagej.display.ColorTables;
import imagej.display.DatasetView;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.display.DisplayView;
import imagej.util.Log;

import java.awt.image.IndexColorModel;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.RandomAccess;
import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable8;
import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.ImgPlus;
import net.imglib2.img.basictypeaccess.PlanarAccess;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

/**
 * Collection of static methods used by the various ImageTranslators and the
 * DatasetHarmonizer. Kept together so that bidirectional translation code is in
 * one place and thus more easily maintained.
 * <p>
 * Note the interfaces have package access to avoid their use outside the legacy
 * layer.
 * </p>
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
public final class LegacyUtils {

	// CTR FIXME - Class too big. Split into multiple classes.
	// Make useful methods public. Eliminate use of static where possible.

	private static Axis[] defaultAxes = new Axis[] { Axes.X, Axes.Y,
		Axes.CHANNEL, Axes.Z, Axes.TIME };

	// -- constructor --

	private LegacyUtils() {
		// utility class: do not instantiate
	}

	// -- Utility methods --

	/**
	 * Modifies IJ1 data structures so that there are no dangling references to an
	 * obsolete ImagePlus.
	 */
	public static void deleteImagePlus(final ImagePlus imp) {
		final ImagePlus currImagePlus = WindowManager.getCurrentImage();
		if (imp == currImagePlus) WindowManager.setTempCurrentImage(null);
		final ImageWindow ij1Window = imp.getWindow();
		if (ij1Window == null) Interpreter.removeBatchModeImage(imp);
		else {
			imp.changes = false;
			if (!ij1Window.isClosed())
				ij1Window.close();
		}
	}

	// -- package interface --

	static Axis[] getPreferredAxisOrder() {
		return defaultAxes;
	}

	/**
	 * Makes a planar {@link Dataset} whose dimensions match a given
	 * {@link ImagePlus}. Data is exactly the same as plane references are shared
	 * between the Dataset and the ImagePlus. Assumes it will never be called with
	 * any kind of color ImagePlus. Does not set metadata of Dataset.
	 */
	static Dataset makeExactDataset(final ImagePlus imp,
		final Axis[] preferredOrder)
	{
		final int x = imp.getWidth();
		final int y = imp.getHeight();
		final int c = imp.getNChannels();
		final int z = imp.getNSlices();
		final int t = imp.getNFrames();
		final int[] inputDims = new int[] { x, y, c, z, t };
		final Axis[] axes = orderedAxes(preferredOrder, inputDims);
		final long[] dims = orderedDims(axes, inputDims);
		final String name = imp.getTitle();
		final int bitsPerPixel = imp.getBitDepth();
		final boolean signed = isSigned(imp);
		final boolean floating = isFloating(imp);
		final Dataset ds =
			Dataset.create(dims, name, axes, bitsPerPixel, signed, floating);

		setDatasetPlanes(ds, imp);

		return ds;
	}

	/**
	 * Makes a gray {@link Dataset} from a gray {@link ImagePlus}. Assumes it will
	 * never be given a color RGB Imageplus. Does not populate the data of the
	 * returned Dataset. That is left to other utility methods. Does not set
	 * metadata of Dataset.
	 */
	static Dataset makeGrayDataset(final ImagePlus imp,
		final Axis[] preferredOrder)
	{

		final int x = imp.getWidth();
		final int y = imp.getHeight();
		final int c = imp.getNChannels();
		final int z = imp.getNSlices();
		final int t = imp.getNFrames();

		final int[] inputDims = new int[] { x, y, c, z, t };
		final Axis[] axes = orderedAxes(preferredOrder, inputDims);
		final long[] dims = orderedDims(axes, inputDims);
		final String name = imp.getTitle();
		final int bitsPerPixel = imp.getBitDepth();
		final boolean signed = isSigned(imp);
		final boolean floating = isFloating(imp);
		final Dataset ds =
			Dataset.create(dims, name, axes, bitsPerPixel, signed, floating);

		return ds;
	}

	/**
	 * Makes a gray {@link Dataset} from a Color {@link ImagePlus} whose channel
	 * count > 1. The Dataset will have isRgbMerged() false, 3 times as many
	 * channels as the input ImagePlus, and bitsperPixel == 8. Does not populate
	 * the data of the returned Dataset. That is left to other utility methods.
	 * Does not set metadata of Dataset. Throws exceptions if input ImagePlus is
	 * not multichannel RGB.
	 */
	static Dataset makeGrayDatasetFromColorImp(final ImagePlus imp,
		final Axis[] preferredOrder)
	{

		final int x = imp.getWidth();
		final int y = imp.getHeight();
		final int c = imp.getNChannels();
		final int z = imp.getNSlices();
		final int t = imp.getNFrames();

		if (imp.getType() != ImagePlus.COLOR_RGB) {
			throw new IllegalArgumentException("this method designed for "
				+ "creating a color Dataset from a multichannel RGB ImagePlus");
		}

		final int[] inputDims = new int[] { x, y, c * 3, z, t };
		final Axis[] axes = orderedAxes(preferredOrder, inputDims);
		final long[] dims = orderedDims(axes, inputDims);
		final String name = imp.getTitle();
		final int bitsPerPixel = 8;
		final boolean signed = false;
		final boolean floating = false;
		final Dataset ds =
			Dataset.create(dims, name, axes, bitsPerPixel, signed, floating);

		return ds;
	}

	/**
	 * Makes a color {@link Dataset} from an {@link ImagePlus}. Color Datasets
	 * have isRgbMerged() true, channels == 3, and bitsperPixel == 8. Does not
	 * populate the data of the returned Dataset. That is left to other utility
	 * methods. Does not set metadata of Dataset. Throws exceptions if input
	 * ImagePlus is not single channel RGB.
	 */
	static Dataset makeColorDataset(final ImagePlus imp,
		final Axis[] preferredOrder)
	{
		final int x = imp.getWidth();
		final int y = imp.getHeight();
		final int c = imp.getNChannels();
		final int z = imp.getNSlices();
		final int t = imp.getNFrames();

		if (c != 1) {
			throw new IllegalArgumentException(
				"can't make a color Dataset from a multichannel ColorProcessor stack");
		}

		if (imp.getType() != ImagePlus.COLOR_RGB) {
			throw new IllegalArgumentException(
				"can't make a color Dataset from a nonRGB ImagePlus");
		}

		final int[] inputDims = new int[] { x, y, c * 3, z, t };
		final Axis[] axes = orderedAxes(preferredOrder, inputDims);
		final long[] dims = orderedDims(axes, inputDims);
		final String name = imp.getTitle();
		final int bitsPerPixel = 8;
		final boolean signed = false;
		final boolean floating = false;
		final Dataset ds =
			Dataset.create(dims, name, axes, bitsPerPixel, signed, floating);

		ds.setRGBMerged(true);

		return ds;
	}

	/**
	 * Makes an {@link ImagePlus} from a {@link Dataset}. Data is exactly the same
	 * between them as planes of data are shared by reference. Assumes the Dataset
	 * can be represented via plane references (thus backled by
	 * {@link PlanarAccess} and in a type compatible with IJ1). Does not set the
	 * metadata of the ImagePlus. Throws an exception if the Dataset has any axis
	 * present that is not IJ1 compatible. Also throws an exception when Dataset
	 * has any axis or total number of planes > Integer.MAX_VALUE.
	 */
	// TODO - check that Dataset can be represented exactly
	static ImagePlus makeExactImagePlus(final Dataset ds) {
		final int[] dimIndices = new int[5];
		final int[] dimValues = new int[5];
		getImagePlusDims(ds, dimIndices, dimValues);
		assertXYPlanesCorrectlyOriented(dimIndices);

		final int c = dimValues[2];
		final int z = dimValues[3];
		final int t = dimValues[4];

		final ImagePlus imp = makeImagePlus(ds, getPlaneMaker(ds), true);

		setImagePlusPlanes(ds, imp);

		imp.setDimensions(c, z, t);

		return imp;
	}

	/**
	 * Makes an {@link ImagePlus} from a {@link Dataset} whose dimensions match.
	 * The type of the ImagePlus is an IJ1 type that can best represent the data
	 * with the least loss of data. Sometimes the IJ1 & IJ2 types are the same
	 * type and sometimes they are not. The data values and metadata are not
	 * assigned. Assumes it will never be sent a color Dataset.
	 */
	static ImagePlus makeNearestTypeGrayImagePlus(final Dataset ds) {
		final PlaneMaker planeMaker = getPlaneMaker(ds);
		return makeImagePlus(ds, planeMaker, false);
	}

	/**
	 * Makes a color {@link ImagePlus} from a color {@link Dataset}. The ImagePlus
	 * will have the same X, Y, Z, & T dimensions. C will be 1. The data values
	 * and metadata are not assigned. Throws an exception if the dataset is not
	 * color compatible. Throws an exception if the Dataset has any axis present
	 * that is not IJ1 compatible. Also throws an exception when Dataset has any
	 * axis or total number of planes > Integer.MAX_VALUE.
	 */
	static ImagePlus makeColorImagePlus(final Dataset ds) {
		if (!isColorCompatible(ds)) {
			throw new IllegalArgumentException("Dataset is not color compatible");
		}

		final int[] dimIndices = new int[5];
		final int[] dimValues = new int[5];
		getImagePlusDims(ds, dimIndices, dimValues);
		final int w = dimValues[0];
		final int h = dimValues[1];
		final int c = dimValues[2] / 3;
		final int z = dimValues[3];
		final int t = dimValues[4];

		final ImageStack stack = new ImageStack(w, h, c * z * t);

		for (int i = 0; i < c * z * t; i++)
			stack.setPixels(new int[w * h], i + 1);

		final ImagePlus imp = new ImagePlus(ds.getName(), stack);

		imp.setDimensions(c, z, t);

		return imp;
	}

	/**
	 * Assigns the data values of an {@link ImagePlus} from a paired
	 * {@link Dataset}. Assumes the Dataset and ImagePlus have the same dimensions
	 * and that the data planes are not directly mapped. If the data values are
	 * directly mapped then this code just wastes time. Sets values via
	 * {@link ImageProcessor}::setf(). Some special case code is in place to
	 * assure that BitType images go to IJ1 as 0/255 value images. Does not change
	 * the ImagePlus' metadata.
	 */
	static void setImagePlusGrayData(final Dataset ds, final ImagePlus imp) {
		final boolean bitData = ds.getType() instanceof BitType;
		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);
		final int x = imp.getWidth();
		final int y = imp.getHeight();
		final int c = imp.getNChannels();
		final int z = imp.getNSlices();
		final int t = imp.getNFrames();
		int imagejPlaneNumber = 1;
		final RandomAccess<? extends RealType<?>> accessor =
			ds.getImgPlus().randomAccess();
		for (int ti = 0; ti < t; ti++) {
			if (tIndex >= 0) accessor.setPosition(ti, tIndex);
			for (int zi = 0; zi < z; zi++) {
				if (zIndex >= 0) accessor.setPosition(zi, zIndex);
				for (int ci = 0; ci < c; ci++) {
					if (cIndex >= 0) accessor.setPosition(ci, cIndex);
					final ImageProcessor proc =
						imp.getStack().getProcessor(imagejPlaneNumber++);
					for (int yi = 0; yi < y; yi++) {
						accessor.setPosition(yi, yIndex);
						for (int xi = 0; xi < x; xi++) {
							accessor.setPosition(xi, xIndex);
							double value = accessor.get().getRealDouble();
							if (bitData) if (value > 0) value = 255;
							proc.setf(xi, yi, (float) value);
						}
					}
				}
			}
		}
	}

	/**
	 * Assigns the data values of a color {@link ImagePlus} from a paired
	 * {@link Dataset}. Assumes the Dataset and ImagePlus have the same dimensions
	 * and that the data planes are not directly mapped. Also assumes that the
	 * Dataset has isRGBMerged() true. Sets values via {@link ImageProcessor}
	 * ::set(). Does not change the ImagePlus' metadata.
	 */
	static void setImagePlusColorData(final Dataset ds, final ImagePlus imp) {
		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);
		final int x = imp.getWidth();
		final int y = imp.getHeight();
		final int c = imp.getNChannels();
		final int z = imp.getNSlices();
		final int t = imp.getNFrames();
		int imagejPlaneNumber = 1;
		final RandomAccess<? extends RealType<?>> accessor =
			ds.getImgPlus().randomAccess();
		for (int ti = 0; ti < t; ti++) {
			if (tIndex >= 0) accessor.setPosition(ti, tIndex);
			for (int zi = 0; zi < z; zi++) {
				if (zIndex >= 0) accessor.setPosition(zi, zIndex);
				for (int ci = 0; ci < c; ci++) {
					final ImageProcessor proc =
						imp.getStack().getProcessor(imagejPlaneNumber++);
					for (int yi = 0; yi < y; yi++) {
						accessor.setPosition(yi, yIndex);
						for (int xi = 0; xi < x; xi++) {
							accessor.setPosition(xi, xIndex);

							accessor.setPosition(3 * ci + 0, cIndex);
							final int rValue = ((int) accessor.get().getRealDouble()) & 0xff;

							accessor.setPosition(3 * ci + 1, cIndex);
							final int gValue = ((int) accessor.get().getRealDouble()) & 0xff;

							accessor.setPosition(3 * ci + 2, cIndex);
							final int bValue = ((int) accessor.get().getRealDouble()) & 0xff;

							final int intValue =
								(0xff << 24) | (rValue << 16) | (gValue << 8) | (bValue);

							proc.set(xi, yi, intValue);
						}
					}
				}
			}
		}
	}

	/**
	 * Assigns the data values of a {@link Dataset} from a paired
	 * {@link ImagePlus}. Assumes the Dataset and ImagePlus have the same
	 * dimensions and that the data planes are not directly mapped. If the data
	 * values are directly mapped then this code just wastes time. Gets values via
	 * {@link ImageProcessor}::getf(). In cases where there is a narrowing of
	 * types into IJ2 data is range clamped. Does not change the Dataset's
	 * metadata.
	 */
	static void setDatasetGrayData(final Dataset ds, final ImagePlus imp) {
		final RealType<?> type = ds.getType();
		final double typeMin = type.getMinValue();
		final double typeMax = type.getMaxValue();
		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);
		final int x = imp.getWidth();
		final int y = imp.getHeight();
		final int c = imp.getNChannels();
		final int z = imp.getNSlices();
		final int t = imp.getNFrames();
		int imagejPlaneNumber = 1;
		final RandomAccess<? extends RealType<?>> accessor =
			ds.getImgPlus().randomAccess();
		for (int ti = 0; ti < t; ti++) {
			if (tIndex >= 0) accessor.setPosition(ti, tIndex);
			for (int zi = 0; zi < z; zi++) {
				if (zIndex >= 0) accessor.setPosition(zi, zIndex);
				for (int ci = 0; ci < c; ci++) {
					if (cIndex >= 0) accessor.setPosition(ci, cIndex);
					final ImageProcessor proc =
						imp.getStack().getProcessor(imagejPlaneNumber++);
					for (int yi = 0; yi < y; yi++) {
						accessor.setPosition(yi, yIndex);
						for (int xi = 0; xi < x; xi++) {
							accessor.setPosition(xi, xIndex);
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

	/**
	 * Assigns the data values of a color {@link Dataset} from a paired
	 * {@link ImagePlus}. Assumes the Dataset and ImagePlus have the same
	 * dimensions and are both of type color. Gets values via
	 * {@link ImageProcessor}::get(). Does not change the Dataset's metadata.
	 */
	static void setDatasetColorData(final Dataset ds, final ImagePlus imp) {
		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);
		final int x = imp.getWidth();
		final int y = imp.getHeight();
		final int c = imp.getNChannels();
		final int z = imp.getNSlices();
		final int t = imp.getNFrames();
		int imagejPlaneNumber = 1;
		final RandomAccess<? extends RealType<?>> accessor =
			ds.getImgPlus().randomAccess();
		for (int ti = 0; ti < t; ti++) {
			if (tIndex >= 0) accessor.setPosition(ti, tIndex);
			for (int zi = 0; zi < z; zi++) {
				if (zIndex >= 0) accessor.setPosition(zi, zIndex);
				for (int ci = 0; ci < c; ci++) {
					final ImageProcessor proc =
						imp.getStack().getProcessor(imagejPlaneNumber++);
					for (int yi = 0; yi < y; yi++) {
						accessor.setPosition(yi, yIndex);
						for (int xi = 0; xi < x; xi++) {
							accessor.setPosition(xi, xIndex);
							final int value = proc.get(xi, yi);
							final int rValue = (value >> 16) & 0xff;
							final int gValue = (value >> 8) & 0xff;
							final int bValue = (value >> 0) & 0xff;
							accessor.setPosition(ci * 3 + 0, cIndex);
							accessor.get().setReal(rValue);
							accessor.setPosition(ci * 3 + 1, cIndex);
							accessor.get().setReal(gValue);
							accessor.setPosition(ci * 3 + 2, cIndex);
							accessor.get().setReal(bValue);
						}
					}
				}
			}
		}
		ds.update();
	}

	/**
	 * Assigns the data values of a gray {@link Dataset} from a paired
	 * multichannel color {@link ImagePlus}. Assumes the Dataset and ImagePlus
	 * have the same dimensions. Gets values via {@link ImageProcessor}::get().
	 * Does not change the Dataset's metadata.
	 */
	static void setDatasetGrayDataFromColorImp(final Dataset ds,
		final ImagePlus imp)
	{
		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);
		final int x = imp.getWidth();
		final int y = imp.getHeight();
		final int c = imp.getNChannels();
		final int z = imp.getNSlices();
		final int t = imp.getNFrames();
		int imagejPlaneNumber = 1;
		final RandomAccess<? extends RealType<?>> accessor =
			ds.getImgPlus().randomAccess();
		for (int ti = 0; ti < t; ti++) {
			if (tIndex >= 0) accessor.setPosition(ti, tIndex);
			for (int zi = 0; zi < z; zi++) {
				if (zIndex >= 0) accessor.setPosition(zi, zIndex);
				for (int ci = 0; ci < c; ci++) {
					final ImageProcessor proc =
						imp.getStack().getProcessor(imagejPlaneNumber++);
					for (int yi = 0; yi < y; yi++) {
						accessor.setPosition(yi, yIndex);
						for (int xi = 0; xi < x; xi++) {
							accessor.setPosition(xi, xIndex);
							final int value = proc.get(xi, yi);
							final int rValue = (value >> 16) & 0xff;
							final int gValue = (value >> 8) & 0xff;
							final int bValue = (value >> 0) & 0xff;
							accessor.setPosition(ci * 3 + 0, cIndex);
							accessor.get().setReal(rValue);
							accessor.setPosition(ci * 3 + 1, cIndex);
							accessor.get().setReal(gValue);
							accessor.setPosition(ci * 3 + 2, cIndex);
							accessor.get().setReal(bValue);
						}
					}
				}
			}
		}
		ds.update();
	}

	/**
	 * Assigns the plane references of an {@link ImagePlus}' {@link ImageStack} to
	 * match those of a given {@link Dataset}. Assumes input Dataset and ImagePlus
	 * match in dimensions and backing type. Throws an exception if the Dataset
	 * has any axis present that is not IJ1 compatible. Also throws an exception
	 * when Dataset has any axis or total number of planes > Integer.MAX_VALUE.
	 */
	static void setImagePlusPlanes(final Dataset ds, final ImagePlus imp) {
		final int[] dimIndices = new int[5];
		final int[] dimValues = new int[5];
		getImagePlusDims(ds, dimIndices, dimValues);
		assertXYPlanesCorrectlyOriented(dimIndices);

		final int cIndex = dimIndices[2];
		final int zIndex = dimIndices[3];
		final int tIndex = dimIndices[4];

		final int cCount = dimValues[2];
		final int zCount = dimValues[3];
		final int tCount = dimValues[4];

		final ImageStack stack = imp.getStack();

		final long[] fullDims = ds.getDims();
		final long[] planeDims = new long[fullDims.length-2];
		for (int i = 0; i < planeDims.length; i++)
			planeDims[i] = fullDims[i+2];
		final Extents extents = new Extents(planeDims);
		final Position planePos = extents.createPosition();

		for (int t = 0; t < tCount; t++) {
			if (tIndex >= 0) planePos.setPosition(t, tIndex-2);
			for (int z = 0; z < zCount; z++) {
				if (zIndex >= 0) planePos.setPosition(z, zIndex-2);
				for (int c = 0; c < cCount; c++) {
					if (cIndex >= 0) planePos.setPosition(c, cIndex-2);
					final int planeNum = (int) planePos.getIndex();
					final Object plane = ds.getPlane(planeNum, false);
					if (plane == null) {
						Log
							.error(message("Couldn't extract plane from Dataset ", c, z, t));
					}
					final int stackPosition = t * zCount * cCount + z * cCount + c + 1;
					stack.setPixels(plane, stackPosition);
				}
			}
		}
	}

	/**
	 * Assigns a planar {@link Dataset}'s plane references to match those of a
	 * given {@link ImagePlus}. Assumes input Dataset and ImagePlus match in
	 * dimensions and backing type.
	 */
	static void setDatasetPlanes(final Dataset ds, final ImagePlus imp) {
		final int c = imp.getNChannels();
		final int z = imp.getNSlices();
		final int t = imp.getNFrames();

		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);

		final long[] fullDims = ds.getDims();
		final long[] planeDims = new long[fullDims.length-2];
		for (int i = 0; i < planeDims.length; i++)
			planeDims[i] = fullDims[i+2];
		final Position planePos = new Extents(planeDims).createPosition();

		// copy planes by reference
		int p = 1;
		for (int ti = 0; ti < t; ti++) {
			if (tIndex >= 0) planePos.setPosition(ti, tIndex-2);
			for (int zi = 0; zi < z; zi++) {
				if (zIndex >= 0) planePos.setPosition(zi, zIndex-2);
				for (int ci = 0; ci < c; ci++) {
					if (cIndex >= 0) planePos.setPosition(ci, cIndex-2);
					final Object plane = imp.getStack().getPixels(p++);
					if (plane == null) {
						Log.error("Could not extract plane from ImageStack: " + (p - 1));
					}
					final int planeNum = (int) planePos.getIndex();
					ds.setPlane(planeNum, plane);
				}
			}
		}
		// no need to call ds.update() - setPlane() tracks it
	}

	/**
	 * Changes the shape of an existing {@link Dataset} to match that of an
	 * {@link ImagePlus}. Assumes that the Dataset type is correct. Does not set
	 * the data values or change the metadata.
	 */
	// assumes the data type of the given Dataset is fine as is
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static void reshapeDataset(final Dataset ds, final ImagePlus imp) {
		final long[] newDims = ds.getDims();
		final double[] cal = new double[newDims.length];
		ds.calibration(cal);
		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);
		if (xIndex >= 0) newDims[xIndex] = imp.getWidth();
		if (yIndex >= 0) newDims[yIndex] = imp.getHeight();
		if (cIndex >= 0) {
			if (imp.getType() == ImagePlus.COLOR_RGB) {
				newDims[cIndex] = 3 * imp.getNChannels();
			}
			else newDims[cIndex] = imp.getNChannels();
		}
		if (zIndex >= 0) newDims[zIndex] = imp.getNSlices();
		if (tIndex >= 0) newDims[tIndex] = imp.getNFrames();
		final ImgFactory factory = ds.getImgPlus().factory();
		final Img<?> img = factory.create(newDims, ds.getType());
		final ImgPlus<?> imgPlus =
			new ImgPlus(img, ds.getName(), ds.getAxes(), cal);
		ds.setImgPlus((ImgPlus<? extends RealType<?>>) imgPlus);
	}

	/** Sets a {@link Dataset}'s metadata to match a given {@link ImagePlus}. */
	static void setDatasetMetadata(final Dataset ds, final ImagePlus imp) {
		ds.setName(imp.getTitle());
		// copy calibration info where possible
		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);
		final Calibration cal = imp.getCalibration();
		if (xIndex >= 0) ds.setCalibration(cal.pixelWidth, xIndex);
		if (yIndex >= 0) ds.setCalibration(cal.pixelHeight, yIndex);
		if (cIndex >= 0) ds.setCalibration(1, cIndex);
		if (zIndex >= 0) ds.setCalibration(cal.pixelDepth, zIndex);
		if (tIndex >= 0) ds.setCalibration(cal.frameInterval, tIndex);
		// no need to ds.update() - these calls should track that themselves
	}

	/** Sets an {@link ImagePlus}' metadata to match a given {@link Dataset}. */
	static void setImagePlusMetadata(final Dataset ds, final ImagePlus imp) {
		imp.setTitle(ds.getName());
		// copy calibration info where possible
		final Calibration cal = imp.getCalibration();
		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);
		if (xIndex >= 0) cal.pixelWidth = ds.calibration(xIndex);
		if (yIndex >= 0) cal.pixelHeight = ds.calibration(yIndex);
		if (cIndex >= 0) {
			// nothing to set on IJ1 side
		}
		if (zIndex >= 0) cal.pixelDepth = ds.calibration(zIndex);
		if (tIndex >= 0) cal.frameInterval = ds.calibration(tIndex);
	}

	/**
	 * Sets the ColorTables of the active view of an IJ2 Display from the LUTs
	 * of a given ImagePlus or CompositeImage.
	 */
	static void setDisplayLuts(final Display disp, final ImagePlus imp) {
		final boolean sixteenBitLuts = imp.getType() == ImagePlus.GRAY16;
		final List<ColorTable<?>> colorTables = colorTablesFromImagePlus(imp);
		assignColorTables(disp, colorTables, sixteenBitLuts);
	}

	/**
	 * Sets LUTs of an ImagePlus or CompositeImage. If given an ImagePlus this
	 * method sets it's single LUT from the first ColorTable of the active
	 * Dataset of the given Display.  If given a CompositeImage this method sets
	 * all it's LUTs from the ColorTables of the active view of the given Display.
	 * If there is no such view the LUTs are assigned with default values.
	 */
	static void setImagePlusLuts(final Display disp, final ImagePlus imp) {
		if (imp instanceof CompositeImage) {
			final CompositeImage ci = (CompositeImage) imp;
			DisplayView activeView = disp.getActiveView(); 
			if (activeView == null)
				setCompositeImageLutsToDefault(ci);
			else {
				final DatasetView view = (DatasetView) activeView;
				setCompositeImageLuts(ci, view.getColorTables());
			}
		}
		else { // regular ImagePlus
			final DisplayService displayService = ImageJ.get(DisplayService.class);
			final Dataset ds = displayService.getActiveDataset(disp);
			setImagePlusLutToFirstInDataset(ds, imp);
		}
	}

	/**
	 * Returns true if a {@link Dataset} can be represented by reference in IJ1.
	 */
	static boolean datasetIsIJ1Compatible(final Dataset ds) {
		return ij1StorageCompatible(ds) && ij1TypeCompatible(ds);
	}

	/**
	 * Returns true if an {@link ImagePlus}' type is the best fit for a given
	 * {@link Dataset}. Best fit means the IJ1 type that is the best at preserving
	 * data.
	 */
	static boolean imagePlusIsNearestType(final Dataset ds, final ImagePlus imp)
	{
		final int impType = imp.getType();

		if (impType == ImagePlus.COLOR_RGB) {
			if (isColorCompatible(ds)) return true;
		}

		final RealType<?> dsType = ds.getType();
		final boolean isSigned = ds.isSigned();
		final boolean isInteger = ds.isInteger();
		final int bitsPerPix = dsType.getBitsPerPixel();

		if (!isSigned && isInteger && bitsPerPix <= 8) {
			return impType == ImagePlus.GRAY8 || impType == ImagePlus.COLOR_256;
		}

		if (!isSigned && isInteger && bitsPerPix <= 16) {
			return impType == ImagePlus.GRAY16;
		}

		// isSigned && !isInteger
		return impType == ImagePlus.GRAY32;
	}

	/**
	 * Returns true if any of the given Axes cannot be represented in an IJ1
	 * ImagePlus.
	 */
	public static boolean hasNonIJ1Axes(final Axis[] axes) {
		for (final Axis axis : axes) {
			if (axis == Axes.X) continue;
			if (axis == Axes.Y) continue;
			if (axis == Axes.CHANNEL) continue;
			if (axis == Axes.Z) continue;
			if (axis == Axes.TIME) continue;
			return true;
		}
		return false;
	}

	/**
	 * Sets the {@link Dataset}'s number of composite channels to display
	 * simultaneously based on an input {@link ImagePlus}'s makeup.
	 */
	static void setDatasetCompositeVariables(final Dataset ds,
		final ImagePlus imp)
	{
		if (imp instanceof CompositeImage &&
			((CompositeImage) imp).getMode() == CompositeImage.COMPOSITE)
		{
			ds.setCompositeChannelCount(imp.getNChannels());
		}
		else if (imp.getType() == ImagePlus.COLOR_RGB && imp.getNChannels() == 1) {
			ds.setCompositeChannelCount(3);
		}
		else ds.setCompositeChannelCount(1);
	}

	/**
	 * Makes a {@link CompositeImage} that wraps a given {@link ImagePlus} and
	 * sets channel LUTs to match how IJ2 displays given paired {@link Dataset}.
	 * Assumes given ImagePlus has channels in range 2..7 and that if Dataset View
	 * has ColorTables defined there is one per channel.
	 */
	// TODO - last assumption may be bad. If I have a 6 channel Dataset with
	// compos chann count == 2 then maybe I only have 2 or 3 ColorTables. Is
	// this configuration even valid. If so then what to do for translation?
	static CompositeImage makeCompositeImage(final ImagePlus imp) {
		return new CompositeImage(imp, CompositeImage.COMPOSITE);
	}

	// -- private helpers --

	/**
	 * Gets a dimension for a given axis from a list of dimensions in XTCZT order.
	 */
	private static int getDim(final Axis axis, final int[] fullDimensions) {
		if (axis == Axes.X) return fullDimensions[0];
		else if (axis == Axes.Y) return fullDimensions[1];
		else if (axis == Axes.CHANNEL) return fullDimensions[2];
		else if (axis == Axes.Z) return fullDimensions[3];
		else if (axis == Axes.TIME) return fullDimensions[4];
		else throw new IllegalArgumentException(
			"incompatible dimension type specified");
	}

	/**
	 * Makes a set of axes in a preferred order. The preferred order may not
	 * include all 5 default axes. This method always returns axes populated with
	 * X, Y, and any other nontrivial dimensions. Output axes are filled in the
	 * preferred order and then unspecified axes of nontrivial dimension are
	 * concatenated in default order
	 */
	private static Axis[] orderedAxes(final Axis[] preferredOrder,
		final int[] fullDimensions)
	{
		int dimCount = 0;
		for (int i = 0; i < fullDimensions.length; i++) {
			if (defaultAxes[i] == Axes.X || defaultAxes[i] == Axes.Y ||
//				defaultAxes[i] == Axes.CHANNEL ||
				getDim(defaultAxes[i], fullDimensions) > 1)
			{
				dimCount++;
			}
		}
		final Axis[] axes = new Axis[dimCount];
		int index = 0;
		for (final Axis axis : preferredOrder) {
			for (final Axis other : defaultAxes) {
				if (axis == other) {
					if (axis == Axes.X || axis == Axes.Y ||
//						axis == Axes.CHANNEL ||
						getDim(axis, fullDimensions) > 1)
					{
						axes[index++] = axis;
					}
					break;
				}
			}
		}
		for (final Axis axis : defaultAxes) {
			boolean present = false;
			for (final Axis other : preferredOrder) {
				if (axis == other) {
					present = true;
					break;
				}
			}
			if (!present) {
				if (axis == Axes.X || axis == Axes.Y ||
//					axis == Axes.CHANNEL ||
					getDim(axis, fullDimensions) > 1)
				{
					axes[index++] = axis;
				}
			}
		}
		return axes;
	}

	/**
	 * makes a set of dimensions in a given Axis order. Assumes that all
	 * nontrivial dimensions have already been prescreened to be included
	 */
	private static long[] orderedDims(final Axis[] axes,
		final int[] fullDimensions)
	{
		final long[] orderedDims = new long[axes.length];
		int index = 0;
		for (final Axis axis : axes) {
			orderedDims[index++] = getDim(axis, fullDimensions);
		}
		return orderedDims;
	}

	/**
	 * tests that a given {@link Dataset} can be represented as a color
	 * {@link ImagePlus}. Some of this test maybe overkill if by definition
	 * rgbMerged can only be true if channels == 3 and type = ubyte are also true.
	 */
	private static boolean isColorCompatible(final Dataset ds) {
		if (!ds.isRGBMerged()) return false;
		if (!ds.isInteger()) return false;
		if (ds.isSigned()) return false;
		if (ds.getType().getBitsPerPixel() != 8) return false;
		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		if (cIndex < 0) return false;
		if (ds.getImgPlus().dimension(cIndex) % 3 != 0) return false;
		return true;
	}

	/**
	 * Makes an {@link ImagePlus} that matches dimensions of a {@link Dataset}.
	 * The data values of the ImagePlus to be populated later elsewhere. Throws an
	 * exception if the Dataset has any axis present that is not IJ1 compatible.
	 * Also throws an exception when Dataset has any axis or total number of
	 * planes > Integer.MAX_VALUE.
	 * 
	 * @param ds - input Dataset to be shape compatible with
	 * @param planeMaker - a PlaneMaker to use to make type correct image planes
	 * @param makeDummyPlanes - save memory by allocating the minimum number of
	 *          planes for the case that we'll be reassigning the planes
	 *          immediately.
	 * @return an ImagePlus whose dimensions math the input Dataset
	 */
	private static ImagePlus makeImagePlus(final Dataset ds,
		final PlaneMaker planeMaker, final boolean makeDummyPlanes)
	{

		final int[] dimIndices = new int[5];
		final int[] dimValues = new int[5];
		getImagePlusDims(ds, dimIndices, dimValues);

		final int cCount = dimValues[2];
		final int zCount = dimValues[3];
		final int tCount = dimValues[4];

		final ImageStack stack = new ImageStack(dimValues[0], dimValues[1]);

		final long[] planeDims = new long[ds.getImgPlus().numDimensions()];
		for (int i = 0; i < planeDims.length; i++)
			planeDims[i] = ds.getImgPlus().dimension(i + 2);

		Object dummyPlane = null;
		for (long t = 0; t < tCount; t++) {
			for (long z = 0; z < zCount; z++) {
				for (long c = 0; c < cCount; c++) {
					Object plane;
					if (makeDummyPlanes) {
						if (dummyPlane == null) {
							dummyPlane = planeMaker.makePlane(dimValues[0], dimValues[1]);
						}
						plane = dummyPlane;
					}
					else plane = planeMaker.makePlane(dimValues[0], dimValues[1]);
					stack.addSlice(null, plane);
				}
			}
		}

		final ImagePlus imp = new ImagePlus(ds.getName(), stack);

		imp.setDimensions(cCount, zCount, tCount);

		return imp;
	}

	/**
	 * Finds the best {@link PlaneMaker} for a given {@link Dataset}. The best
	 * PlaneMaker is the one that makes planes in the type that can best represent
	 * the Dataset's data values in IJ1.
	 */
	private static PlaneMaker getPlaneMaker(final Dataset ds) {
		final boolean signed = ds.isSigned();
		final boolean integer = ds.isInteger();
		final int bitsPerPixel = ds.getType().getBitsPerPixel();
		if (!signed && integer && bitsPerPixel <= 8) return new BytePlaneMaker();
		if (!signed && integer && bitsPerPixel <= 16) return new ShortPlaneMaker();
		return new FloatPlaneMaker();
	}

	/**
	 * Copies a {@link Dataset}'s dimensions and axis indices into provided
	 * arrays. The order of dimensions is formatted to be X,Y,C,Z,T. If an axis is
	 * not present in the Dataset its value is set to 1 and its index is set to
	 * -1. Throws an exception if the Dataset has any axis present that is not IJ1
	 * compatible. Also throws an exception when Dataset has any axis or total
	 * number of planes > Integer.MAX_VALUE.
	 */
	private static void getImagePlusDims(final Dataset dataset,
		final int[] outputIndices, final int[] outputDims)
	{
		// make sure there are not any other axis types present
		final Axis[] axes = dataset.getAxes();
		if (hasNonIJ1Axes(axes)) {
			throw new IllegalArgumentException("Dataset has one or more axes "
				+ "that can not be classified as X, Y, Z, C, or T");
		}

		final long[] dims = dataset.getDims();

		// get axis indices
		final int xIndex = dataset.getAxisIndex(Axes.X);
		final int yIndex = dataset.getAxisIndex(Axes.Y);
		final int cIndex = dataset.getAxisIndex(Axes.CHANNEL);
		final int zIndex = dataset.getAxisIndex(Axes.Z);
		final int tIndex = dataset.getAxisIndex(Axes.TIME);

		final long xCount = xIndex < 0 ? 1 : dims[xIndex];
		final long yCount = yIndex < 0 ? 1 : dims[yIndex];
		final long cCount = cIndex < 0 ? 1 : dims[cIndex];
		final long zCount = zIndex < 0 ? 1 : dims[zIndex];
		final long tCount = tIndex < 0 ? 1 : dims[tIndex];

		// check width
		if (xIndex < 0) throw new IllegalArgumentException("missing X axis");
		if (xCount > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Width out of range: " + xCount);
		}

		// check height
		if (yIndex < 0) throw new IllegalArgumentException("missing Y axis");
		if (yCount > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Height out of range: " + yCount);
		}

		// check plane size
		if ((xCount * yCount) > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("too many elements per plane: " +
				"value(" + (xCount * yCount) + ") : max (" + Integer.MAX_VALUE + ")");
		}

		// check total channels, slices and frames not too large

		if (cCount * zCount * tCount > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("too many planes : value(" +
				(cCount * zCount * tCount) + ") : max (" + Integer.MAX_VALUE + ")");
		}

		// set output values : indices
		outputIndices[0] = xIndex;
		outputIndices[1] = yIndex;
		outputIndices[2] = cIndex;
		outputIndices[3] = zIndex;
		outputIndices[4] = tIndex;

		// set output values : dimensions
		outputDims[0] = (int) xCount;
		outputDims[1] = (int) yCount;
		outputDims[2] = (int) cCount;
		outputDims[3] = (int) zCount;
		outputDims[4] = (int) tCount;
	}

	/**
	 * Throws an Exception if the planes of a Dataset are not compatible with IJ1.
	 */
	private static void assertXYPlanesCorrectlyOriented(final int[] dimIndices) {
		if (dimIndices[0] != 0) {
			throw new IllegalArgumentException(
				"Dataset does not have X as the first axis");
		}
		if (dimIndices[1] != 1) {
			throw new IllegalArgumentException(
				"Dataset does not have Y as the second axis");
		}
	}

	/** Returns true if a {@link Dataset} is backed by {@link PlanarAccess}. */
	private static boolean ij1StorageCompatible(final Dataset ds) {
		return ds.getImgPlus().getImg() instanceof PlanarAccess;
	}

	/**
	 * Returns true if a {@link Dataset} has a type that can be directly
	 * represented in an IJ1 ImagePlus.
	 */
	private static boolean ij1TypeCompatible(final Dataset ds) {
		final RealType<?> type = ds.getType();
		final int bitsPerPix = type.getBitsPerPixel();
		final boolean integer = ds.isInteger();
		final boolean signed = ds.isSigned();

		Object plane;
		if ((bitsPerPix == 8) && !signed && integer) {
			plane = ds.getPlane(0, false);
			if (plane != null && plane instanceof byte[]) return true;
		}
		else if ((bitsPerPix == 16) && !signed && integer) {
			plane = ds.getPlane(0, false);
			if (plane != null && plane instanceof short[]) return true;
		}
		else if ((bitsPerPix == 32) && signed && !integer) {
			plane = ds.getPlane(0, false);
			if (plane != null && plane instanceof float[]) return true;
		}
		return false;
	}

	/** Returns true if an {@link ImagePlus} is of type GRAY32. */
	private static boolean isGray32(final ImagePlus imp) {
		final int type = imp.getType();
		return type == ImagePlus.GRAY32;
	}

	/** Returns true if an {@link ImagePlus} is backed by a signed type. */
	private static boolean isSigned(final ImagePlus imp) {
		// TODO - ignores IJ1's support of signed 16 bit. OK?
		return isGray32(imp);
	}

	/** Returns true if an {@link ImagePlus} is backed by a floating type. */
	private static boolean isFloating(final ImagePlus imp) {
		return isGray32(imp);
	}

	/** Formats an error message. */
	private static String message(final String message, final long c,
		final long z, final long t)
	{
		return message + ": c=" + c + ", z=" + z + ", t=" + t;
	}

	/** Assigns the color tables of the active view of a Display. */
	private static void assignColorTables(final Display disp,
		final List<ColorTable<?>> colorTables,
		@SuppressWarnings("unused") final boolean sixteenBitLuts)
	{
		// FIXME HACK
		// Grab the active view of the given Display and set it's default channel
		// luts. When we allow multiple views of a Dataset this will break. We
		// avoid setting a Dataset's per plane LUTs because it would be expensive
		// and also IJ1 LUTs are not model space constructs but rather view space
		// constructs.
		final DisplayView dispView = disp.getActiveView();
		if (dispView == null) return;
		final DatasetView dsView = (DatasetView) dispView;
		for (int i = 0; i < colorTables.size(); i++)
			dsView.setColorTable((ColorTable8) colorTables.get(i), i);
	}

	/** Creates a list of ColorTables from an ImagePlus. */
	private static List<ColorTable<?>> colorTablesFromImagePlus(
		final ImagePlus imp)
	{
		final List<ColorTable<?>> colorTables = new ArrayList<ColorTable<?>>();
		final LUT[] luts = imp.getLuts();
		if (luts == null) { // not a CompositeImage
			if (imp.getType() == ImagePlus.COLOR_RGB) {
				for (int i = 0; i < imp.getNChannels(); i++) {
					colorTables.add(ColorTables.RED);
					colorTables.add(ColorTables.GREEN);
					colorTables.add(ColorTables.BLUE);
				}
			}
			else { // not a direct color model image
				final IndexColorModel icm =
					(IndexColorModel) imp.getProcessor().getColorModel();
				ColorTable<?> cTable;
//				if (icm.getPixelSize() == 16) // is 16 bit table
//					cTable = make16BitColorTable(icm);
//				else // 8 bit color table
				cTable = make8BitColorTable(icm);
				colorTables.add(cTable);
			}
		}
		else { // we have multiple LUTs from a CompositeImage, 1 per channel
			ColorTable<?> cTable;
			for (int i = 0; i < luts.length; i++) {
//				if (luts[i].getPixelSize() == 16) // is 16 bit table
//					cTable = make16BitColorTable(luts[i]);
//				else // 8 bit color table
				cTable = make8BitColorTable(luts[i]);
				colorTables.add(cTable);
			}
		}

		return colorTables;
	}

	/**
	 * Makes a ColorTable8 from an IndexColorModel. Note that IJ1 LUT's are a kind
	 * of IndexColorModel.
	 */
	private static ColorTable8 make8BitColorTable(final IndexColorModel icm) {
		final byte[] reds = new byte[256];
		final byte[] greens = new byte[256];
		final byte[] blues = new byte[256];
		icm.getReds(reds);
		icm.getGreens(greens);
		icm.getBlues(blues);
		return new ColorTable8(reds, greens, blues);
	}

	/** Makes an 8-bit LUT from a ColorTable8. */
	private static LUT make8BitLut(final ColorTable8 cTable) {
		final byte[] reds = new byte[256];
		final byte[] greens = new byte[256];
		final byte[] blues = new byte[256];

		for (int i = 0; i < 256; i++) {
			reds[i] = (byte) cTable.get(0, i);
			greens[i] = (byte) cTable.get(1, i);
			blues[i] = (byte) cTable.get(2, i);
		}
		return new LUT(reds, greens, blues);
	}

	/**
	 * For each channel in CompositeImage, sets LUT to one from default
	 * progression
	 */
	private static void setCompositeImageLutsToDefault(final CompositeImage ci) {
		for (int i = 0; i < ci.getNChannels(); i++) {
			final ColorTable8 cTable = ColorTables.getDefaultColorTable(i);
			final LUT lut = make8BitLut(cTable);
			ci.setChannelLut(lut, i + 1);
		}
	}

	/**
	 * For each channel in CompositeImage, sets LUT to one from a given
	 * ColorTables
	 */
	private static void setCompositeImageLuts(final CompositeImage ci,
		final List<ColorTable8> cTables)
	{
		if (cTables == null || cTables.size() == 0) {
			setCompositeImageLutsToDefault(ci);
			ci.setMode(CompositeImage.COMPOSITE);
		}
		else {
			boolean allGreyLuts = true;
			for (int i = 0; i < ci.getNChannels(); i++) {
				final ColorTable8 cTable = cTables.get(i);
				if (cTable != ColorTables.GRAYS) allGreyLuts = false;
				final LUT lut = make8BitLut(cTable);
				ci.setChannelLut(lut, i + 1);
			}
			if (allGreyLuts) ci.setMode(CompositeImage.GRAYSCALE);
			else ci.setMode(CompositeImage.COLOR);
		}
	}

	/** Sets the single LUT of an ImagePlus to the first ColorTable of a Dataset */
	private static void setImagePlusLutToFirstInDataset(final Dataset ds,
		final ImagePlus imp)
	{
		ColorTable8 cTable = ds.getColorTable8(0);
		if (cTable == null) cTable = ColorTables.GRAYS;
		final LUT lut = make8BitLut(cTable);
		imp.getProcessor().setColorModel(lut);
		// or imp.getStack().setColorModel(lut);
	}

	// -- helper classes --

	/** Helper class to simplify the making of planes of different type data. */
	private interface PlaneMaker {

		Object makePlane(int w, int h);
	}

	/** Makes planes of bytes given width & height. */
	private static class BytePlaneMaker implements PlaneMaker {

		public BytePlaneMaker() {
			// nothing to do
		}

		@Override
		public Object makePlane(final int w, final int h) {
			return new byte[w * h];
		}
	}

	/** Makes planes of shorts given width & height. */
	private static class ShortPlaneMaker implements PlaneMaker {

		public ShortPlaneMaker() {
			// nothing to do
		}

		@Override
		public Object makePlane(final int w, final int h) {
			return new short[w * h];
		}
	}

	/** Makes planes of floats given width & height. */
	private static class FloatPlaneMaker implements PlaneMaker {

		public FloatPlaneMaker() {
			// nothing to do
		}

		@Override
		public Object makePlane(final int w, final int h) {
			return new float[w * h];
		}
	}

}
