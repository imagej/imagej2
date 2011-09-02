//
// AbstractSwingUI.java
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

package imagej.ui.swing;

import imagej.ui.common.awt.AWTKeyEventDispatcherGlobal;
import imagej.ImageJ;
import imagej.display.Display;
import imagej.display.DisplayPanel;
import imagej.display.DisplayService;
import imagej.display.ImageDisplay;
import imagej.display.event.DisplayCreatedEvent;
import imagej.display.event.DisplayDeletedEvent;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.ext.menu.MenuService;
import imagej.ext.menu.ShadowMenu;
import imagej.ext.ui.swing.SwingJMenuBarCreator;
import imagej.platform.event.AppMenusCreatedEvent;
import imagej.platform.event.AppQuitEvent;
import imagej.ui.AbstractUI;
import imagej.ui.UIService;
import imagej.ui.swing.display.SwingDisplayPanel;
import imagej.ui.swing.display.SwingDisplayWindow;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * Abstract superclass for Swing-based user interfaces.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 * @author Grant Harris
 */
public abstract class AbstractSwingUI extends AbstractUI {

	private SwingApplicationFrame appFrame;
	private SwingToolBar toolBar;
	private SwingStatusBar statusBar;

	private ArrayList<EventSubscriber<?>> subscribers;

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
	public void createMenus() {
		final JMenuBar menuBar = createMenuBar(appFrame);
		getUIService().getEventService().publish(new AppMenusCreatedEvent(menuBar));
	}

	// -- Internal methods --

	@Override
	protected void createUI() {
		appFrame = new SwingApplicationFrame("ImageJ");
		toolBar = new SwingToolBar();
		statusBar = new SwingStatusBar();
		createMenus();

		setupAppFrame();

		appFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		appFrame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(final WindowEvent evt) {
				getUIService().getEventService().publish(new AppQuitEvent());
			}
		});

		appFrame.getContentPane().add(toolBar, BorderLayout.NORTH);
		appFrame.getContentPane().add(statusBar, BorderLayout.SOUTH);

		appFrame.pack();
		appFrame.setVisible(true);

		String osName = System.getProperty("os.name").toLowerCase();
		boolean isMacOs = osName.startsWith("mac os x");
		if (isMacOs) {
			subscribeToEvents();
		}
		addGlobalKeyListener();
	}

	void addGlobalKeyListener() {
		AWTKeyEventDispatcherGlobal globalKey = AWTKeyEventDispatcherGlobal.getInstance();
	}
	
	protected abstract void setupAppFrame();

	/**
	 * Creates a {@link JMenuBar} from the master {@link ShadowMenu} structure,
	 * and adds it to the given {@link JFrame}.
	 */
	protected JMenuBar createMenuBar(final JFrame f) {
		final MenuService menuService = ImageJ.get(MenuService.class);
		final JMenuBar menuBar =
			menuService.createMenus(new SwingJMenuBarCreator(), new JMenuBar());
		f.setJMenuBar(menuBar);
		f.validate();
		return menuBar;
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

	// -- Helper methods --

	private void subscribeToEvents() {
		final UIService uiService = getUIService();
		final EventService eventService = uiService.getEventService();
		subscribers = new ArrayList<EventSubscriber<?>>();

		if (uiService.getPlatformService().isMenuBarDuplicated()) {
			// NB: If menu bars are supposed to be duplicated across all window
			// frames, listen for display creations and deletions and clone the menu
			// bar accordingly.

			final EventSubscriber<DisplayCreatedEvent> createSubscriber =
				new EventSubscriber<DisplayCreatedEvent>() {

					@Override
					public void onEvent(final DisplayCreatedEvent event) {
						final Display display = event.getObject();
						final DisplayPanel panel = display.getDisplayPanel();
						if (!(panel instanceof SwingDisplayPanel)) return;
						final SwingDisplayPanel swingPanel = (SwingDisplayPanel) panel;
						final SwingDisplayWindow swingWindow =
							(SwingDisplayWindow) SwingUtilities.getWindowAncestor(swingPanel);
						// add a copy of the JMenuBar to the new display
						if (swingWindow.getJMenuBar() == null) createMenuBar(swingWindow);
					}
				};
			subscribers.add(createSubscriber);
			eventService.subscribe(DisplayCreatedEvent.class, createSubscriber);

			final EventSubscriber<DisplayDeletedEvent> deleteSubscriber =
				new EventSubscriber<DisplayDeletedEvent>() {

					@Override
					public void onEvent(final DisplayDeletedEvent event) {
						final Display display = event.getObject();
						final DisplayPanel panel = display.getDisplayPanel();
						if (!(panel instanceof SwingDisplayPanel)) return;
						final SwingDisplayPanel swingPanel = (SwingDisplayPanel) panel;
						final SwingDisplayWindow swingWindow =
							(SwingDisplayWindow) SwingUtilities.getWindowAncestor(swingPanel);
						deleteMenuBar(swingWindow);
					}
				};
			subscribers.add(deleteSubscriber);
			eventService.subscribe(DisplayDeletedEvent.class, deleteSubscriber);
		}
	}

}
