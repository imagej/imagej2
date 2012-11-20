/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
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

import imagej.util.FileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngineFactory;

/**
 * Data structure for managing registered script languages.
 * 
 * @author Johannes Schindelin
 */
public class ScriptLanguageIndex extends HashSet<ScriptEngineFactory> {

	private static final long serialVersionUID = 1L;

	private final Map<String, ScriptEngineFactory> byFileExtension =
		new HashMap<String, ScriptEngineFactory>();

	private final Map<String, ScriptEngineFactory> byName =
		new HashMap<String, ScriptEngineFactory>();

	@Override
	public boolean add(final ScriptEngineFactory language) {
		return add(language, false);
	}

	public boolean add(final ScriptEngineFactory language, final boolean gently) {
		for (final String fileExtension : language.getExtensions()) {
			if ("".equals(fileExtension)) continue;
			if (byFileExtension.containsKey(fileExtension)) {
				if (gently) continue;
				final String previous =
					byFileExtension.get(fileExtension).getEngineName();
				throw new IllegalArgumentException(
					"Duplicate script languages for file extension " + fileExtension +
						": " + language.getEngineName() + " vs " + previous);
			}
			byFileExtension.put(fileExtension, language);
		}

		final String name = language.getLanguageName();
		if (byName.containsKey(name)) {
			if (gently) return false;
			final String previous = byName.get(name).getEngineName();
			throw new IllegalArgumentException(
				"Duplicate script languages for name " + name + ": " +
					language.getEngineName() + " vs " + previous);
		}
		byName.put(name, language);

		return super.add(language);
	}

	public ScriptEngineFactory getByFileExtension(final String fileExtension) {
		return byFileExtension.get(fileExtension);
	}

	public ScriptEngineFactory getByName(final String name) {
		return byName.get(name);
	}

	public String[] getFileExtensions(final ScriptEngineFactory language) {
		final List<String> extensions = language.getExtensions();
		return extensions.toArray(new String[extensions.size()]);
	}

	public boolean canHandleFile(final File file) {
		final String fileExtension = FileUtils.getExtension(file);
		if ("".equals(fileExtension)) return false;
		return byFileExtension.containsKey(fileExtension);
	}

	public boolean canHandleFile(final String fileName) {
		final String fileExtension = FileUtils.getExtension(fileName);
		if ("".equals(fileExtension)) return false;
		return byFileExtension.containsKey(fileExtension);
	}

}
