
package imagej.ui.pivot;

import imagej.ImageJ;
import imagej.event.EventService;
import imagej.ext.menu.MenuService;
import imagej.ext.ui.pivot.PivotMenuCreator;
import imagej.platform.event.AppMenusCreatedEvent;

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Orientation;

/**
 * Pivot {@link Application} implementation for ImageJ.
 * 
 * @author Curtis Rueden
 */
public class PivotApplication implements Application {

	private ImageJ context;
	private EventService eventService;
	private MenuService menuService;

	private PivotApplicationFrame frame;
	private PivotToolBar toolBar;
	private PivotStatusBar statusBar;

	private BoxPane contentPane;

	// -- Constructor --

	public PivotApplication() {
		// get the current ImageJ context
		context = ImageJ.getContext();
		eventService = context.getService(EventService.class);
		menuService = context.getService(MenuService.class);
	}

	// -- Application methods --

	@Override
	public void startup(final Display display,
		final Map<String, String> properties)
	{
		frame = new PivotApplicationFrame();
		toolBar = new PivotToolBar();
		System.out.println("PivotUI: " + this);
		System.out.println("PivotUI.startup: event service = " + eventService);
		statusBar = new PivotStatusBar(eventService);

		contentPane = new BoxPane();
		contentPane.setOrientation(Orientation.VERTICAL);
		frame.setContent(contentPane);

		// create menus
		final BoxPane menuPane =
			menuService.createMenus(new PivotMenuCreator(), new BoxPane());
		contentPane.add(menuPane);
		eventService.publish(new AppMenusCreatedEvent(menuPane));

		contentPane.add(toolBar);
		contentPane.add(statusBar);

		frame.setTitle("ImageJ");
		frame.setMaximized(true);
		frame.open(display);
	}

	@Override
	public boolean shutdown(final boolean optional) {
		if (frame != null) frame.close();
		return false;
	}

	@Override
	public void suspend() {
		// NB: no action needed.
	}

	@Override
	public void resume() {
		// NB: no action needed.
	}

}
