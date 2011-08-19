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

import imagej.AbstractService;
import imagej.ImageJ;
import imagej.Service;
import imagej.data.DataObject;
import imagej.data.Dataset;
import imagej.display.event.DisplayActivatedEvent;
import imagej.display.event.DisplayCreatedEvent;
import imagej.display.event.DisplayDeletedEvent;
import imagej.display.event.window.WinActivatedEvent;
import imagej.display.event.window.WinClosedEvent;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.ext.InstantiableException;
import imagej.ext.plugin.PluginInfo;
import imagej.ext.plugin.PluginService;
import imagej.object.ObjectService;
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
public final class DisplayService extends AbstractService {

	private final EventService eventService;
	private final ObjectService objectService;
	private final PluginService pluginService;

	private Display activeDisplay;

	/** Maintain list of subscribers, to avoid garbage collection. */
	private List<EventSubscriber<?>> subscribers;

	public DisplayService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public DisplayService(final ImageJ context, final EventService eventService,
		final ObjectService objectService, final PluginService pluginService)
	{
		super(context);
		this.eventService = eventService;
		this.objectService = objectService;
		this.pluginService = pluginService;
	}

	// -- DisplayService methods --

	public EventService getEventService() {
		return eventService;
	}

	public ObjectService getObjectService() {
		return objectService;
	}

	public PluginService getPluginService() {
		return pluginService;
	}

	/** Gets the currently active {@link Display}. */
	public Display getActiveDisplay() {
		return activeDisplay;
	}
	
	/** Gets the currently active {@link Display}. */
	public ImageDisplay getActiveImageDisplay() {
		if (activeDisplay == null || !(activeDisplay instanceof ImageDisplay) ) return null;
		return (ImageDisplay) activeDisplay;
	}

	/** Sets the currently active {@link Display}. */
	public void setActiveDisplay(final Display display) {
		activeDisplay = display;
		eventService.publish(new DisplayActivatedEvent(display));
	}

	/**
	 * Gets the active {@link Dataset}, if any, of the currently active
	 * {@link Display}.nbv
	 */
	public Dataset getActiveDataset() {
		return getActiveDataset(getActiveImageDisplay() );
	}

	/**
	 * Gets the active {@link DatasetView}, if any, of the currently active
	 * {@link Display}.
	 */
	public DatasetView getActiveDatasetView() {
		return getActiveDatasetView(getActiveImageDisplay());
	}

	/** Gets the active {@link Dataset}, if any, of the given {@link Display}. */
	public Dataset getActiveDataset(final ImageDisplay display) {
		final DatasetView activeDatasetView = getActiveDatasetView(display);
		return activeDatasetView == null ? null : activeDatasetView.getDataObject();
	}

	/**
	 * Gets the active {@link DatasetView}, if any, of the given {@link Display}.
	 */
	public DatasetView getActiveDatasetView(final ImageDisplay display) {
		if (display == null ) {
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
		return objectService.getObjects(Display.class);
	}
	
	/** Gets a list of all available {@link ImageDisplay}s. */
	public List<ImageDisplay> getImageDisplays() {
		return objectService.getObjects(ImageDisplay.class);
	}

	/** Gets a {@link Display} by its name. */
	public Display getDisplay(final String name) {
		for (final Display display : getDisplays()) {
			if (name.equalsIgnoreCase(display.getName())) {
				return display;
			}
		}
		return null;
	}

	/**
	 * Gets a list of {@link ImageDisplay}s containing the given {@link DataObject}.
	 */
	public List<ImageDisplay> getDisplays(final DataObject dataObject) {
		final ArrayList<ImageDisplay> imageDisplays = new ArrayList<ImageDisplay>();
		for (final Display display : getImageDisplays()) {
			// check whether data object is present in this display
			for (final DisplayView view :((ImageDisplay)display).getViews()) {
				if (dataObject == view.getDataObject()) {
					imageDisplays.add( (ImageDisplay)display);
					break;
				}
			}
		}
		return imageDisplays;
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

	/** Creates a {@link ImageDisplay} and adds the given {@link Dataset} as a view. */
	public ImageDisplay createDisplay(final Dataset dataset) {
		// get available display plugins from the plugin service
		final List<PluginInfo<ImageDisplay>> plugins =
				pluginService.getPluginsOfType(ImageDisplay.class);

		for (final PluginInfo<ImageDisplay> pe : plugins) {
			try {
				final ImageDisplay displayPlugin = pe.createInstance();
				// display dataset using the first compatible DisplayPlugin
				// TODO: how to handle multiple matches? prompt user with dialog box?
				if (displayPlugin.canDisplay(dataset)) {
					displayPlugin.display(dataset);
					eventService.publish(new DisplayCreatedEvent(displayPlugin));
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
						if(display instanceof ImageDisplay) {
						final ArrayList<DisplayView> views =
								new ArrayList<DisplayView>(((ImageDisplay)display).getViews());
						for (final DisplayView view : views) {
							view.dispose();
						}}

					// HACK - Necessary to plug memory leak when closing the last window.
					// Might be slow since it has to walk the whole ObjectService object
					// list. Note that we could ignore this. Next created display will
					// make old invalid activeDataset reference reclaimable.
					if (getDisplays().size() == 1) {
						setActiveDisplay(null);
					}

					getEventService().publish(new DisplayDeletedEvent(display));
				}

			};
		subscribers.add(winClosedSubscriber);
		eventService.subscribe(WinClosedEvent.class, winClosedSubscriber);

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
		eventService.subscribe(WinActivatedEvent.class, winActivatedSubscriber);
	}

}
