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

package imagej.plugins.commands.display;

import imagej.command.Command;
import imagej.command.ContextCommand;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.menu.MenuConstants;

import java.util.ArrayList;

import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Closes all displays.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Command.class, menu = {
	@Menu(label = MenuConstants.FILE_LABEL, weight = MenuConstants.FILE_WEIGHT,
		mnemonic = MenuConstants.FILE_MNEMONIC),
	@Menu(label = "Close All", weight = 17) })
public class CloseAll extends ContextCommand {

	// -- Parameters --

	@Parameter
	private DisplayService displayService;

	// -- Runnable methods --

	@Override
	public void run() {
		// NB: Make a copy to avoid concurrent modification issues.
		final ArrayList<Display<?>> displays =
			new ArrayList<Display<?>>(displayService.getDisplays());
		for (final Display<?> display : displays) {
			display.close();
		}
	}

}
