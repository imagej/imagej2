//
// PluginModule.java
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

package imagej.plugin;

import imagej.module.Module;
import imagej.module.ModuleItem;
import imagej.util.Log;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Module class for working with a {@link BasePlugin} instance, particularly its
 * {@link Parameter}s.
 * 
 * @author Curtis Rueden
 * @author Johannes Schindelin
 * @author Grant Harris
 */
public class PluginModule<T extends BasePlugin> implements Module {

	/** The plugin entry describing this module. */
	private final PluginEntry<T> entry;

	/** The plugin instance handled by this module. */
	private final T plugin;

	/** Metadata about this plugin. */
	private final PluginModuleInfo<T> info;

	/** Table indicating resolved inputs. */
	private final HashSet<String> resolvedInputs;

	/** Creates a plugin module for a new instance of the given plugin entry. */
	public PluginModule(final PluginEntry<T> entry) throws PluginException {
		this.entry = entry;
		plugin = entry.createInstance();
		info = new PluginModuleInfo<T>(entry, plugin);
		resolvedInputs = new HashSet<String>();
	}

	/** Gets the plugin instance handled by this module. */
	public T getPlugin() {
		return plugin;
	}

	/**
	 * Gets the current toggle state of the plugin, if any.
	 * 
	 * @return The toggle state, or false if not a toggle plugin.
	 */
	public boolean isSelected() {
		final String tParam = entry.getToggleParameter();
		if (tParam == null || tParam.isEmpty()) return false; // not a toggle plugin
		final Object value = getInput(tParam);
		if (!(value instanceof Boolean)) return false; // invalid parameter value
		return (Boolean) value;
	}

	/**
	 * Sets the current toggle state of the plugin. For non-toggle plugins, does
	 * nothing.
	 */
	public void setSelected(final boolean selected) {
		final String tParam = entry.getToggleParameter();
		if (tParam == null || tParam.isEmpty()) return; // not a toggle plugin
		setInput(tParam, selected);
		setResolved(tParam, true);
	}

	/**
	 * Computes a preview of the plugin's results. For this method to do anything,
	 * the plugin must implement the {@link PreviewPlugin} interface.
	 */
	public void preview() {
		if (!(plugin instanceof PreviewPlugin)) return; // cannot preview
		final PreviewPlugin previewPlugin = (PreviewPlugin) plugin;
		previewPlugin.preview();
	}

	// -- Object methods --

	@Override
	public String toString() {
		final PluginModuleInfo<?> moduleInfo = getInfo();

		// use module label, if available
		final String label = moduleInfo.getLabel();
		if (label != null && !label.isEmpty()) return label;

		// use name of leaf menu item, if available
		final List<MenuEntry> menuPath = moduleInfo.getPluginEntry().getMenuPath();
		if (menuPath != null && menuPath.size() > 0) {
			final MenuEntry menuEntry = menuPath.get(menuPath.size() - 1);
			final String menuName = menuEntry.getName();
			if (menuName != null && !menuName.isEmpty()) return menuName;
		}

		// use module name, if available
		final String name = moduleInfo.getName();
		if (name != null && !name.isEmpty()) return name;

		// use plugin class name
		final String className = entry.getClassName();
		final int dot = className.lastIndexOf(".");
		return dot < 0 ? className : className.substring(dot + 1);
	}

	// -- Module methods --

	@Override
	public PluginModuleInfo<T> getInfo() {
		return info;
	}

	@Override
	public Object getInput(final String name) {
		final PluginModuleItem item = info.getInput(name);
		return getValue(item.getField(), plugin);
	}

	@Override
	public Object getOutput(final String name) {
		final PluginModuleItem item = info.getOutput(name);
		return getValue(item.getField(), plugin);
	}

	@Override
	public Map<String, Object> getInputs() {
		return getMap(info.inputs());
	}

	@Override
	public Map<String, Object> getOutputs() {
		return getMap(info.outputs());
	}

	@Override
	public void setInput(final String name, final Object value) {
		final PluginModuleItem item = info.getInput(name);
		setValue(item.getField(), plugin, value);
	}

	@Override
	public void setOutput(final String name, final Object value) {
		final PluginModuleItem item = info.getOutput(name);
		setValue(item.getField(), plugin, value);
	}

	@Override
	public void setInputs(final Map<String, Object> inputs) {
		for (final String name : inputs.keySet()) {
			setInput(name, inputs.get(name));
		}
	}

	@Override
	public void setOutputs(final Map<String, Object> outputs) {
		for (final String name : outputs.keySet()) {
			setOutput(name, outputs.get(name));
		}
	}

	/**
	 * Gets the item's resolution status. A "resolved" item is known to have a
	 * final, valid value for use with the module.
	 */
	@Override
	public boolean isResolved(final String name) {
		return resolvedInputs.contains(name);
	}

	/**
	 * Sets the item's resolution status. A "resolved" item is known to have a
	 * final, valid value for use with the module.
	 */
	@Override
	public void setResolved(final String name, final boolean resolved) {
		if (resolved) resolvedInputs.add(name);
		else resolvedInputs.remove(name);
	}

	// -- Helper methods --

	private Map<String, Object> getMap(final Iterable<ModuleItem> items) {
		final Map<String, Object> map = new HashMap<String, Object>();
		for (final ModuleItem item : items) {
			final PluginModuleItem pmi = (PluginModuleItem) item;
			final String name = item.getName();
			final Object value = getValue(pmi.getField(), plugin);
			map.put(name, value);
		}
		return map;
	}

	// -- Utility methods --

	public static void setValue(final Field field, final Object instance,
		final Object value)
	{
		if (instance == null) return;
		try {
			field.set(instance, value);
		}
		catch (final IllegalArgumentException e) {
			Log.error(e);
			assert false;
		}
		catch (final IllegalAccessException e) {
			Log.error(e);
			assert false;
		}
	}

	public static Object getValue(final Field field, final Object instance) {
		if (instance == null) return null;
		try {
			return field.get(instance);
		}
		catch (final IllegalArgumentException e) {
			Log.error(e);
			return null;
		}
		catch (final IllegalAccessException e) {
			Log.error(e);
			return null;
		}
	}

}
