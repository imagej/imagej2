//
// GradientImage.java
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

package imagej.core.plugins;

import imagej.model.AxisLabel;
import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.plugin.gui.WidgetStyle;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;

/**
 * TODO
 * 
 * @author Curtis Rueden
 * @author Rick Lentz
 */
@Plugin(menuPath = "PureIJ2>Process>Gradient")
public class GradientImage implements ImageJPlugin {

	@Parameter(min = "1", max = "2000", widgetStyle = WidgetStyle.NUMBER_SCROLL_BAR)
	private int width = 512;

	@Parameter(min = "1", max = "2000", widgetStyle = WidgetStyle.NUMBER_SLIDER)
	private int height = 512;

	@Parameter(output = true)
	private Dataset dataset;

	@Override
	public void run() {
		byte[] data = new byte[width * height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int index = y * width + x;
				data[index] = (byte) (x + y);
			}
		}

		final String name = "Gradient Image";
		final int[] dims = { width, height };
		final AxisLabel[] axes = { AxisLabel.X, AxisLabel.Y };
		dataset = Dataset.create(name, new UnsignedByteType(), dims, axes);
		dataset.setPlane(0, data);
	}

}
