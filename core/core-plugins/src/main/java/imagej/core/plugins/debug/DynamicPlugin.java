//
// DynamicPlugin.java
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

package imagej.core.plugins.debug;

import imagej.ImageJ;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.module.AbstractModuleItem;
import imagej.plugin.ImageJModule;
import imagej.plugin.Plugin;
import imagej.util.Log;

import java.util.Map;

/**
 * An example plugin showing how to have a varying number of inputs using the
 * ImageJ module framework.
 * 
 * @author Curtis Rueden
 */
@Plugin(menuPath = "Plugins>Debug>Dynamic Plugin")
public class DynamicPlugin extends ImageJModule {

	public DynamicPlugin() {
		// add one input per available display
		final DisplayService displayService = ImageJ.get(DisplayService.class);
		for (Display display : displayService.getDisplays()) {
			getInfo().addInput(new DisplayModuleItem(display));
		}
	}

	@Override
	public void run() {
		Log.info("DynamicPlugin results:");
		final Map<String, Object> inputs = getInputs();
		for (String name : inputs.keySet()) {
			final Object value = inputs.get(name);
			Log.info("\t" + name + " = " + value);
		}
	}

	private class DisplayModuleItem extends AbstractModuleItem<Integer> {

		private Display display;

		public DisplayModuleItem(final Display display) {
			super(DynamicPlugin.this.getInfo());
			this.display = display;
		}

		@Override
		public Class<Integer> getType() {
			return Integer.class;
		}

		@Override
		public String getName() {
			return display.getName();
		}

	}

}
