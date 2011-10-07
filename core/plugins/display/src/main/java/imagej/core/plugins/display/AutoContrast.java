//
// AutoContrast.java
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

package imagej.core.plugins.display;

import imagej.data.Dataset;
import imagej.data.display.DatasetView;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.util.Log;

import java.util.Iterator;
import java.util.List;

import net.imglib2.display.RealLUTConverter;
import net.imglib2.type.numeric.RealType;

/**
 * Plugin that auto-thresholds each channel.
 * 
 * @author Adam Fraser
 */
@Plugin(menu = {
	@Menu(label = "Image"),
	@Menu(label = "Adjust"),
	@Menu(label = "Auto-Contrast", accelerator = "control shift alt L",
		weight = 0) })
public class AutoContrast implements ImageJPlugin {

	static final int AUTO_THRESHOLD = 5000;
	static int autoThreshold;

	@Parameter(required = true, persist = false)
	private DatasetView view;

	@Override
	public void run() {
		final Dataset dataset = view.getData();

		final Iterator<?> itr = dataset.getImgPlus().getImg().cursor();
		final int[] histogram = new int[256];
		int pixelCount = 0;
		while (itr.hasNext()) {
			final String v = itr.next().toString();
			// TODO: v needs to be scaled to 0-255 for non 8-bit images
			final int val = Integer.valueOf(v);
			histogram[val] += 1;
			pixelCount += 1;
		}

		if (autoThreshold < 10) autoThreshold = AUTO_THRESHOLD;
		else autoThreshold /= 2;
		final int threshold = pixelCount / autoThreshold;
		final int limit = pixelCount / 10;
		int i = -1;
		boolean found = false;
		int count;
		do {
			i++;
			count = histogram[i];
			if (count > limit) count = 0;
			found = count > threshold;
		}
		while (!found && i < 255);
		final int hmin = i;
		i = 256;
		do {
			i--;
			count = histogram[i];
			if (count > limit) count = 0;
			found = count > threshold;
		}
		while (!found && i > 0);
		final int hmax = i;
		double min;
		double max;
		if (hmax >= hmin) {
			// XXX
			// Was: min = stats.histMin + hmin * stats.binSize;
			// max = stats.histMin + hmax * stats.binSize;
			final double histMin = 0;
			min = histMin + hmin * 1.0;
			max = histMin + hmax * 1.0;

			// XXX
//			if (RGBImage && roi!=null) 
//				imp.setRoi(roi);
		}
		else {
			// TODO: respect other image types
			min = 0;
			max = 255;
		}

		Log.debug("AutoContrast: " + hmin + "," + hmax);
		final List<RealLUTConverter<? extends RealType<?>>> converters =
			view.getConverters();
		for (final RealLUTConverter<? extends RealType<?>> conv : converters) {
			conv.setMin(min);
			conv.setMax(max);
		}
		view.getProjector().map();
		view.update();

	}

}
