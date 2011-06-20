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
import imagej.data.Dataset;

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

	private ImageTranslator imageTranslator;
	private final OverlayTranslator overlayTranslator;
	private Map<ImagePlus,Integer> bitDepthMap = new HashMap<ImagePlus,Integer>();

	/** construct a {@link DatasetHarmonizer} with a given
	 *  {@link ImageTranslator}. The translator is used to create new Datasets
	 *  and ImagePluses when necessary.
	 */
	public DatasetHarmonizer(final ImageTranslator translator) {
		imageTranslator = translator;
		overlayTranslator = new OverlayTranslator();
	}

	/** remember the type of an {@link ImagePlus}. This type can be checked
	 * after a call to a plugin to see if the ImagePlus underwent a type change.
	 */
	public void registerType(ImagePlus imp) {
		bitDepthMap.put(imp, imp.getBitDepth());
	}
	
	/** forget the types of all {@link ImagePlus}es. Called before a plugin is
	 * run to reset the tracking of types.
	 */
	public void resetTypeTracking() {
		bitDepthMap.clear();
	}
	
	/**
	 * Changes the data within an {@link ImagePlus} to match data in a
	 * {@link Dataset}. Assumes Dataset has planar primitive access in an IJ1
	 * compatible format.
	 */
	public void updateLegacyImage(Dataset ds, ImagePlus imp) {
		//System.out.println("DatasetHarmonizer::updateLegacyImage() - Dataset "+ds.getName()+"associated with ImagePlus "+imp.getID());
		if ( ! LegacyUtils.imagePlusIsNearestType(ds,imp) ) {
			ImagePlus newImp = imageTranslator.createLegacyImage(ds);
			imp.setStack(newImp.getStack());
			int c = newImp.getNChannels();
			int z = newImp.getNSlices();
			int t = newImp.getNFrames();
			imp.setDimensions(c, z, t);
			LegacyUtils.removeImagePlusFromIJ1(newImp);
			//System.out.println("imp type "+imp.getType());
			//System.out.println("from new imp type "+newImp.getType());
		}
		else {
			if (dimensionsDifferent(ds, imp)) {
				ImagePlus newImp = imageTranslator.createLegacyImage(ds);
				imp.setStack(newImp.getStack());
				int c = newImp.getNChannels();
				int z = newImp.getNSlices();
				int t = newImp.getNFrames();
				imp.setDimensions(c, z, t);
				LegacyUtils.removeImagePlusFromIJ1(newImp);
			}
			else if (imp.getType() == ImagePlus.COLOR_RGB)
				LegacyUtils.setImagePlusColorData(ds, imp);
			else if (LegacyUtils.datasetIsIJ1Compatible(ds))
				LegacyUtils.setImagePlusPlanes(ds, imp);
			else
				LegacyUtils.setImagePlusGrayData(ds, imp);
		}
		LegacyUtils.setImagePlusMetadata(ds, imp);
		overlayTranslator.setImagePlusOverlays(ds, imp);
		LegacyUtils.setImagePlusLuts(ds, imp);
	}
	
	/**
	 * Changes the data within a {@link Dataset} to match data in an
	 * {@link ImagePlus}.
	 */
	public void updateDataset(Dataset ds, ImagePlus imp) {
		// did type of ImagePlus change?
		if (imp.getBitDepth() != bitDepthMap.get(imp)) {
			Dataset tmp = imageTranslator.createDataset(imp, ds.getAxes());
			ds.setImgPlus(tmp.getImgPlus());
			ds.setRGBMerged(tmp.isRGBMerged());
		}
		else { // ImagePlus type unchanged
			if (dimensionsDifferent(ds, imp))
				LegacyUtils.reshapeDataset(ds, imp);
			if (imp.getType() == ImagePlus.COLOR_RGB)
				LegacyUtils.setDatasetColorData(ds, imp);
			else if (LegacyUtils.datasetIsIJ1Compatible(ds))
				LegacyUtils.setDatasetPlanes(ds, imp);
			else
				LegacyUtils.setDatasetGrayData(ds, imp);
		}
		LegacyUtils.setDatasetMetadata(ds, imp);
		LegacyUtils.setDatasetCompositeVariables(ds, imp);
		overlayTranslator.setDatasetOverlays(ds, imp);
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

		final boolean different =
			dimensionDifferent(imgPlus, ds.getAxisIndex(Axes.X), imp.getWidth()) ||
			dimensionDifferent(imgPlus, ds.getAxisIndex(Axes.Y), imp.getHeight()) ||
			dimensionDifferent(imgPlus, ds.getAxisIndex(Axes.CHANNEL), imp.getNChannels()) ||
			dimensionDifferent(imgPlus, ds.getAxisIndex(Axes.Z), imp.getNSlices()) ||
			dimensionDifferent(imgPlus, ds.getAxisIndex(Axes.TIME), imp.getNFrames());

		if ( ! different )
			if ( LegacyUtils.hasNonIJ1Axes(ds.getAxes()) )
				throw new IllegalStateException(
					"Dataset associated with ImagePlus has axes incompatible with IJ1");
		
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
