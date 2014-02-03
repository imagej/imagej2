/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;

import org.scijava.Context;
import org.scijava.Contextual;
import org.scijava.ItemIO;
import org.scijava.ItemVisibility;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.util.ConversionUtils;

/**
 * Metadata about a script.
 * 
 * @author Curtis Rueden
 * @author Johannes Schindelin
 */
public class ScriptInfo extends AbstractModuleInfo implements Contextual {

	private static final int PARAM_CHAR_MAX = 640 * 1024; // should be enough ;-)

	private final String path;
	private final Reader reader;

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
		this.reader = reader;
		try {
			parseParameters();
			addReturnValue();
		}
		catch (final ScriptException exc) {
			log.error(exc);
		}
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
	public Reader getReader() {
		return reader;
	}

	/**
	 * Parses the script's input and output parameters from the script header.
	 * <p>
	 * This method is called automatically the first time any parameter accessor
	 * method is called ({@link #getInput}, {@link #getOutput}, {@link #inputs()},
	 * {@link #outputs()}, etc.). Subsequent calls will reparse the parameters.
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
	 * <li>
	 * {@code // @<IOType>(<attr1>=<value1>, ..., <attrN>=<valueN>) <type> <varName>}
	 * </li>
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
	 */
	// NB: Widened visibility from AbstractModuleInfo.
	@Override
	public void parseParameters() {
		clearParameters();

		try {
			final BufferedReader in;
			if (reader == null) {
				in = new BufferedReader(new FileReader(getPath()));
			}
			else {
				in = new BufferedReader(reader, PARAM_CHAR_MAX);
				in.mark(PARAM_CHAR_MAX);
			}
			while (true) {
				final String line = in.readLine();
				if (line == null) break;

				// scan for lines containing an '@' stopping at the first line
				// containing at least one alphameric character but no '@'.
				final int at = line.indexOf('@');
				if (at >= 0) parseParam(line.substring(at + 1));
				else if (line.matches(".*\\w.*")) break;
			}
			if (reader == null) in.close();
			else in.reset();
		}
		catch (final IOException exc) {
			log.error("Error reading script: " + path, exc);
		}
		catch (final ScriptException exc) {
			log.error("Invalid parameter syntax for script: " + path, exc);
		}
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

	// -- Identifiable methods --

	@Override
	public String getIdentifier() {
		return "script:" + path;
	}

	// -- Helper methods --

	private void parseParam(final String param) throws ScriptException {
		final int lParen = param.indexOf("(");
		final int rParen = param.lastIndexOf(")");
		if (rParen < lParen) {
			throw new ScriptException("Invalid parameter: " + param);
		}
		if (lParen < 0) parseParam(param, parseAttrs(""));
		else {
			final String cutParam =
				param.substring(0, lParen) + param.substring(rParen + 1);
			final String attrs = param.substring(lParen + 1, rParen);
			parseParam(cutParam, parseAttrs(attrs));
		}
	}

	private void parseParam(final String param,
		final HashMap<String, String> attrs) throws ScriptException
	{
		final String[] tokens = param.trim().split("[ \t\n]+");
		checkValid(tokens.length >= 1, param);
		final String typeName, varName;
		if (isIOType(tokens[0])) {
			// assume syntax: <IOType> <type> <varName>
			checkValid(tokens.length >= 3, param);
			attrs.put("type", tokens[0]);
			typeName = tokens[1];
			varName = tokens[2];
		}
		else {
			// assume syntax: <type> <varName>
			checkValid(tokens.length >= 2, param);
			typeName = tokens[0];
			varName = tokens[1];
		}
		final Class<?> type = scriptService.lookupClass(typeName);
		addItem(varName, type, attrs);
	}

	/** Parses a comma-delimited list of {@code key=value} pairs into a map. */
	private HashMap<String, String> parseAttrs(final String attrs)
		throws ScriptException
	{
		// TODO: We probably want to use a real CSV parser.
		final HashMap<String, String> attrsMap = new HashMap<String, String>();
		for (final String token : attrs.split(",")) {
			if (token.isEmpty()) continue;
			final int equals = token.indexOf("=");
			if (equals < 0) throw new ScriptException("Invalid attribute: " + token);
			final String key = token.substring(0, equals).trim();
			String value = token.substring(equals + 1).trim();
			if (value.startsWith("\"") && value.endsWith("\"")) {
				value = value.substring(1, value.length() - 2);
			}
			attrsMap.put(key, value);
		}
		return attrsMap;
	}

	private boolean isIOType(final String token) {
		return ConversionUtils.convert(token, ItemIO.class) != null;
	}

	private void checkValid(final boolean valid, final String param)
		throws ScriptException
	{
		if (!valid) throw new ScriptException("Invalid parameter: " + param);
	}

	/** Adds an output for the value returned by the script itself. */
	private void addReturnValue() throws ScriptException {
		final HashMap<String, String> attrs = new HashMap<String, String>();
		attrs.put("type", "OUTPUT");
		addItem(ScriptModule.RETURN_VALUE, Object.class, attrs);
	}

	private <T> void addItem(final String name, final Class<T> type,
		final Map<String, String> attrs) throws ScriptException
	{
		final DefaultMutableModuleItem<T> item =
			new DefaultMutableModuleItem<T>(this, name, type);
		for (final String key : attrs.keySet()) {
			final String value = attrs.get(key);
			assignAttribute(item, key, value);
		}
		if (item.isInput()) {
			inputMap().put(item.getName(), item);
			inputList().add(item);
		}
		if (item.isOutput()) {
			outputMap().put(item.getName(), item);
			outputList().add(item);
		}
	}

	private <T> void assignAttribute(final DefaultMutableModuleItem<T> item,
		final String key, final String value) throws ScriptException
	{
		// CTR: There must be an easier way to do this.
		// Just compile the thing using javac? Or parse via javascript, maybe?
		if ("callback".equalsIgnoreCase(key)) {
			item.setCallback(value);
		}
		else if ("choices".equalsIgnoreCase(key)) {
			// FIXME: Regex above won't handle {a,b,c} syntax.
//			item.setChoices(choices);
		}
		else if ("columns".equalsIgnoreCase(key)) {
			item.setColumnCount(ConversionUtils.convert(value, int.class));
		}
		else if ("description".equalsIgnoreCase(key)) {
			item.setDescription(value);
		}
		else if ("initializer".equalsIgnoreCase(key)) {
			item.setInitializer(value);
		}
		else if ("type".equalsIgnoreCase(key)) {
			item.setIOType(ConversionUtils.convert(value, ItemIO.class));
		}
		else if ("label".equalsIgnoreCase(key)) {
			item.setLabel(value);
		}
		else if ("max".equalsIgnoreCase(key)) {
			item.setMaximumValue(ConversionUtils.convert(value, item.getType()));
		}
		else if ("min".equalsIgnoreCase(key)) {
			item.setMinimumValue(ConversionUtils.convert(value, item.getType()));
		}
		else if ("name".equalsIgnoreCase(key)) {
			item.setName(value);
		}
		else if ("persist".equalsIgnoreCase(key)) {
			item.setPersisted(ConversionUtils.convert(value, boolean.class));
		}
		else if ("persistKey".equalsIgnoreCase(key)) {
			item.setPersistKey(value);
		}
		else if ("required".equalsIgnoreCase(key)) {
			item.setRequired(ConversionUtils.convert(value, boolean.class));
		}
		else if ("softMax".equalsIgnoreCase(key)) {
			item.setSoftMaximum(ConversionUtils.convert(value, item.getType()));
		}
		else if ("softMin".equalsIgnoreCase(key)) {
			item.setSoftMinimum(ConversionUtils.convert(value, item.getType()));
		}
		else if ("stepSize".equalsIgnoreCase(key)) {
			// FIXME
			item.setStepSize(ConversionUtils.convert(value, Number.class));
		}
		else if ("visibility".equalsIgnoreCase(key)) {
			item.setVisibility(ConversionUtils.convert(value, ItemVisibility.class));
		}
		else if ("value".equalsIgnoreCase(key)) {
			item.setWidgetStyle(value);
		}
		else {
			throw new ScriptException("Invalid attribute name: " + key);
		}
	}

	private void clearParameters() {
		inputMap().clear();
		inputList().clear();
		outputMap().clear();
		outputList().clear();
	}

}
