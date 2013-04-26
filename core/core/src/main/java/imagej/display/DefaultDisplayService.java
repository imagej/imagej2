/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package imagej.display;

import imagej.display.event.DisplayActivatedEvent;
import imagej.display.event.DisplayCreatedEvent;
import imagej.display.event.DisplayDeletedEvent;
import imagej.display.event.window.WinActivatedEvent;
import imagej.display.event.window.WinClosedEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.object.ObjectService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Default service for working with {@link Display}s.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 * @author Grant Harris
 */
@Plugin(type = Service.class)
public final class DefaultDisplayService extends AbstractService implements
	DisplayService
{

	// -- Parameters --

	@Parameter
	private LogService log;

	@Parameter
	private EventService eventService;

	@Parameter
	private ObjectService objectService;

	@Parameter
	private PluginService pluginService;

	// -- instance variables --

	private LinkedList<Display<?>> displayList = new LinkedList<Display<?>>();


	// -- DisplayService methods --

	@Override
	public EventService getEventService() {
		return eventService;
	}

	@Override
	public ObjectService getObjectService() {
		return objectService;
	}

	@Override
	public PluginService getPluginService() {
		return pluginService;
	}

	// -- DisplayService methods - active displays --

	@Override
	public Display<?> getActiveDisplay() {
		if (displayList.size() == 0) return null;
		return displayList.get(0);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <D extends Display<?>> D getActiveDisplay(Class<D> displayClass) {
		for (Display<?> disp : displayList) {
			if (displayClass.isAssignableFrom(disp.getClass())) return (D) disp;
		}
		return null;
	}
	
	@Override
	public void setActiveDisplay(final Display<?> display) {
		if (display != null) {
			displayList.remove(display);
			displayList.addFirst(display);
			eventService.publish(new DisplayActivatedEvent(display));
		}
	}

	// -- DisplayService methods - display plugin discovery --

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<PluginInfo<Display<?>>> getDisplayPlugins() {
		return (List) pluginService.getPluginsOfType(Display.class);
	}

	@Override
	public <D extends Display<?>> PluginInfo<Display<?>> getDisplayPlugin(
		final Class<D> pluginClass)
	{
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final PluginInfo<Display<?>> displayPlugin =
			(PluginInfo) pluginService.getPlugin(pluginClass, Display.class);
		return displayPlugin;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public PluginInfo<Display<?>> getDisplayPlugin(final String className) {
		return (PluginInfo) pluginService.getPlugin(className);
	}

	@Override
	public <D extends Display<?>> List<PluginInfo<D>> getDisplayPluginsOfType(
		final Class<D> type)
	{
		return pluginService.getPluginsOfType(type);
	}

	// -- DisplayService methods - display discovery --

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Display<?>> getDisplays() {
		return (List) objectService.getObjects(Display.class);
	}

	@Override
	public <D extends Display<?>> List<D> getDisplaysOfType(final Class<D> type)
	{
		return objectService.getObjects(type);
	}

	@Override
	public Display<?> getDisplay(final String name) {
		for (final Display<?> display : getDisplays()) {
			if (name.equalsIgnoreCase(display.getName())) {
				return display;
			}
		}
		return null;
	}

	@Override
	public List<Display<?>> getDisplays(final Object o) {
		final ArrayList<Display<?>> displays = new ArrayList<Display<?>>();
		for (final Display<?> display : getDisplays()) {
			if (display.isDisplaying(o)) displays.add(display);
		}
		return displays;
	}

	// -- DisplayService methods - display creation --

	@Override
	public boolean isUniqueName(final String name) {
		final List<Display<?>> displays = getDisplays();
		for (final Display<?> display : displays) {
			if (name.equalsIgnoreCase(display.getName())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Display<?> createDisplay(final Object o) {
		return createDisplay(null, o);
	}

	@Override
	public Display<?> createDisplay(final String name, final Object o) {
		// get available display plugins from the plugin service
		final List<PluginInfo<Display<?>>> displayPlugins = getDisplayPlugins();

		for (final PluginInfo<Display<?>> info : displayPlugins) {
			final Display<?> display = pluginService.createInstance(info);
			if (display == null) continue;
			// display object using the first compatible Display
			// TODO: how to handle multiple matches? prompt user with dialog box?
			if (display.canDisplay(o)) {
				display.display(o);
				if (name != null) display.setName(name);
				eventService.publish(new DisplayCreatedEvent(display));
				return display;
			}
		}
		return null;
	}

	// -- Event handlers --

	/** Deletes the display when display window is closed. */
	@EventHandler
	protected void onEvent(final WinClosedEvent event) {
		final Display<?> display = event.getDisplay();
		if (display != null) display.close();
	}

	/** Sets the display to active when its window is activated. */
	@EventHandler
	protected void onEvent(final WinActivatedEvent event) {
		final Display<?> display = event.getDisplay();
		if (display != null) setActiveDisplay(display);
	}

	/** Removes a display from the display list when it is deleted */
	@EventHandler
	protected void onEvent(final DisplayDeletedEvent evt) {
		displayList.remove(evt.getObject());
	}
}
