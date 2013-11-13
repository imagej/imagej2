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

import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

/**
 * Abstract superclass for {@link ScriptLanguage} implementations which adapt an
 * existing {@link ScriptEngineFactory}.
	 * <p>
	 * This is useful for situations where a JSR-223-compliant script engine has
	 * been provided, but whose behavior we need to extend or tweak.
	 * </p>
 * 
 * @author Curtis Rueden
 */
public class AdaptedScriptLanguage implements ScriptLanguage {

	/** The {@link ScriptEngineFactory} which this one adapts. */
	private final ScriptEngineFactory base;

	/**
	 * Creates a new {@link AdaptedScriptLanguage} wrapping the given
	 * {@link ScriptEngineFactory}.
	 */
	public AdaptedScriptLanguage(final ScriptEngineFactory base) {
		this.base = base;
	}

	/**
	 * Creates a new {@link AdaptedScriptLanguage} wrapping the
	 * {@link ScriptEngineFactory} with the given name.
	 */
	public AdaptedScriptLanguage(final String factoryName) {
		this(findFactory(factoryName));
	}

	// -- ScriptLanguage methods --

	@Override
	public boolean isCompiledLanguage() {
		return false;
	}

	// -- ScriptEngineFactory methods --

	@Override
	public String getEngineName() {
		return base.getEngineName();
	}

	@Override
	public String getEngineVersion() {
		return base.getEngineVersion();
	}

	@Override
	public List<String> getExtensions() {
		return base.getExtensions();
	}

	@Override
	public List<String> getMimeTypes() {
		return base.getMimeTypes();
	}

	@Override
	public List<String> getNames() {
		return base.getNames();
	}

	@Override
	public String getLanguageName() {
		return base.getLanguageName();
	}

	@Override
	public String getLanguageVersion() {
		return base.getLanguageVersion();
	}

	@Override
	public Object getParameter(final String key) {
		return base.getParameter(key);
	}

	@Override
	public String getMethodCallSyntax(final String obj, final String m,
		final String... args)
	{
		return base.getMethodCallSyntax(obj, m, args);
	}

	@Override
	public String getOutputStatement(final String toDisplay) {
		return base.getOutputStatement(toDisplay);
	}

	@Override
	public String getProgram(final String... statements) {
		return base.getProgram(statements);
	}

	@Override
	public ScriptEngine getScriptEngine() {
		return base.getScriptEngine();
	}

	// -- Helper methods --

	private static ScriptEngineFactory findFactory(String factoryName) {
		final ScriptEngineManager manager = new javax.script.ScriptEngineManager();
		for (final ScriptEngineFactory factory : manager.getEngineFactories()) {
			for (final String name : factory.getNames()) {
				if (factoryName.equals(name)) return factory;
			}
		}
		return null;
	}

}
