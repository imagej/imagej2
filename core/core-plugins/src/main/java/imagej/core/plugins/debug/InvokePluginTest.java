//
// InvokePluginTest.java
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
import imagej.ext.module.Module;
import imagej.ext.module.ModuleException;
import imagej.ext.module.ModuleInfo;
import imagej.ext.module.ModuleItem;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Plugin;
import imagej.ext.plugin.PluginModule;
import imagej.ext.plugin.PluginModuleInfo;
import imagej.ext.plugin.PluginService;
import imagej.ext.plugin.RunnablePlugin;
import imagej.util.Log;

import java.util.List;
import java.util.Map;

/**
 * TODO
 * 
 * @author Grant Harris
 */
@Plugin(menuPath = "Plugins>Sandbox>InvokePluginTest")
public class InvokePluginTest implements ImageJPlugin {

	private final PluginService pluginService = ImageJ.get(PluginService.class);

	private Object[] passedParameters;
	private Dataset dataset;

	@Override
	public void run() {
		testRun();
	}

	void testRun() {
		final String name = "Untitled";
		final String bitDepth = "8-bit";
		final boolean signed = false;
		final boolean floating = false;
		final String fillType = "Ramp";
		final int width = 512;
		final int height = 512;
		invoke("imagej.io.plugins.NewImage", name, bitDepth, signed, floating,
			fillType, width, height);
	}

	// TODO...
	// private void invoke(String plugin, Map<String, Object> parameters) {}

	private void invoke(final String plugin, final Object... parameters) {
		try {
			final ModuleInfo info = findModuleInfoFor(plugin);
			// add parameter inputFile
			// info.inputs();
			final PluginModule pModule = (PluginModule) info.createModule();
			if (this.dataset == null) {
				Util.setInputFromActive(pModule);
			}
			if (setInputsFromParameters(pModule, parameters)) {
				pluginService.run(pModule, true);
			}
			else {
				pluginService.run(info, true);
			}
			final Map<String, Object> outMap = pModule.getOutputs();
			for (final Map.Entry<String, Object> entry : outMap.entrySet()) {
				System.out.println("Output: " + entry.getKey() + "/" +
					entry.getValue());
			}
		}
		catch (final ModuleException ex) {
			Log.info(ex);
		}
	}

	public ModuleInfo findModuleInfoFor(final String pluginName) {
		final List<PluginModuleInfo<RunnablePlugin>> plugins =
			pluginService.getRunnablePluginsOfClass(pluginName);
		return plugins == null || plugins.size() == 0 ? null : plugins.get(0);
	}

	public boolean setInputsFromParameters(final Module module,
		final Object... params)
	{
		final Map<String, Object> inputMap = module.getInputs();
		if (inputMap.size() != params.length) {
			System.err.println("inputMap.size() != parameters.length");
			return false;
		}
		final Iterable<ModuleItem<?>> inputs = module.getInfo().inputs();
		int n = 0;
		for (final ModuleItem<?> item : inputs) {
			final String name = item.getName();
			System.out.print("input  " + name);
			final Object pValue = params[n];
			System.out.println(", param[" + n + "] = " + pValue);
			// final Object value = module.getInput(name);
			module.setInput(name, pValue);
			module.setResolved(name, true);
			n++;
		}
		return true;
	}

}
