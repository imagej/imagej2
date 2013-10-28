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

import java.util.Collections;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

/**
 * Abstract superclass for {@link ScriptLanguage} implementations.
 * <p>
 * This class implements dummy versions of {@link ScriptEngineFactory}'s methods
 * that are not needed by ImageJ's scripting interface.
 * </p>
 * 
 * @author Johannes Schindelin
 */
public abstract class AbstractScriptLanguage implements ScriptLanguage {

	// (Possibly) unsupported operations

	@Override
	public String getMethodCallSyntax(final String object, final String method,
		final String... args)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getOutputStatement(final String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getProgram(final String... statements) {
		throw new UnsupportedOperationException();
	}

	// Default implementations

	@Override
	public List<String> getExtensions() {
		return Collections.<String> emptyList();
	}

	@Override
	public String getEngineName() {
		return inferNameFromClassName();
	}

	@Override
	public String getLanguageName() {
		return inferNameFromClassName();
	}

	@Override
	public List<String> getNames() {
		return Collections.<String> singletonList(getEngineName());
	}

	@Override
	public String getLanguageVersion() {
		return "0.0";
	}

	@Override
	public List<String> getMimeTypes() {
		return Collections.<String> emptyList();
	}

	@Override
	public Object getParameter(final String key) {
		if (key.equals(ScriptEngine.ENGINE)) {
			return getEngineName();
		}
		else if (key.equals(ScriptEngine.ENGINE_VERSION)) {
			return getEngineVersion();
		}
		else if (key.equals(ScriptEngine.NAME)) {
			final List<String> list = getNames();
			return list.size() > 0 ? list.get(0) : null;
		}
		else if (key.equals(ScriptEngine.LANGUAGE)) {
			return getLanguageName();
		}
		else if (key.equals(ScriptEngine.LANGUAGE_VERSION)) {
			return getLanguageVersion();
		}
		return null;
	}

	@Override
	public String getEngineVersion() {
		return "0.0";
	}

	protected String inferNameFromClassName() {
		final String className = getClass().getName().replace('_', ' ');
		final int dot = className.lastIndexOf('.');
		return className.substring(dot + 1);
	}

}
