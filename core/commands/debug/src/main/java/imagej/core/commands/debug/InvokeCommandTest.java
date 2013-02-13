/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package imagej.core.commands.debug;

import imagej.Context;
import imagej.command.Command;
import imagej.command.CommandService;
import imagej.data.Dataset;
import imagej.io.plugins.NewImage;
import imagej.log.LogService;
import imagej.module.Module;
import imagej.module.ModuleService;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import net.imglib2.meta.Axes;

/**
 * A test of {@link CommandService#run}. The source code demonstrates three
 * different ways of invoking a command programmatically:
 * <ol>
 * <li>Using {@link CommandService} with a list of arguments</li>
 * <li>Using {@link CommandService} with arguments in a {@link Map}</li>
 * <li>Calling a command's Java API directly</li>
 * </ol>
 * <p>
 * The first two approaches can be used to invoke any command, but the compiler
 * cannot guarantee the correctness of the input types. Conversely, the third
 * approach is fully compile-time safe, but not all commands support it.
 * </p>
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 */
@Plugin(menuPath = "Plugins>Sandbox>Invoke Command Test", headless = true)
public class InvokeCommandTest implements Command {

	@Parameter
	private Context context;

	@Parameter
	private LogService log;

	@Parameter
	private ModuleService moduleService;

	@Parameter
	private CommandService commandService;

	@Override
	public void run() {
//		final Dataset dataset = invokeWithArgs();
//		final Dataset dataset = invokeWithMap();
		final Dataset dataset = invokeFromJava();
		log.info("InvokePluginTest: dataset = " + dataset);
	}

	/**
	 * Invokes the {@code NewImage} command using the {@link CommandService} with
	 * a list of arguments. This approach is very flexible and compact, but the
	 * compiler cannot guarantee the correctness of the input types.
	 */
	public Dataset invokeWithArgs() {
		final Future<Module> future =
			commandService.run("imagej.io.plugins.NewImage", "name", "Untitled",
				"bitDepth", "8-bit", "signed", false, "floating", false, "fillType",
				"Ramp", "width", 512, "height", 512);
		final Module module = moduleService.waitFor(future);
		return (Dataset) module.getOutput("dataset");
	}

	/**
	 * Invokes the {@code NewImage} command using the {@link CommandService} with
	 * arguments in a {@link Map}.
	 * <p>
	 * This approach is extremely flexible, but the compiler cannot guarantee the
	 * correctness of the input types.
	 * </p>
	 */
	public Dataset invokeWithMap() {
		final Map<String, Object> inputMap = new HashMap<String, Object>();
		inputMap.put("name", "Untitled");
		inputMap.put("bitDepth", "8-bit");
		inputMap.put("signed", false);
		inputMap.put("floating", false);
		inputMap.put("fillType", "Ramp");
		inputMap.put("width", 512L);
		inputMap.put("height", 512L);
		final Future<Module> future =
			commandService.run("imagej.io.plugins.NewImage", inputMap);
		final Module module = moduleService.waitFor(future);
		return (Dataset) module.getOutput("dataset");
	}

	/**
	 * Directly invokes the {@code NewImage} command using its Java API.
	 * <p>
	 * This approach is fully compile-time safe, but only commands that expose
	 * their Java API can be invoked in this way. Note that no pre- or
	 * postprocessing of the command is done, and no events are published with
	 * respect to the execution, making this approach very convenient for calling
	 * commands internally from within another one.
	 * </p>
	 */
	public Dataset invokeFromJava() {
		final NewImage newImage = new NewImage();
		newImage.setContext(context);
		newImage.setName("Untitled");
		newImage.setBitsPerPixel(8);
		newImage.setSigned(false);
		newImage.setFloating(false);
		newImage.setFillType(NewImage.RAMP);
		newImage.setDimension(Axes.X, 512);
		newImage.setDimension(Axes.Y, 512);
		newImage.run();
		return newImage.getDataset();
	}

}
