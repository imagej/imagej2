//
// UserInterface.java
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

package imagej.ui;

import imagej.ext.display.Display;
import imagej.ext.plugin.IPlugin;
import imagej.ext.plugin.Plugin;

/**
 * An end-user ImageJ application user interface. UIs discoverable at runtime
 * must implement this interface and be annotated with
 * <code>@{@link Plugin}(type = UserInterface.class)</code>.
 * 
 * @author Curtis Rueden
 * @see Plugin
 * @see UIService
 */
public interface UserInterface extends IPlugin {

	void initialize(UIService uiService);

	UIService getUIService();

	void processArgs(final String[] args);

	/** Desktop for use with multi-document interfaces (MDI). */
	Desktop getDesktop();

	ApplicationFrame getApplicationFrame();

	ToolBar getToolBar();

	StatusBar getStatusBar();

	void createMenus();

	OutputWindow newOutputWindow(String title);

	/**
	 * Creates a dialog prompter.
	 * 
	 * @param message The message in the dialog itself.
	 * @param title The title of the dialog.
	 * @param messageType The type of message. This typically is rendered as an
	 *          icon next to the message. For example,
	 *          {@link DialogPrompt.MessageType#WARNING_MESSAGE} typically appears
	 *          as an exclamation point.
	 * @param optionType The choices available when dismissing the dialog. These
	 *          choices are typically rendered as buttons for the user to click.
	 * @return The newly created DialogPrompt object.
	 */
	DialogPrompt dialogPrompt(String message, String title,
		DialogPrompt.MessageType messageType, DialogPrompt.OptionType optionType);

	/**
	 * Displays a popup context menu for the given display at the specified
	 * position.
	 */
	void showContextMenu(String menuRoot, Display<?> display, int x, int y);

}
