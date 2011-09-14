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

package imagej.data.display;

import imagej.AbstractService;
import imagej.ImageJ;
import imagej.Service;
import imagej.data.Dataset;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.ext.InstantiableException;
import imagej.ext.display.Display;
import imagej.ext.display.event.DisplayActivatedEvent;
import imagej.ext.display.event.DisplayCreatedEvent;
import imagej.ext.display.event.DisplayDeletedEvent;
import imagej.ext.display.event.window.WinActivatedEvent;
import imagej.ext.display.event.window.WinClosedEvent;
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

	// TODO - implement queue of most recently activated displays.
	// Can actually keep a list of all known displays in this class, and pull
	// the most recently activated one to the front. Then can have an API method
	// for asking for "active" display of a particular type, and it will just
	// iterate through the list of known displays for the first match.

	private Display<?> activeDisplay;

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

	// -- DisplayService methods - active displays --

	/** Gets the currently active display. */
	public Display<?> getActiveDisplay() {
		return activeDisplay;
	}

	/** Sets the currently active display. */
	public void setActiveDisplay(final Display<?> display) {
		activeDisplay = display;
		eventService.publish(new DisplayActivatedEvent(display));
	}

	// -- DisplayService methods - display plugin discovery --

	/** Gets the list of known display plugins. */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<PluginInfo<Display<?>>> getDisplayPlugins() {
		return (List) pluginService.getPluginsOfType(Display.class);
	}

	/**
	 * Gets the display plugin of the given class, or null if none.
	 */
	public <D extends Display<?>> PluginInfo<D> getDisplayPlugin(
		final Class<D> pluginClass)
	{
		return pluginService.getPlugin(pluginClass);
	}

	/**
	 * Gets the display plugin of the given class name, or null if none.
	 * 
	 * @throws ClassCastException if the plugin found is not a display plugin.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public PluginInfo<Display<?>> getDisplayPlugin(final String className) {
		return (PluginInfo) pluginService.getPlugin(className);
	}

	/**
	 * Gets the list of display plugins of the given type (e.g.,
	 * <code>ImageDisplay.class</code>).
	 */
	public <D extends Display<?>> List<PluginInfo<D>> getDisplayPluginsOfType(
		final Class<D> type)
	{
		return pluginService.getPluginsOfType(type);
	}

	// -- DisplayService methods - display discovery --

	/** Gets a list of all available displays. */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Display<?>> getDisplays() {
		return (List) objectService.getObjects(Display.class);
	}

	/**
	 * Gets a list of all available displays of the given type (e.g.,
	 * <code>ImageDisplay.class</code>).
	 */
	public <D extends Display<?>> List<D> getDisplaysOfType(final Class<D> type) {
		return objectService.getObjects(type);
	}

	/** Gets a display by its name. */
	public Display<?> getDisplay(final String name) {
		for (final Display<?> display : getDisplays()) {
			if (name.equalsIgnoreCase(display.getName())) {
				return display;
			}
		}
		return null;
	}

	/** Gets a list of displays containing the given object. */
	public List<Display<?>> getDisplaysContaining(final Object o) {
		final ArrayList<Display<?>> displays = new ArrayList<Display<?>>();
		for (final Display<?> display : getDisplays()) {
			if (display.contains(o)) displays.add(display);
		}
		return displays;
	}

	// -- DisplayService methods - display creation --

	/**
	 * Checks whether the given name is already taken by an existing display.
	 * 
	 * @param name The name to check.
	 * @return true if the name is available, false if already taken.
	 */
	public boolean isUniqueName(final String name) {
		final List<Display<?>> displays = getDisplays();
		for (final Display<?> display : displays) {
			if (name.equalsIgnoreCase(display.getName())) {
				return false;
			}
		}
		return true;
	}

	/** Creates a display for the given object. */
	public Display<?> createDisplay(final String name, final Object o) {
		// get available display plugins from the plugin service
		final List<PluginInfo<Display<?>>> displayPlugins = getDisplayPlugins();

		for (final PluginInfo<Display<?>> info : displayPlugins) {
			try {
				final Display<?> display = info.createInstance();
				// display object using the first compatible Display
				// TODO: how to handle multiple matches? prompt user with dialog box?
				if (display.canDisplay(o)) {
					display.setName(name);
					display.display(o);
					eventService.publish(new DisplayCreatedEvent(display));
					return display;
				}
			}
			catch (final InstantiableException e) {
				Log.error("Invalid display plugin: " + info, e);
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

				// CTR FIXME display views should not be disposed here!
				// This is the job of the display itself when display.dispose()
				// and/or display.close() gets called.

				@Override
				public void onEvent(final WinClosedEvent event) {
					final Display<?> display = event.getDisplay();
					if (display instanceof ImageDisplay) {
						final ImageDisplay imageDisplay = (ImageDisplay) display;
						final ArrayList<DisplayView> views =
							new ArrayList<DisplayView>(imageDisplay);
						for (final DisplayView view : views) {
							view.dispose();
						}
					}

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
					final Display<?> display = event.getDisplay();
					setActiveDisplay(display);
				}

			};
		subscribers.add(winActivatedSubscriber);
		eventService.subscribe(WinActivatedEvent.class, winActivatedSubscriber);
	}

	// -- Deprecated methods --

	/** Gets the currently active {@link Display}. */
	@Deprecated
	public ImageDisplay getActiveImageDisplay() {
		if (activeDisplay == null || !(activeDisplay instanceof ImageDisplay)) {
			return null;
		}
		return (ImageDisplay) activeDisplay;
	}

	/**
	 * Gets the active {@link Dataset}, if any, of the currently active
	 * {@link Display}.nbv
	 */
	@Deprecated
	public Dataset getActiveDataset() {
		return getActiveDataset(getActiveImageDisplay());
	}

	/**
	 * Gets the active {@link DatasetView}, if any, of the currently active
	 * {@link Display}.
	 */
	@Deprecated
	public DatasetView getActiveDatasetView() {
		return getActiveDatasetView(getActiveImageDisplay());
	}

	/** Gets the active {@link Dataset}, if any, of the given {@link Display}. */
	@Deprecated
	public Dataset getActiveDataset(final ImageDisplay display) {
		final DatasetView activeDatasetView = getActiveDatasetView(display);
		return activeDatasetView == null ? null : activeDatasetView.getDataObject();
	}

	/**
	 * Gets the active {@link DatasetView}, if any, of the given {@link Display}.
	 */
	@Deprecated
	public DatasetView getActiveDatasetView(final ImageDisplay display) {
		if (display == null) {
			return null;
		}
		final DisplayView activeView = display.getActiveView();
		if (activeView instanceof DatasetView) {
			return (DatasetView) activeView;
		}
		return null;
	}

	/** Gets a list of all available {@link ImageDisplay}s. */
	@Deprecated
	public List<ImageDisplay> getImageDisplays() {
		return objectService.getObjects(ImageDisplay.class);
	}

}
