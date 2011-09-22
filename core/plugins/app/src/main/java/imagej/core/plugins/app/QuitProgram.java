//
// QuitProgram.java
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

package imagej.core.plugins.app;

import imagej.ImageJ;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Plugin;
import imagej.ui.DialogPrompt;
import imagej.ui.UIService;
import imagej.ui.IUserInterface;

/**
 * Quits ImageJ.
 * 
 * @author Grant Harris
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
@Plugin(iconPath = "/icons/plugins/door_in.png", menu = {
		@Menu(label = "File"),
		@Menu(label = "Quit", weight = Double.MAX_VALUE, mnemonic = 'q',
				accelerator = "control Q") })
public class QuitProgram implements ImageJPlugin {

	public static final String MESSAGE = "Really quit ImageJ?";

	@Override
	public void run() {
		if (promptForQuit()) {
			// TODO - save existing data
			// TODO - close windows
			// TODO - call ImageJ.getContext().shutdown() or some such, rather than
			// using System.exit(0), which kills the entire JVM.
			System.exit(0);
		}
	}

	private boolean promptForQuit() {
		final IUserInterface ui = ImageJ.get(UIService.class).getUI();
		final DialogPrompt dialog =
			ui.dialogPrompt(MESSAGE, "Quit",
				DialogPrompt.MessageType.QUESTION_MESSAGE,
				DialogPrompt.OptionType.YES_NO_OPTION);
		final DialogPrompt.Result result = dialog.prompt();
		return result == DialogPrompt.Result.YES_OPTION;
	}

}
