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

import ij.ImagePlus;
import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.display.ImageDisplay;
import imagej.ext.display.DisplayService;
import net.imglib2.meta.AxisType;

/**
 * Creates {@link ImageDisplay}s from {@link ImagePlus}es containing color data.
 * 
 * @author Barry DeZonia
 */
public class ColorDisplayCreator implements DisplayCreator {

	// -- instance variables --

	private final ImageJ context;

	private final ColorPixelHarmonizer pixelHarmonizer;
	private final ColorTableHarmonizer colorTableHarmonizer;
	private final MetadataHarmonizer metadataHarmonizer;
	private final CompositeHarmonizer compositeHarmonizer;
	private final OverlayHarmonizer overlayHarmonizer;
	private final PositionHarmonizer positionHarmonizer;
	
	// NB - OverlayHarmonizer required because IJ1 plugins can hatch displays
	// while
	// avoiding the Harmonizer. Not required in the Display->ImagePlus direction
	// as
	// Harmonizer always catches that case.

	// -- constructor --

	public ColorDisplayCreator(final ImageJ context) {
		this.context = context;
		pixelHarmonizer = new ColorPixelHarmonizer();
		colorTableHarmonizer = new ColorTableHarmonizer(context);
		metadataHarmonizer = new MetadataHarmonizer();
		compositeHarmonizer = new CompositeHarmonizer();
		overlayHarmonizer = new OverlayHarmonizer(context);
		positionHarmonizer = new PositionHarmonizer();
	}

	// -- public interface --

	@Override
	public ImageDisplay createDisplay(final ImagePlus imp) {
		return createDisplay(imp, LegacyUtils.getPreferredAxisOrder());
	}

	@Override
	public ImageDisplay createDisplay(final ImagePlus imp,
		final AxisType[] preferredOrder)
	{
		final Dataset ds = makeColorDataset(imp, preferredOrder);
		pixelHarmonizer.updateDataset(ds, imp);
		metadataHarmonizer.updateDataset(ds, imp);
		compositeHarmonizer.updateDataset(ds, imp);

		final DisplayService displayService =
			context.getService(DisplayService.class);
		// CTR FIXME
		final ImageDisplay display =
			(ImageDisplay) displayService.createDisplay(ds.getName(), ds);

		colorTableHarmonizer.updateDisplay(display, imp);
		overlayHarmonizer.updateDisplay(display, imp);
		positionHarmonizer.updateDisplay(display, imp);

		return display;
	}

	// -- private interface --

	/**
	 * Makes a color {@link Dataset} from an {@link ImagePlus}. Color Datasets
	 * have isRgbMerged() true, channels == 3, and bitsperPixel == 8. Does not
	 * populate the data of the returned Dataset. That is left to other utility
	 * methods. Does not set metadata of Dataset. Throws exceptions if input
	 * ImagePlus is not single channel RGB.
	 */
	private Dataset makeColorDataset(final ImagePlus imp,
		final AxisType[] preferredOrder)
	{
		final int x = imp.getWidth();
		final int y = imp.getHeight();
		final int c = imp.getNChannels();
		final int z = imp.getNSlices();
		final int t = imp.getNFrames();

		if (imp.getType() != ImagePlus.COLOR_RGB) {
			throw new IllegalArgumentException(
				"can't make a color Dataset from a nonRGB ImagePlus");
		}

		if (c != 1) {
			throw new IllegalArgumentException(
				"can't make a color Dataset from a multichannel ColorProcessor stack");
		}

		final int[] inputDims = new int[] { x, y, 3, z, t };
		final AxisType[] axes = LegacyUtils.orderedAxes(preferredOrder, inputDims);
		final long[] dims = LegacyUtils.orderedDims(axes, inputDims);
		final String name = imp.getTitle();
		final int bitsPerPixel = 8;
		final boolean signed = false;
		final boolean floating = false;
		final DatasetService datasetService =
			context.getService(DatasetService.class);
		final Dataset ds =
			datasetService.create(dims, name, axes, bitsPerPixel, signed, floating);

		ds.setRGBMerged(true);

		DatasetUtils.initColorTables(ds);

		return ds;
	}
}
