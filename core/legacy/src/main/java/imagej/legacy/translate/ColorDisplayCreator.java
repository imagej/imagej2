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

import ij.ImagePlus;
import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.DatasetFactory;
import imagej.data.display.ImageDisplay;
import imagej.ext.display.DisplayService;
import imagej.legacy.translate.LegacyUtils;
import net.imglib2.img.Axis;


/**
 * Creates ImageDisplays from ImagePluses containing color data
 * 
 * @author Barry DeZonia
 *
 */
public class ColorDisplayCreator implements DisplayCreator {

	// -- instance variables --

	private final ColorPixelHarmonizer pixelHarmonizer = new ColorPixelHarmonizer();
	private final ColorTableHarmonizer colorTableHarmonizer = new ColorTableHarmonizer();
	private final MetadataHarmonizer metadataHarmonizer = new MetadataHarmonizer();
	private final CompositeHarmonizer compositeHarmonizer = new CompositeHarmonizer();
	private final OverlayHarmonizer overlayHarmonizer = new OverlayHarmonizer();
	// NB - OverlayHarmonizer required because IJ1 plugins can hatch displays while
	// avoiding the Harmonizer. Not required in the Display->ImagePlus direction as
	// Harmonizer always catches that case.
	
	// -- public interface --

	@Override
	public ImageDisplay createDisplay(ImagePlus imp) {
		return createDisplay(imp, LegacyUtils.getPreferredAxisOrder());
	}

	@Override
	public ImageDisplay createDisplay(ImagePlus imp, Axis[] preferredOrder) {
		final Dataset ds = makeColorDataset(imp, preferredOrder);
		pixelHarmonizer.updateDataset(ds, imp);
		metadataHarmonizer.updateDataset(ds, imp);
		compositeHarmonizer.updateDataset(ds, imp);

		final DisplayService displayService = ImageJ.get(DisplayService.class);
		// CTR FIXME
		final ImageDisplay display =
			(ImageDisplay) displayService.createDisplay(ds.getName(), ds);

		colorTableHarmonizer.updateDisplay(display, imp);
		overlayHarmonizer.updateDisplay(display, imp);

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
		final Axis[] preferredOrder)
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
		final Axis[] axes = LegacyUtils.orderedAxes(preferredOrder, inputDims);
		final long[] dims = LegacyUtils.orderedDims(axes, inputDims);
		final String name = imp.getTitle();
		final int bitsPerPixel = 8;
		final boolean signed = false;
		final boolean floating = false;
		final Dataset ds =
			DatasetFactory.create(dims, name, axes, bitsPerPixel, signed, floating);

		ds.setRGBMerged(true);

		return ds;
	}

}
