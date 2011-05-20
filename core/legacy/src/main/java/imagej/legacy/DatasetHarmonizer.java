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

import net.imglib2.img.Axes;
import net.imglib2.img.ImgPlus;
import net.imglib2.img.basictypeaccess.PlanarAccess;
import ij.CompositeImage;
import ij.ImagePlus;
import ij.ImageStack;
import imagej.data.Dataset;
import imagej.util.Index;


/**
 * 
 * @author Barry DeZonia
 *
 * Updates a Dataset's organization, data, and metadata to match
 * a given ImagePlus.
 *  
 */
public class DatasetHarmonizer {

	private LegacyMetadataTranslator metadataTranslator =
		new LegacyMetadataTranslator();

	private ImageTranslator imageTranslator;
	
	public DatasetHarmonizer(ImageTranslator translator) {
		imageTranslator = translator;
	}
	
	/** changes the data within a Dataset to match data in an ImagePlus */
	public void harmonize(Dataset ds, ImagePlus imp) {
		
		// is our dataset not sharing planes with the ImagePlus by reference?
		// if so assume any change possible and thus rebuild all
		if ( ! (ds.getImgPlus().getImg() instanceof PlanarAccess) ) {
			rebuildNonplanarData(ds, imp);
			// NB - as RGBImageTranslator defined RGBMerged doesn't need to be planar
			ds.setRGBMerged(imp.getType() == ImagePlus.COLOR_RGB);
			setCompositeChannels(ds, imp);
			return;
		}

		// color data is not shared by reference
		// any change to plane data must somehow be copied back
		// the easiest way to copy back is via new creation
		if (imp.getType() == ImagePlus.COLOR_RGB) {
			rebuildData(ds, imp);
			ds.setRGBMerged(true);
			setCompositeChannels(ds, imp);
			return;
		}
		
		// if here we know its not a RGB imp. If we were a color Dataset
		// then we no longer are.
		ds.setRGBMerged(false);

		// set num compos channels to display at once based on makeup of ImagePlus
		setCompositeChannels(ds, imp);
		
		// was a slice added or deleted?
		if (dimensionsDifferent(ds, imp)) {
			rebuildData(ds, imp);
			return;
		}

		// if here we know we have planar backing
		// the plane references could have changed in some way:
		//   - setPixels, setProcessor, stack rearrangement, etc.
		// its easier to always reassign them rather than
		//   calculate exactly what to do
		assignPlaneReferences(ds, imp);
		
		// make sure metadata accurately updated
		metadataTranslator.setDatasetMetadata(ds,imp);
		
		// TODO - any other cases?

		// Since we are storing planes by reference we're done
		
		// assume plugin changed ImagePlus in some way and report Dataset changed 
		ds.update();
	}

	// -- private helpers -- 
	
	/** fills a nonplanar Dataset's values with data from an ImagePlus */
	private void rebuildNonplanarData(Dataset ds, ImagePlus imp) {
		Dataset tmpDs = imageTranslator.createDataset(imp);
		ds.copyDataFrom(tmpDs);
	}
	
	/** fills a Dataset's values with data from an ImagePlus */
	private void rebuildData(Dataset ds, ImagePlus imp) {
		Dataset tmpDs = imageTranslator.createDataset(imp);
		ds.setImgPlus(tmpDs.getImgPlus());
	}

	/** sets the Dataset's number of composite channels to display simultaneously
	 * based on an input ImagePlus' makeup */
	private void setCompositeChannels(Dataset ds, ImagePlus imp) {
		if ((imp instanceof CompositeImage) &&
				(((CompositeImage)imp).getMode() == CompositeImage.COMPOSITE))
			ds.setCompositeChannelCount(imp.getNChannels());
		else if (imp.getType() == ImagePlus.COLOR_RGB)
			ds.setCompositeChannelCount(3);
		else
			ds.setCompositeChannelCount(1);
	}
	
	/** determines whether a Dataset and an ImagePlus have different dimensionality */
	private boolean dimensionsDifferent(Dataset ds, ImagePlus imp) {
		ImgPlus<?> imgPlus = ds.getImgPlus();

		boolean different =
			dimensionDifferent(imgPlus, ds.getAxisIndex(Axes.X), imp.getWidth()) ||
			dimensionDifferent(imgPlus, ds.getAxisIndex(Axes.Y), imp.getHeight()) ||
			dimensionDifferent(imgPlus, ds.getAxisIndex(Axes.CHANNEL), imp.getNChannels()) ||
			dimensionDifferent(imgPlus, ds.getAxisIndex(Axes.Z), imp.getNSlices()) ||
			dimensionDifferent(imgPlus, ds.getAxisIndex(Axes.TIME), imp.getNFrames());
		
		if ( ! different )
			if ( LegacyUtils.hasNonIJ1Axes(imgPlus) )
				throw new IllegalStateException(
					"Dataset associated with ImagePlus has axes incompatible with IJ1");
		
		return different;
	}

	/** determines whether a single dimension in an ImgPlus differs from
	 *  a given value */ 
	private boolean dimensionDifferent(ImgPlus<?> imgPlus, int axis, int value) {
		if (axis >= 0)
			return imgPlus.dimension(axis) != value;
		// axis < 0 : not present in imgPlus
		return value != 1;
	}

	/** assigns the plane references of a planar Dataset to match the plane
	 *  references of a given ImagePlus
	 */
	private void assignPlaneReferences(Dataset ds, ImagePlus imp) {
		ImageStack stack = imp.getStack();
		if (stack == null) {  // just a 2d image
			Object pixels = imp.getProcessor().getPixels();
			ds.setPlane(0, pixels);
			return;
		}
		int zIndex = ds.getAxisIndex(Axes.Z);
		int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		int tIndex = ds.getAxisIndex(Axes.TIME);
		int z = (int) ( (zIndex < 0) ? 1 : ds.getImgPlus().dimension(zIndex) );
		int c = (int) ( (cIndex < 0) ? 1 : ds.getImgPlus().dimension(cIndex) );
		int t = (int) ( (tIndex < 0) ? 1 : ds.getImgPlus().dimension(tIndex) );
		long[] planeDims = new long[ds.getImgPlus().numDimensions()-2];
		if (zIndex >= 0) planeDims[zIndex-2] = z;
		if (cIndex >= 0) planeDims[cIndex-2] = c;
		if (tIndex >= 0) planeDims[tIndex-2] = t;
		long[] planePos = new long[planeDims.length];
		int imagejPlaneNumber = 1;
		for (int ti = 0; ti < t; ti++) {
			if (tIndex >= 0) planePos[tIndex-2] = ti;
			for (int zi = 0; zi < z; zi++) {
				if (zIndex >= 0) planePos[zIndex-2] = zi;
				for (int ci = 0; ci < c; ci++) {
					if (cIndex >= 0) planePos[cIndex-2] = ci;
					long imglibPlaneNumber = Index.indexNDto1D(planeDims, planePos);
					Object plane = stack.getPixels(imagejPlaneNumber);
					ds.setPlane((int)imglibPlaneNumber, plane);
					imagejPlaneNumber++;
				}
			}
		}
	}
}
