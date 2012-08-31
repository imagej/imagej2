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

package imagej.plugin;

import imagej.InstantiableException;
import imagej.event.EventService;
import imagej.module.ItemVisibility;
import imagej.module.Module;
import imagej.module.ModuleException;
import imagej.module.ModuleInfo;
import imagej.module.ModuleItem;
import imagej.module.event.ModulesUpdatedEvent;
import imagej.util.ClassUtils;
import imagej.util.Log;
import imagej.util.StringMaker;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A collection of metadata about a particular {@link RunnablePlugin}. Unlike
 * its more general superclass {@link PluginInfo}, a
 * <code>PluginModuleInfo</code> implements {@link ModuleInfo}, allowing it to
 * describe and instantiate the plugin in {@link Module} form.
 * 
 * @author Curtis Rueden
 * @author Johannes Schindelin
 * @author Grant Harris
 * @see ModuleInfo
 * @see PluginModule
 * @see RunnablePlugin
 */
public class PluginModuleInfo<R extends RunnablePlugin> extends PluginInfo<R>
	implements ModuleInfo
{

	/** List of items with fixed, preset values. */
	private Map<String, Object> presets;

	/** Factory used to create a module associated with the associated plugin. */
	private PluginModuleFactory factory;

	/**
	 * Flag indicating whether the plugin parameters have been parsed. Parsing the
	 * parameters requires loading the plugin class, so doing so is deferred until
	 * information about the parameters is actively needed.
	 */
	private boolean paramsParsed;

	/** List of problems detected when parsing plugin parameters. */
	private List<String> paramErrors = new ArrayList<String>();

	/** Table of inputs, keyed on name. */
	private final Map<String, ModuleItem<?>> inputMap =
		new HashMap<String, ModuleItem<?>>();

	/** Table of outputs, keyed on name. */
	private final Map<String, ModuleItem<?>> outputMap =
		new HashMap<String, ModuleItem<?>>();

	/** Ordered list of input items. */
	private final List<ModuleItem<?>> inputList = new ArrayList<ModuleItem<?>>();

	/** Ordered list of output items. */
	private final List<ModuleItem<?>> outputList =
		new ArrayList<ModuleItem<?>>();

	// -- Constructors --

	public PluginModuleInfo(final String className, final Class<R> pluginType) {
		super(className, pluginType);
		setPresets(null);
		setPluginModuleFactory(null);
	}

	public PluginModuleInfo(final String className, final Class<R> pluginType,
		final Plugin plugin)
	{
		super(className, pluginType, plugin);
		setPresets(null);
		setPluginModuleFactory(null);
	}

	// -- PluginModuleInfo methods --

	/** Sets the table of items with fixed, preset values. */
	public void setPresets(final Map<String, Object> presets) {
		if (presets == null) {
			this.presets = new HashMap<String, Object>();
		}
		else {
			this.presets = presets;
		}
	}

	/** Gets the table of items with fixed, preset values. */
	public Map<String, Object> getPresets() {
		return presets;
	}

	/** Sets the factory used to construct {@link PluginModule} instances. */
	public void setPluginModuleFactory(final PluginModuleFactory factory) {
		if (factory == null) {
			this.factory = new DefaultPluginModuleFactory();
		}
		else {
			this.factory = factory;
		}
	}

	/** Gets the factory used to construct {@link PluginModule} instances. */
	public PluginModuleFactory getPluginModuleFactory() {
		return factory;
	}

	/**
	 * Instantiates the module described by this module info, around the specified
	 * existing plugin instance.
	 */
	public Module createModule(final R pluginInstance) {
		return factory.createModule(this, pluginInstance);
	}

	// -- Object methods --

	@Override
	public String toString() {
		final StringMaker sm = new StringMaker(super.toString());
		for (final String key : presets.keySet()) {
			final Object value = presets.get(key);
			sm.append(key, value);
		}
		return sm.toString();
	}

	// -- ModuleInfo methods --

	@Override
	public PluginModuleItem<?> getInput(final String name) {
		parseParams();
		return (PluginModuleItem<?>) inputMap.get(name);
	}

	@Override
	public PluginModuleItem<?> getOutput(final String name) {
		parseParams();
		return (PluginModuleItem<?>) outputMap.get(name);
	}

	@Override
	public Iterable<ModuleItem<?>> inputs() {
		parseParams();
		return Collections.unmodifiableList(inputList);
	}

	@Override
	public Iterable<ModuleItem<?>> outputs() {
		parseParams();
		return Collections.unmodifiableList(outputList);
	}

	@Override
	public String getDelegateClassName() {
		return getClassName();
	}

	@Override
	public Module createModule() throws ModuleException {
		return factory.createModule(this);
	}

	@Override
	public boolean canPreview() {
		final Class<?> pluginClass = loadPluginClass();
		if (pluginClass == null) return false;
		return PreviewPlugin.class.isAssignableFrom(pluginClass);
	}

	@Override
	public boolean canCancel() {
		return plugin == null ? false : plugin.cancelable();
	}

	@Override
	public boolean canRunHeadless() {
		return plugin == null ? false : plugin.headless();
	}

	@Override
	public String getInitializer() {
		return plugin == null ? null : plugin.initializer();
	}

	@Override
	public void update(final EventService eventService) {
		eventService.publish(new ModulesUpdatedEvent(this));
	}

	@Override
	public boolean isValid() {
		parseParams();
		return paramErrors.isEmpty();
	}

	@Override
	public List<String> getProblems() {
		return isValid() ? null : Collections.unmodifiableList(paramErrors);
	}

	// -- UIDetails methods --

	@Override
	public String getTitle() {
		final String title = super.getTitle();
		if (!title.equals(getClass().getSimpleName())) return title;

		// use delegate class name rather than actual class name
		final String className = getDelegateClassName();
		final int dot = className.lastIndexOf(".");
		return dot < 0 ? className : className.substring(dot + 1);
	}

	// -- Helper methods --

	/**
	 * Parses the plugin's inputs and outputs. Invoked lazily, as needed, to defer
	 * class loading as long as possible.
	 */
	private void parseParams() {
		if (paramsParsed) return;
		paramsParsed = true;
		checkFields(loadPluginClass());
	}

	/** Processes the given class's @{@link Parameter}-annotated fields. */
	private void checkFields(final Class<?> type) {
		if (type == null) return;
		final List<Field> fields =
			ClassUtils.getAnnotatedFields(type, Parameter.class);

		for (final Field f : fields) {
			f.setAccessible(true); // expose private fields

			final Parameter param = f.getAnnotation(Parameter.class);

			boolean valid = true;

			final boolean isFinal = Modifier.isFinal(f.getModifiers());
			final boolean isMessage = param.visibility() == ItemVisibility.MESSAGE;
			if (isFinal && !isMessage) {
				// NB: Final parameters are bad because they cannot be modified.
				final String error = "Invalid final parameter: " + f;
				paramErrors.add(error);
				Log.error(error);
				valid = false;
			}

			final String name = f.getName();
			if (inputMap.containsKey(name) || outputMap.containsKey(name)) {
				// NB: Shadowed parameters are bad because they are ambiguous.
				final String error = "Invalid duplicate parameter: " + f;
				paramErrors.add(error);
				Log.error(error);
				valid = false;
			}

			if (!valid) {
				// NB: Skip invalid parameters.
				continue;
			}

			final boolean isPreset = presets.containsKey(name);

			// add item to the relevant list (inputs or outputs)
			final PluginModuleItem<Object> item =
				new PluginModuleItem<Object>(this, f);
			if (item.isInput()) {
				inputMap.put(name, item);
				if (!isPreset) inputList.add(item);
			}
			if (item.isOutput()) {
				outputMap.put(name, item);
				if (!isPreset) outputList.add(item);
			}
		}
	}

	private Class<?> loadPluginClass() {
		try {
			return loadClass();
		}
		catch (final InstantiableException e) {
			Log.error("Could not initialize plugin class: " + getClassName(), e);
		}
		return null;
	}

}
