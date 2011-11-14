//
// ShadowsDemo.java
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

package imagej.core.plugins.convolve;

import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.event.StatusEvent;
import imagej.ext.KeyCode;
import imagej.ext.display.Display;
import imagej.ext.display.event.DisplayDeletedEvent;
import imagej.ext.display.event.input.KyPressedEvent;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.util.Log;
import imagej.util.RealRect;
import net.imglib2.img.Axes;
import net.imglib2.img.Axis;

/**
 * Implements IJ1's Shadows Demo plugin functionality.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = { @Menu(label = "Process", mnemonic = 'p'),
	@Menu(label = "Shadows", mnemonic = 's'),
	@Menu(label = "Shadows Demo", weight = 200) })
public class ShadowsDemo implements ImageJPlugin {

	private static final double[][] KERNELS = new double[][] {
		ShadowsNorth.KERNEL, ShadowsNortheast.KERNEL, ShadowsEast.KERNEL,
		ShadowsSoutheast.KERNEL, ShadowsSouth.KERNEL, ShadowsSouthwest.KERNEL,
		ShadowsWest.KERNEL, ShadowsNorthwest.KERNEL };

	// -- instance variables that are Parameters --

	@Parameter(required = true, persist = false)
	private EventService eventService;

	@Parameter(required = true, persist = false)
	private ImageDisplayService imgDispService;

	@Parameter(required = true, persist = false)
	private OverlayService overlayService;

	@Parameter
	private Dataset input;

	// -- private instance variables --

	private boolean userHasQuit = false;
	private ImageDisplay currDisplay = null;
	private EventSubscriber<KyPressedEvent> kyPressSubscriber;
	private EventSubscriber<DisplayDeletedEvent> displaySubscriber;

	// -- public interface --

	/**
	 * Runs the plugin. The plugin continually runs each shadow transformation
	 * until ESC is pressed.
	 */
	@Override
	public void run() {

		final ImageDisplay display = imgDispService.getActiveImageDisplay();
		if (display == null) return;
		currDisplay = display;
		if (unsupportedImage()) {
			Log.error("This command only works with a single plane of data");
			return;
		}
		subscribeToEvents();
		eventService.publish(new StatusEvent("Press ESC to terminate"));

		final RealRect selection = overlayService.getSelectionBounds(currDisplay);
		final Dataset originalData = input.duplicate();
		userHasQuit = false;
		while (!userHasQuit) {
			for (int i = 0; i < KERNELS.length; i++) {
				final Convolve3x3Operation operation =
					new Convolve3x3Operation(input, selection, KERNELS[i]);
				operation.run();
				try {
					Thread.sleep(100);
				}
				catch (final Exception e) {
					// do nothing
				}
				originalData.copyInto(input);
				if (userHasQuit) break;
			}
		}
		eventService.publish(new StatusEvent("Shadows demo terminated"));
		unsubscribeFromEvents();
	}

	/**
	 * Returns true if image cannot be represented as a single plane for display.
	 * This mirrors IJ1's behavior.
	 */
	private boolean unsupportedImage() {
		final Axis[] axes = input.getAxes();
		final long[] dims = input.getDims();
		for (int i = 0; i < axes.length; i++) {
			final Axis axis = axes[i];
			if (axis == Axes.X) continue;
			if (axis == Axes.Y) continue;
			if ((axis == Axes.CHANNEL) && input.isRGBMerged()) continue;
			if (dims[i] != 1) return true;
		}
		return false;
	}

	/**
	 * Subscribes to events that will track when the user has decided to quit.
	 */
	@SuppressWarnings("synthetic-access")
	private void subscribeToEvents() {
		kyPressSubscriber = new EventSubscriber<KyPressedEvent>() {

			@Override
			public void onEvent(final KyPressedEvent event) {
				if (event.getCode() == KeyCode.ESCAPE) {
					final Display<?> display = event.getDisplay();
					if (display != null) {
						if (display == currDisplay) userHasQuit = true;
					}
					else { // display == null : event from application bar
						if (imgDispService.getActiveImageDisplay() == currDisplay) userHasQuit =
							true;
					}
				}
			}
		};
		eventService.subscribe(kyPressSubscriber);

		displaySubscriber = new EventSubscriber<DisplayDeletedEvent>() {

			@Override
			public void onEvent(final DisplayDeletedEvent event) {
				if (event.getObject() == currDisplay) userHasQuit = true;
			}
		};
		eventService.subscribe(displaySubscriber);
	}

	/**
	 * Unsubscribes from events. this keeps IJ2 from maintaining dangling
	 * references to obsolete event listeners.
	 */
	private void unsubscribeFromEvents() {
		eventService.unsubscribe(kyPressSubscriber);
		eventService.unsubscribe(displaySubscriber);
	}

}
