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

package imagej.plugins.uis.swing.sdi;

import imagej.plugins.uis.swing.AbstractSwingUI;
import imagej.plugins.uis.swing.SwingApplicationFrame;
import imagej.plugins.uis.swing.sdi.viewer.SwingDisplayWindow;
import imagej.ui.DialogPrompt;
import imagej.ui.DialogPrompt.MessageType;
import imagej.ui.DialogPrompt.OptionType;
import imagej.ui.UserInterface;
import imagej.ui.common.awt.AWTDropTargetEventDispatcher;
import imagej.ui.common.awt.AWTInputEventDispatcher;
import imagej.ui.common.awt.AWTWindowEventDispatcher;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.scijava.display.Display;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Swing-based SDI user interface for ImageJ.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
@Plugin(type = UserInterface.class, name = SwingUI.NAME)
public class SwingUI extends AbstractSwingUI {

	public static final String NAME = "swing";

	@Parameter
	private EventService eventService;

	// -- UserInterface methods --

	@Override
	public SwingDisplayWindow createDisplayWindow(final Display<?> display) {
		final SwingDisplayWindow displayWindow = new SwingDisplayWindow();

		// broadcast input events (keyboard and mouse)
		new AWTInputEventDispatcher(display).register(displayWindow, true, false);

		// broadcast window events
		new AWTWindowEventDispatcher(display).register(displayWindow);

		// broadcast drag-and-drop events
		new AWTDropTargetEventDispatcher(display, eventService);

		return displayWindow;
	}

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
	}

}
