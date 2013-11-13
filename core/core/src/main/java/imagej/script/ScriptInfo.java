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

import imagej.command.Command;
import imagej.module.AbstractModuleInfo;
import imagej.module.DefaultMutableModuleItem;
import imagej.module.Module;
import imagej.module.ModuleException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;

import org.scijava.Context;
import org.scijava.Contextual;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.service.Service;

/**
 * Metadata about a script.
 * 
 * @author Curtis Rueden
 * @author Johannes Schindelin
 */
public class ScriptInfo extends AbstractModuleInfo implements Contextual {

	private final String path;

	@Parameter
	private Context context;

	@Parameter
	private LogService log;

	private Map<String, Class<?>> typeMap;

	public ScriptInfo(final String path, final Context context) {
		this.path = path;
		setContext(context);
		try {
			parseInputs();
		}
		catch (final ScriptException exc) {
			log.error(exc);
		}
		catch (final IOException exc) {
			log.error(exc);
		}
	}

	// -- ScriptInfo methods --

	public String getPath() {
		return path;
	}

	public boolean hasInputs() {
		return !inputMap.isEmpty();
	}

	// -- ModuleInfo methods --

	@Override
	public String getDelegateClassName() {
		return ScriptModule.class.getName();
	}

	@Override
	public Module createModule() throws ModuleException {
		return new ScriptModule(this);
	}

	// -- Contextual methods --

	@Override
	public Context getContext() {
		return context;
	}

	@Override
	public void setContext(final Context context) {
		context.inject(this);
	}

	// -- Helper methods --

	/**
	 * Parses the script's input parameters.
	 * <p>
	 * ImageJ's scripting framework supports specifying @{@link Parameter}-style
	 * parameters in a preamble. The idea is to specify the input parameters in
	 * this way:
	 * 
	 * <pre>
	 * // @UIService ui
	 * // @double degrees
	 * </pre>
	 * 
	 * i.e. in the form <code>&#x40;&lt;type&gt; &lt;name&gt;</code>. These input
	 * parameters will be parsed and filled just like @{@link Parameter}
	 * -annotated fields in {@link Command}s.
	 * </p>
	 * 
	 * @throws ScriptException If a parameter annotation is malformed.
	 * @throws IOException If there is a problem reading the script file.
	 */
	private void parseInputs() throws ScriptException, IOException {
		final FileReader fileReader = new FileReader(getPath());
		final BufferedReader in = new BufferedReader(fileReader, 16384);
		while (true) {
			final String line = in.readLine();
			if (line == null) break;

			// scan for lines containing an '@' stopping at the first line
			// containing at least one alpha-numerical character but no '@'.
			final int at = line.indexOf('@');
			if (at < 0) {
				if (line.matches(".*[A-Za-z0-9].*")) break;
				continue;
			}
			parseInput(line.substring(at + 1));
		}
		in.close();
	}

	private <T> void parseInput(final String line) throws ScriptException {
		final String[] parts = line.trim().split("[ \t\n]+");
		if (parts.length != 2) {
			throw new ScriptException("Expected 'type name': " + line);
		}
		addInput(parts[1], parseType(parts[0]));
	}

	private <T> void addInput(final String name, final Class<T> type) {
		final DefaultMutableModuleItem<T> item =
			new DefaultMutableModuleItem<T>(this, name, type);
		inputMap.put(name, item);
		inputList.add(item);
	}

	private synchronized Class<?> parseType(final String string)
		throws ScriptException
	{
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
				final int dot = className.lastIndexOf('.');
				if (dot > 0) typeMap.put(className.substring(dot + 1), clazz);
			}
		}
		final Class<?> type = typeMap.get(string);
		if (type == null) {
			try {
				final Class<?> clazz =
					Thread.currentThread().getContextClassLoader().loadClass(string);
				typeMap.put(string, clazz);
				return clazz;
			}
			catch (final ClassNotFoundException e) {
				throw new ScriptException("Unknown type: " + string);
			}
		}
		return type;
	}

}
