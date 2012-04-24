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

package imagej.ui.swing.sdi;

import imagej.ImageJ;
import imagej.event.EventHandler;
import imagej.ext.InstantiableException;
import imagej.ext.display.Display;
import imagej.ext.display.DisplayViewer;
import imagej.ext.display.DisplayWindow;
import imagej.ext.display.event.DisplayCreatedEvent;
import imagej.ext.display.event.DisplayDeletedEvent;
import imagej.ext.plugin.Plugin;
import imagej.ext.plugin.PluginInfo;
import imagej.ext.plugin.PluginService;
import imagej.ui.DialogPrompt;
import imagej.ui.DialogPrompt.MessageType;
import imagej.ui.DialogPrompt.OptionType;
import imagej.ui.UserInterface;
import imagej.ui.swing.AbstractSwingUI;
import imagej.ui.swing.SwingApplicationFrame;
import imagej.ui.swing.sdi.display.SwingDisplayWindow;
import imagej.util.Log;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Swing-based SDI user interface for ImageJ.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
@Plugin(type = UserInterface.class)
public class SwingUI extends AbstractSwingUI {

	// -- UserInterface methods --

	@Override
	public DialogPrompt dialogPrompt(final String message, final String title,
		final MessageType msg, final OptionType option)
	{
		return new SwingDialogPrompt(message, title, msg, option);
	}

	// -- Internal methods --

	@Override
	protected void setupAppFrame() {
		final SwingApplicationFrame appFrame = getApplicationFrame();
		final JPanel pane = new JPanel();
		appFrame.setContentPane(pane);
		pane.setLayout(new BorderLayout());
		appFrame.pack();
	}

	
	protected void deleteMenuBar(final JFrame f) {
		f.setJMenuBar(null);
		// HACK - w/o this next call the JMenuBars do not get garbage collected.
		// At least its true on the Mac. This might be a Java bug. Update:
		// I hunted on web and have found multiple people with the same problem.
		// The Apple ScreenMenus don't GC when a Frame disposes. Their workaround
		// was exactly the same. I have not found any official documentation of
		// this issue.
		f.setMenuBar(null);
	}

	// -- Event handlers --

	/**
	 * This is the magical place where the display model
	 * is connected with the real UI.
	 * 
	 * @param event
	 */
	@EventHandler
	protected void onEvent(final DisplayCreatedEvent event) {
		final Display<?> display = event.getObject();
		final ImageJ imageJ = display.getContext();
		final PluginService pluginService = imageJ.getService(PluginService.class);
		for (@SuppressWarnings("rawtypes") PluginInfo<DisplayViewer> info:pluginService.getPluginsOfType(DisplayViewer.class)) {
			try {
				final DisplayViewer<?> displayViewer = info.createInstance();
				if (displayViewer.canView(display)){
					final SwingDisplayWindow displayWindow = new SwingDisplayWindow(); 
					displayViewer.view(displayWindow, display);
					displayViewers.add(displayViewer);
					// add a copy of the JMenuBar to the new display
					if (displayWindow.getJMenuBar() == null) {
						createMenuBar(displayWindow);
					}
					displayWindow.showDisplay(true);
					return;
				}
			} catch (InstantiableException e) {
				Log.warn("Failed to create instance of " + info.getClassName(), e);
			}
		}
		Log.warn("No suitable DisplayViewer found for display");
	}

	@EventHandler
	protected void onEvent(final DisplayDeletedEvent event) {
		final DisplayViewer<?> displayViewer = getDisplayViewer(event.getObject());
		final DisplayWindow displayWindow = displayViewer.getDisplayWindow();
		if ((displayWindow != null) && (displayWindow instanceof JFrame)) {
			deleteMenuBar((JFrame)displayWindow);
		}
	}

}
