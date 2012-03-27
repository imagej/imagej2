
package imagej.ui.awt;

import imagej.ext.display.Display;
import imagej.ext.menu.MenuService;
import imagej.ext.plugin.Plugin;
import imagej.ext.ui.awt.AWTMenuBarCreator;
import imagej.platform.event.AppMenusCreatedEvent;
import imagej.ui.ApplicationFrame;
import imagej.ui.Desktop;
import imagej.ui.DialogPrompt;
import imagej.ui.DialogPrompt.MessageType;
import imagej.ui.DialogPrompt.OptionType;
import imagej.ui.UserInterface;
import imagej.ui.OutputWindow;
import imagej.ui.UIService;

import java.awt.BorderLayout;
import java.awt.MenuBar;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * AWT-based user interface for ImageJ.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = UserInterface.class)
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
		final MenuService menuService = uiService.getMenuService();
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
	public void showContextMenu(final String menuRoot, final Display<?> display,
		final int x, final int y)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
