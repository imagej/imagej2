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

package imagej.ui;

import imagej.Contextual;
import imagej.Prioritized;
import imagej.display.Display;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Plugin;
import imagej.ui.viewer.DisplayWindow;
import imagej.widget.FileWidget;

import java.io.File;

/**
 * An end-user ImageJ application user interface.
 * <p>
 * UIs discoverable at runtime must implement this interface and be annotated
 * with @{@link Plugin} with attribute {@link Plugin#type()} =
 * {@link UserInterface}.class. While it possible to create a UI merely by
 * implementing this interface, it is encouraged to instead extend
 * {@link AbstractUserInterface}, for convenience.
 * </p>
 * 
 * @author Curtis Rueden
 * @see Plugin
 * @see UIService
 */
public interface UserInterface extends ImageJPlugin, Contextual, Prioritized {

	/** Gets the service responsible for managing this UI. */
	UIService getUIService();

	/**
	 * Shows the UI.
	 * <p>
	 * Note that the actual UI components are created lazily when this method is
	 * called, rather then upon the UI's initial construction.
	 * </p>
	 */
	void show();

	/** Whether this UI is visible onscreen. */
	boolean isVisible();

	/** Gets the desktop, for use with multi-document interfaces (MDI). */
	Desktop getDesktop();

	/** Gets the main ImageJ application frame, or null if not applicable. */
	ApplicationFrame getApplicationFrame();

	/** Gets the main ImageJ toolbar, or null if not applicable. */
	ToolBar getToolBar();

	/** Gets the main ImageJ status bar, or null if not applicable. */
	StatusBar getStatusBar();

	/** Gets the system clipboard associated with this UI. */
	SystemClipboard getSystemClipboard();
	
	/** Creates a new display window housing the given display. */
	DisplayWindow createDisplayWindow(Display<?> display);

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
	 * Prompts the user to choose a file.
	 * 
	 * @param file The initial value displayed in the file chooser prompt.
	 * @param style The style of chooser to use:
	 *          <ul>
	 *          <li>{@link FileWidget#OPEN_STYLE}</li>
	 *          <li>{@link FileWidget#SAVE_STYLE}</li>
	 *          <li>{@link FileWidget#DIRECTORY_STYLE}</li>
	 *          </ul>
	 */
	File chooseFile(File file, String style);

	/**
	 * Displays a popup context menu for the given display at the specified
	 * position.
	 */
	void showContextMenu(String menuRoot, Display<?> display, int x, int y);

	/** Persists the application frame's current location. */
	void saveLocation();

	/** Restores the application frame's current location. */
	void restoreLocation();

}
