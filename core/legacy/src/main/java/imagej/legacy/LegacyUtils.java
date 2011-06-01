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
import ij.CompositeImage;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import ij.process.LUT;
import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.display.AbstractDatasetView;
import imagej.display.ColorTables;
import imagej.display.Display;
import imagej.display.DisplayManager;
import imagej.display.DisplayView;
import imagej.util.Dimensions;
import imagej.util.Index;
import imagej.util.Log;


/**
 * Collection of static methods used by the various ImageTranslators and the
 * DatasetHarmonizer. Kept together so that bidirectional translation code is
 * in one place and thus more easily maintained.
 * 
 * Note the interfaces have package access to avoid their use outside the
 * legacy layer.
 * 
 * @author Barry DeZonia
 * */
public class LegacyUtils {

	// -- constructor --
	
	private LegacyUtils() {
		// utility class: do not instantiate
	}

	// -- package interface --
	
	/** Makes a planar {@link Dataset} whose dimensions match a given
	 *  {@link ImagePlus}. Data is exactly the same as plane references
	 *  are shared between the Dataset and the ImagePlus. Assumes it will
	 *  never be called with any kind of color ImagePlus. Does not set
	 *  metadata of Dataset. */
	static Dataset makeExactDataset(ImagePlus imp) {
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
	
	
	/** Makes a color {@link Dataset} from an {@link ImagePlus}. Color Datasets
	 *  have isRgbMerged() true, channels == 3, and bitsperPixel == 8. Does not
	 *  populate the data of the returned Dataset. That is left to other utility
	 *  methods. Does not set metadata of Dataset.
	 *  
	 *  Throws exceptions if input ImagePlus is not single channel RGB. */ 
	static Dataset makeColorDataset(ImagePlus imp) {
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
		final int bitsPerPixel = 8;
		final boolean signed = false;
		final boolean floating = false;
		final Dataset ds =
			Dataset.create(dims, name, axes, bitsPerPixel, signed, floating);
		
		ds.setRGBMerged(true);
		
		return ds;
	}

	/** Makes an {@link ImagePlus} from a {@link Dataset}. Data is exactly the
	 * same between them as planes of data are shared by reference. Assumes the
	 * Dataset can be represented via plane references (thus backled by
	 * {@link PlanarAccess} and in a type compatible with IJ1). Does not set
	 * the metadata of the ImagePlus.
	 * 
	 * Throws an exception if the Dataset has any axis present that is not IJ1
	 * compatible. Also throws an exception when Dataset has any axis or total
	 * number of planes > Integer.MAX_VALUE. 
	 */
	// TODO - check that Dataset can be represented exactly
	static ImagePlus makeExactImagePlus(Dataset ds) {
		int[] dimIndices = new int[5];
		int[] dimValues = new int[5];
		getImagePlusDims(ds, dimIndices, dimValues);
		assertXYPlanesCorrectlyOriented(dimIndices);
		
		final int cIndex = dimIndices[2];
		final int zIndex = dimIndices[3];
		final int tIndex = dimIndices[4];
		final int c = cIndex < 0 ? 1 : dimValues[cIndex];
		final int z = zIndex < 0 ? 1 : dimValues[zIndex];
		final int t = tIndex < 0 ? 1 : dimValues[tIndex];

		ImagePlus imp = makeImagePlus(ds, getPlaneMaker(ds), true);
		
		setImagePlusPlanes(ds, imp);

		imp.setDimensions(c, z, t);
		
		return imp;
	}

	/** Makes an {@link ImagePlus} from a {@link Dataset} whose dimensions match.
	 * The type of the ImagePlus is an IJ1 type that can best represent the data
	 * with the least loss of data. Sometimes the IJ1 & IJ2 types are the same
	 * type and sometimes they are not. The data values and metadata are not
	 * assigned. Assumes it will never be sent a color Dataset.
	 */
	static ImagePlus makeNearestTypeGrayImagePlus(Dataset ds) {
		PlaneMaker planeMaker = getPlaneMaker(ds);
		return makeImagePlus(ds, planeMaker, false);
	}

	/** Makes a color {@link ImagePlus} from a color {@link Dataset}. The
	 *  ImagePlus will have the same X, Y, Z, & T dimensions. C will be 1.
	 *  The data values and metadata are not assigned.
	 * 
	 *  Throws an exception if the dataset is not color compatible. Throws an
	 *  exception if the Dataset has any axis present that is not IJ1 compatible.
	 *  Also throws an exception when Dataset has any axis or total number of
	 *  planes > Integer.MAX_VALUE. 
	 */
	static ImagePlus makeColorImagePlus(Dataset ds) {
		if ( ! isColorCompatible(ds) )
			throw new IllegalArgumentException("Dataset is not color compatible");
		
		int[] dimIndices = new int[5];
		int[] dimValues = new int[5];
		getImagePlusDims(ds, dimIndices, dimValues);
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

	/** Assigns the data values of an {@link ImagePlus} from a paired
	 *  {@link Dataset}. Assumes the Dataset and ImagePlus have the same
	 *  dimensions and that the data  planes are not directly mapped. If
	 *  the data values are directly mapped then this code just wastes
	 *  time. Sets values via {@link ImageProcessor}::setf(). Some special
	 *  case code is in place to assure that BitType images go to IJ1 as
	 *  0/255 value images. Does not change the ImagePlus' metadata.
	 */
	static void setImagePlusGrayData(Dataset ds, ImagePlus imp) {
		boolean bitData = ds.getType() instanceof BitType;
		int xIndex = ds.getAxisIndex(Axes.X);
		int yIndex = ds.getAxisIndex(Axes.Y);
		int zIndex = ds.getAxisIndex(Axes.Z);
		int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		int tIndex = ds.getAxisIndex(Axes.TIME);
		int x = imp.getWidth();
		int y = imp.getHeight();
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
						accessor.setPosition(yi, yIndex);
						for (int xi = 0; xi < x; xi++) {
							accessor.setPosition(xi, xIndex);
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

	/** Assigns the data values of a color {@link ImagePlus} from a paired
	 *  {@link Dataset}. Assumes the Dataset and ImagePlus have the same
	 *  dimensions and that the data planes are not directly mapped. Also
	 *  assumes that the Dataset has isRGBMerged() true. Sets values via
	 *  {@link ImageProcessor}::set(). Does not change the ImagePlus' metadata.
	 */
	static void setImagePlusColorData(Dataset ds, ImagePlus imp) {
		int xIndex = ds.getAxisIndex(Axes.X);
		int yIndex = ds.getAxisIndex(Axes.Y);
		int zIndex = ds.getAxisIndex(Axes.Z);
		int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		int tIndex = ds.getAxisIndex(Axes.TIME);
		int x = imp.getWidth();
		int y = imp.getHeight();
		//  c is always 3 in this case
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
					accessor.setPosition(yi, yIndex);
					for (int xi = 0; xi < x; xi++) {
						accessor.setPosition(xi, xIndex);
						
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

	/** Assigns the data values of a {@link Dataset} from a paired
	 *  {@link ImagePlus}. Assumes the Dataset and ImagePlus have the same
	 *  dimensions and that the data planes are not directly mapped. If the
	 *  data values are directly mapped then this code just wastes time. Gets
	 *  values via {@link ImageProcessor}::getf(). In cases where there is a
	 *  narrowing of types into IJ2 data is range clamped. Does not change
	 *  the Dataset's metadata.
	 */
	static void setDatasetGrayData(Dataset ds, ImagePlus imp) {
		RealType<?> type = ds.getType();
		double typeMin = type.getMinValue();
		double typeMax = type.getMaxValue();
		int xIndex = ds.getAxisIndex(Axes.X);
		int yIndex = ds.getAxisIndex(Axes.Y);
		int zIndex = ds.getAxisIndex(Axes.Z);
		int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		int tIndex = ds.getAxisIndex(Axes.TIME);
		int x = imp.getWidth();
		int y = imp.getHeight();
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

	/** Assigns the data values of a color {@link Dataset} from a paired
	 *  {@link ImagePlus}. Assumes the Dataset and ImagePlus have the same
	 *  dimensions and are both of type color. Gets values via
	 *  {@link ImageProcessor}::get(). Does not change the Dataset's metadata.
	 */
	static void setDatasetColorData(Dataset ds, ImagePlus imp) {
		int xIndex = ds.getAxisIndex(Axes.X);
		int yIndex = ds.getAxisIndex(Axes.Y);
		int zIndex = ds.getAxisIndex(Axes.Z);
		int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		int tIndex = ds.getAxisIndex(Axes.TIME);
		int x = imp.getWidth();
		int y = imp.getHeight();
		//  c is always 3 in this case
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
					accessor.setPosition(yi, yIndex);
					for (int xi = 0; xi < x; xi++) {
						accessor.setPosition(xi, xIndex);
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

	/**
	 * Assigns the plane references of an {@link ImagePlus}' {@link ImageStack}
	 * to match those of a given {@link Dataset}. Assumes input Dataset and
	 * ImagePlus match in dimensions and backing type.
	 * 
	 * Throws an exception if the Dataset has any axis present that is not IJ1
	 * compatible. Also throws an exception when Dataset has any axis or total
	 * number of planes > Integer.MAX_VALUE. 
	 */
	static void setImagePlusPlanes(Dataset ds, ImagePlus imp) {
		int[] dimIndices = new int[5];
		int[] dimValues = new int[5];
		getImagePlusDims(ds, dimIndices, dimValues);
		assertXYPlanesCorrectlyOriented(dimIndices);

		final int cIndex = dimIndices[2];
		final int zIndex = dimIndices[3];
		final int tIndex = dimIndices[4];

		final int cCount = dimValues[2];
		final int zCount = dimValues[3];
		final int tCount = dimValues[4];

		ImageStack stack = imp.getStack();

		final long[] dims = ds.getDims();
		final long[] planeDims = Dimensions.getDims3AndGreater(dims);
		final long[] planePos = new long[planeDims.length];

		for (int t = 0; t < tCount; t++) {
			if (tIndex >= 0) planePos[tIndex - 2] = t;
			for (int z = 0; z < zCount; z++) {
				if (zIndex >= 0) planePos[zIndex - 2] = z;
				for (int c = 0; c < cCount; c++) {
					if (cIndex >= 0) planePos[cIndex - 2] = c;
					final int planeNum = (int) Index.indexNDto1D(planeDims, planePos);
					final Object plane = ds.getPlane(planeNum, false);
					if (plane == null) {
						Log.error(message("Couldn't extract plane from Dataset ", c, z, t));
					}
					stack.setPixels(plane, planeNum+1);
				}
			}
		}
	}
	
	/**
	 * Assigns a planar {@link Dataset}'s plane references to match those of a
	 * given {@link ImagePlus}. Assumes input Dataset and ImagePlus match in
	 * dimensions and backing type.
	 */
	static void setDatasetPlanes(Dataset ds, ImagePlus imp) {
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

	/** Changes the shape of an existing {@link Dataset} to match that of an
	 * {@link ImagePlus}. Assumes that the Dataset type is correct. Does not
	 * set the data values or change the metadata.
	 */
	@SuppressWarnings({"rawtypes","unchecked"})
	// assumes the data type of the given Dataset is fine as is
	static void reshapeDataset(Dataset ds, ImagePlus imp) {
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

	/** sets a {@link Dataset}'s metadata to match a given {@link ImagePlus} */
	static void setDatasetMetadata(Dataset ds, ImagePlus imp) {
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
	
	/** sets an {@link ImagePlus}' metadata to match a given {@link Dataset} */
	static void setImagePlusMetadata(Dataset ds, ImagePlus imp) {
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

	static void setViewLuts(Dataset ds, ImagePlus imp) {
		boolean sixteenBitLuts = imp.getType() == ImagePlus.GRAY16;
		List<ColorTable<?>> colorTables = colorTablesFromImagePlus(imp);
		assignColorTables(ds, colorTables, sixteenBitLuts);
	}

	static void setImagePlusLuts(Dataset ds, ImagePlus imp) {
		// TODO - how to do this? Steal BioFormats Colorizer code
		//imp.getProcessor().setColorModel(cm);
		//imp.getStack().setColorModel(cm);
	}
	
	/** returns true if a {@link Dataset} can be represented by reference in IJ1
	*/
	static boolean datasetIsIJ1Compatible(Dataset ds) {
		return ij1StorageCompatible(ds) && ij1TypeCompatible(ds);
	}

	/** returns true if an {@link ImagePlus}' type is the best fit for a given
	 * {@link Dataset}. Best fit means the IJ1 type that is the best at
	 * preserving data.
	 */
	static boolean imagePlusIsNearestType(Dataset ds, ImagePlus imp) {
		int impType = imp.getType();
		
		if (impType == ImagePlus.COLOR_RGB) {
			if (isColorCompatible(ds))
				return true;
		}
		
		RealType<?> dsType = ds.getType();
		boolean isSigned = ds.isSigned();
		boolean isInteger = ds.isInteger();
		int bitsPerPix = dsType.getBitsPerPixel();

		if ((!isSigned) && (isInteger) && (bitsPerPix <= 8))
			return ((impType == ImagePlus.GRAY8) || (impType == ImagePlus.COLOR_256));
			
		if ((!isSigned) && (isInteger) && (bitsPerPix <= 16))
			return impType == ImagePlus.GRAY16;

		// Unnecessary
		//if ((isSigned) && (!isInteger))
		//	return impType == ImagePlus.GRAY32;
		
		return impType == ImagePlus.GRAY32;
	}

	/** returns true if any of the given Axes cannot be represented
	 * in an IJ1 ImagePlus.
	 */
	static boolean hasNonIJ1Axes(Axis[] axes) {
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

	/**
	 * Sets the {@link Dataset}'s number of composite channels to display
	 * simultaneously based on an input {@link ImagePlus}'s makeup.
	 */
	static void setDatasetCompositeVariables(final Dataset ds, final ImagePlus imp) {
		if ((imp instanceof CompositeImage) &&
			(((CompositeImage) imp).getMode() == CompositeImage.COMPOSITE))
		{
			ds.setCompositeChannelCount(imp.getNChannels());
		}
		else if (imp.getType() == ImagePlus.COLOR_RGB) {
			ds.setCompositeChannelCount(3);
		}
		else ds.setCompositeChannelCount(1);
	}

	// -- private helpers --

	/** tests that a given {@link Dataset} can be represented as a color
	 * {@link ImagePlus}. Some of this test maybe overkill if by definition
	 * rgbMerged can only be true if channels == 3 and type = ubyte are also
	 * true. 
	 */
	private static boolean isColorCompatible(Dataset ds) {
		if ( ! ds.isRGBMerged() ) return false;
		if ( ! ds.isInteger() ) return false;
		if (ds.isSigned()) return false;
		if (ds.getType().getBitsPerPixel() != 8) return false;
		int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		if (cIndex < 0) return false;
		if (ds.getImgPlus().dimension(cIndex) != 3) return false;
		return true;
	}

	/** helper class to simplify the making of planes of different type data */
	private interface PlaneMaker {
		Object makePlane(int w, int h);
	}

	/** makes planes of bytes given width & height */
	private static class BytePlaneMaker implements PlaneMaker {
		public BytePlaneMaker() {
			// nothing to do
		}
		@Override
		public Object makePlane(int w, int h) {
			return new byte[w * h];
		}
	}
	
	/** makes planes of shorts given width & height */
	private static class ShortPlaneMaker implements PlaneMaker {
		public ShortPlaneMaker() {
			// nothing to do
		}
		@Override
		public Object makePlane(int w, int h) {
			return new short[w * h];
		}
	}
	
	/** makes planes of floats given width & height */
	private static class FloatPlaneMaker implements PlaneMaker {
		public FloatPlaneMaker() {
			// nothing to do
		}
		@Override
		public Object makePlane(int w, int h) {
			return new float[w * h];
		}
	}
	
	/**
	 * makes an {@link ImagePlus} that matches dimensions of a {@link Dataset}.
	 * The data values of the ImagePlus to be populated later elsewhere.
	 *
	 * Throws an exception if the Dataset has any axis present that is not IJ1
	 * compatible. Also throws an exception when Dataset has any axis or total
	 * number of planes > Integer.MAX_VALUE. 
	 *
	 * @param ds - input Dataset to be shape compatible with
	 * @param planeMaker - a PlaneMaker to use to make type correct image planes 
	 * @param makeDummyPlanes - save memory by allocating the minimum number of planes
	 *          for the case that we'll be reassigning the planes immediately. 
	 * @return an ImagePlus whose dimensions math the input Dataset
	 */
	private static ImagePlus makeImagePlus(Dataset ds, PlaneMaker planeMaker, boolean makeDummyPlanes) {

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

		Object dummyPlane = null;
		for (long t = 0; t < tCount; t++) {
			if (tIndex >= 0) planePos[tIndex - 2] = t;
			for (long z = 0; z < zCount; z++) {
				if (zIndex >= 0) planePos[zIndex - 2] = z;
				for (long c = 0; c < cCount; c++) {
					if (cIndex >= 0) planePos[cIndex - 2] = c;
					Object plane;
					if (makeDummyPlanes) {
						if (dummyPlane == null)
							dummyPlane = planeMaker.makePlane(dimValues[0], dimValues[1]);
						plane = dummyPlane;
					}
					else
						plane = planeMaker.makePlane(dimValues[0], dimValues[1]);
					stack.addSlice(null, plane);
				}
			}
		}

		ImagePlus imp = new ImagePlus(ds.getName(), stack);
		
		imp.setDimensions(cCount, zCount, tCount);
		
		return imp;
	}

	/** finds the best {@link PlaneMaker} for a given {@link Dataset}. The best
	 *  PlaneMaker is the one that makes planes in the type that can best
	 *  represent the Dataset's data values in IJ1. */
	private static PlaneMaker getPlaneMaker(Dataset ds) {
		boolean signed = ds.isSigned();
		boolean integer = ds.isInteger();
		int bitsPerPixel = ds.getType().getBitsPerPixel();
		if (!signed && integer && bitsPerPixel <= 8)
			return new BytePlaneMaker();
		if (!signed && integer && bitsPerPixel <= 16)
				return new ShortPlaneMaker();
		return new FloatPlaneMaker();
	}
	
	/** copies a {@link Dataset}'s dimensions and axis indices into provided
	 *  arrays. The order of dimensions is formatted to be X,Y,C,Z,T. If an
	 *  axis is not present in the Dataset its value is set to 1 and its index
	 *  is set to -1.
	 *  
	 * Throws an exception if the Dataset has any axis present that is not IJ1
	 * compatible. Also throws an exception when Dataset has any axis or total
	 * number of planes > Integer.MAX_VALUE. 
	 */
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
		if (xIndex < 0)
			throw new IllegalArgumentException("missing X axis");
		if (dims[xIndex] > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Width out of range: " + dims[xIndex]);
		}

		// check height
		final int yIndex = dataset.getAxisIndex(Axes.Y);
		if (yIndex < 0)
			throw new IllegalArgumentException("missing Y axis");
		if (dims[yIndex] > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Height out of range: " + dims[yIndex]);
		}

		if ((dims[xIndex]*dims[yIndex]) > Integer.MAX_VALUE)
			throw new IllegalArgumentException("too many elements per plane : value(" +
				(dims[xIndex]*dims[yIndex])+") : max ("+Integer.MAX_VALUE+")");
		
		// check channels, slices and frames
		final int cIndex = dataset.getAxisIndex(Axes.CHANNEL);
		final int zIndex = dataset.getAxisIndex(Axes.Z);
		final int tIndex = dataset.getAxisIndex(Axes.TIME);
		
		final long cCount = cIndex < 0 ? 1 : dims[cIndex];
		final long zCount = zIndex < 0 ? 1 : dims[zIndex];
		final long tCount = tIndex < 0 ? 1 : dims[tIndex];
		
		if (cCount * zCount * tCount > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("too many planes : value(" +
				(cCount*zCount*tCount)+") : max ("+Integer.MAX_VALUE+")");
		}
		outputIndices[0] = xIndex;
		outputIndices[1] = yIndex;
		outputIndices[2] = cIndex;
		outputIndices[3] = zIndex;
		outputIndices[4] = tIndex;
		
		outputDims[0] = (int) dims[xIndex];
		outputDims[1] = (int) dims[yIndex];
		outputDims[2] = (int) cCount;
		outputDims[3] = (int) zCount;
		outputDims[4] = (int) tCount;
	}

	/** throws an Exception if the planes of a Dataset are not compatible with IJ1 */
	private static void assertXYPlanesCorrectlyOriented(int[] dimIndices) {
		if (dimIndices[0] != 0)
			throw new IllegalArgumentException("Dataset does not have X as the first axis");
		if (dimIndices[1] != 1)
			throw new IllegalArgumentException("Dataset does not have Y as the second axis");
	}
	
	/** returns true if a {@link Dataset} is backed by {@link PlanarAccess} */
	private static boolean ij1StorageCompatible(Dataset ds) {
		return ds.getImgPlus().getImg() instanceof PlanarAccess;
	}
	
	/** returns true if a {@link Dataset} has a type that can be directly
	 * represented in an IJ1 ImagePlus. 
	 */
	private static boolean ij1TypeCompatible(Dataset ds) {
		RealType<?> type = ds.getType();
		int bitsPerPix = type.getBitsPerPixel();
		boolean integer = ds.isInteger();
		boolean signed = ds.isSigned();
		
		Object plane;
		if ((bitsPerPix == 8) && !signed && integer) {
			plane = ds.getPlane(0,false);
			if ((plane != null) && (plane instanceof byte[]))
				return true;
		}
		else if ((bitsPerPix == 16) && !signed && integer) {
			plane = ds.getPlane(0,false);
			if ((plane != null) && (plane instanceof short[]))
				return true;
		}
		else if ((bitsPerPix == 32) && signed && !integer) {
			plane = ds.getPlane(0,false);
			if ((plane != null) && (plane instanceof float[]))
				return true;
		}
		return false;
	}

	/** returns true if an {@link ImagePlus} is of type GRAY32 */
	private static boolean isGray32(final ImagePlus imp) {
		final int type = imp.getType();
		return type == ImagePlus.GRAY32;
	}
	
	/** returns true if an {@link ImagePlus} is backed by a signed type */ 
	private static boolean isSigned(final ImagePlus imp) {
	  // TODO - ignores IJ1's support of signed 16 bit. OK?
		return isGray32(imp);
	}

	/** returns true if an {@link ImagePlus} is backed by a floating type */ 
	private static boolean isFloating(final ImagePlus imp) {
		return isGray32(imp);
	}

	/** error message formatting helper */
	private static String message(final String message, final long c, final long z,
		final long t)
	{
		return message + ": c=" + c + ", z=" + z + ", t=" + t;
	}

	private static void assignColorTables(Dataset ds, List<ColorTable<?>> colorTables, boolean sixteenBitLuts) {
		// FIXME - hack - for now until legacy layer maps Display <--> ImagePlus.
		//   grab the first Display and set its default channel luts. When we allow
		//   multiple views of a Dataset this will break. We avoid setting a
		//   Dataset's per plane LUTs because it would be expensive and also IJ1
		//   LUTs are not model space constructs but rather view space constructs.
		DisplayManager dispMgr = ImageJ.get(DisplayManager.class);
		for (Display display : dispMgr.getDisplays(ds)) {
			for (DisplayView view : display.getViews()) {
				AbstractDatasetView dsView = (AbstractDatasetView)view;
				if (dsView.getDataObject() != ds) continue;
				for (int i = 0; i < colorTables.size(); i++) {
					dsView.setColorTable((ColorTable8)colorTables.get(i), i);
				}
				return;
			}
		}
	}

	private static List<ColorTable<?>> colorTablesFromImagePlus(ImagePlus imp) {
		List<ColorTable<?>> colorTables = new ArrayList<ColorTable<?>>();
		LUT[] luts = imp.getLuts();
		if (luts == null) { // not a CompositeImage
			if (imp.getType() == ImagePlus.COLOR_RGB) {
				colorTables.add(ColorTables.RED);
				colorTables.add(ColorTables.GREEN);
				colorTables.add(ColorTables.BLUE);
			}
			else { // not a direct color model image
				IndexColorModel icm =
					(IndexColorModel)imp.getProcessor().getColorModel();
				ColorTable<?> cTable;
				//if (icm.getPixelSize() == 16) // is 16 bit table
				//	cTable = make16BitColorTable(icm);
				//else  // 8 bit color table
					cTable = make8BitColorTable(icm);
				colorTables.add(cTable);
			}
		}
		else { // we have multiple LUTs from a CompositeImage, 1 per channel
			ColorTable<?> cTable;
			for (int i = 0; i < luts.length; i++) {
				//if (luts[i].getPixelSize() == 16) // is 16 bit table
				//	cTable = make16BitColorTable(luts[i]);
				//else // 8 bit color table
					cTable = make8BitColorTable(luts[i]);
				colorTables.add(cTable);
			}
		}

		return colorTables;
	}
	
//	private static ColorTable16 make16BitColorTable(IndexColorModel icm) {
//		return new ColorTable16();
//	}

	private static ColorTable8 make8BitColorTable(IndexColorModel icm) {
		byte[] reds = new byte[256];
		byte[] greens = new byte[256];
		byte[] blues = new byte[256];
		icm.getReds(reds);
		icm.getGreens(greens);
		icm.getBlues(blues);
		return new ColorTable8(reds, greens, blues);
	}
}
