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

import imagej.service.IJService;

import java.util.List;

import org.scijava.event.EventService;
import org.scijava.object.ObjectService;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;

/**
 * Interface for service that tracks available {@link Display}s.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 * @author Grant Harris
 */
public interface DisplayService extends IJService {

	EventService getEventService();

	ObjectService getObjectService();

	PluginService getPluginService();

	/** Gets the currently active display (of any Display type). */
	Display<?> getActiveDisplay();

	/** Gets the most recently active display (of the specified Display type). */
	<D extends Display<?>> D getActiveDisplay(Class<D> displayClass);

	/**
	 * Set the active display.
	 * 
	 * @param display
	 */
	void setActiveDisplay(Display<?> display);

	/** Gets the list of known display plugins. */
	List<PluginInfo<Display<?>>> getDisplayPlugins();

	/**
	 * Gets the display plugin of the given class, or null if none.
	 */
	<D extends Display<?>> PluginInfo<Display<?>> getDisplayPlugin(
		Class<D> pluginClass);

	/**
	 * Gets the display plugin of the given class name, or null if none.
	 * 
	 * @throws ClassCastException if the plugin found is not a display plugin.
	 */
	PluginInfo<Display<?>> getDisplayPlugin(String className);

	/**
	 * Gets the list of display plugins of the given type (e.g.,
	 * {@code ImageDisplay.class}).
	 */
	<DT extends Display<?>> List<PluginInfo<DT>> getDisplayPluginsOfType(
		Class<DT> type);

	/** Gets a list of all available displays. */
	List<Display<?>> getDisplays();

	/**
	 * Gets a list of all available displays of the given type (e.g.,
	 * {@code ImageDisplay.class}).
	 */
	<DT extends Display<?>> List<DT> getDisplaysOfType(Class<DT> type);

	/** Gets a display by its name. */
	Display<?> getDisplay(String name);

	/** Gets a list of displays currently visualizing the given object. */
	List<Display<?>> getDisplays(Object o);

	/**
	 * Checks whether the given name is already taken by an existing display.
	 * 
	 * @param name The name to check.
	 * @return true if the name is available, false if already taken.
	 */
	boolean isUniqueName(String name);

	/**
	 * Creates a display for the given object.
	 * 
	 * @param o The object for which a display should be created. The object is
	 *          then added to the display.
	 * @return Newly created {@code Display<?>} containing the given object. The
	 *         Display is typed with ? rather than T matching the Object because
	 *         it is possible for the Display to be a collection of some other
	 *         sort of object than the one being added. For example, ImageDisplay
	 *         is a {@code Display<DataView>} with the DataView wrapping a
	 *         Dataset, yet the ImageDisplay supports adding Datasets directly,
	 *         taking care of wrapping them in a DataView as needed.
	 */
	Display<?> createDisplay(Object o);

	/**
	 * Creates a display for the given object.
	 * 
	 * @param name The name to be assigned to the display.
	 * @param o The object for which a display should be created. The object is
	 *          then added to the display.
	 * @return Newly created {@code Display<?>} containing the given object. The
	 *         Display is typed with ? rather than T matching the Object because
	 *         it is possible for the Display to be a collection of some other
	 *         sort of object than the one being added. For example, ImageDisplay
	 *         is a {@code Display<DataView>} with the DataView wrapping a
	 *         Dataset, yet the ImageDisplay supports adding Datasets directly,
	 *         taking care of wrapping them in a DataView as needed.
	 */
	Display<?> createDisplay(String name, Object o);

}
