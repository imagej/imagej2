//
// AWTUI.java
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

package imagej.ui.awt;

import imagej.ImageJ;
import imagej.display.TextDisplay;
import imagej.ext.menu.MenuService;
import imagej.ext.ui.awt.AWTMenuBarCreator;
import imagej.platform.event.AppMenusCreatedEvent;
import imagej.ui.ApplicationFrame;
import imagej.ui.Desktop;
import imagej.ui.DialogPrompt;
import imagej.ui.DialogPrompt.MessageType;
import imagej.ui.DialogPrompt.OptionType;
import imagej.ui.OutputWindow;
import imagej.ui.UI;
import imagej.ui.UIService;
import imagej.ui.UserInterface;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.MenuBar;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * AWT-based user interface for ImageJ.
 * 
 * @author Curtis Rueden
 */
@UI
public class AWTUI implements UserInterface {

	private UIService uiService;
	private AWTApplicationFrame frame;
	private AWTToolBar toolBar;
	private AWTStatusBar statusBar;

	// -- UserInterface methods --

	@Override
	public void initialize(final UIService service) {
		uiService = service;

		frame = new AWTApplicationFrame("ImageJ");
		toolBar = new AWTToolBar();
		statusBar = new AWTStatusBar();
		createMenus();

		frame.setLayout(new BorderLayout());
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(final WindowEvent e) {
				System.exit(0);
			}
		});

		frame.add(toolBar, BorderLayout.NORTH);
		frame.add(statusBar, BorderLayout.SOUTH);

		frame.pack();
		frame.setVisible(true);
	}

	@Override
	public void processArgs(final String[] args) {
		// TODO
	}

	@Override
	public void createMenus() {
		final MenuService menuService = ImageJ.get(MenuService.class);
		final MenuBar menuBar =
			menuService.createMenus(new AWTMenuBarCreator(), new MenuBar());
		frame.setMenuBar(menuBar);
		uiService.getEventService().publish(new AppMenusCreatedEvent(menuBar));
	}

	@Override
	public UIService getUIService() {
		return uiService;
	}

	@Override
	public ApplicationFrame getApplicationFrame() {
		return frame;
	}

	@Override
	public Desktop getDesktop() {
		return null;
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
	public TextDisplay newOutputWindow(final String title) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public DialogPrompt dialogPrompt(final String message, final String title,
		final MessageType msg, final OptionType option)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}



}
