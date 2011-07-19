//
// DisplayService.java
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

package imagej.display;

import imagej.IService;
import imagej.ImageJ;
import imagej.Service;
import imagej.data.DataObject;
import imagej.data.Dataset;
import imagej.display.event.DisplayActivatedEvent;
import imagej.display.event.DisplayCreatedEvent;
import imagej.display.event.DisplayDeletedEvent;
import imagej.display.event.window.WinActivatedEvent;
import imagej.display.event.window.WinClosedEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.object.ObjectService;
import imagej.plugin.InstantiableException;
import imagej.plugin.PluginInfo;
import imagej.plugin.PluginService;
import imagej.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for working with {@link Display}s.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 * @author Grant Harris
 */
@Service
public final class DisplayService implements IService {

	private Display activeDisplay;

	/** Maintain list of subscribers, to avoid garbage collection. */
	private List<EventSubscriber<?>> subscribers;

	// -- DisplayService methods --

	/** Gets the currently active {@link Display}. */
	public Display getActiveDisplay() {
		return activeDisplay;
	}

	/** Sets the currently active {@link Display}. */
	public void setActiveDisplay(final Display display) {
		activeDisplay = display;
		Events.publish(new DisplayActivatedEvent(display));
	}

	/**
	 * Gets the active {@link Dataset}, if any, of the currently active
	 * {@link Display}.
	 */
	public Dataset getActiveDataset() {
		return getActiveDataset(activeDisplay);
	}

	/**
	 * Gets the active {@link DatasetView}, if any, of the currently active
	 * {@link Display}.
	 */
	public DatasetView getActiveDatasetView() {
		return getActiveDatasetView(activeDisplay);
	}

	/** Gets the active {@link Dataset}, if any, of the given {@link Display}. */
	public Dataset getActiveDataset(final Display display) {
		final DatasetView activeDatasetView = getActiveDatasetView(display);
		return activeDatasetView == null ? null : activeDatasetView
			.getDataObject();
	}

	/** Gets the active {@link DatasetView}, if any, of the given {@link Display}. */
	public DatasetView getActiveDatasetView(final Display display) {
		if (display == null) {
			return null;
		}
		final DisplayView activeView = display.getActiveView();
		if (activeView instanceof DatasetView) {
			return (DatasetView) activeView;
		}
		return null;
	}

	/** Gets a list of all available {@link Display}s. */
	public List<Display> getDisplays() {
		final ObjectService objectService = ImageJ.get(ObjectService.class);
		return objectService.getObjects(Display.class);
	}

	/**
	 * Gets a list of {@link Display}s containing the given {@link DataObject}.
	 */
	public List<Display> getDisplays(final DataObject dataObject) {
		final ArrayList<Display> displays = new ArrayList<Display>();
		for (final Display display : getDisplays()) {
			// check whether data object is present in this display
			for (final DisplayView view : display.getViews()) {
				if (dataObject == view.getDataObject()) {
					displays.add(display);
					break;
				}
			}
		}
		return displays;
	}

	public boolean isUniqueName(final String name) {
		final List<Display> displays = getDisplays();
		for (final Display display : displays) {
			if (name.equalsIgnoreCase(display.getName())) {
				return false;
			}
		}
		return true;
	}

	/** Creates a {@link Display} and adds the given {@link Dataset} as a view. */
	public Display createDisplay(final Dataset dataset) {
		// get available display plugins from the plugin service
		final PluginService pluginService = ImageJ.get(PluginService.class);
		final List<PluginInfo<Display>> plugins =
			pluginService.getPluginsOfType(Display.class);

		for (final PluginInfo<Display> pe : plugins) {
			try {
				final Display displayPlugin = pe.createInstance();
				// display dataset using the first compatible DisplayPlugin
				// TODO: how to handle multiple matches? prompt user with dialog box?
				if (displayPlugin.canDisplay(dataset)) {
					displayPlugin.display(dataset);
					Events.publish(new DisplayCreatedEvent(displayPlugin));
					return displayPlugin;
				}
			}
			catch (final InstantiableException e) {
				Log.error("Invalid display plugin: " + pe, e);
			}
		}
		return null;
	}

	// -- IService methods --

	@Override
	public void initialize() {
		activeDisplay = null;
		subscribeToEvents();
	}

	// -- Helper methods --

	private void subscribeToEvents() {
		subscribers = new ArrayList<EventSubscriber<?>>();

		// dispose views and delete display when display window is closed
		final EventSubscriber<WinClosedEvent> winClosedSubscriber =
			new EventSubscriber<WinClosedEvent>() {

				@Override
				public void onEvent(final WinClosedEvent event) {
					final Display display = event.getDisplay();
					final ArrayList<DisplayView> views =
						new ArrayList<DisplayView>(display.getViews());
					for (final DisplayView view : views) {
						view.dispose();
					}
					
					// HACK - Necessary to plug memory leak when closing the last window.
					//  Might be slow since it has to walk the whole ObjectService object
					//  list. Note that we could ignore this. Next created display will
					//  make old invalid activeDataset reference reclaimable.
					if (getDisplays().size() == 1)
						setActiveDisplay(null);
					
					Events.publish(new DisplayDeletedEvent(display));
				}

			};
		subscribers.add(winClosedSubscriber);
		Events.subscribe(WinClosedEvent.class, winClosedSubscriber);

		// set display to active when its window is activated
		final EventSubscriber<WinActivatedEvent> winActivatedSubscriber =
			new EventSubscriber<WinActivatedEvent>() {

				@Override
				public void onEvent(final WinActivatedEvent event) {
					final Display display = event.getDisplay();
					setActiveDisplay(display);
				}

			};
		subscribers.add(winActivatedSubscriber);
		Events.subscribe(WinActivatedEvent.class, winActivatedSubscriber);
	}

}
