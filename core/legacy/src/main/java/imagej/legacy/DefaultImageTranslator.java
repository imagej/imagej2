//
// DefaultImageTranslator.java
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
import imagej.display.DisplayService;
import net.imglib2.img.Axis;

/**
 * The default {@link ImageTranslator} between legacy and modern ImageJ image
 * structures. It delegates to the appropriate more specific
 * {@link ImageTranslator}, based on the type of data being translated.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public class DefaultImageTranslator implements ImageTranslator {

	private final RGBImageTranslator rgbTranslator = new RGBImageTranslator();
	private final GrayscaleImageTranslator grayscaleTranslator =
		new GrayscaleImageTranslator();
	private final MixedModeTranslator mixedModeTranslator =
		new MixedModeTranslator();

	/**
	 * Creates a {@link Display} from an {@link ImagePlus}. Shares planes of data
	 * when possible.
	 */
	@Override
	public Display createDisplay(final ImagePlus imp) {
		if (imp.getType() == ImagePlus.COLOR_RGB) {
			if (imp.getNChannels() == 1) return rgbTranslator.createDisplay(imp);
			return mixedModeTranslator.createDisplay(imp);
		}
		return grayscaleTranslator.createDisplay(imp);
	}

	/**
	 * Creates a {@link Display} from an {@link ImagePlus}. Shares planes of data
	 * when possible. Builds Display with preferred Axis ordering.
	 */
	@Override
	public Display
		createDisplay(final ImagePlus imp, final Axis[] preferredOrder)
	{
		if (imp.getType() == ImagePlus.COLOR_RGB) {
			if (imp.getNChannels() == 1) return rgbTranslator.createDisplay(imp,
				preferredOrder);
			return mixedModeTranslator.createDisplay(imp, preferredOrder);
		}
		return grayscaleTranslator.createDisplay(imp, preferredOrder);
	}

	/**
	 * Creates an {@link ImagePlus} from a {@link Display}. Shares planes of data
	 * when possible.
	 */
	@Override
	public ImagePlus createLegacyImage(final Display display) {
		final DisplayService displayService = ImageJ.get(DisplayService.class);
		final Dataset ds = displayService.getActiveDataset(display);
		if (ds.isRGBMerged()) {
			return rgbTranslator.createLegacyImage(display);
		}

		return grayscaleTranslator.createLegacyImage(display);
	}

}
