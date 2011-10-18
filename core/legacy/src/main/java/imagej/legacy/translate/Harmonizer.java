//
//
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

package imagej.legacy.translate;

import java.awt.image.IndexColorModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.RandomAccess;
import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable8;
import net.imglib2.display.RealLUTConverter;
import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.ImgPlus;
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
import imagej.data.Extents;
import imagej.data.Position;
import imagej.data.display.ColorMode;
import imagej.data.display.ColorTables;
import imagej.data.display.DataView;
import imagej.data.display.DatasetView;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.legacy.translate.LegacyUtils;
import imagej.util.Log;


/**
 * Provides methods for synchronizing data between an ImageDisplay and an
 * ImagePlus.
 * 
 * @author Barry DeZonia
 * 
 */
public class Harmonizer {

	// -- instance variables --

	private ImageTranslator imageTranslator;
	private final OverlayTranslator overlayTranslator;
	private final Map<ImagePlus, Integer> bitDepthMap;
	
	// -- constructor --

	public Harmonizer() {
		imageTranslator = null;  // TODO - initialization happens later. Points out need for another class/object 
		overlayTranslator = new OverlayTranslator();
		bitDepthMap = new HashMap<ImagePlus, Integer>();
	}
	
	// -- public interface --

	public void setImageTranslator(ImageTranslator translator) {
		this.imageTranslator = translator;
	}

	/**
	 * Changes the data within an {@link ImagePlus} to match data in a
	 * {@link ImageDisplay}. Assumes Dataset has planar primitive access in an IJ1
	 * compatible format.
	 */
	public void
		updateLegacyImage(final ImageDisplay display, final ImagePlus imp)
	{
		final ImageDisplayService imageDisplayService =
			ImageJ.get(ImageDisplayService.class);
		final Dataset ds = imageDisplayService.getActiveDataset(display);
		if (!imagePlusIsNearestType(ds, imp)) {
			rebuildImagePlusData(display, imp);
		}
		else {
			if ((dimensionsIncompatible(ds, imp)) || (imp.getStack().getSize() == 0))
			{ // NB - in IJ1 stack size can be zero for single slice image!
				rebuildImagePlusData(display, imp);
			}
			else if (imp.getType() == ImagePlus.COLOR_RGB) {
				setImagePlusColorData(ds, imp);
			}
			else if (LegacyUtils.datasetIsIJ1Compatible(ds)) {
				setImagePlusPlanes(ds, imp);
			}
			else setImagePlusGrayData(ds, imp);
		}
		setImagePlusMetadata(ds, imp);
		overlayTranslator.setImagePlusOverlays(display, imp);
		setImagePlusLuts(display, imp);
	}

	/**
	 * Changes the data within a {@link ImageDisplay} to match data in an
	 * {@link ImagePlus}. Assumes the given ImagePlus is not a degenerate
	 * set of data (an empty stack).
	 */
	public void updateDisplay(final ImageDisplay display, final ImagePlus imp) {
		
		// NB - if ImagePlus is degenerate the following code can fail. This is
		// because imglib cannot represent an empty data container. So we catch
		// the issue here:
		
		if (imp.getStack().getSize() == 0)
			throw new IllegalArgumentException(
				"cannot update a display with an ImagePlus that has an empty stack");
			
		final ImageDisplayService imageDisplayService =
			ImageJ.get(ImageDisplayService.class);
		final Dataset ds = imageDisplayService.getActiveDataset(display);

		// did type of ImagePlus change?
		if (imp.getBitDepth() != bitDepthMap.get(imp)) {
			final ImageDisplay tmp = imageTranslator.createDisplay(imp, ds.getAxes());
			final Dataset dsTmp = imageDisplayService.getActiveDataset(tmp);
			ds.setImgPlus(dsTmp.getImgPlus());
			ds.setRGBMerged(dsTmp.isRGBMerged());
		}
		else { // ImagePlus type unchanged
			if (dimensionsIncompatible(ds, imp)) {
				reshapeDataset(ds, imp);
			}
			if (imp.getType() == ImagePlus.COLOR_RGB) {
				setDatasetColorData(ds, imp);
			}
			else if (LegacyUtils.datasetIsIJ1Compatible(ds)) {
				setDatasetPlanes(ds, imp);
			}
			else setDatasetGrayData(ds, imp);
		}
		setDatasetMetadata(ds, imp);
		setDatasetCompositeVariables(ds, imp);
		overlayTranslator.setDisplayOverlays(display, imp);
		setDisplayLuts(display, imp);
		// NB - make it the lower level methods' job to call ds.update()
	}

	/**
	 * Remembers the type of an {@link ImagePlus}. This type can be checked after
	 * a call to a plugin to see if the ImagePlus underwent a type change.
	 */
	public void registerType(final ImagePlus imp) {
		bitDepthMap.put(imp, imp.getBitDepth());
	}

	/**
	 * Forgets the types of all {@link ImagePlus}es. Called before a plugin is run
	 * to reset the tracking of types.
	 */
	public void resetTypeTracking() {
		bitDepthMap.clear();
	}

	// -- package access interface --

	/**
	 * Returns true if an {@link ImagePlus}' type is the best fit for a given
	 * {@link Dataset}. Best fit means the IJ1 type that is the best at preserving
	 * data.
	 */
	boolean imagePlusIsNearestType(final Dataset ds, final ImagePlus imp) {
		final int impType = imp.getType();

		if (impType == ImagePlus.COLOR_RGB) {
			if (LegacyUtils.isColorCompatible(ds)) return true;
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
	 * Assigns the data values of a color {@link ImagePlus} from a paired
	 * {@link Dataset}. Assumes the Dataset and ImagePlus have compatible
	 * dimensions and that the data planes are not directly mapped. Also assumes
	 * that the Dataset has isRGBMerged() true. Sets values via
	 * {@link ImageProcessor}::set(). Does not change the ImagePlus' metadata.
	 */
	void setImagePlusColorData(final Dataset ds, final ImagePlus imp) {
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
	 * Assigns the data values of a color {@link Dataset} from a paired
	 * {@link ImagePlus}. Assumes the Dataset and ImagePlus have compatible
	 * dimensions and are both of type color. Gets values via
	 * {@link ImageProcessor}::get(). Does not change the Dataset's metadata.
	 */
	void setDatasetColorData(final Dataset ds, final ImagePlus imp) {
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
	 * Assigns the data values of an {@link ImagePlus} from a paired
	 * {@link Dataset}. Assumes the Dataset and ImagePlus are not directly mapped.
	 * It is possible that multiple IJ2 axes are encoded as a single set of
	 * channels in the ImagePlus. Sets values via {@link ImageProcessor}::setf().
	 * Some special case code is in place to assure that BitType images go to IJ1
	 * as 0/255 value images. Does not change the ImagePlus' metadata.
	 */
	void setImagePlusGrayData(final Dataset ds, final ImagePlus imp) {
		final boolean bitData = ds.getType() instanceof BitType;
		final RandomAccess<? extends RealType<?>> accessor =
			ds.getImgPlus().randomAccess();
		final long[] dims = ds.getDims();
		final Axis[] axes = ds.getAxes();
		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);
		final int xSize = imp.getWidth();
		final int ySize = imp.getHeight();
		final int zSize = imp.getNSlices();
		final int tSize = imp.getNFrames();
		final int cSize = imp.getNChannels();
		final ImageStack stack = imp.getStack();
		int planeNum = 1;
		final long[] pos = new long[dims.length];
		for (int t = 0; t < tSize; t++) {
			if (tIndex >= 0) pos[tIndex] = t;
			for (int z = 0; z < zSize; z++) {
				if (zIndex >= 0) pos[zIndex] = z;
				for (int c = 0; c < cSize; c++) {
					fillChannelIndices(dims, axes, c, pos);
					final ImageProcessor proc = stack.getProcessor(planeNum++);
					for (int x = 0; x < xSize; x++) {
						if (xIndex >= 0) pos[xIndex] = x;
						for (int y = 0; y < ySize; y++) {
							if (yIndex >= 0) pos[yIndex] = y;
							accessor.setPosition(pos);
							float value = accessor.get().getRealFloat();
							if (bitData) if (value > 0) value = 255;
							proc.setf(x, y, value);
						}
					}
				}
			}
		}
	}

	/**
	 * Assigns the data values of a {@link Dataset} from a paired
	 * {@link ImagePlus}. Assumes the Dataset and ImagePlus have compatible
	 * dimensions and that the data planes are not directly mapped. Gets values
	 * via {@link ImageProcessor}::getf(). In cases where there is a narrowing of
	 * data into IJ2 types the data is range clamped. Does not change the
	 * Dataset's metadata.
	 */
	void setDatasetGrayData(final Dataset ds, final ImagePlus imp) {
		final RealType<?> type = ds.getType();
		final double typeMin = type.getMinValue();
		final double typeMax = type.getMaxValue();
		final RandomAccess<? extends RealType<?>> accessor =
			ds.getImgPlus().randomAccess();
		final long[] dims = ds.getDims();
		final Axis[] axes = ds.getAxes();
		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);
		final int xSize = imp.getWidth();
		final int ySize = imp.getHeight();
		final int zSize = imp.getNSlices();
		final int tSize = imp.getNFrames();
		final int cSize = imp.getNChannels();
		final ImageStack stack = imp.getStack();
		int planeNum = 1;
		final long[] pos = new long[dims.length];
		for (int t = 0; t < tSize; t++) {
			if (tIndex >= 0) pos[tIndex] = t;
			for (int z = 0; z < zSize; z++) {
				if (zIndex >= 0) pos[zIndex] = z;
				for (int c = 0; c < cSize; c++) {
					fillChannelIndices(dims, axes, c, pos);
					final ImageProcessor proc = stack.getProcessor(planeNum++);
					for (int x = 0; x < xSize; x++) {
						if (xIndex >= 0) pos[xIndex] = x;
						for (int y = 0; y < ySize; y++) {
							if (yIndex >= 0) pos[yIndex] = y;
							accessor.setPosition(pos);
							float value = proc.getf(x, y);
							if (value < typeMin) value = (float) typeMin;
							if (value > typeMax) value = (float) typeMax;
							accessor.get().setReal(value);
						}
					}
				}
			}
		}
		ds.update();
	}

	/**
	 * Changes the shape of an existing {@link Dataset} to match that of an
	 * {@link ImagePlus}. Assumes that the Dataset type is correct. Does not set
	 * the data values or change the metadata.
	 */
	// assumes the data type of the given Dataset is fine as is
	@SuppressWarnings({ "rawtypes", "unchecked" })
	void reshapeDataset(final Dataset ds, final ImagePlus imp) {
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

	/**
	 * Sets the {@link Dataset}'s number of composite channels to display
	 * simultaneously based on an input {@link ImagePlus}'s makeup.
	 */
	void setDatasetCompositeVariables(final Dataset ds, final ImagePlus imp)
	{
		if ((imp instanceof CompositeImage) &&
			(((CompositeImage) imp).getMode() == CompositeImage.COMPOSITE))
		{
			ds.setCompositeChannelCount(imp.getNChannels());
		}
		else if (imp.getType() == ImagePlus.COLOR_RGB && imp.getNChannels() == 1) {
			ds.setCompositeChannelCount(3);
		}
		else ds.setCompositeChannelCount(1);
	}

	/** Sets a {@link Dataset}'s metadata to match a given {@link ImagePlus}. */
	void setDatasetMetadata(final Dataset ds, final ImagePlus imp) {
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
	void setImagePlusMetadata(final Dataset ds, final ImagePlus imp) {
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
	 * Sets the ColorTables of the active view of an IJ2 ImageDisplay from the
	 * LUTs of a given ImagePlus or CompositeImage.
	 */
	void setDisplayLuts(final ImageDisplay disp, final ImagePlus imp) {
		final boolean sixteenBitLuts = imp.getType() == ImagePlus.GRAY16;
		final List<ColorTable<?>> colorTables = colorTablesFromImagePlus(imp);
		assignColorTables(disp, colorTables, sixteenBitLuts);
		assignChannelMinMax(disp, imp);
	}

	/**
	 * Sets LUTs of an ImagePlus or CompositeImage. If given an ImagePlus this
	 * method sets it's single LUT from the first ColorTable of the active Dataset
	 * of the given ImageDisplay. If given a CompositeImage this method sets all
	 * it's LUTs from the ColorTables of the active view of the given
	 * ImageDisplay. If there is no such view the LUTs are assigned with default
	 * values.
	 */
	void setImagePlusLuts(final ImageDisplay disp, final ImagePlus imp) {
		if (imp instanceof CompositeImage) {
			final CompositeImage ci = (CompositeImage) imp;
			final DataView activeView = disp.getActiveView();
			if (activeView == null) {
				setCompositeImageLutsToDefault(ci);
			}
			else {
				final DatasetView view = (DatasetView) activeView;
				setCompositeImageLuts(ci, view.getColorTables());
			}
		}
		else { // regular ImagePlus
			final ImageDisplayService imageDisplayService =
				ImageJ.get(ImageDisplayService.class);
			final Dataset ds = imageDisplayService.getActiveDataset(disp);
			setImagePlusLutToFirstInDataset(ds, imp);
		}
		assignImagePlusMinMax(disp, imp);
	}

	/**
	 * Assigns the plane references of an {@link ImagePlus}' {@link ImageStack} to
	 * match those of a given {@link Dataset}. Assumes input Dataset and ImagePlus
	 * match in dimensions and backing type. Throws an exception if Dataset axis 0
	 * is not X or Dataset axis 1 is not Y.
	 */
	void setImagePlusPlanes(final Dataset ds, final ImagePlus imp) {
		final int[] dimIndices = new int[5];
		final int[] dimValues = new int[5];
		LegacyUtils.getImagePlusDims(ds, dimIndices, dimValues);
		LegacyUtils.assertXYPlanesCorrectlyOriented(dimIndices);

		final int cIndex = dimIndices[2];
		final int zIndex = dimIndices[3];
		final int tIndex = dimIndices[4];

		final int cCount = dimValues[2];
		final int zCount = dimValues[3];
		final int tCount = dimValues[4];

		final ImageStack stack = imp.getStack();

		final long[] fullDims = ds.getDims();
		final long[] planeDims = new long[fullDims.length - 2];
		for (int i = 0; i < planeDims.length; i++)
			planeDims[i] = fullDims[i + 2];
		final Extents extents = new Extents(planeDims);
		final Position planePos = extents.createPosition();

		if (imp.getStackSize() == 1)
			imp.getProcessor().setPixels(ds.getPlane(0));
		else {
			int stackPosition = 1;
			for (int t = 0; t < tCount; t++) {
				if (tIndex >= 0) planePos.setPosition(t, tIndex - 2);
				for (int z = 0; z < zCount; z++) {
					if (zIndex >= 0) planePos.setPosition(z, zIndex - 2);
					for (int c = 0; c < cCount; c++) {
						if (cIndex >= 0) planePos.setPosition(c, cIndex - 2);
						final int planeNum = (int) planePos.getIndex();
						final Object plane = ds.getPlane(planeNum, false);
						if (plane == null) {
							Log.error(message("Couldn't extract plane from Dataset ", c, z, t));
						}
						stack.setPixels(plane, stackPosition++);
					}
				}
			}
		}
	}

	/**
	 * Assigns a planar {@link Dataset}'s plane references to match those of a
	 * given {@link ImagePlus}. Assumes input Dataset and ImagePlus match in
	 * dimensions and backing type.
	 */
	void setDatasetPlanes(final Dataset ds, final ImagePlus imp) {
		final int c = imp.getNChannels();
		final int z = imp.getNSlices();
		final int t = imp.getNFrames();

		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);

		final long[] fullDims = ds.getDims();
		final long[] planeDims = new long[fullDims.length - 2];
		for (int i = 0; i < planeDims.length; i++)
			planeDims[i] = fullDims[i + 2];
		final Position planePos = new Extents(planeDims).createPosition();

		// copy planes by reference
		int stackPosition = 1;
		for (int ti = 0; ti < t; ti++) {
			if (tIndex >= 0) planePos.setPosition(ti, tIndex - 2);
			for (int zi = 0; zi < z; zi++) {
				if (zIndex >= 0) planePos.setPosition(zi, zIndex - 2);
				for (int ci = 0; ci < c; ci++) {
					if (cIndex >= 0) planePos.setPosition(ci, cIndex - 2);
					final Object plane = imp.getStack().getPixels(stackPosition++);
					if (plane == null) {
						Log.error("Could not extract plane from ImageStack: " +
							(stackPosition - 1));
					}
					final int planeNum = (int) planePos.getIndex();
					ds.setPlane(planeNum, plane);
				}
			}
		}
		// no need to call ds.update() - setPlane() tracks it
	}

	// -- private interface --

	/**
	 * Fills IJ1 incompatible indices of a position array. The channel from IJ1
	 * is rasterized into potentially multiple indices in IJ2's position array.
	 * For instance an IJ2 image with CHANNELs and SPECTRA get encoded as
	 * multiple channels in IJ!. When coming back from IJ1 need to rasterize
	 * the its single channel index back into (CHANNEL,SPECTRA) pairs.
	 * @param dims - the dimensions of the IJ2 Dataset
	 * @param axes - the axes labels that match the Dataset dimensions
	 * @param channelIndex - the index of the IJ1 encoded channel position
	 * @param pos - the position array to fill with rasterized values
	 */
	private void fillChannelIndices(final long[] dims, final Axis[] axes,
		final long channelIndex, final long[] pos)
	{
		long workingIndex = channelIndex;
		for (int i = 0; i < dims.length; i++) {
			final Axis axis = axes[i];
			// skip axes we don't encode as channels
			if (axis == Axes.X) continue;
			if (axis == Axes.Y) continue;
			if (axis == Axes.Z) continue;
			if (axis == Axes.TIME) continue;
			// calc index of encoded channels
			final long subIndex = workingIndex % dims[i];
			pos[i] = subIndex;
			workingIndex = workingIndex / dims[i];
		}
	}

	/** Assigns the color tables of the active view of a ImageDisplay. */
	private void assignColorTables(final ImageDisplay disp,
		final List<ColorTable<?>> colorTables, @SuppressWarnings("unused")
		final boolean sixteenBitLuts)
	{
		// FIXME HACK
		// Grab the active view of the given ImageDisplay and set it's default
		// channel
		// luts. When we allow multiple views of a Dataset this will break. We
		// avoid setting a Dataset's per plane LUTs because it would be expensive
		// and also IJ1 LUTs are not model space constructs but rather view space
		// constructs.
		final DataView dispView = disp.getActiveView();
		if (dispView == null) return;
		final DatasetView dsView = (DatasetView) dispView;

		final ColorMode currMode = dsView.getColorMode();

		if (currMode == ColorMode.GRAYSCALE) return;

		// either we're given one color table for whole dataset
		if (colorTables.size() == 1) {
			final ColorTable8 newTable = (ColorTable8) colorTables.get(0);
			final List<ColorTable8> existingColorTables = dsView.getColorTables();
			for (int i = 0; i < existingColorTables.size(); i++)
				dsView.setColorTable(newTable, i);
		}
		else { // or we're given one per channel
			for (int i = 0; i < colorTables.size(); i++)
				dsView.setColorTable((ColorTable8) colorTables.get(i), i);
		}

		// force current plane to redraw : HACK to fix bug #668
		dsView.getProjector().map();
		disp.update();
	}

	/**
	 * Assigns the per-channel min/max values of active view of given
	 * ImageDisplay to the specified ImagePlus/CompositeImage range(s).
	 */
	private void assignChannelMinMax(final ImageDisplay disp,
		final ImagePlus imp)
	{
		final DataView dataView = disp.getActiveView();
		if (!(dataView instanceof DatasetView)) return;
		final DatasetView view = (DatasetView) dataView;
		final List<RealLUTConverter<? extends RealType<?>>> converters =
			view.getConverters();
		final int channelCount = converters.size();
		final double[] min = new double[channelCount];
		final double[] max = new double[channelCount];

		if (imp instanceof CompositeImage) {
			final CompositeImage ci = (CompositeImage) imp;
			final LUT[] luts = ci.getLuts();
			if (channelCount != luts.length) {
				throw new IllegalArgumentException("Channel mismatch: " +
					converters.size() + " vs. " + luts.length);
			}
			for (int c = 0; c < channelCount; c++) {
				min[c] = luts[c].min;
				max[c] = luts[c].max;
			}
		}
		else {
			final double mn = imp.getDisplayRangeMin();
			final double mx = imp.getDisplayRangeMax();
			for (int c = 0; c < channelCount; c++) {
				min[c] = mn;
				max[c] = mx;
			}
		}

		for (int c = 0; c < channelCount; c++) {
			final RealLUTConverter<? extends RealType<?>> conv = converters.get(c);
			conv.setMin(min[c]);
			conv.setMax(max[c]);
		}
	}

	/**
	 * Assigns the given ImagePlus's per-channel min/max values to the active view
	 * of the specified ImageDisplay.
	 */
	private void assignImagePlusMinMax(final ImageDisplay disp,
		final ImagePlus imp)
	{
		final DataView dataView = disp.getActiveView();
		if (!(dataView instanceof DatasetView)) return;
		final DatasetView view = (DatasetView) dataView;
		final List<RealLUTConverter<? extends RealType<?>>> converters =
			view.getConverters();
		final int channelCount = converters.size();
		final double[] min = new double[channelCount];
		final double[] max = new double[channelCount];
		double overallMin = Double.POSITIVE_INFINITY;
		double overallMax = Double.NEGATIVE_INFINITY;
		for (int c = 0; c < channelCount; c++) {
			final RealLUTConverter<? extends RealType<?>> conv = converters.get(c);
			min[c] = conv.getMin();
			max[c] = conv.getMax();
			if (min[c] < overallMin) overallMin = min[c];
			if (max[c] > overallMax) overallMax = max[c];
		}
		
		if (imp instanceof CompositeImage) {
			CompositeImage ci = (CompositeImage) imp;
			LUT[] luts = ci.getLuts();
			if (channelCount != luts.length) {
				throw new IllegalArgumentException("Channel mismatch: " +
					converters.size() + " vs. " + luts.length);
			}
			for (int i = 0; i < luts.length; i++) {
				luts[i].min = min[i];
				luts[i].max = max[i];
			}
		}
		else { // regular ImagePlus
			imp.setDisplayRange(overallMin, overallMax);
		}
	}
	
	/** Creates a list of ColorTables from an ImagePlus. */
	private List<ColorTable<?>> colorTablesFromImagePlus(
		final ImagePlus imp)
	{
		final List<ColorTable<?>> colorTables = new ArrayList<ColorTable<?>>();
		final LUT[] luts = imp.getLuts();
		if (luts == null) { // not a CompositeImage
			if (imp.getType() == ImagePlus.COLOR_RGB) {
				for (int i = 0; i < imp.getNChannels() * 3; i++) {
					final ColorTable<?> cTable = ColorTables.getDefaultColorTable(i);
					colorTables.add(cTable);
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
	private ColorTable8 make8BitColorTable(final IndexColorModel icm) {
		final byte[] reds = new byte[256];
		final byte[] greens = new byte[256];
		final byte[] blues = new byte[256];
		icm.getReds(reds);
		icm.getGreens(greens);
		icm.getBlues(blues);
		return new ColorTable8(reds, greens, blues);
	}

	/** Makes an 8-bit LUT from a ColorTable8. */
	private LUT make8BitLut(final ColorTable8 cTable) {
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
	private void setCompositeImageLutsToDefault(final CompositeImage ci) {
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
	private void setCompositeImageLuts(final CompositeImage ci,
		final List<ColorTable8> cTables)
	{
		if (cTables == null || cTables.size() == 0) {
			setCompositeImageLutsToDefault(ci);
			ci.setMode(CompositeImage.COMPOSITE);
		}
		else {
			boolean allGrayLuts = true;
			for (int i = 0; i < ci.getNChannels(); i++) {
				final ColorTable8 cTable = cTables.get(i);
				if ((allGrayLuts) && (!ColorTables.isGrayColorTable(cTable)))
					allGrayLuts = false;
				final LUT lut = make8BitLut(cTable);
				ci.setChannelLut(lut, i + 1);
			}
			if (allGrayLuts) {
				ci.setMode(CompositeImage.GRAYSCALE);
			}
			else {
				ci.setMode(CompositeImage.COLOR);
			}
		}
	}

	/** Sets the single LUT of an ImagePlus to the first ColorTable of a Dataset */
	private void setImagePlusLutToFirstInDataset(final Dataset ds,
		final ImagePlus imp)
	{
		ColorTable8 cTable = ds.getColorTable8(0);
		if (cTable == null) cTable = ColorTables.GRAYS;
		final LUT lut = make8BitLut(cTable);
		imp.getProcessor().setColorModel(lut);
		// or imp.getStack().setColorModel(lut);
	}

	/** Formats an error message. */
	private String message(final String message, final long c,
		final long z, final long t)
	{
		return message + ": c=" + c + ", z=" + z + ", t=" + t;
	}

	/**
	 * Determines whether a {@link Dataset} and an {@link ImagePlus} have
	 * incompatible dimensionality.
	 */
	private boolean dimensionsIncompatible(final Dataset ds, final ImagePlus imp)
	{
		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);

		final long[] dimensions = ds.getDims();

		final long x = (xIndex < 0) ? 1 : dimensions[xIndex];
		final long y = (yIndex < 0) ? 1 : dimensions[yIndex];
		final long z = (zIndex < 0) ? 1 : dimensions[zIndex];
		final long t = (tIndex < 0) ? 1 : dimensions[tIndex];

		if (x != imp.getWidth()) return true;
		if (y != imp.getHeight()) return true;
		if (z != imp.getNSlices()) return true;
		if (t != imp.getNFrames()) return true;
		// channel case a little different
		if (imp.getType() == ImagePlus.COLOR_RGB) {
			int cIndex = ds.getAxisIndex(Axes.CHANNEL);
			if (cIndex < 0) return true;
			long c = dimensions[cIndex];
			if (c != imp.getNChannels() * 3) return true;
		}
		else { // not color data
			long c = LegacyUtils.ij1ChannelCount(dimensions, ds.getAxes());
			if (c != imp.getNChannels()) return true;
		}

		return false;
	}

	/**
	 * Creates a new {@link ImageStack} of data from a {@link ImageDisplay} and
	 * assigns it to given {@link ImagePlus}
	 * 
	 * @param display
	 * @param imp
	 */
	private void rebuildImagePlusData(final ImageDisplay display,
		final ImagePlus imp)
	{
		final ImagePlus newImp = imageTranslator.createLegacyImage(display);
		imp.setStack(newImp.getStack());
		final int c = newImp.getNChannels();
		final int z = newImp.getNSlices();
		final int t = newImp.getNFrames();
		imp.setDimensions(c, z, t);
		LegacyUtils.deleteImagePlus(newImp);
	}

}
