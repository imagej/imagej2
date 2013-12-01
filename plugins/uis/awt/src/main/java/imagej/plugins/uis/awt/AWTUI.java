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

package imagej.plugins.uis.awt;

import imagej.display.Display;
import imagej.menu.MenuService;
import imagej.platform.AppEventService;
import imagej.platform.event.AppMenusCreatedEvent;
import imagej.plugins.uis.awt.menu.AWTMenuBarCreator;
import imagej.ui.AbstractUserInterface;
import imagej.ui.ApplicationFrame;
import imagej.ui.DialogPrompt;
import imagej.ui.DialogPrompt.MessageType;
import imagej.ui.DialogPrompt.OptionType;
import imagej.ui.SystemClipboard;
import imagej.ui.UserInterface;
import imagej.ui.common.awt.AWTClipboard;
import imagej.ui.viewer.DisplayWindow;

import java.awt.BorderLayout;
import java.awt.MenuBar;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * AWT-based user interface for ImageJ.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = UserInterface.class, name = AWTUI.NAME)
public class AWTUI extends AbstractUserInterface {

	public static final String NAME = "awt";

	@Parameter
	private AppEventService appEventService;

	@Parameter
	private EventService eventService;

	@Parameter
	private MenuService menuService;

	private AWTApplicationFrame frame;
	private AWTToolBar toolBar;
	private AWTStatusBar statusBar;
	private AWTClipboard systemClipboard;

	// -- UserInterface methods --

	@Override
	public ApplicationFrame getApplicationFrame() {
		return frame;
	}

	@Override
	public AWTToolBar getToolBar() {
		return toolBar;
	}

	@Override
	public AWTStatusBar getStatusBar() {
		return statusBar;
	}

	@Override
	public SystemClipboard getSystemClipboard() {
		return systemClipboard;
	}

	@Override
	public DisplayWindow createDisplayWindow(final Display<?> display) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public DialogPrompt dialogPrompt(final String message, final String title,
		final MessageType msg, final OptionType option)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public File chooseFile(final File file, final String style) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void showContextMenu(final String menuRoot, final Display<?> display,
		final int x, final int y)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override()
	public boolean requiresEDT() {
		return true;
	}

	// -- Disposable methods --

	@Override
	public void dispose() {
		if (frame != null) frame.dispose();
	}

	// -- Internal methods --

	@Override
	protected void createUI() {
		frame = new AWTApplicationFrame(getApp().getTitle());

		toolBar = new AWTToolBar(getContext());
		statusBar = new AWTStatusBar(getContext());

		systemClipboard = new AWTClipboard();

		createMenus();

		frame.setLayout(new BorderLayout());
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(final WindowEvent e) {
				appEventService.quit();
			}
		});

		frame.add(toolBar, BorderLayout.NORTH);
		frame.add(statusBar, BorderLayout.SOUTH);

		super.createUI();

		frame.pack();
		frame.setVisible(true);
	}

	protected void createMenus() {
		final MenuBar menuBar =
			menuService.createMenus(new AWTMenuBarCreator(), new MenuBar());
		frame.setMenuBar(menuBar);
		eventService.publish(new AppMenusCreatedEvent(menuBar));
	}

}
