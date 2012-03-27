/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
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

package imagej.core.plugins.convolve;

import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.event.StatusEvent;
import imagej.ext.KeyCode;
import imagej.ext.display.Display;
import imagej.ext.display.event.DisplayDeletedEvent;
import imagej.ext.display.event.input.KyPressedEvent;
import imagej.ext.menu.MenuConstants;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.util.Log;
import imagej.util.RealRect;

import java.util.List;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;

/**
 * Implements IJ1's Shadows Demo plugin functionality.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = MenuConstants.PROCESS_LABEL,
		weight = MenuConstants.PROCESS_WEIGHT,
		mnemonic = MenuConstants.PROCESS_MNEMONIC),
	@Menu(label = "Shadows", mnemonic = 's'),
	@Menu(label = "Shadows Demo", weight = 200) }, headless = true)
public class ShadowsDemo implements ImageJPlugin {

	private static final double[][] KERNELS = new double[][] {
		ShadowsNorth.KERNEL, ShadowsNortheast.KERNEL, ShadowsEast.KERNEL,
		ShadowsSoutheast.KERNEL, ShadowsSouth.KERNEL, ShadowsSouthwest.KERNEL,
		ShadowsWest.KERNEL, ShadowsNorthwest.KERNEL };

	// -- instance variables that are Parameters --

	@Parameter(persist = false)
	private EventService eventService;

	@Parameter(persist = false)
	private ImageDisplayService imgDispService;

	@Parameter(persist = false)
	private OverlayService overlayService;

	@Parameter
	private Dataset input;

	// -- private instance variables --

	private boolean userHasQuit = false;
	private ImageDisplay currDisplay = null;

	private List<EventSubscriber<?>> subscribers;

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
		subscribers = eventService.subscribe(this);
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

		// unsubscribe from events; this keeps ImageJ from maintaining
		// dangling references to obsolete event listeners
		eventService.unsubscribe(subscribers);
	}

	/**
	 * Returns true if image cannot be represented as a single plane for display.
	 * This mirrors IJ1's behavior.
	 */
	private boolean unsupportedImage() {
		final AxisType[] axes = input.getAxes();
		final long[] dims = input.getDims();
		for (int i = 0; i < axes.length; i++) {
			final AxisType axis = axes[i];
			if (axis == Axes.X) continue;
			if (axis == Axes.Y) continue;
			if ((axis == Axes.CHANNEL) && input.isRGBMerged()) continue;
			if (dims[i] != 1) return true;
		}
		return false;
	}

	@EventHandler
	protected void onEvent(final KyPressedEvent event) {
		if (event.getCode() == KeyCode.ESCAPE) {
			final Display<?> display = event.getDisplay();
			if (display != null) {
				if (display == currDisplay) userHasQuit = true;
			}
			else { // display == null : event from application bar
				if (imgDispService.getActiveImageDisplay() == currDisplay) {
					userHasQuit = true;
				}
			}
		}
	}

	@EventHandler
	protected void onEvent(final DisplayDeletedEvent event) {
		if (event.getObject() == currDisplay) userHasQuit = true;
	}

}
