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

import imagej.service.IJService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import org.scijava.log.LogService;
import org.scijava.plugin.PluginService;

/**
 * A service discovering all available script languages and convenience methods
 * to interact with them
 * 
 * @author Johannes Schindelin
 */
public interface ScriptService extends IJService {

	// CTR TODO: Extend SingletonService<ScriptEngineFactory>.

	/**
	 * The script service puts the current ImageJ context into the engine's bindings
	 * using this key. That way, scripts can access the context by accessing the global
	 * variable of that name.
	 */
	final static String CONTEXT = "IJ";

	PluginService getPluginService();

	LogService getLogService();

	/** Gets the index of available script languages. */
	ScriptLanguageIndex getIndex();

	List<ScriptEngineFactory> getLanguages();

	ScriptEngineFactory getByFileExtension(final String fileExtension);

	ScriptEngineFactory getByName(final String name);

	Object eval(final File file) throws FileNotFoundException,
		ScriptException;

	Object eval(final String filename, final Reader reader)
			throws IOException, ScriptException;

	boolean canHandleFile(final File file);
	boolean canHandleFile(final String fileName);

	void initialize(final ScriptEngine engine, final String fileName,
			final Writer writer, final Writer errorWriter);

	boolean isCompiledLanguage(ScriptEngineFactory currentLanguage);

}

