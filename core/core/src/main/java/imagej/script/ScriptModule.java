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
import imagej.module.Module;
import imagej.module.ModuleItem;

import java.io.FileReader;
import java.io.Reader;
import java.io.Writer;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.scijava.Context;
import org.scijava.Contextual;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.util.ConversionUtils;
import org.scijava.util.FileUtils;

/**
 * A {@link Module} which executes a script.
 * 
 * @author Curtis Rueden
 * @author Johannes Schindelin
 */
public class ScriptModule extends AbstractModule implements Contextual {

	public static final String RETURN_VALUE = "result";

	private final ScriptInfo info;

	@Parameter
	private Context context;

	@Parameter
	private ScriptService scriptService;

	@Parameter
	private LogService log;

	/** Script language in which the script should be executed. */
	private ScriptLanguage scriptLanguage;

	/** Script engine with which the script should be executed. */
	private ScriptEngine scriptEngine;

	/** Destination for standard output during script execution. */
	private Writer output;

	/** Destination for standard error during script execution. */
	private Writer error;

	public ScriptModule(final ScriptInfo info) {
		this.info = info;
	}

	// -- ScriptModule methods --

	/** Gets the scripting language of the script. */
	public ScriptLanguage getLanguage() {
		if (scriptLanguage == null) {
			// infer the language from the script path's extension
			final String path = getInfo().getPath();
			final String extension = FileUtils.getExtension(path);
			scriptLanguage = scriptService.getLanguageByExtension(extension);
		}
		return scriptLanguage;
	}

	/** Overrides the script language to use when executing the script. */
	public void setLanguage(final ScriptLanguage scriptLanguage) {
		this.scriptLanguage = scriptLanguage;
	}

	/** Sets the writer used to record the standard output stream. */
	public void setOutputWriter(final Writer output) {
		this.output = output;
	}

	/** Sets the writer used to record the standard error stream. */
	public void setErrorWriter(final Writer error) {
		this.error = error;
	}

	/** Gets the script engine used to execute the script. */
	public ScriptEngine getEngine() {
		if (scriptEngine == null) {
			scriptEngine = getLanguage().getScriptEngine();
		}
		return scriptEngine;
	}

	/** Gets the return value of the script. */
	public Object getReturnValue() {
		return getOutput(RETURN_VALUE);
	}

	// -- Module methods --

	@Override
	public ScriptInfo getInfo() {
		return info;
	}

	// -- Runnable methods --

	@Override
	public void run() {
		final ScriptEngine engine = getEngine();
		final String path = getInfo().getPath();

		// initialize the script engine
		engine.put(ScriptEngine.FILENAME, path);
		final ScriptContext scriptContext = engine.getContext();
		if (output != null) scriptContext.setWriter(output);
		if (error != null) scriptContext.setErrorWriter(error);

		// populate bindings with the input values
		for (final ModuleItem<?> item : getInfo().inputs()) {
			final String name = item.getName();
			engine.put(name, getInput(name));
		}

		// execute script!
		final ScriptLanguage language = getLanguage();
		try {
			final Reader reader = getInfo().getReader();
			final Object returnValue;
			if (reader == null) returnValue = engine.eval(new FileReader(path));
			else returnValue = engine.eval(reader);
			setOutput(RETURN_VALUE, language.decode(returnValue));
			setResolved(RETURN_VALUE, true);
		}
		catch (final ScriptException e) {
			log.error(e.getCause());
		}
		catch (final Throwable e) {
			log.error(e);
		}

		// populate output values
		for (final ModuleItem<?> item : getInfo().outputs()) {
			final String name = item.getName();
			if (isResolved(name)) continue;
			final Object value = engine.get(name);
			final Object decoded = language.decode(value);
			final Object typed = ConversionUtils.convert(decoded, item.getType());
			setOutput(name, typed);
		}
	}

	// -- Contextual methods --

	@Override
	public Context getContext() {
		return context;
	}

	@Override
	public void setContext(Context context) {
		context.inject(this);
	}

}
