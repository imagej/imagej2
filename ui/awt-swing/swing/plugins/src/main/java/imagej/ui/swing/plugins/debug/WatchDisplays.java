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

package imagej.ui.swing.plugins.debug;

import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.ext.display.Display;
import imagej.ext.display.DisplayService;
import imagej.ext.display.event.DisplayActivatedEvent;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.object.event.ObjectsListEvent;
import imagej.ui.swing.StaticSwingUtils;
import imagej.ui.swing.SwingOutputWindow;

import java.util.List;

import javax.swing.SwingUtilities;

/**
 * TODO
 * 
 * @author Grant Harris
 */
@Plugin(menuPath = "Plugins>Debug>Watch Displays")
public class WatchDisplays implements ImageJPlugin {

	@Parameter(persist = false)
	private EventService eventService;

	@Parameter(persist = false)
	private DisplayService displayService;

	protected static SwingOutputWindow window;

	/** Maintains the list of event subscribers, to avoid garbage collection. */
	@SuppressWarnings("unused")
	private List<EventSubscriber<?>> subscribers;

	@Override
	public void run() {
		window = new SwingOutputWindow("Displays");
		StaticSwingUtils.locateLowerRight(window);
		window.setVisible(true);
		showDisplays();
		subscribers = eventService.subscribe(this);
	}

	public void showDisplays() {
		window.clear();
		final List<Display<?>> displays = displayService.getDisplays();
		final Display<?> active = displayService.getActiveDisplay();
		for (final Display<?> display : displays) {
			if (display == active) {
				window.append("** " + display.toString() + "\n");
			}
			else {
				window.append(display.toString() + "\n");
			}
		}

		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					window.repaint();
				}

			});
		}
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(@SuppressWarnings("unused")
	final ObjectsListEvent event)
	{
		showDisplays();
	}

//	@EventHandler
//	protected void onEvent(final WinActivatedEvent event) {
//		showDisplays();
//	}

	@EventHandler
	public void onEvent(@SuppressWarnings("unused")
	final DisplayActivatedEvent event)
	{
		showDisplays();
	}

}
