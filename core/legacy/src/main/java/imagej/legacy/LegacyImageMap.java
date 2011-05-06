//
// LegacyImageMap.java
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
import ij.WindowManager;

import imagej.data.Dataset;

import java.util.Map;
import java.util.WeakHashMap;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.ImgPlus;
import net.imglib2.img.basictypeaccess.PlanarAccess;
import net.imglib2.type.numeric.RealType;

/**
 * TODO
 *
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public class LegacyImageMap {

	// -- instance variables --
	
	private Map<ImagePlus, Dataset> imageTable =
		new WeakHashMap<ImagePlus, Dataset>();

	private ImageTranslator imageTranslator = new DefaultImageTranslator();

	private LegacyMetadataTranslator metadataTranslator =
		new LegacyMetadataTranslator();

	// -- public interface --
	
	/**
	 * Ensures that the given legacy image has a corresponding dataset.
	 *
	 * @return the {@link Dataset} object shadowing this legacy image.
	 */
	public Dataset registerLegacyImage(ImagePlus imp) {
		synchronized (imageTable) {
			Dataset dataset = imageTable.get(imp);
			if (dataset == null) {
				// mirror image window to dataset
				dataset = imageTranslator.createDataset(imp);
				imageTable.put(imp, dataset);
			}
			else {  // dataset was already existing
				reconcileDifferences(dataset, imp);
			}
			return dataset;
		}
	}

	/**
	 * Ensures that the given dataset has a corresponding legacy image.
	 *
	 * @return the {@link ImagePlus} object shadowing this dataset.
	 */
	public ImagePlus registerDataset(Dataset dataset) {
		// find image window
		ImagePlus imp = null;
		synchronized (imageTable) {
			for (final ImagePlus key : imageTable.keySet()) {
				final Dataset value = imageTable.get(key);
				if (dataset == value) {
					imp = key;
					break;
				}
			}
			if (imp == null) {
				// mirror dataset to image window
				imp = imageTranslator.createLegacyImage(dataset);
				imageTable.put(imp, dataset);
			}
		}
		WindowManager.setTempCurrentImage(imp);
		return imp;
	}

	// -- private helpers -- 
	
	private boolean dimensionDifferent(ImgPlus<?> imgPlus, int axis, int value) {
		if (axis >= 0)
			return imgPlus.dimension(axis) != value;
		// axis < 0 : not present in imgPlus
		return value != 1;
	}

	// TODO - make this public somewhere in legacy layer
	
	private boolean hasNonIJ1Dimensions(ImgPlus<?> imgPlus) {
		Axes[] axes = new Axes[imgPlus.numDimensions()];
		imgPlus.axes(axes);
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
	
	private boolean dimensionsDifferent(Dataset ds, ImagePlus imp) {
		ImgPlus<?> imgPlus = ds.getImgPlus();

		boolean different =
			dimensionDifferent(imgPlus, ds.getAxisIndex(Axes.X), imp.getWidth()) ||
			dimensionDifferent(imgPlus, ds.getAxisIndex(Axes.Y), imp.getHeight()) ||
			dimensionDifferent(imgPlus, ds.getAxisIndex(Axes.CHANNEL), imp.getNChannels()) ||
			dimensionDifferent(imgPlus, ds.getAxisIndex(Axes.Z), imp.getNSlices()) ||
			dimensionDifferent(imgPlus, ds.getAxisIndex(Axes.TIME), imp.getNFrames());
		
		if ( ! different )
			if ( hasNonIJ1Dimensions(imgPlus) )
				throw new IllegalStateException("Dataset associated with ImagePlus has dimensions incompatible with IJ1");
		
		return different;
	}

	// TODO - this belongs in a public place. Maybe Imglib has a method
	// I should be using instead.
	private void copyData(ImgPlus<? extends RealType<?>> input,
		ImgPlus<? extends RealType<?>> output)
	{
		long[] position = new long[input.numDimensions()];
		Cursor<? extends RealType<?>> inputCur = input.cursor();
		RandomAccess<? extends RealType<?>> outputAcc = output.randomAccess();
		while (inputCur.hasNext()) {
			inputCur.localize(position);
			outputAcc.setPosition(position);
			double value = inputCur.get().getRealDouble();
			outputAcc.get().setReal(value);
		}
	}
	
	private void rebuildNonplanarData(Dataset ds, ImagePlus imp) {
		Dataset tmp = imageTranslator.createDataset(imp);
		long[] dimensions = tmp.getDims();
		ImgPlus<? extends RealType<?>> legacyImgPlus = tmp.getImgPlus();
		ImgFactory factory = ds.getImgPlus().factory();
		Img<? extends RealType<?>> newImg =
			factory.create(dimensions, ds.getType());
		ImgPlus<? extends RealType<?>> newImgPlus = new ImgPlus(newImg,ds);
		copyData(legacyImgPlus, newImgPlus);
		ds.setImgPlus(newImgPlus);
		ds.rebuild();
	}
	
	private void rebuildData(Dataset ds, ImagePlus imp) {
		Dataset tmp = imageTranslator.createDataset(imp);
		ds.setImgPlus(tmp.getImgPlus());
		ds.rebuild();
	}

	private void reconcileDifferences(Dataset ds, ImagePlus imp) {
		
		// is our dataset not sharing planes with the ImagePlus by reference?
		// if so assume any change possible and thus rebuild all
		if ( ! (ds.getImgPlus().getImg() instanceof PlanarAccess) ) {
			rebuildNonplanarData(ds, imp);  // TODO - or copy data pixel by pixel???
			return;
		}

		// was a slice added or deleted?
		if (dimensionsDifferent(ds, imp)) {
			rebuildData(ds, imp);
			return;
		}

		// color data is not shared by reference
		// any change to plane data must somehow be copied back
		// the easiest way to copy back is via new creation
		if (imp.getType() == ImagePlus.COLOR_RGB) {
			rebuildData(ds, imp);
			return;
		}

		// make sure metatdata accurately updated
		metadataTranslator.setDatasetMetadata(ds,imp);
		
		// other data changes
		// TODO - anything need to be done?
		//   Or since we store planes by reference its okay?
		//   Do we really store planes by reference?
		
		ds.update();
	}
}
