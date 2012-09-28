//
// ScriptLanguageIndex.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
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
