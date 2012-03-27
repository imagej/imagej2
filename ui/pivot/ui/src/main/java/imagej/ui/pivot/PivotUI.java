
package imagej.ui.pivot;

import imagej.ext.display.Display;
import imagej.ext.plugin.Plugin;
import imagej.ui.Desktop;
import imagej.ui.DialogPrompt;
import imagej.ui.DialogPrompt.MessageType;
import imagej.ui.DialogPrompt.OptionType;
import imagej.ui.UserInterface;
import imagej.ui.OutputWindow;
import imagej.ui.UIService;

import java.util.concurrent.Callable;

import org.apache.pivot.wtk.DesktopApplicationContext;

/**
 * Apache Pivot-based user interface for ImageJ.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = UserInterface.class)
public class PivotUI implements UserInterface, Callable<Object> {

	private UIService uiService;

	// -- IUserInterface methods --

	@Override
	public void initialize(final UIService service) {
		uiService = service;
		uiService.getThreadService().run(this);
	}

	@Override
	public void processArgs(final String[] args) {
		// TODO
	}

	@Override
	public void createMenus() {
		//
	}

	@Override
	public UIService getUIService() {
		return uiService;
	}

	@Override
	public PivotApplicationFrame getApplicationFrame() {
		return null;
	}

	@Override
	public Desktop getDesktop() {
		return null;
	}

	@Override
	public PivotToolBar getToolBar() {
		return null;
	}

	@Override
	public PivotStatusBar getStatusBar() {
		return null;
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

	// -- Callable methods --

	@Override
	public Object call() {
		final String[] args = { PivotApplication.class.getName() };
		DesktopApplicationContext.main(args);
		return null;
	}

}
