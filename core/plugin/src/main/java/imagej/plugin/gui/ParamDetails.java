//
// ParamDetails.java
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

package imagej.plugin.gui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import imagej.Log;
import imagej.plugin.BasePlugin;
import imagej.plugin.Parameter;
import imagej.plugin.PluginModule;

/**
 * TODO
 *
 * @author Curtis Rueden
 */
public class ParamDetails {

	private final InputPanel inputPanel;
	private final PluginModule<?> module;
	private final String name;
	private final Class<?> type;
	private final Parameter param;

	private final String label;
	private final String description;
	private final String callback;
	private final WidgetStyle style;

	private final Method callbackMethod;

	public ParamDetails(final InputPanel inputPanel,
		final PluginModule<?> module, final String name,
		final Class<?> type, final Parameter param)
	{
		this.inputPanel = inputPanel;
		this.module = module;
		this.name = name;
		this.type = type;
		this.param = param;

		label = makeLabel(param.label());
		description = param.description();
		callback = param.callback();
		style = param.style();

		callbackMethod = lookupCallbackMethod();
	}

	public PluginModule<?> getModule() {
		return module;
	}

	public Object getValue() {
		return module.getInput(name);
	}

	public void setValue(final Object value) {
		module.setInput(name, value);
		executeCallbackMethod();
	}

	public String getName() {
		return name;
	}

	public Class<?> getType() {
		return type;
	}

	public Parameter getParameter() {
		return param;
	}

	public String getLabel() {
		return label;
	}

	public String getDescription() {
		return description;
	}

	public String getCallback() {
		return callback;
	}

	public WidgetStyle getStyle() {
		return style;
	}

	// -- Helper methods --

	private String makeLabel(final String s) {
		if (s == null || s.isEmpty()) {
			return name.substring(0, 1).toUpperCase() + name.substring(1);
		}
		return s;
	}

	private Method lookupCallbackMethod() {
		if (callback.isEmpty()) return null;
		final BasePlugin plugin = module.getPlugin();
		try {
			// TODO - support inherited callback methods
			final Method m = plugin.getClass().getDeclaredMethod(callback);
			m.setAccessible(true);
			return m;
		}
		catch (SecurityException e) {
			Log.warn(plugin.getClass().getName() +
				": illegal callback method \"" + callback +
				"\" for parameter " + name, e);
		}
		catch (NoSuchMethodException e) {
			Log.warn(plugin.getClass().getName() +
				": no callback method \"" + callback +
				"\" for parameter " + name, e);
		}
		return null;
	}

	/** Invokes the callback method associated with the parameter. */
	private void executeCallbackMethod() {
		if (callbackMethod == null) return;
		final BasePlugin plugin = module.getPlugin();
		try {
			Log.debug(plugin.getClass().getName() +
				": executing callback method: " + callback);
			callbackMethod.invoke(plugin);
			inputPanel.refresh();
		}
		catch (IllegalArgumentException e) {
			Log.warn(plugin.getClass().getName() +
				": error executing callback method \"" + callback +
				"\" for parameter " + name, e);
		}
		catch (IllegalAccessException e) {
			Log.warn(plugin.getClass().getName() +
				": error executing callback method \"" + callback +
				"\" for parameter " + name, e);
		}
		catch (InvocationTargetException e) {
			Log.warn(plugin.getClass().getName() +
				": error executing callback method \"" + callback +
				"\" for parameter " + name, e);
		}
	}

}
