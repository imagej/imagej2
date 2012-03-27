/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.legacy.translate;

import ij.CompositeImage;
import ij.ImagePlus;
import ij.ImageStack;
import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import net.imglib2.img.basictypeaccess.PlanarAccess;

/**
 * Creates {@link ImagePlus}es from {@link ImageDisplay}s containing gray data.
 * 
 * @author Barry DeZonia
 */
public class GrayImagePlusCreator implements ImagePlusCreator {

	// -- instance variables --

	private final ImageJ context;
	
	private final GrayPixelHarmonizer pixelHarmonizer;
	private final ColorTableHarmonizer colorTableHarmonizer;
	private final MetadataHarmonizer metadataHarmonizer;
	private final PlaneHarmonizer planeHarmonizer;
	private final PositionHarmonizer positionHarmonizer;

	// -- public interface --

	public GrayImagePlusCreator(ImageJ context) {
		this.context = context;
		pixelHarmonizer = new GrayPixelHarmonizer();
		colorTableHarmonizer = new ColorTableHarmonizer(context);
		metadataHarmonizer = new MetadataHarmonizer();
		planeHarmonizer = new PlaneHarmonizer();
		positionHarmonizer = new PositionHarmonizer();
	}
	
	@Override
	public ImagePlus createLegacyImage(final ImageDisplay display) {
		final ImageDisplayService imageDisplayService =
			context.getService(ImageDisplayService.class);
		final Dataset dataset = imageDisplayService.getActiveDataset(display);
		ImagePlus imp;
		if (LegacyUtils.datasetIsIJ1Compatible(dataset)) {
			imp = makeExactImagePlus(dataset);
		}
		else {
			imp = makeNearestTypeGrayImagePlus(dataset);
			pixelHarmonizer.updateLegacyImage(dataset, imp);
		}
		metadataHarmonizer.updateLegacyImage(dataset, imp);
		if (shouldBeComposite(dataset, imp)) {
			imp = makeCompositeImage(imp);
		}
		colorTableHarmonizer.updateLegacyImage(display, imp);
		positionHarmonizer.updateLegacyImage(display, imp);
		return imp;
	}

	// -- private interface --

	/**
	 * Makes an {@link ImagePlus} that matches dimensions of a {@link Dataset}.
	 * The data values of the ImagePlus to be populated later elsewhere.
	 * 
	 * @param ds - input Dataset to be shape compatible with
	 * @param planeMaker - a PlaneMaker to use to make type correct image planes
	 * @param makeDummyPlanes - save memory by allocating the minimum number of
	 *          planes for the case that we'll be reassigning the planes
	 *          immediately.
	 * @return an ImagePlus whose dimensions are IJ1 compatible with the input
	 *         Dataset.
	 */
	private ImagePlus makeImagePlus(final Dataset ds,
		final PlaneMaker planeMaker, final boolean makeDummyPlanes)
	{

		final int[] dimIndices = new int[5];
		final int[] dimValues = new int[5];
		LegacyUtils.getImagePlusDims(ds, dimIndices, dimValues);

		final int cCount = dimValues[2];
		final int zCount = dimValues[3];
		final int tCount = dimValues[4];

		final ImageStack stack = new ImageStack(dimValues[0], dimValues[1]);

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
	 * Makes an {@link ImagePlus} from a {@link Dataset}. Data is exactly the same
	 * between them as planes of data are shared by reference. Assumes the Dataset
	 * can be represented via plane references (thus XYCZT and backed by
	 * {@link PlanarAccess} and in a type compatible with IJ1). Does not set the
	 * metadata of the ImagePlus. Throws an exception if Dataset axis 0 is not X
	 * or Dataset axis 1 is not Y.
	 */
	// TODO - check that Dataset can be represented exactly
	private ImagePlus makeExactImagePlus(final Dataset ds) {
		final int[] dimIndices = new int[5];
		final int[] dimValues = new int[5];
		LegacyUtils.getImagePlusDims(ds, dimIndices, dimValues);
		LegacyUtils.assertXYPlanesCorrectlyOriented(dimIndices);

		final int c = dimValues[2];
		final int z = dimValues[3];
		final int t = dimValues[4];

		final ImagePlus imp = makeImagePlus(ds, getPlaneMaker(ds), true);

		planeHarmonizer.updateLegacyImage(ds, imp);

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
	private ImagePlus makeNearestTypeGrayImagePlus(final Dataset ds) {
		final PlaneMaker planeMaker = getPlaneMaker(ds);
		return makeImagePlus(ds, planeMaker, false);
	}

	// TODO - is this logic correct? Specifically is testing compChanCnt
	// sufficient?
	private boolean shouldBeComposite(final Dataset ds, final ImagePlus imp) {
		if (ds.getCompositeChannelCount() == 1) return false;
		final int channels = imp.getNChannels();
		if (channels < 2 || channels > 7) return false;
		return true;
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
	private CompositeImage makeCompositeImage(final ImagePlus imp) {
		return new CompositeImage(imp, CompositeImage.COMPOSITE);
	}

	/**
	 * Finds the best {@link PlaneMaker} for a given {@link Dataset}. The best
	 * PlaneMaker is the one that makes planes in the type that can best represent
	 * the Dataset's data values in IJ1.
	 */
	private PlaneMaker getPlaneMaker(final Dataset ds) {
		final boolean signed = ds.isSigned();
		final boolean integer = ds.isInteger();
		final int bitsPerPixel = ds.getType().getBitsPerPixel();
		if (!signed && integer && bitsPerPixel <= 8) return new BytePlaneMaker();
		if (!signed && integer && bitsPerPixel <= 16) return new ShortPlaneMaker();
		return new FloatPlaneMaker();
	}

	/** Helper class to simplify the making of planes of different type data. */
	private interface PlaneMaker {

		Object makePlane(int w, int h);
	}

	/** Makes planes of bytes given width & height. */
	private class BytePlaneMaker implements PlaneMaker {

		public BytePlaneMaker() {
			// nothing to do
		}

		@Override
		public Object makePlane(final int w, final int h) {
			return new byte[w * h];
		}
	}

	/** Makes planes of shorts given width & height. */
	private class ShortPlaneMaker implements PlaneMaker {

		public ShortPlaneMaker() {
			// nothing to do
		}

		@Override
		public Object makePlane(final int w, final int h) {
			return new short[w * h];
		}
	}

	/** Makes planes of floats given width & height. */
	private class FloatPlaneMaker implements PlaneMaker {

		public FloatPlaneMaker() {
			// nothing to do
		}

		@Override
		public Object makePlane(final int w, final int h) {
			return new float[w * h];
		}
	}
}
