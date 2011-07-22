//
// WatchDisplays.java
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

package imagej.ui.swing.plugins.debug;

import imagej.ImageJ;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.display.event.DisplayActivatedEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Plugin;
import imagej.object.event.ObjectsChangedEvent;
import imagej.ui.swing.StaticSwingUtils;
import imagej.ui.swing.SwingOutputWindow;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

/**
 * TODO
 *
 * @author Grant Harris
 */
@Plugin(menuPath = "Plugins>Debug>Watch Displays")
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
		final DisplayService displayService = ImageJ.get(DisplayService.class);
		List<Display> displays = displayService.getDisplays();
		final Display active = displayService.getActiveDisplay();
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

		final EventSubscriber<ObjectsChangedEvent> objectsUpdatedSubscriber =
				new EventSubscriber<ObjectsChangedEvent>() {

					@Override
					public void onEvent(final ObjectsChangedEvent event) {
						showDisplays();
					}

				};
		subscribers.add(objectsUpdatedSubscriber);
		Events.subscribe(ObjectsChangedEvent.class, objectsUpdatedSubscriber);
		
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
