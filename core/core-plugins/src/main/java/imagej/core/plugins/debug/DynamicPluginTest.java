//
// DynamicPluginTest.java
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
import imagej.data.Dataset;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.ext.module.DefaultModuleItem;
import imagej.ext.plugin.DynamicPlugin;
import imagej.ext.plugin.Plugin;
import imagej.util.Log;

import java.util.Map;

import net.imglib2.img.Axes;
import net.imglib2.img.Axis;

/**
 * An example ImageJ plugin with a varying number of inputs and outputs.
 * 
 * @author Curtis Rueden
 */
@Plugin(menuPath = "Plugins>Sandbox>Dynamic Plugin")
public class DynamicPluginTest extends DynamicPlugin {

	public DynamicPluginTest() {
		// add one input and one output per available display
		final DisplayService displayService = ImageJ.get(DisplayService.class);
		for (final Display display : displayService.getDisplays()) {
			final String name = display.getName();
			final DefaultModuleItem<Integer> input =
				new DefaultModuleItem<Integer>(this, name, Integer.class);
			input.setMinimumValue(1);
			addInput(input);
			final DefaultModuleItem<Dataset> output =
				new DefaultModuleItem<Dataset>(this, name + "-data", Dataset.class);
			addOutput(output);
		}
	}

	@Override
	public void run() {
		Log.info("DynamicPlugin results:");
		final Map<String, Object> inputs = getInputs();
		for (final String name : inputs.keySet()) {
			// harvest input value
			final int value = (Integer) inputs.get(name);

			// print some debugging information
			Log.info("\t" + name + " = " + value);

			// create dataset and assign to the corresponding output
			final String outputName = name + "-data";
			final long[] dims = { value, value };
			final Axis[] axes = { Axes.X, Axes.Y };
			final int bitsPerPixel = 8;
			final boolean signed = false;
			final boolean floating = false;
			final Dataset dataset =
				Dataset.create(dims, outputName, axes, bitsPerPixel, signed, floating);
			setOutput(outputName, dataset);
		}
	}

}
