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

package imagej.ui;

import imagej.display.Display;
import imagej.service.IJService;
import imagej.ui.viewer.DisplayViewer;
import imagej.widget.FileWidget;

import java.io.File;
import java.util.List;

import org.scijava.app.App;
import org.scijava.app.StatusService;
import org.scijava.app.event.StatusEvent;
import org.scijava.plugin.PluginInfo;

/**
 * Interface for service that handles ImageJ user interfaces.
 * 
 * @author Curtis Rueden
 */
public interface UIService extends IJService {

	// CTR TODO: Extend SingletonService<UserInterface>.

	/** System property to set for overriding the default UI. */
	String UI_PROPERTY = "ij.ui";

	App getApp();

	/**
	 * Adds the given UI to those managed by the service.
	 * <p>
	 * Note that a UI added explicitly via this method will never be considered
	 * the default UI unless {@link #setDefaultUI(UserInterface)} is also called.
	 * </p>
	 * 
	 * @param ui The UI to add.
	 */
	void addUI(UserInterface ui);

	/**
	 * Adds the given UI to those managed by the service.
	 * <p>
	 * Note that a UI added explicitly via this method will never be considered
	 * the default UI unless {@link #setDefaultUI(UserInterface)} is also called.
	 * </p>
	 * 
	 * @param name The nickname for the UI.
	 * @param ui The UI to add.
	 */
	void addUI(String name, UserInterface ui);

	/**
	 * Displays the UI for the default user interface.
	 * 
	 * @see #getDefaultUI()
	 * @see #setDefaultUI(UserInterface)
	 */
	void showUI();

	/** Displays the UI with the given name (or class name). */
	void showUI(String name);

	/** Displays the given UI. */
	void showUI(UserInterface ui);

	/**
	 * Gets whether the default UI is visible.
	 * 
	 * @see #getDefaultUI()
	 * @see #setDefaultUI(UserInterface)
	 */
	boolean isVisible();

	/** Gets whether the UI with the given name or class name is visible. */
	boolean isVisible(String name);

	/**
	 * Gets the default user interface.
	 * 
	 * @see #showUI()
	 * @see #isVisible()
	 */
	UserInterface getDefaultUI();

	/**
	 * Sets the default user interface.
	 * 
	 * @see #showUI()
	 */
	void setDefaultUI(UserInterface ui);

	/**
	 * Gets whether the UI with the given name (or class name) is the default one.
	 */
	boolean isDefaultUI(String name);

	/** Gets the UI with the given name (or class name). */
	UserInterface getUI(String name);

	/** Gets the user interfaces available to the service. */
	List<UserInterface> getAvailableUIs();

	/** Gets the user interfaces that are currently visible. */
	List<UserInterface> getVisibleUIs();

	/** Gets the list of known viewer plugins. */
	List<PluginInfo<DisplayViewer<?>>> getViewerPlugins();

	/** Registers the given viewer with the service. */
	void addDisplayViewer(DisplayViewer<?> viewer);

	/** Gets the UI widget being used to visualize the given {@link Display}. */
	DisplayViewer<?> getDisplayViewer(Display<?> display);

	/**
	 * Creates a {@link Display} for the given object, and shows it using an
	 * appropriate UI widget of the default user interface.
	 */
	void show(Object o);

	/**
	 * Creates a {@link Display} for the given object, and shows it using an
	 * appropriate UI widget of the default user interface.
	 * 
	 * @param name The name to use when displaying the object.
	 * @param o The object to be displayed.
	 */
	void show(String name, Object o);

	/**
	 * Creates and shows the given {@link Display} using an appropriate UI widget
	 * of the default user interface.
	 */
	void show(Display<?> display);

	/**
	 * Displays a dialog prompt.
	 * <p>
	 * The prompt is displayed in the default user interface.
	 * </p>
	 * 
	 * @param message The message in the dialog itself.
	 * @return The choice selected by the user when dismissing the dialog.
	 */
	DialogPrompt.Result showDialog(String message);

	/**
	 * Displays a dialog prompt.
	 * <p>
	 * The prompt is displayed in the default user interface.
	 * </p>
	 * 
	 * @param message The message in the dialog itself.
	 * @param messageType The type of message. This typically is rendered as an
	 *          icon next to the message. For example,
	 *          {@link DialogPrompt.MessageType#WARNING_MESSAGE} typically appears
	 *          as an exclamation point.
	 * @return The choice selected by the user when dismissing the dialog.
	 */
	DialogPrompt.Result showDialog(String message,
		DialogPrompt.MessageType messageType);

	/**
	 * Displays a dialog prompt.
	 * <p>
	 * The prompt is displayed in the default user interface.
	 * </p>
	 * 
	 * @param message The message in the dialog itself.
	 * @param messageType The type of message. This typically is rendered as an
	 *          icon next to the message. For example,
	 *          {@link DialogPrompt.MessageType#WARNING_MESSAGE} typically appears
	 *          as an exclamation point.
	 * @param optionType The choices available when dismissing the dialog. These
	 *          choices are typically rendered as buttons for the user to click.
	 * @return The choice selected by the user when dismissing the dialog.
	 */
	DialogPrompt.Result showDialog(String message,
		DialogPrompt.MessageType messageType, DialogPrompt.OptionType optionType);

	/**
	 * Displays a dialog prompt.
	 * <p>
	 * The prompt is displayed in the default user interface.
	 * </p>
	 * 
	 * @param message The message in the dialog itself.
	 * @param title The title of the dialog.
	 * @return The choice selected by the user when dismissing the dialog.
	 */
	DialogPrompt.Result showDialog(String message, String title);

	/**
	 * Displays a dialog prompt.
	 * <p>
	 * The prompt is displayed in the default user interface.
	 * </p>
	 * 
	 * @param message The message in the dialog itself.
	 * @param title The title of the dialog.
	 * @param messageType The type of message. This typically is rendered as an
	 *          icon next to the message. For example,
	 *          {@link DialogPrompt.MessageType#WARNING_MESSAGE} typically appears
	 *          as an exclamation point.
	 * @return The choice selected by the user when dismissing the dialog.
	 */
	DialogPrompt.Result showDialog(String message, String title,
		DialogPrompt.MessageType messageType);

	/**
	 * Displays a dialog prompt.
	 * <p>
	 * The prompt is displayed in the default user interface.
	 * </p>
	 * 
	 * @param message The message in the dialog itself.
	 * @param title The title of the dialog.
	 * @param messageType The type of message. This typically is rendered as an
	 *          icon next to the message. For example,
	 *          {@link DialogPrompt.MessageType#WARNING_MESSAGE} typically appears
	 *          as an exclamation point.
	 * @param optionType The choices available when dismissing the dialog. These
	 *          choices are typically rendered as buttons for the user to click.
	 * @return The choice selected by the user when dismissing the dialog.
	 */
	DialogPrompt.Result showDialog(String message, String title,
		DialogPrompt.MessageType messageType, DialogPrompt.OptionType optionType);

	/**
	 * Prompts the user to choose a file.
	 * <p>
	 * The prompt is displayed in the default user interface.
	 * </p>
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
	 * <p>
	 * The context menu is displayed in the default user interface.
	 * </p>
	 */
	void showContextMenu(String menuRoot, Display<?> display, int x, int y);

	/**
	 * Gets the status message associated with the given event.
	 * 
	 * @see StatusService#getStatusMessage(String, StatusEvent)
	 */
	String getStatusMessage(StatusEvent statusEvent);

}
