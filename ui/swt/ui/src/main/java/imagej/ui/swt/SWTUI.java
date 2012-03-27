
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
