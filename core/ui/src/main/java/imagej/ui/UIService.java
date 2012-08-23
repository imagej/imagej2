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

import imagej.data.display.ImageDisplay;
import imagej.display.Display;
import imagej.event.EventService;
import imagej.event.StatusService;
import imagej.ext.plugin.PluginService;
import imagej.log.LogService;
import imagej.menu.MenuService;
import imagej.options.OptionsService;
import imagej.platform.AppService;
import imagej.platform.PlatformService;
import imagej.service.Service;
import imagej.thread.ThreadService;
import imagej.tool.ToolService;
import imagej.ui.viewer.DisplayViewer;
import imagej.ui.viewer.ImageDisplayViewer;

import java.util.List;

/**
 * Interface for service that handles ImageJ user interfaces.
 * 
 * @author Curtis Rueden
 */
public interface UIService extends Service {

	String UI_PROPERTY = "ij.ui";

	LogService getLog();

	ThreadService getThreadService();

	EventService getEventService();

	StatusService getStatusService();

	PlatformService getPlatformService();

	PluginService getPluginService();

	MenuService getMenuService();

	ToolService getToolService();

	OptionsService getOptionsService();

	AppService getAppService();

	/** Creates and displays the UI for the default user interface. */
	void createUI();

	/** Creates and displays the UI with the given name or class name. */
	void createUI(String name);

	/** Creates and displays the given UI. */
	void createUI(UserInterface ui);

	/** Gets the default user interface. */
	UserInterface getUI();

	/** Sets the default user interface. */
	void setUI(UserInterface ui);

	/** Gets the user interfaces available to the service. */
	List<UserInterface> getAvailableUIs();

	/** Gets the UI widget being used to visualize the given {@link Display}. */
	DisplayViewer<?> getDisplayViewer(Display<?> display);

	/** Gets the UI widget being used to visualize the given {@link ImageDisplay}. */
	ImageDisplayViewer getImageDisplayViewer(ImageDisplay display);

	/** Creates a new output window. */
	OutputWindow createOutputWindow(String title);

	/**
	 * Displays a dialog prompt.
	 * 
	 * @param message The message in the dialog itself.
	 * @return The choice selected by the user when dismissing the dialog.
	 */
	DialogPrompt.Result showDialog(String message);

	/**
	 * Displays a dialog prompt.
	 * 
	 * @param message The message in the dialog itself.
	 * @param title The title of the dialog.
	 * @return The choice selected by the user when dismissing the dialog.
	 */
	DialogPrompt.Result showDialog(String message, String title);

	/**
	 * Displays a dialog prompt.
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
	 * Displays a popup context menu for the given display at the specified
	 * position.
	 */
	void showContextMenu(String menuRoot, Display<?> display, int x, int y);

}
