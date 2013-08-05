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

package imagej.ui.swt;

import imagej.menu.MenuService;
import imagej.platform.event.AppMenusCreatedEvent;
import imagej.ui.AbstractUserInterface;
import imagej.ui.DialogPrompt;
import imagej.ui.DialogPrompt.MessageType;
import imagej.ui.DialogPrompt.OptionType;
import imagej.ui.SystemClipboard;
import imagej.ui.UIService;
import imagej.ui.UserInterface;
import imagej.ui.swt.menu.SWTMenuCreator;
import imagej.ui.viewer.DisplayWindow;

import java.io.File;

import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * An SWT-based user interface for ImageJ.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = UserInterface.class, name = SWTUI.NAME)
public class SWTUI extends AbstractUserInterface implements Runnable {

	public static final String NAME = "swt";

	@Parameter
	private EventService eventService;

	@Parameter
	private MenuService menuService;

	@Parameter
	private UIService uiService;

	private SWTApplicationFrame shell;
	private SWTToolBar toolBar;
	private SWTStatusBar statusBar;

	private Display swtDisplay;

	// -- UserInterface methods --

	@Override
	public SWTApplicationFrame getApplicationFrame() {
		return shell;
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
	public SystemClipboard getSystemClipboard() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public DisplayWindow createDisplayWindow(
		final imagej.display.Display<?> display)
	{
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
	public void showContextMenu(final String menuRoot,
		final imagej.display.Display<?> display, final int x, final int y)
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
		if (shell != null) shell.dispose();
	}

	// -- Runnable methods --

	@Override
	public void run() {
		while (!shell.isDisposed()) {
			if (!swtDisplay.readAndDispatch()) swtDisplay.sleep();
		}
		swtDisplay.dispose();
	}

	// -- Internal methods --

	@Override
	protected void createUI() {
		swtDisplay = new Display();

		shell = new SWTApplicationFrame(swtDisplay);
		shell.setLayout(new MigLayout("wrap 1"));
		shell.setText(uiService.getApp().getTitle());

		toolBar = new SWTToolBar(swtDisplay, shell, getContext());
		statusBar = new SWTStatusBar(shell, getContext());

		createMenus();

		super.createUI();

		shell.pack();
		shell.open();

		new Thread(this, "SWT-Dispatch").start();
	}

	protected void createMenus() {
		final Menu menuBar =
			menuService.createMenus(new SWTMenuCreator(), new Menu(shell));
		shell.setMenuBar(menuBar); // TODO - is this necessary?
		eventService.publish(new AppMenusCreatedEvent(menuBar));
	}

}
