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

package imagej.core.plugins.debug;

import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.ext.module.Module;
import imagej.ext.module.ModuleService;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.ext.plugin.PluginService;
import imagej.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * A test of {@link PluginService#run}. The source code demonstrates two
 * different ways of invoking a plugin programmatically: with a list of
 * arguments, or by declaring them in a {@link Map}. The latter mechanism is
 * more flexible in that you can provide any subset of input values of your
 * choice, leaving the rest to be harvested in other ways.
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 */
@Plugin(menuPath = "Plugins>Sandbox>Invoke Plugin Test", headless = true)
public class InvokePluginTest implements ImageJPlugin {

	@Parameter(persist = false)
	private ModuleService moduleService;

	@Parameter(persist = false)
	private PluginService pluginService;

	@Override
	public void run() {
		final Future<Module> future = invokeWithArgs(); // or invokeWithMap()
		final Module module = moduleService.waitFor(future);
		final Dataset dataset = (Dataset) module.getOutput("dataset");
		Log.info("InvokePluginTest: dataset = " + dataset);
	}

	public Future<Module> invokeWithArgs() {
		final DatasetService datasetService = null; // will be autofilled
		final String name = "Untitled";
		final String bitDepth = "8-bit";
		final boolean signed = false;
		final boolean floating = false;
		final String fillType = "Ramp";
		final long width = 512;
		final long height = 512;
		return pluginService.run("imagej.io.plugins.NewImage", datasetService,
			name, bitDepth, signed, floating, fillType, width, height);
	}

	public Future<Module> invokeWithMap() {
		final Map<String, Object> inputMap = new HashMap<String, Object>();
		inputMap.put("name", "Untitled");
		inputMap.put("bitDepth", "8-bit");
		inputMap.put("signed", false);
		inputMap.put("floating", false);
		inputMap.put("fillType", "Ramp");
		inputMap.put("width", 512L);
		inputMap.put("height", 512L);
		return pluginService.run("imagej.io.plugins.NewImage", inputMap);
	}

}
