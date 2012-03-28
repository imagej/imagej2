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

package imagej.ui.swt;

import imagej.event.EventService;
import imagej.ext.menu.MenuService;
import imagej.ext.plugin.Plugin;
import imagej.ext.ui.swt.SWTMenuCreator;
import imagej.platform.event.AppMenusCreatedEvent;
import imagej.ui.Desktop;
import imagej.ui.DialogPrompt;
import imagej.ui.DialogPrompt.MessageType;
import imagej.ui.DialogPrompt.OptionType;
import imagej.ui.UserInterface;
import imagej.ui.OutputWindow;
import imagej.ui.UIService;
import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;

/**
 * An SWT-based user interface for ImageJ.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = UserInterface.class)
public class SWTUI implements UserInterface, Runnable {

	private UIService uiService;
	private EventService eventService;

	private SWTApplicationFrame shell;
	private SWTToolBar toolBar;
	private SWTStatusBar statusBar;

	private Display display;

	// -- UserInterface methods --

	@Override
	public void initialize(final UIService service) {
		uiService = service;
		eventService = uiService.getEventService();

		display = new Display();

		shell = new SWTApplicationFrame(display);
		shell.setLayout(new MigLayout("wrap 1"));
		shell.setText("ImageJ");
		toolBar = new SWTToolBar(display, shell);
		statusBar = new SWTStatusBar(shell, eventService);
		createMenus();

		shell.pack();
		shell.open();

		new Thread(this, "SWT-Dispatch").start();
	}

	@Override
	public void processArgs(final String[] args) {
		// TODO
	}

	@Override
	public void createMenus() {
		final MenuService menuService = uiService.getMenuService();
		final Menu menuBar =
			menuService.createMenus(new SWTMenuCreator(), new Menu(shell));
		shell.setMenuBar(menuBar); // TODO - is this necessary?
		eventService.publish(new AppMenusCreatedEvent(menuBar));
	}

	@Override
	public UIService getUIService() {
		return uiService;
	}

	@Override
	public SWTApplicationFrame getApplicationFrame() {
		return shell;
	}

	@Override
	public Desktop getDesktop() {
		return null;
	}

	@Override
	public SWTToolBar getToolBar() {
		return toolBar;
	}

	@Override
	public SWTStatusBar getStatusBar() {
		return statusBar;
	}

	@Override
	public OutputWindow newOutputWindow(final String title) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public DialogPrompt dialogPrompt(final String message, final String title,
		final MessageType msg, final OptionType option)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void showContextMenu(final String menuRoot,
		final imagej.ext.display.Display<?> display, final int x, final int y)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	// -- Runnable methods --

	@Override
	public void run() {
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
	}

}
