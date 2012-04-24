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

package imagej.ui.swing.mdi;

import imagej.ImageJ;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.ext.InstantiableException;
import imagej.ext.display.Display;
import imagej.ext.display.DisplayViewer;
import imagej.ext.display.event.DisplayCreatedEvent;
import imagej.ext.display.event.DisplayDeletedEvent;
import imagej.ext.plugin.Plugin;
import imagej.ext.plugin.PluginInfo;
import imagej.ext.plugin.PluginService;
import imagej.ui.Desktop;
import imagej.ui.UIService;
import imagej.ui.DialogPrompt.MessageType;
import imagej.ui.DialogPrompt.OptionType;
import imagej.ui.UserInterface;
import imagej.ui.swing.AbstractSwingUI;
import imagej.ui.swing.SwingApplicationFrame;
import imagej.ui.swing.mdi.display.SwingMdiDisplayWindow;
import imagej.util.Log;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import javax.swing.JScrollPane;

/**
 * Swing-based MDI user interface for ImageJ.
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 */
@Plugin(type = UserInterface.class)
public class SwingMdiUI extends AbstractSwingUI {

	private JMDIDesktopPane desktopPane;

	private JScrollPane scrollPane;

	// -- UserInterface methods --

	@Override
	public Desktop getDesktop() {
		return desktopPane; 
	}

	@Override
	public SwingMdiDialogPrompt dialogPrompt(final String message,
		final String title, final MessageType msg, final OptionType option)
	{
		return new SwingMdiDialogPrompt(message, title, msg, option);
	}

	// -- Internal methods --

	@Override
	protected void setupAppFrame() {
		final SwingApplicationFrame appFrame = getApplicationFrame();
		desktopPane = new JMDIDesktopPane();
		// TODO desktopPane.setTransferHandler(new DropFileTransferHandler());
		scrollPane = new JScrollPane();
		scrollPane.setViewportView(desktopPane);
		desktopPane.setBackground(new Color(200, 200, 255));
		appFrame.getContentPane().add(scrollPane);
		appFrame.setBounds(getWorkSpaceBounds());
	}

	// -- Helper methods --

	private Rectangle getWorkSpaceBounds() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment()
			.getMaximumWindowBounds();
	}

	// -- Event handlers --

	/**
	 * This is the magical place where the display model
	 * is connected with the real UI.
	 * 
	 * @param event
	 */
	@Override
	protected void onDisplayCreated(final DisplayCreatedEvent event) {
		final Display<?> display = event.getObject();
		final ImageJ imageJ = display.getContext();
		final PluginService pluginService = imageJ.getService(PluginService.class);
		final EventService eventService = imageJ.getService(EventService.class);
		for (@SuppressWarnings("rawtypes") PluginInfo<DisplayViewer> info:pluginService.getPluginsOfType(DisplayViewer.class)) {
			try {
				final DisplayViewer<?> displayViewer = info.createInstance();
				if (displayViewer.canView(display)){
					final SwingMdiDisplayWindow displayWindow = new SwingMdiDisplayWindow(); 
					displayViewer.view(displayWindow, display);
					displayWindow.showDisplay(true);
					desktopPane.add(displayWindow);
					displayWindow.addEventDispatcher(
							new InternalFrameEventDispatcher(display, eventService));
					return;
				}
			} catch (InstantiableException e) {
				Log.warn("Failed to create instance of " + info.getClassName(), e);
			}
		}
		Log.warn("No suitable DisplayViewer found for display");
	}

	@Override
	protected void onDisplayDeleted(DisplayDeletedEvent e) {
		// No action.
		
	}
}
