//
// AbstractUI.java
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

package imagej.ui;

import imagej.ImageJ;
import imagej.util.Log;
import imagej.util.Prefs;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Abstract superclass for {@link UserInterface} implementations.
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractUI implements UserInterface {

	private static final String README_FILE = "README.txt";
	private static final String PREF_FIRST_RUN = "firstRun-" + ImageJ.VERSION;

	private UIService uiService;

	// -- UserInterface methods --

	@Override
	public void initialize(final UIService service) {
		uiService = service;
		createUI();
		displayReadme();
	}

	@Override
	public UIService getUIService() {
		return uiService;
	}

	@Override
	public void processArgs(final String[] args) {
		// TODO
	}

	@Override
	public Desktop getDesktop() {
		return null;
	}

	@Override
	public ApplicationFrame getApplicationFrame() {
		return null;
	}

	@Override
	public ToolBar getToolBar() {
		return null;
	}

	@Override
	public StatusBar getStatusBar() {
		return null;
	}

	// -- Internal methods --

	protected void createUI() {
		// does nothing by default
	}

	// -- Helper methods --

	private void displayReadme() {
		final String firstRun = Prefs.get(getClass(), PREF_FIRST_RUN);
		if (firstRun != null) return;
		Prefs.put(getClass(), PREF_FIRST_RUN, false);

		// CTR TODO - invoke a ShowReadme plugin instead of using OutputWindow directly
		final OutputWindow out =
			newOutputWindow("ImageJ v" + ImageJ.VERSION + " - " + README_FILE);

		final String readmeText = loadReadmeFile();
		out.append(readmeText);
		out.setVisible(true);
	}

	private String loadReadmeFile() { 
		final File baseDir = getBaseDirectory();
		final File readmeFile = new File(baseDir, README_FILE);

		try {
			final DataInputStream in =
				new DataInputStream(new FileInputStream(readmeFile));
			final int len = (int) readmeFile.length();
			final byte[] bytes = new byte[len];
			in.readFully(bytes);
			in.close();
			return new String(bytes);
		}
		catch (final FileNotFoundException e) {
			throw new IllegalArgumentException(README_FILE + " not found at " +
				baseDir.getAbsolutePath());
		}
		catch (final IOException e) {
			throw new IllegalStateException(e.getMessage());
		}
	}

	private File getBaseDirectory() {
		final File pathToClass = getPathToClass();
		final String path = pathToClass.getPath();

		final File baseDir;
		if (path.endsWith(".class")) {
			// assume class is in a subfolder of Maven target
			File dir = pathToClass;
			while (dir != null && !dir.getName().equals("target")) {
				dir = up(dir);
			}
			// NB: Base directory is 5 levels up from ui/awt-swing/swing/ui/target.
			baseDir = up(up(up(up(up(dir)))));
		}
		else if (path.endsWith(".jar")) {
			// assume class is in a library folder of the distribution
			final File dir = pathToClass.getParentFile();
			baseDir = up(dir);
		}
		else baseDir = null;

		// return current working directory if not found
		return baseDir == null ? new File(".") : baseDir;
	}

	/**
	 * Gets the file on disk containing this class.
	 * <p>
	 * This could be a jar archive, or a standalone class file.
	 * </p>
	 */
	private File getPathToClass() {
		final Class<?> c = getClass();
		final String className = c.getSimpleName();
		String path = getClass().getResource(className + ".class").toString();
		path = path.replaceAll("^jar:", "");
		path = path.replaceAll("^file:", "");
		path = path.replaceAll("^/*/", "/");
		path = path.replaceAll("^/([A-Z]:)", "$1");
		path = path.replaceAll("!.*", "");
		try {
			path = URLDecoder.decode(path, "UTF-8");
		}
		catch (final UnsupportedEncodingException e) {
			Log.warn("Cannot parse class: " + className, e);
		}
		String slash = File.separator;
		if (slash.equals("\\")) slash = "\\\\";
		path = path.replaceAll("/", slash);
		return new File(path);
	}

	private File up(final File file) {
		if (file == null) return null;
		return file.getParentFile();
	}

}
