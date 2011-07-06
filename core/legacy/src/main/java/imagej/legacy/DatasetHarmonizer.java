//
// DatasetHarmonizer.java
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

package imagej.legacy;

import ij.ImagePlus;
import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.display.Display;
import imagej.display.DisplayManager;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.img.Axes;
import net.imglib2.img.ImgPlus;

/**
 * Synchronizes data between a {@link Dataset} and a paired {@link ImagePlus}.
 * After harmonization, data and metadata will match as closely as possible
 * given differences in pixel types, and data organization. When possible, the
 * harmonizer uses data by references to avoid additional memory overhead.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
public class DatasetHarmonizer {

	private final ImageTranslator imageTranslator;
	private final OverlayTranslator overlayTranslator;
	private final Map<ImagePlus, Integer> bitDepthMap =
		new HashMap<ImagePlus, Integer>();

	/**
	 * Constructs a {@link DatasetHarmonizer} with a given {@link ImageTranslator}
	 * . The translator is used to create new Datasets and ImagePluses when
	 * necessary.
	 */
	public DatasetHarmonizer(final ImageTranslator translator) {
		imageTranslator = translator;
		overlayTranslator = new OverlayTranslator();
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

	/**
	 * Changes the data within an {@link ImagePlus} to match data in a
	 * {@link Display}. Assumes Dataset has planar primitive access in an IJ1
	 * compatible format.
	 */
	public void updateLegacyImage(final Display display, final ImagePlus imp) {
		final DisplayManager displayManager = ImageJ.get(DisplayManager.class);
		final Dataset ds = displayManager.getActiveDataset(display);
//		System.out.println("DatasetHarmonizer::updateLegacyImage() - Dataset " +
//			ds.getName() + "associated with ImagePlus " + imp.getID());
		if (!LegacyUtils.imagePlusIsNearestType(ds, imp)) {
			final ImagePlus newImp = imageTranslator.createLegacyImage(display);
			imp.setStack(newImp.getStack());
			final int c = newImp.getNChannels();
			final int z = newImp.getNSlices();
			final int t = newImp.getNFrames();
			imp.setDimensions(c, z, t);
			LegacyUtils.deleteImagePlus(newImp);
			// System.out.println("imp type "+imp.getType());
			// System.out.println("from new imp type "+newImp.getType());
		}
		else {
			if (dimensionsDifferent(ds, imp)) {
				final ImagePlus newImp = imageTranslator.createLegacyImage(display);
				imp.setStack(newImp.getStack());
				final int c = newImp.getNChannels();
				final int z = newImp.getNSlices();
				final int t = newImp.getNFrames();
				imp.setDimensions(c, z, t);
				LegacyUtils.deleteImagePlus(newImp);
			}
			else if (imp.getType() == ImagePlus.COLOR_RGB) {
				LegacyUtils.setImagePlusColorData(ds, imp);
			}
			else if (LegacyUtils.datasetIsIJ1Compatible(ds)) {
				LegacyUtils.setImagePlusPlanes(ds, imp);
			}
			else LegacyUtils.setImagePlusGrayData(ds, imp);
		}
		LegacyUtils.setImagePlusMetadata(ds, imp);
		overlayTranslator.setImagePlusOverlays(display, imp);
		LegacyUtils.setImagePlusLuts(ds, imp);
	}

	/**
	 * Changes the data within a {@link Display} to match data in an
	 * {@link ImagePlus}.
	 */
	public void updateDisplay(final Display display, final ImagePlus imp) {
		final DisplayManager displayManager = ImageJ.get(DisplayManager.class);
		final Dataset ds = displayManager.getActiveDataset(display);

		// did type of ImagePlus change?
		if (imp.getBitDepth() != bitDepthMap.get(imp)) {
			final Display tmp = imageTranslator.createDisplay(imp, ds.getAxes());
			final Dataset dsTmp = displayManager.getActiveDataset(tmp);
			ds.setImgPlus(dsTmp.getImgPlus());
			ds.setRGBMerged(dsTmp.isRGBMerged());
		}
		else { // ImagePlus type unchanged
			if (dimensionsDifferent(ds, imp)) {
				LegacyUtils.reshapeDataset(ds, imp);
			}
			if (imp.getType() == ImagePlus.COLOR_RGB) {
				LegacyUtils.setDatasetColorData(ds, imp);
			}
			else if (LegacyUtils.datasetIsIJ1Compatible(ds)) {
				LegacyUtils.setDatasetPlanes(ds, imp);
			}
			else LegacyUtils.setDatasetGrayData(ds, imp);
		}
		LegacyUtils.setDatasetMetadata(ds, imp);
		LegacyUtils.setDatasetCompositeVariables(ds, imp);
		overlayTranslator.setDisplayOverlays(display, imp);
		LegacyUtils.setViewLuts(ds, imp);
		// NB - make it the lower level methods' job to call ds.update()
	}

	// -- private helpers --

	/**
	 * Determines whether a {@link Dataset} and an {@link ImagePlus} have
	 * different dimensionality.
	 */
	private boolean dimensionsDifferent(final Dataset ds, final ImagePlus imp) {
		final ImgPlus<?> imgPlus = ds.getImgPlus();

		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);
		final boolean different =
			dimensionDifferent(imgPlus, xIndex, imp.getWidth()) ||
				dimensionDifferent(imgPlus, yIndex, imp.getHeight()) ||
				dimensionDifferent(imgPlus, cIndex, imp.getNChannels()) ||
				dimensionDifferent(imgPlus, zIndex, imp.getNSlices()) ||
				dimensionDifferent(imgPlus, tIndex, imp.getNFrames());

		if (!different && LegacyUtils.hasNonIJ1Axes(ds.getAxes())) {
			throw new IllegalStateException(
				"Dataset associated with ImagePlus has axes incompatible with IJ1");
		}

		return different;
	}

	/**
	 * Determines whether a single dimension in an ImgPlus differs from a given
	 * value.
	 */
	private boolean dimensionDifferent(final ImgPlus<?> imgPlus, final int axis,
		final int value)
	{
		if (axis >= 0) return imgPlus.dimension(axis) != value;
		// axis < 0 : not present in imgPlus
		return value != 1;
	}

}
