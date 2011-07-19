//
// CommandFinder.java
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

package imagej.ui.swing.plugins;

import imagej.ext.module.ModuleException;
import imagej.ext.module.ModuleInfo;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Plugin;
import imagej.plugin.ui.swing.SwingUtils;
import imagej.ui.swing.CommandFinderPanel;
import imagej.util.Log;

import javax.swing.JOptionPane;

/**
 * A plugin to display the {@link CommandFinderPanel} in a dialog.
 * 
 * @author Curtis Rueden
 */
@Plugin(menu = { @Menu(label = "Plugins"), @Menu(label = "Utilities"),
	@Menu(label = "Find Commands...", accelerator = "control L") })
public class CommandFinder implements ImageJPlugin {

	@Override
	public void run() {
		final CommandFinderPanel commandFinderPanel = new CommandFinderPanel();
		final int rval =
			SwingUtils.showDialog(null, commandFinderPanel, "Find Commands",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, false,
				commandFinderPanel.getSearchField());
		if (rval != JOptionPane.OK_OPTION) return; // dialog canceled

		final ModuleInfo info = commandFinderPanel.getCommand();
		if (info == null) return; // no plugin selected

		// execute selected plugin
		try {
			info.createModule().run();
		}
		catch (final ModuleException exc) {
			Log.error("Could not execute command: " + info, exc);
		}
	}

}
