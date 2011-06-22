//
// MixedModeTranslator.java
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
import imagej.display.Display;
import net.imglib2.img.Axis;

/**
 * This class used to create Datasets from ImagePluses that are RGB and yet are
 * not single channel
 * 
 * @author Barry DeZonia
 */
public class MixedModeTranslator implements ImageTranslator {

	@Override
	public Display createDisplay(final ImagePlus imp) {
		return createDisplay(imp, LegacyUtils.getPreferredAxisOrder());
	}

	@Override
	public Display
		createDisplay(final ImagePlus imp, final Axis[] preferredOrder)
	{
		final Dataset ds =
			LegacyUtils.makeGrayDatasetFromColorImp(imp, preferredOrder);
		LegacyUtils.setDatasetGrayDataFromColorImp(ds, imp);
		LegacyUtils.setDatasetMetadata(ds, imp);
		LegacyUtils.setDatasetCompositeVariables(ds, imp);
		LegacyUtils.setViewLuts(ds, imp); // TODO probably does nothing since
																			// Dataset not in view?

		// CTR FIXME - Create a Display here and return it.
		return null;
	}

	// TODO - do we need this in the other direction too?? if isRGBMerged == true
	// and unsigned byte type and channels evenly divisible by 3. May want to
	// not worry about this to phase out ColorProcessor reliance.
	@Override
	public ImagePlus createLegacyImage(final Display display) {
		throw new UnsupportedOperationException(
			"createLegacyImage(Display) is not implemented for this translator");
	}

}
