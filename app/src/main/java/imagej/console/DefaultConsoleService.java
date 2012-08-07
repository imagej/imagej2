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

package imagej.console;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.ext.display.DisplayService;
import imagej.ext.plugin.Plugin;
import imagej.ext.plugin.PluginService;
import imagej.io.IOService;
import imagej.log.LogService;
import imagej.options.OptionsService;
import imagej.service.AbstractService;
import imagej.service.IService;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.io.ImgIOException;

/**
 * Default service for managing interaction with the console.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = IService.class)
public class DefaultConsoleService extends AbstractService implements
	ConsoleService
{

	private final LogService log;
	private final OptionsService optionsService;
	private final PluginService pluginService;
	private final IOService ioService;
	private final DisplayService displayService;

	// -- Constructors --

	public DefaultConsoleService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public DefaultConsoleService(final ImageJ context, final LogService log,
		final OptionsService optionsService, final PluginService pluginService,
		final IOService ioService, final DisplayService displayService)
	{
		super(context);
		this.log = log;
		this.optionsService = optionsService;
		this.pluginService = pluginService;
		this.ioService = ioService;
		this.displayService = displayService;
	}

	// -- ConsoleService methods --

	@Override
	public void processArgs(final String... args) {
		// TODO: Implement handling of more command line arguments.
		log.debug("Received command line arguments:");
		for (int i = 0; i < args.length; i++) {
			final String arg = args[i];
			log.debug("\t" + arg);
			if (arg.equals("--open")) {
				open(args[i + 1]);
			}
			else if (arg.equals("--run")) {
				run(args[i + 1]);
			}
		}
	}

	// -- Helper methods --

	/** Implements the "--open" command line argument. */
	private void open(final String source) {
		try {
			final Dataset dataset = ioService.loadDataset(source);
			displayService.createDisplay(dataset.getName(), dataset);
		}
		catch (final ImgIOException exc) {
			log.error("Error loading dataset '" + source + "'", exc);
		}
		catch (final IncompatibleTypeException exc) {
			log.error("Error loading dataset '" + source + "'", exc);
		}
	}

	/** Implements the "--run" command line argument. */
	private void run(final String className) {
		pluginService.run(className);
	}

}
