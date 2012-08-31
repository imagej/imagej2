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
import imagej.log.LogService;
import imagej.module.Module;
import imagej.module.ModuleService;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.plugin.PluginService;
import imagej.plugin.RunnablePlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * A test of {@link PluginService#run}. The source code demonstrates two
 * different ways of invoking a plugin programmatically: with a list of
 * arguments, or by declaring them in a {@link Map}.
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 */
@Plugin(menuPath = "Plugins>Sandbox>Invoke Plugin Test", headless = true)
public class InvokePluginTest implements RunnablePlugin {

	@Parameter
	private LogService log;

	@Parameter
	private ModuleService moduleService;

	@Parameter
	private PluginService pluginService;

	@Override
	public void run() {
		final Future<Module> future = invokeWithArgs(); // or invokeWithMap()
		final Module module = moduleService.waitFor(future);
		final Dataset dataset = (Dataset) module.getOutput("dataset");
		log.info("InvokePluginTest: dataset = " + dataset);
	}

	public Future<Module> invokeWithArgs() {
		return pluginService.run("imagej.io.plugins.NewImage", "name", "Untitled",
			"bitDepth", "8-bit", "signed", false, "floating", false, "fillType",
			"Ramp", "width", 512, "height", 512);
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
