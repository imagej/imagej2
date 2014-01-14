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

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngineFactory;

import org.scijava.util.FileUtils;

/**
 * Data structure for managing registered scripting languages.
 * 
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
public class ScriptLanguageIndex extends HashSet<ScriptLanguage> {

	private static final long serialVersionUID = 1L;

	private final Map<String, ScriptLanguage> byExtension =
		new HashMap<String, ScriptLanguage>();

	private final Map<String, ScriptLanguage> byName =
		new HashMap<String, ScriptLanguage>();

	public boolean add(final ScriptEngineFactory factory, final boolean gently) {
		final String duplicateName = checkDuplicate(factory);
		if (duplicateName != null) {
			if (gently) return false;
			throw new IllegalArgumentException("Duplicate scripting language '" +
				duplicateName + "': existing=" +
				byName.get(duplicateName).getClass().getName() + ", new=" +
				factory.getClass().getName());
		}

		final ScriptLanguage language = wrap(factory);

		// add language names
		for (final String name : language.getNames()) {
			byName.put(name, language);
		}

		// add file extensions
		for (final String extension : language.getExtensions()) {
			if ("".equals(extension)) continue;
			byExtension.put(extension, language);
		}

		return super.add(language);
	}

	public ScriptLanguage getByExtension(final String extension) {
		return byExtension.get(extension);
	}

	public ScriptLanguage getByName(final String name) {
		return byName.get(name);
	}

	public String[] getFileExtensions(final ScriptLanguage language) {
		final List<String> extensions = language.getExtensions();
		return extensions.toArray(new String[extensions.size()]);
	}

	public boolean canHandleFile(final File file) {
		final String extension = FileUtils.getExtension(file);
		if ("".equals(extension)) return false;
		return byExtension.containsKey(extension);
	}

	public boolean canHandleFile(final String fileName) {
		final String extension = FileUtils.getExtension(fileName);
		if ("".equals(extension)) return false;
		return byExtension.containsKey(extension);
	}

	// -- Collection methods --

	@Override
	public boolean add(final ScriptLanguage language) {
		return add(language, false);
	}

	// -- Helper methods --

	private String checkDuplicate(final ScriptEngineFactory factory) {
		for (final String name : factory.getNames()) {
			if (byName.containsKey(name)) {
				return name;
			}
		}
		return null;
	}

	private ScriptLanguage wrap(final ScriptEngineFactory factory) {
		if (factory instanceof ScriptLanguage) return (ScriptLanguage) factory;
		return new AdaptedScriptLanguage(factory);
	}

}
