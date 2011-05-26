//
// SetDisplayScale.java
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

import imagej.ImageJ;
import imagej.display.AbstractDatasetView;
import imagej.display.Display;
import imagej.display.DisplayManager;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

import java.util.List;

import net.imglib2.display.RealLUTConverter;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * Plugin that sets the Min and Max for scaling of display values. Sets the same
 * min/max for each channel.
 * 
 * @author Grant Harris
 */
@Plugin(menuPath = "Image>SetDisplayScale")
//		menu = {
//	@Menu(label = "Image", mnemonic = 'f'),
//	@Menu(label = "Set Diplay Scale")})
public class SetDisplayScale<T extends RealType<T> & NativeType<T>> implements
	ImageJPlugin
{

	@Parameter
	// (min="0", max = "255")
	private final int max = 255;

	@Parameter
	// (min = "0", max = "255")
	private final int min = 0;

	@Override
	public void run() {
		final DisplayManager manager = ImageJ.get(DisplayManager.class);
		final Display display = manager.getActiveDisplay();
		if (display == null) {
			return; // headless UI or no open images
		}
		AbstractDatasetView sdv =  (AbstractDatasetView) display.getActiveView();
		List<RealLUTConverter<? extends RealType<?>>> converters = sdv.getConverters();
		for (RealLUTConverter<? extends RealType<?>> realLUTConverter : converters) {
			realLUTConverter.setMin(min);
			realLUTConverter.setMax(max);
		}
		sdv.getProjector().map();
		sdv.update();

	}

}
