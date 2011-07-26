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

package imagej.ext.plugin;

import imagej.ext.InstantiableException;
import imagej.ext.module.AbstractModule;
import imagej.ext.module.ModuleException;
import imagej.ext.module.ModuleItem;
import imagej.util.ClassUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Module class for working with a {@link RunnablePlugin} instance.
 * 
 * @author Curtis Rueden
 * @author Johannes Schindelin
 * @author Grant Harris
 */
public class PluginModule<R extends RunnablePlugin> extends AbstractModule {

	/** The plugin info describing the plugin. */
	private final PluginModuleInfo<R> info;

	/** The plugin instance handled by this module. */
	private final R plugin;

	/** Creates a plugin module for the given {@link PluginInfo}. */
	public PluginModule(final PluginModuleInfo<R> info) throws ModuleException {
		super(info);
		this.info = info;
		plugin = instantiatePlugin();
		assignPresets();
	}

	// -- PluginModule methods --

	/** Gets the plugin instance handled by this module. */
	public R getPlugin() {
		return plugin;
	}

	// -- Module methods --

	/**
	 * Computes a preview of the plugin's results. For this method to do anything,
	 * the plugin must implement the {@link PreviewPlugin} interface.
	 */
	@Override
	public void preview() {
		if (!(plugin instanceof PreviewPlugin)) return; // cannot preview
		final PreviewPlugin previewPlugin = (PreviewPlugin) plugin;
		previewPlugin.preview();
	}

	/**
	 * Cancels the plugin, undoing the effects of any calls to {@link #preview()}.
	 * For this method to do anything, the plugin must implement the
	 * {@link PreviewPlugin} interface.
	 */
	@Override
	public void cancel() {
		if (!(plugin instanceof PreviewPlugin)) return; // nothing to cancel
		final PreviewPlugin previewPlugin = (PreviewPlugin) plugin;
		previewPlugin.cancel();
	}

	@Override
	public PluginModuleInfo<R> getInfo() {
		return info;
	}

	@Override
	public Object getDelegateObject() {
		return plugin;
	}

	@Override
	public Object getInput(final String name) {
		final PluginModuleItem<?> item = info.getInput(name);
		return ClassUtils.getValue(item.getField(), plugin);
	}

	@Override
	public Object getOutput(final String name) {
		final PluginModuleItem<?> item = info.getOutput(name);
		return ClassUtils.getValue(item.getField(), plugin);
	}

	@Override
	public Map<String, Object> getInputs() {
		return createMap(info.inputs());
	}

	@Override
	public Map<String, Object> getOutputs() {
		return createMap(info.outputs());
	}

	@Override
	public void setInput(final String name, final Object value) {
		final PluginModuleItem<?> item = info.getInput(name);
		ClassUtils.setValue(item.getField(), plugin, value);
	}

	@Override
	public void setOutput(final String name, final Object value) {
		final PluginModuleItem<?> item = info.getOutput(name);
		ClassUtils.setValue(item.getField(), plugin, value);
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

	// -- Runnable methods --

	@Override
	public void run() {
		plugin.run();
	}

	// -- Helper methods --

	private R instantiatePlugin() throws ModuleException {
		try {
			return info.createInstance();
		}
		catch (final InstantiableException exc) {
			throw new ModuleException(exc);
		}
	}

	private void assignPresets() {
		final Map<String, Object> presets = info.getPresets();
		for (final String name : presets.keySet()) {
			final Object value = presets.get(name);
			setInput(name, value);
			setResolved(name, true);
		}
	}

	private Map<String, Object> createMap(final Iterable<ModuleItem<?>> items) {
		final Map<String, Object> map = new HashMap<String, Object>();
		for (final ModuleItem<?> item : items) {
			final PluginModuleItem<?> pmi = (PluginModuleItem<?>) item;
			final String name = item.getName();
			final Object value = ClassUtils.getValue(pmi.getField(), plugin);
			map.put(name, value);
		}
		return map;
	}

}
