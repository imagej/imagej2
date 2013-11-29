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
import imagej.module.ModuleException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import javax.script.ScriptException;

import org.scijava.Context;
import org.scijava.Contextual;
import org.scijava.ItemIO;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

/**
 * Metadata about a script.
 * 
 * @author Curtis Rueden
 * @author Johannes Schindelin
 */
public class ScriptInfo extends AbstractModuleInfo implements Contextual {

	private static final int PARAM_CHAR_MAX = 640 * 1024; // should be enough ;-)

	private final String path;
	private final BufferedReader reader;

	@Parameter
	private Context context;

	@Parameter
	private LogService log;

	@Parameter
	private ScriptService scriptService;

	/**
	 * Creates a script metadata object which describes the given script file.
	 * 
	 * @param context The ImageJ application context to use when populating
	 *          service inputs.
	 * @param file The script file.
	 */
	public ScriptInfo(final Context context, final File file) {
		this(context, file.getPath());
	}

	/**
	 * Creates a script metadata object which describes the given script file.
	 * 
	 * @param context The ImageJ application context to use when populating
	 *          service inputs.
	 * @param path Path to the script file.
	 */
	public ScriptInfo(final Context context, final String path) {
		this(context, path, null);
	}

	/**
	 * Creates a script metadata object which describes a script provided by the
	 * given {@link Reader}.
	 * 
	 * @param context The ImageJ application context to use when populating
	 *          service inputs.
	 * @param path Pseudo-path to the script file. This file does not actually
	 *          need to exist, but rather provides a name for the script with file
	 *          extension.
	 * @param reader Reader which provides the script itself (i.e., its contents).
	 */
	public ScriptInfo(final Context context, final String path,
		final Reader reader)
	{
		setContext(context);
		this.path = path;
		this.reader = new BufferedReader(reader, PARAM_CHAR_MAX);
		try {
			parseParameters();
		}
		catch (final ScriptException exc) {
			log.error(exc);
		}
		catch (final IOException exc) {
			log.error(exc);
		}
		addReturnValue();
	}

	// -- ScriptInfo methods --

	/**
	 * Gets the path to the script on disk.
	 * <p>
	 * If the path doesn't actually exist on disk, then this is a pseudo-path
	 * merely for the purpose of naming the script with a file extension, and the
	 * actual script content is delivered by the {@link BufferedReader} given by
	 * {@link #getReader()}.
	 * </p>
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Gets the reader which delivers the script's content.
	 * <p>
	 * This might be null, in which case the content is stored in a file on disk
	 * given by {@link #getPath()}.
	 * </p>
	 */
	public BufferedReader getReader() {
		return reader;
	}

	/**
	 * Parses the script's input and output parameters.
	 * <p>
	 * ImageJ's scripting framework supports specifying @{@link Parameter}-style
	 * inputs and outputs in a preamble. The format is a simplified version of the
	 * Java @{@link Parameter} annotation syntax. The following syntaxes are
	 * supported:
	 * </p>
	 * <ul>
	 * <li>{@code // @<type> <varName>}</li>
	 * <li>{@code // @<type>(<attr1>=<value1>, ..., <attrN>=<valueN>) <varName>}</li>
	 * <li>{@code // @<IOType> <type> <varName>}</li>
	 * <li>{@code // @<IOType>(<attr1>=<value1>, ..., <attrN>=<valueN>) <type> <varName>}</li>
	 * </ul>
	 * <p>
	 * Where:
	 * </p>
	 * <ul>
	 * <li>{@code //} = the comment style of the scripting language, so that the
	 * parameter line is ignored by the script engine itself.</li>
	 * <li>{@code <IOType>} = one of {@code INPUT}, {@code OUTPUT}, or
	 * {@code BOTH}.</li>
	 * <li>{@code <varName>} = the name of the input or output variable.</li>
	 * <li>{@code <type>} = the Java {@link Class} of the variable.</li>
	 * <li>{@code <attr*>} = an attribute key.</li>
	 * <li>{@code <value*>} = an attribute value.</li>
	 * </ul>
	 * <p>
	 * See the @{@link Parameter} annotation for a list of valid attributes.
	 * </p>
	 * <p>
	 * Here are a few examples:
	 * </p>
	 * <ul>
	 * <li>{@code // @Dataset dataset}</li>
	 * <li>{@code // @double(type=OUTPUT) result}</li>
	 * <li>{@code // @BOTH ImageDisplay display}</li>
	 * <li>{@code // @INPUT(persist=false, visibility=INVISIBLE) boolean verbose}</li>
	 * parameters will be parsed and filled just like @{@link Parameter}
	 * -annotated fields in {@link Command}s.
	 * </ul>
	 * 
	 * @throws ScriptException If the parameter syntax is malformed.
	 * @throws IOException If there is a problem reading the script file.
	 */
	public void parseParameters() throws ScriptException, IOException {
		clearParameters();

		final BufferedReader in;
		if (reader == null) {
			in = new BufferedReader(new FileReader(getPath()));
		}
		else {
			in = reader;
			in.mark(PARAM_CHAR_MAX);
		}
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
			parseParam(line.substring(at + 1));
		}
		if (reader == null) in.close();
		else in.reset();
	}

	// -- ModuleInfo methods --

	@Override
	public String getDelegateClassName() {
		return ScriptModule.class.getName();
	}

	@Override
	public ScriptModule createModule() throws ModuleException {
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

	private <T> void parseParam(final String line) throws ScriptException {
		final String[] parts = line.trim().split("[ \t\n]+");
		if (parts.length != 2) {
			throw new ScriptException("Expected 'type name': " + line);
		}
		addInput(parts[1], scriptService.lookupClass(parts[0]));
	}

	private <T> void addInput(final String name, final Class<T> type) {
		addItem(name, type, ItemIO.INPUT);
	}

	/** Adds an output for the value returned by the script itself. */
	private void addReturnValue() {
		addItem(ScriptModule.RETURN_VALUE, Object.class, ItemIO.OUTPUT);
	}

	private <T> void addItem(final String name, final Class<T> type,
		final ItemIO ioType)
	{
		final DefaultMutableModuleItem<T> item =
			new DefaultMutableModuleItem<T>(this, name, type);
		item.setIOType(ioType);
		if (item.isInput()) {
			inputMap.put(item.getName(), item);
			inputList.add(item);
		}
		if (item.isOutput()) {
			outputMap.put(item.getName(), item);
			outputList.add(item);
		}
	}

	private void clearParameters() {
		inputMap.clear();
		inputList.clear();
		outputMap.clear();
		outputList.clear();
	}

}
