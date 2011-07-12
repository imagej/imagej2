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
import ij.ImageStack;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.display.Display;
import imagej.display.DisplayManager;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.img.Axes;

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
		if (!LegacyUtils.imagePlusIsNearestType(ds, imp)) {
			rebuildImagePlusData(display, imp);
		}
		else {
			if ((dimensionsIncompatible(ds, imp)) ||
					(imp.getStack().getSize() == 0)) {  // NB unfortunate issue with IJ1
				rebuildImagePlusData(display, imp);
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
		LegacyUtils.setImagePlusLuts(display, imp);
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
			// NB - ImagePlus with an empty stack avoided earlier.
			if (dimensionsIncompatible(ds, imp)) {
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
		LegacyUtils.setDisplayLuts(display, imp);
		// NB - make it the lower level methods' job to call ds.update()
	}

	// -- private helpers --

	/**
	 * Determines whether a {@link Dataset} and an {@link ImagePlus} have
	 * incompatible dimensionality.
	 */
	private boolean dimensionsIncompatible(final Dataset ds, final ImagePlus imp) {
		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);
		
		long[] dimensions = new long[ds.getImgPlus().numDimensions()];
		ds.getImgPlus().dimensions(dimensions);
		
		final long x = (xIndex < 0) ? 1 : dimensions[xIndex];
		final long y = (yIndex < 0) ? 1 : dimensions[yIndex];
		final long c = (cIndex < 0) ? 1 : dimensions[cIndex];
		final long z = (zIndex < 0) ? 1 : dimensions[zIndex];
		final long t = (tIndex < 0) ? 1 : dimensions[tIndex];
		
		if (x != imp.getWidth()) return true;
		if (y != imp.getHeight()) return true;
		if (z != imp.getNSlices()) return true;
		if (t != imp.getNFrames()) return true;
		// channel case a little different
		if (imp.getType() == ImagePlus.COLOR_RGB) {
			if (c != imp.getNChannels()*3) return true;
		}
		else { // not color data
			if (c != imp.getNChannels()) return true;
		}

		if (LegacyUtils.hasNonIJ1Axes(ds.getAxes())) {
			throw new IllegalStateException(
				"Dataset associated with ImagePlus has axes incompatible with IJ1");
		}

		return false;
	}

	/**
	 * Creates a new {@link ImageStack} of data from a {@link Display} and
	 * assigns it to given {@link ImagePlus}
	 * @param display
	 * @param imp
	 */
	private void rebuildImagePlusData(Display display, ImagePlus imp) {
		final ImagePlus newImp = imageTranslator.createLegacyImage(display);
		imp.setStack(newImp.getStack());
		final int c = newImp.getNChannels();
		final int z = newImp.getNSlices();
		final int t = newImp.getNFrames();
		imp.setDimensions(c, z, t);
		LegacyUtils.deleteImagePlus(newImp);
	}
}
