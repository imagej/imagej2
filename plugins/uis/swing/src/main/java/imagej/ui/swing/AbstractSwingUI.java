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

package imagej.ui.swing;

import imagej.display.Display;
import imagej.menu.MenuService;
import imagej.menu.ShadowMenu;
import imagej.platform.event.AppMenusCreatedEvent;
import imagej.ui.AbstractUserInterface;
import imagej.ui.SystemClipboard;
import imagej.ui.common.awt.AWTClipboard;
import imagej.ui.common.awt.AWTDropTargetEventDispatcher;
import imagej.ui.common.awt.AWTInputEventDispatcher;
import imagej.ui.common.awt.AWTWindowEventDispatcher;
import imagej.ui.swing.menu.SwingJMenuBarCreator;
import imagej.ui.swing.menu.SwingJPopupMenuCreator;
import imagej.ui.viewer.DisplayViewer;
import imagej.widget.FileWidget;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.WindowConstants;

/**
 * Abstract superclass for Swing-based user interfaces.
 * 
 * @author Curtis Rueden
 * @author Lee Kamentsky
 * @author Barry DeZonia
 * @author Grant Harris
 */
public abstract class AbstractSwingUI extends AbstractUserInterface {

	private SwingApplicationFrame appFrame;
	private SwingToolBar toolBar;
	private SwingStatusBar statusBar;
	private AWTClipboard systemClipboard;

	// -- UserInterface methods --

	@Override
	public SwingApplicationFrame getApplicationFrame() {
		return appFrame;
	}

	@Override
	public SwingToolBar getToolBar() {
		return toolBar;
	}

	@Override
	public SwingStatusBar getStatusBar() {
		return statusBar;
	}

	@Override
	public SystemClipboard getSystemClipboard() {
		return systemClipboard;
	}

	@Override
	public File chooseFile(File file, String style) {
		final JFileChooser chooser = new JFileChooser(file);
		if (FileWidget.DIRECTORY_STYLE.equals(style)) {
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		final int rval;
		if (FileWidget.SAVE_STYLE.equals(style)) {
			rval = chooser.showSaveDialog(appFrame);
		}
		else { // default behavior
			rval = chooser.showOpenDialog(appFrame);
		}
		if (rval != JFileChooser.APPROVE_OPTION) return null;
		return chooser.getSelectedFile();
	}

	@Override
	public void showContextMenu(final String menuRoot, final Display<?> display,
		final int x, final int y)
	{
		final MenuService menuService = getUIService().getMenuService();
		final ShadowMenu shadowMenu = menuService.getMenu(menuRoot);

		final JPopupMenu popupMenu = new JPopupMenu();
		new SwingJPopupMenuCreator().createMenus(shadowMenu, popupMenu);

		final DisplayViewer<?> displayViewer =
			getUIService().getDisplayViewer(display);
		if (displayViewer != null) {
			final Component invoker = (Component) displayViewer.getPanel();
			popupMenu.show(invoker, x, y);
		}
	}

	// -- Disposable methods --

	@Override
	public void dispose() {
		if (appFrame != null) appFrame.dispose();
	}

	// -- Internal methods --

	@Override
	protected void createUI() {
		final JMenuBar menuBar = createMenus();

		appFrame = new SwingApplicationFrame(getApp().getTitle());
		if (menuBar != null) appFrame.setJMenuBar(menuBar);

		toolBar = new SwingToolBar(getUIService());
		statusBar = new SwingStatusBar(getUIService());
		systemClipboard = new AWTClipboard();

		setupAppFrame();

		super.createUI();

		// NB: The following setup happens for both SDI and MDI frames.

		appFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		appFrame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(final WindowEvent evt) {
				getUIService().getAppEventService().quit();
			}

		});

		appFrame.getContentPane().add(toolBar, BorderLayout.NORTH);
		appFrame.getContentPane().add(statusBar, BorderLayout.SOUTH);

		// listen for input events on all components of the app frame
		final AWTInputEventDispatcher inputDispatcher =
			new AWTInputEventDispatcher(null, getEventService());
		appFrame.addEventDispatcher(inputDispatcher);

		// listen for window events on the app frame
		final AWTWindowEventDispatcher windowDispatcher =
			new AWTWindowEventDispatcher(getEventService());
		windowDispatcher.register(appFrame);

		// listen for drag and drop events
		final AWTDropTargetEventDispatcher dropTargetDispatcher =
			new AWTDropTargetEventDispatcher(null, getEventService());
		dropTargetDispatcher.register(toolBar);
		dropTargetDispatcher.register(statusBar);
		dropTargetDispatcher.register(appFrame);

		appFrame.pack();
		appFrame.setVisible(true);
	}

	/**
	 * Creates a {@link JMenuBar} from the master {@link ShadowMenu} structure.
	 */
	protected JMenuBar createMenus() {
		final MenuService menuService = getUIService().getMenuService();
		final JMenuBar menuBar =
			menuService.createMenus(new SwingJMenuBarCreator(), new JMenuBar());
		final AppMenusCreatedEvent appMenusCreatedEvent =
			new AppMenusCreatedEvent(menuBar);
		getEventService().publish(appMenusCreatedEvent);
		if (appMenusCreatedEvent.isConsumed()) {
			// something else (e.g., MacOSXPlatform) handled the menus
			return null;
		}
		return menuBar;
	}

	/**
	 * Configures the application frame for subclass-specific settings (e.g., SDI
	 * or MDI).
	 */
	protected abstract void setupAppFrame();

}
