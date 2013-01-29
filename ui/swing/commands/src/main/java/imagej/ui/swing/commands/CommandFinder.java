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

package imagej.ui.swing.commands;

import imagej.command.CommandService;
import imagej.command.ContextCommand;
import imagej.menu.MenuConstants;
import imagej.module.ModuleInfo;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.util.swing.SwingUtils;

import javax.swing.JOptionPane;

/**
 * A plugin to display the {@link CommandFinderPanel} in a dialog.
 * 
 * @author Curtis Rueden
 * @author Johannes Schindelin
 */
@Plugin(menu = {
	@Menu(label = MenuConstants.PLUGINS_LABEL,
		weight = MenuConstants.PLUGINS_WEIGHT,
		mnemonic = MenuConstants.PLUGINS_MNEMONIC), @Menu(label = "Utilities"),
	@Menu(label = "Find Commands...", accelerator = "control L") })
public class CommandFinder extends ContextCommand {

	@Parameter
	private CommandService commandService;

	@Override
	public void run() {
		final CommandFinderPanel commandFinderPanel =
			new CommandFinderPanel(commandService.getModuleService());
		final int rval =
			SwingUtils.showDialog(null, commandFinderPanel, "Find Commands",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, false,
				commandFinderPanel.getSearchField());
		if (rval != JOptionPane.OK_OPTION) return; // dialog canceled

		final ModuleInfo info = commandFinderPanel.getCommand();
		if (info == null) return; // no command selected

		// execute selected command
		commandService.run(info);
	}

}
