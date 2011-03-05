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
import imagej.plugin.api.PluginEntry;
import imagej.plugin.api.PluginException;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Module class for working with a {@link BasePlugin} instance,
 * particularly its {@link Parameter}s.
 *
 * @author Curtis Rueden
 * @author Johannes Schindelin
 * @author Grant Harris
 */
public class PluginModule<T extends BasePlugin> implements Module {

	/** The plugin instance handled by this module. */
	private final T plugin;

	/** Metadata about this plugin. */
	private PluginModuleInfo<T> info;

	/** Creates a plugin module for a new instance of the given plugin entry. */
	public PluginModule(final PluginEntry<T> entry) throws PluginException {
		plugin = entry.createInstance();
		info = new PluginModuleInfo<T>(entry, plugin);
	}

	/** Gets the plugin instance handled by this module. */
	public T getPlugin() {
		return plugin;
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

	public static void setValue(final Field field,
		final Object instance, final Object value)
	{
		if (instance == null) return;
		try {
			field.set(instance, value);
		}
		catch (IllegalArgumentException e) {
			assert false;
		}
		catch (IllegalAccessException e) {
			assert false;
		}
	}

	public static Object getValue(final Field field, final Object instance) {
		if (instance == null) return null;
		try {
			return field.get(instance);
		}
		catch (IllegalArgumentException e) {
			return null;
		}
		catch (IllegalAccessException e) {
			return null;
		}
	}

}
