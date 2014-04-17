/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.command.console;

import imagej.command.CommandInfo;
import imagej.command.CommandService;

import java.util.LinkedList;

import org.scijava.console.AbstractConsoleArgument;
import org.scijava.console.ConsoleArgument;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Handles the {@code --run} command line argument.
 * 
 * @author Curtis Rueden
 * @author Johannes Schindelin
 */
@Plugin(type = ConsoleArgument.class)
public class RunArgument extends AbstractConsoleArgument {

	@Parameter
	private CommandService commandService;

	// -- ConsoleArgument methods --

	@Override
	public void handle(final LinkedList<String> args) {
		if (!supports(args)) return;

		args.removeFirst(); // --run
		final String commandToRun = args.removeFirst();
		final String optionString = args.isEmpty() ? "" : args.removeFirst();

		run(commandToRun, optionString);
	}

	// -- Typed methods --

	@Override
	public boolean supports(final LinkedList<String> args) {
		return args != null && args.size() >= 2 && args.getFirst().equals("--run");
	}

	// -- Helper methods --

	/** Implements the {@code --run} command line argument. */
	private void run(final String commandToRun, final String optionString) {
		CommandInfo info = commandService.getCommand(commandToRun);
		if (info == null) {
			// command was not a class name; search for command by title instead
			final String label = commandToRun.replace('_', ' ');
			for (final CommandInfo ci : commandService.getCommands()) {
				if (label.equals(ci.getTitle())) {
					info = ci;
					break;
				}
			}
		}
		if (info == null) return;
		// TODO: parse the optionString a la ImageJ1
		commandService.run(info, true);
	}

}
