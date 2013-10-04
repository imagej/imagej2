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

package imagej.script;

import imagej.module.AbstractModule;
import imagej.module.AbstractModuleInfo;
import imagej.module.AbstractModuleItem;
import imagej.module.Module;
import imagej.module.ModuleException;
import imagej.module.ModuleInfo;
import imagej.module.ModuleItem;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;

import org.scijava.Context;
import org.scijava.service.Service;

/**
 * A {@link Module} to populate input parameters for scripts.
 * 
 * @see org.scijava.plugin.Parameter
 * 
 * @author Johannes Schindelin
 */
public class ScriptInputs extends AbstractModule {

	final private Info info = new Info();
	final private Context context;
	final String title;

	public ScriptInputs(final Context context, final String title) {
		this.context = context;
		this.title = title;
	}

	@Override
	public ModuleInfo getInfo() {
		return info;
	}

	@Override
	public void initialize() {
		for (final ModuleItem<?> item : info.inputs()) {
			final Class<?> type = item.getType();
			final Object value;
			if (Context.class.isAssignableFrom(type)) {
				value = context;
			} else if (Service.class.isAssignableFrom(type)) {
				@SuppressWarnings("unchecked")
				final Class<? extends Service> serviceType =
					(Class<? extends Service>) type;
				value = context.getService(serviceType);
			} else continue;
			final String name = item.getName();
			setInput(name, value);
			setResolved(name, true);
		}
	}

	@Override
	public void run() {
	}

	public boolean hasInputs() {
		return info.hasInputs();
	}

	public <T> void addInput(final String name, final Class<T> type) {
		info.addInput(name, type);
	}

	public <T> void parseInput(final String line) throws ScriptException {
		final String[] parts = line.trim().split("[ \t\n]+");
		if (parts.length != 2) throw new ScriptException("Expected 'type name': " + line);
		addInput(parts[1], parseType(parts[0]));
	}

	private Map<String, Class<?>> typeMap;

	private synchronized Class<?> parseType(final String string) throws ScriptException {
		if (typeMap == null) {
			typeMap = new HashMap<String, Class<?>>();
			typeMap.put("byte", Byte.TYPE);
			typeMap.put("short", Short.TYPE);
			typeMap.put("int", Integer.TYPE);
			typeMap.put("long", Long.TYPE);
			typeMap.put("float", Float.TYPE);
			typeMap.put("double", Double.TYPE);
			typeMap.put("String", String.class);
			typeMap.put("File", File.class);
			
			for (final Service service : context.getServiceIndex()) {
				final Class<?> clazz = service.getClass();
				final String className = clazz.getName();
				typeMap.put(className, clazz);
				int dot = className.lastIndexOf('.');
				if (dot > 0) typeMap.put(className.substring(dot + 1), clazz);
			}
		}
		final Class<?> type = typeMap.get(string);
		if (type == null) {
			try {
				final Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(string);
				typeMap.put(string, clazz);
				return clazz;
			} catch (ClassNotFoundException e) {
				throw new ScriptException("Unknown type: " + string);
			}
		}
		return type;
	}

	private class Info extends AbstractModuleInfo {

		@Override
		public String getTitle() {
			return title != null ? title : "Script Inputs";
		}

		@Override
		public String getDelegateClassName() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Module createModule() throws ModuleException {
			throw new UnsupportedOperationException();
		}

		public boolean hasInputs() {
			return !inputMap.isEmpty();
		}

		public <T> void addInput(final String name, final Class<T> type) {
			final Item<T> item = new Item<T>(name,
					type);
			inputMap.put(name, item);
			inputList.add(item);
		}

		private class Item<T> extends AbstractModuleItem<T> {
			final String name;
			final Class<T> type;

			public Item(final String name, final Class<T> type) {
				super(Info.this);
				this.name = name;
				this.type = type;
			}

			@Override
			public Class<T> getType() {
				return type;
			}

			@Override
			public boolean isRequired() {
				return true;
			}

			@Override
			public boolean isPersisted() {
				return false;
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public String getLabel() {
				return name;
			}

			@Override
			public String getDescription() {
				return name;
			}
		}

	}
}