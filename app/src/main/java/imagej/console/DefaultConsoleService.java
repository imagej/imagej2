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

package imagej.console;

import imagej.command.CommandInfo;
import imagej.command.CommandService;
import imagej.data.Dataset;
import imagej.display.DisplayService;
import imagej.io.IOService;
import net.imglib2.exception.IncompatibleTypeException;

import ome.scifio.io.img.ImgIOException;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Default service for managing interaction with the console.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class DefaultConsoleService extends AbstractService implements
	ConsoleService
{

	@Parameter
	private LogService log;

	@Parameter
	private CommandService commandService;

	@Parameter
	private IOService ioService;

	@Parameter
	private DisplayService displayService;

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
				if (!run(args[i + 1])) {
					run(args[i + 1], args.length < i + 3 ? null : args[i + 2]);
				}
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
	private boolean run(final String className) {
		return commandService.run(className) != null;
	}

	/** Implements the "--run <label> <optionString>" legacy handling */
	private boolean run(String menuLabel, final String optionString) {
		final String label = menuLabel.replace('_', ' ');
		CommandInfo info = null;
		for (final CommandInfo info2 : commandService.getCommands()) {
			if (label.equals(info2.getTitle())) {
				info = info2;
				break;
			}
		}
		if (info == null) return false;
		// TODO: parse the optionString a la ImageJ1
		return commandService.run(info) != null;
	}

}
