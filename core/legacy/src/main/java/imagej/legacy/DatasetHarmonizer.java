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

import ij.CompositeImage;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import imagej.data.Dataset;
import imagej.util.Index;
import net.imglib2.RandomAccess;
import net.imglib2.img.Axes;
import net.imglib2.img.ImgPlus;
import net.imglib2.img.basictypeaccess.PlanarAccess;
import net.imglib2.type.numeric.RealType;

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

	private final MetadataTranslator metadataTranslator;
	private final ImageTranslator imageTranslator;

	public DatasetHarmonizer(final ImageTranslator translator) {
		imageTranslator = translator;
		metadataTranslator = new MetadataTranslator();
	}

	/**
	 * Changes the data within an {@link ImagePlus} to match data in a
	 * {@link Dataset}. Assumes Dataset has planar primitive access in an IJ1
	 * compatible format.
	 */
	public void updateLegacyImage(final Dataset ds, final ImagePlus imp) {
		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);
		final int c = (int) ((cIndex < 0) ? 1 : ds.getImgPlus().dimension(cIndex));
		final int z = (int) ((zIndex < 0) ? 1 : ds.getImgPlus().dimension(zIndex));
		final int t = (int) ((tIndex < 0) ? 1 : ds.getImgPlus().dimension(tIndex));
		final ImagePlus newImp = imageTranslator.createLegacyImage(ds);
		imp.setStack(newImp.getStack());
		imp.setDimensions(c, z, t);
		metadataTranslator.setImagePlusMetadata(ds, imp);
	}

	/**
	 * Changes the data within a {@link Dataset} to match data in an
	 * {@link ImagePlus}.
	 */
	public void updateDataset(final Dataset ds, final ImagePlus imp) {

		// is our dataset not sharing planes with the ImagePlus by reference?
		// if so assume any change possible and thus rebuild all
		if (!(ds.getImgPlus().getImg() instanceof PlanarAccess)) {
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

		// can I not assign plane references?
		if (planeTypesDifferent(ds, imp)) {
			assignDatasetValues(ds, imp);
		}
		else {
			// if here we know we have planar backing of right type.
			// The plane references could have changed in some way:
			// - setPixels, setProcessor, stack rearrangement, etc.
			// its easier to always reassign them rather than
			// calculate exactly what to do
			assignDatasetPlaneReferences(ds, imp);
		}

		// make sure metadata accurately updated
		metadataTranslator.setDatasetMetadata(ds, imp);

		// TODO - any other cases?

		// Since we are storing planes by reference we're done

		// assume plugin changed ImagePlus in some way and report Dataset changed
		ds.update();
	}

	// -- private helpers --

	/**
	 * Fills a non-planar {@link Dataset}'s values with data from an
	 * {@link ImagePlus}.
	 */
	private void rebuildNonplanarData(final Dataset ds, final ImagePlus imp) {
		final Dataset tmpDs = imageTranslator.createDataset(imp);
		ds.copyDataFrom(tmpDs);
	}

	/** Fills a {@link Dataset}'s values with data from an {@link ImagePlus}. */
	private void rebuildData(final Dataset ds, final ImagePlus imp) {
		final Dataset tmpDs = imageTranslator.createDataset(imp);
		ds.setImgPlus(tmpDs.getImgPlus());
	}

	/**
	 * Sets the {@link Dataset}'s number of composite channels to display
	 * simultaneously based on an input {@link ImagePlus}' makeup.
	 */
	private void setCompositeChannels(final Dataset ds, final ImagePlus imp) {
		if ((imp instanceof CompositeImage) &&
			(((CompositeImage) imp).getMode() == CompositeImage.COMPOSITE)) ds
			.setCompositeChannelCount(imp.getNChannels());
		else if (imp.getType() == ImagePlus.COLOR_RGB) ds
			.setCompositeChannelCount(3);
		else ds.setCompositeChannelCount(1);
	}

	/**
	 * Determines whether a {@link Dataset} and an {@link ImagePlus} have
	 * different dimensionality.
	 */
	private boolean dimensionsDifferent(final Dataset ds, final ImagePlus imp) {
		final ImgPlus<?> imgPlus = ds.getImgPlus();

		final boolean different =
			dimensionDifferent(imgPlus, ds.getAxisIndex(Axes.X), imp.getWidth()) ||
				dimensionDifferent(imgPlus, ds.getAxisIndex(Axes.Y), imp.getHeight()) ||
				dimensionDifferent(imgPlus, ds.getAxisIndex(Axes.CHANNEL), imp
					.getNChannels()) ||
				dimensionDifferent(imgPlus, ds.getAxisIndex(Axes.Z), imp.getNSlices()) ||
				dimensionDifferent(imgPlus, ds.getAxisIndex(Axes.TIME), imp
					.getNFrames());

		if (!different) if (LegacyUtils.hasNonIJ1Axes(imgPlus)) throw new IllegalStateException(
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

	/**
	 * Returns true if a planar {@link Dataset} and an {@link ImagePlus} have
	 * different primitive array backing.
	 */
	private boolean planeTypesDifferent(final Dataset ds, final ImagePlus imp) {
		final RealType<?> dsType = ds.getType();
		final int bitsPerPixel = dsType.getBitsPerPixel();
		final boolean integer = ds.isInteger();
		final boolean signed = ds.isSigned();
		switch (imp.getType()) {
			case ImagePlus.GRAY8:
				if ((bitsPerPixel == 8) && (integer) && (!signed)) return false;
				break;
			case ImagePlus.GRAY16:
				if ((bitsPerPixel == 16) && (integer) && (!signed)) return false;
				break;
			case ImagePlus.GRAY32:
				if ((bitsPerPixel == 32) && (!integer) && (signed)) return false;
				break;
		}
		return true;
	}

	/**
	 * Assigns actual pixel values of {@link Dataset}. Needed for those types that
	 * do not directly map from IJ1 types.
	 */
	private void assignDatasetValues(final Dataset ds, final ImagePlus imp) {
		final int x = imp.getWidth();
		final int y = imp.getHeight();
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		final int tIndex = ds.getAxisIndex(Axes.TIME);
		final int z = (int) ((zIndex < 0) ? 1 : ds.getImgPlus().dimension(zIndex));
		final int c = (int) ((cIndex < 0) ? 1 : ds.getImgPlus().dimension(cIndex));
		final int t = (int) ((tIndex < 0) ? 1 : ds.getImgPlus().dimension(tIndex));
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
						accessor.setPosition(yi, 1);
						for (int xi = 0; xi < x; xi++) {
							accessor.setPosition(xi, 0);
							final float value = proc.getf(xi, yi);
							accessor.get().setReal(value);
						}
					}
				}
			}
		}
	}

	/**
	 * Assigns the plane references of a planar {@link Dataset} to match the plane
	 * references of a given {@link ImagePlus}.
	 */
	private void assignDatasetPlaneReferences(final Dataset ds,
		final ImagePlus imp)
	{
		final ImageStack stack = imp.getStack();
		if (stack == null) { // just a 2d image
			final Object pixels = imp.getProcessor().getPixels();
			ds.setPlane(0, pixels);
			return;
		}
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		final int tIndex = ds.getAxisIndex(Axes.TIME);
		final int z = (int) ((zIndex < 0) ? 1 : ds.getImgPlus().dimension(zIndex));
		final int c = (int) ((cIndex < 0) ? 1 : ds.getImgPlus().dimension(cIndex));
		final int t = (int) ((tIndex < 0) ? 1 : ds.getImgPlus().dimension(tIndex));
		final long[] planeDims = new long[ds.getImgPlus().numDimensions() - 2];
		if (zIndex >= 0) planeDims[zIndex - 2] = z;
		if (cIndex >= 0) planeDims[cIndex - 2] = c;
		if (tIndex >= 0) planeDims[tIndex - 2] = t;
		final long[] planePos = new long[planeDims.length];
		int imagejPlaneNumber = 1;
		for (int ti = 0; ti < t; ti++) {
			if (tIndex >= 0) planePos[tIndex - 2] = ti;
			for (int zi = 0; zi < z; zi++) {
				if (zIndex >= 0) planePos[zIndex - 2] = zi;
				for (int ci = 0; ci < c; ci++) {
					if (cIndex >= 0) planePos[cIndex - 2] = ci;
					final long imglibPlaneNumber =
						Index.indexNDto1D(planeDims, planePos);
					final Object plane = stack.getPixels(imagejPlaneNumber);
					ds.setPlane((int) imglibPlaneNumber, plane);
					imagejPlaneNumber++;
				}
			}
		}
	}

}
