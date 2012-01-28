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

package imagej.ext.display;

import imagej.IService;
import imagej.event.EventService;
import imagej.ext.plugin.PluginInfo;
import imagej.ext.plugin.PluginService;
import imagej.object.ObjectService;

import java.util.List;

/**
 * Interface for service that tracks available {@link Display}s.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 * @author Grant Harris
 */
public interface DisplayService extends IService {

	EventService getEventService();

	ObjectService getObjectService();

	PluginService getPluginService();

	/** Gets the currently active display. */
	Display<?> getActiveDisplay();

	/** Sets the currently active display. */
	void setActiveDisplay(final Display<?> display);

	/** Gets the list of known display plugins. */
	List<PluginInfo<Display<?>>> getDisplayPlugins();

	/**
	 * Gets the display plugin of the given class, or null if none.
	 */
	<D extends Display<?>> PluginInfo<D> getDisplayPlugin(
		final Class<D> pluginClass);

	/**
	 * Gets the display plugin of the given class name, or null if none.
	 * 
	 * @throws ClassCastException if the plugin found is not a display plugin.
	 */
	PluginInfo<Display<?>> getDisplayPlugin(final String className);

	/**
	 * Gets the list of display plugins of the given type (e.g.,
	 * <code>ImageDisplay.class</code>).
	 */
	<D extends Display<?>> List<PluginInfo<D>> getDisplayPluginsOfType(
		final Class<D> type);

	/** Gets a list of all available displays. */
	List<Display<?>> getDisplays();

	/**
	 * Gets a list of all available displays of the given type (e.g.,
	 * <code>ImageDisplay.class</code>).
	 */
	<D extends Display<?>> List<D> getDisplaysOfType(final Class<D> type);

	/** Gets a display by its name. */
	Display<?> getDisplay(final String name);

	/** Gets a list of displays containing the given object. */
	List<Display<?>> getDisplaysContaining(final Object o);

	/**
	 * Checks whether the given name is already taken by an existing display.
	 * 
	 * @param name The name to check.
	 * @return true if the name is available, false if already taken.
	 */
	boolean isUniqueName(final String name);

	/** Creates a display for the given object. */
	Display<?> createDisplay(final String name, final Object o);

}
