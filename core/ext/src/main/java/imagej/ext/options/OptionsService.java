//
// OptionsService.java
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

package imagej.ext.options;

import imagej.AbstractService;
import imagej.ImageJ;
import imagej.Service;
import imagej.event.EventService;
import imagej.ext.InstantiableException;
import imagej.ext.module.ModuleException;
import imagej.ext.plugin.IPlugin;
import imagej.ext.plugin.PluginInfo;
import imagej.ext.plugin.PluginModuleInfo;
import imagej.ext.plugin.PluginService;
import imagej.util.ClassUtils;
import imagej.util.Log;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * Service for keeping track of the available options and their settings.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 * @see OptionsPlugin
 */
@Service
public class OptionsService extends AbstractService {

	private final EventService eventService;
	private final PluginService pluginService;

	// -- Constructors --

	public OptionsService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public OptionsService(final ImageJ context, final EventService eventService,
		final PluginService pluginService)
	{
		super(context);
		this.eventService = eventService;
		this.pluginService = pluginService;
	}

	// -- OptionsService methods --

	public EventService getEventService() {
		return eventService;
	}

	public PluginService getPluginService() {
		return pluginService;
	}

	/** Gets a list of all available options. */
	public List<OptionsPlugin> getOptions() {
		return pluginService.createInstances(OptionsPlugin.class);
	}

	/** Gets options associated with the given options plugin, or null if none. */
	public <O extends OptionsPlugin> O getOptions(final Class<O> optionsClass) {
		return createInstance(getOptionsInfo(optionsClass));
	}

	/** Gets the option with the given name, from the specified options plugin. */
	public <O extends OptionsPlugin> Object getOption(
		final Class<O> optionsClass, final String name)
	{
		return getInput(getOptionsInfo(optionsClass), name);
	}

	/** Gets the option with the given name, from the specified options plugin. */
	public Object getOption(final String className, final String name) {
		return getInput(getOptionsInfo(className), name);
	}

	/** Sets the option with the given name, from the specified options plugin,
	 *  to the given value.
	 */
	public void setOption(
		final String className, final String name, final Object value)
	{
		PluginModuleInfo<?> info = getOptionsInfo(className);
		Object valueObject = typedValue(info.getInput(name).getType(), value);
		setInput(info, name, valueObject);
	}

	/** Gets a map of all options from the given options plugin. */
	public <O extends OptionsPlugin> Map<String, Object> getOptionsMap(
		final Class<O> optionsClass)
	{
		return getInputs(getOptionsInfo(optionsClass));
	}

	/** Gets a map of all options from the given options plugin. */
	public Map<String, Object> getOptionsMap(final String className) {
		return getInputs(getOptionsInfo(className));
	}

	// -- IService methods --

	@Override
	public void initialize() {
		// no action needed
	}

	/*
	public OptionsPlugin getInstance(String optionsPluginClassName) {
		return null;
	}
	*/
	
	// -- Helper methods --

	private <P extends IPlugin> P createInstance(final PluginInfo<P> info) {
		if (info == null) return null;
		try {
			return info.createInstance();
		}
		catch (final InstantiableException e) {
			Log.error("Cannot create plugin: " + info.getClassName());
		}
		return null;
	}

	private <O extends OptionsPlugin> PluginModuleInfo<O> getOptionsInfo(
		final Class<O> optionsClass)
	{
		return pluginService.getRunnablePlugin(optionsClass);
	}

	private PluginModuleInfo<?> getOptionsInfo(final String className) {
		final PluginModuleInfo<?> info = pluginService.getRunnablePlugin(className);
		if (!OptionsPlugin.class.isAssignableFrom(info.getPluginType())) {
			Log.error("Not an options plugin: " + className);
			// not an options plugin
			return null;
		}
		return info;
	}

	private Object getInput(final PluginModuleInfo<?> info, final String name) {
		if (info == null) return null;
		try {
			return info.createModule().getInput(name);
		}
		catch (final ModuleException e) {
			Log.error("Cannot create module: " + info.getClassName());
		}
		return null;
	}

	private Map<String, Object> getInputs(final PluginModuleInfo<?> info) {
		if (info == null) return null;
		try {
			return info.createModule().getInputs();
		}
		catch (final ModuleException e) {
			Log.error("Cannot create module: " + info.getClassName());
		}
		return null;
	}

	private void setInput(
		final PluginModuleInfo<?> info, final String name, Object value)
	{
		if (info == null) return;
		try {
			info.createModule().setInput(name,value);
		}
		catch (final ModuleException e) {
			Log.error("Cannot create module: " + info.getClassName());
		}
	}

	// TODO - move to ClassUtils? update convert() there?
	
	private Object typedValue(Class<?> type, Object obj) {
		if (obj.getClass() == String.class) {
			String text = (String) obj;
			if (type == String.class)
				return text;
			else if ((type == char.class) || (type == Character.class))
				return new Character(text.charAt(0));
			else if ((type == byte.class) || (type == Byte.class))
				return new Byte(text);
			else if ((type == short.class) || (type == Short.class))
					return new Short(text);
			else if ((type == int.class) || (type == Integer.class))
				return new Integer(text);
			else if ((type == long.class) || (type == Long.class))
				return new Long(text);
			else if ((type == float.class) || (type == Float.class))
				return new Float(text);
			else if ((type == double.class) || (type == Double.class))
				return new Double(text);
			else if ((type == boolean.class) || (type == Boolean.class))
				return new Boolean(text);
			else if (type == BigInteger.class)
				return new BigInteger(text);
			else if (type == BigDecimal.class)
				return new BigDecimal(text);
			else
				throw new IllegalArgumentException("unknown class type : " + type);
			// TODO - a Color class conversion???
		}
		
		return ClassUtils.convert(obj, type);
	}
}
