/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.ui.swing.plugins;

import imagej.ImageJ;
import imagej.display.Display;
import imagej.display.DisplayManager;
import imagej.display.event.DisplayActivatedEvent;
import imagej.display.event.window.WinActivatedEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.object.event.ObjectsUpdatedEvent;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Plugin;
import imagej.ui.swing.StaticSwingUtils;
import imagej.ui.swing.SwingOutputWindow;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 *
 * @author GBH
 */
@Plugin(menuPath = "Plugins>WatchDisplays")
public class WatchDisplays implements ImageJPlugin {

	private static SwingOutputWindow window;
	/** Maintains the list of event subscribers, to avoid garbage collection. */
	private List<EventSubscriber<?>> subscribers;

	@Override
	public void run() {
		window = new SwingOutputWindow("Displays");
		StaticSwingUtils.locateLowerRight(window);
		showDisplays();
		subscribeToEvents();
	}

	public void showDisplays() {
		window.clear();
		final DisplayManager manager = ImageJ.get(DisplayManager.class);
		List<Display> displays = manager.getDisplays();
		final Display active = manager.getActiveDisplay();
		for (Display display : displays) {
			if (display == active) {
				window.append("** " + display.toString() + "\n");
			} else {
				window.append(display.toString() + "\n");
			}
		}

		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					window.repaint();
				}

			});
		}
	}

	private void subscribeToEvents() {
		subscribers = new ArrayList<EventSubscriber<?>>();

		final EventSubscriber<ObjectsUpdatedEvent> objectsUpdatedSubscriber =
				new EventSubscriber<ObjectsUpdatedEvent>() {

					@Override
					public void onEvent(final ObjectsUpdatedEvent event) {
						showDisplays();
					}

				};
		subscribers.add(objectsUpdatedSubscriber);
		Events.subscribe(ObjectsUpdatedEvent.class, objectsUpdatedSubscriber);
		
//		final EventSubscriber<WinActivatedEvent> WinActivatedSubscriber =
//				new EventSubscriber<WinActivatedEvent>() {
//
//					@Override
//					public void onEvent(final WinActivatedEvent event) {
//						showDisplays();
//					}
//
//				};
//		subscribers.add(WinActivatedSubscriber);
//		Events.subscribe(WinActivatedEvent.class, WinActivatedSubscriber);
		
		final EventSubscriber<DisplayActivatedEvent> DisplaySelectedSubscriber =
				new EventSubscriber<DisplayActivatedEvent>() {

					@Override
					public void onEvent(final DisplayActivatedEvent event) {
						showDisplays();
					}

				};
		subscribers.add(DisplaySelectedSubscriber);
		Events.subscribe(DisplayActivatedEvent.class, DisplaySelectedSubscriber);
		
	}

}
