//
// SwingUI.java
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

// TODO - see TODOs below

package imagej.ui.swing;

import imagej.ImageJ;
import imagej.event.Events;
import imagej.platform.event.AppMenusCreatedEvent;
import imagej.platform.event.AppQuitEvent;
import imagej.plugin.PluginEntry;
import imagej.plugin.PluginManager;
import imagej.plugin.ui.ShadowMenu;
import imagej.plugin.ui.swing.JMenuBarCreator;
import imagej.ui.UI;
import imagej.ui.UserInterface;
import imagej.util.Prefs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

/**
 * Swing-based user interface for ImageJ.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
@UI
public class SwingUI implements UserInterface {

	private static final String README_FILE = "README.txt";
	private static final String PREF_FIRST_RUN = "firstRun-2.0.0-alpha1";

	private JFrame frame;
	private SwingToolBar toolBar;
	private SwingStatusBar statusBar;

	// -- UserInterface methods --

	@Override
	public void initialize() {
		frame = new JFrame("ImageJ");
		toolBar = new SwingToolBar();
		statusBar = new SwingStatusBar();
		createMenuBar();

		final JPanel pane = new JPanel();
		frame.setContentPane(pane);
		pane.setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent evt) {
				Events.publish(new AppQuitEvent());
			}
		});

		pane.add(toolBar, BorderLayout.NORTH);
		pane.add(statusBar, BorderLayout.SOUTH);

		frame.pack();
		frame.setVisible(true);

		displayReadme();
	}

	@Override
	public void processArgs(final String[] args) {
		// TODO
	}

	@Override
	public SwingToolBar getToolBar() {
		return toolBar;
	}

	@Override
	public SwingStatusBar getStatusBar() {
		return statusBar;
	}

	// -- Helper methods --

	private void createMenuBar() {
		final PluginManager pluginManager = ImageJ.get(PluginManager.class);
		final List<PluginEntry<?>> entries = pluginManager.getPlugins();
		final ShadowMenu rootMenu = new ShadowMenu(entries);
		final JMenuBar menuBar = new JMenuBar();
		new JMenuBarCreator().createMenus(rootMenu, menuBar);
		frame.setJMenuBar(menuBar);
		Events.publish(new AppMenusCreatedEvent(menuBar));
	}

	private void displayReadme() {
		final String firstRun = Prefs.get(getClass(), PREF_FIRST_RUN);
		if (firstRun != null) return;
		Prefs.put(getClass(), PREF_FIRST_RUN, false);

		final JFrame readmeFrame = new JFrame();
		final JTextArea text = new JTextArea();
		text.setEditable(false);
		final JScrollPane scrollPane = new JScrollPane(text);
		scrollPane.setPreferredSize(new Dimension(600, 500));
		readmeFrame.setLayout(new BorderLayout());
		readmeFrame.add(scrollPane, BorderLayout.CENTER);
		readmeFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		readmeFrame.setTitle("ImageJ v" + ImageJ.VERSION + " - " + README_FILE);
		readmeFrame.pack();

		final String readmeText = loadReadmeFile();
		text.setText(readmeText);

		readmeFrame.setVisible(true);
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
		catch (FileNotFoundException e) {
			throw new IllegalArgumentException(README_FILE +
				" not found at " + baseDir.getAbsolutePath());
		}
		catch (IOException e) {
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
			while (dir != null && !dir.getName().equals("target")) dir = up(dir);
			baseDir = up(up(up(dir)));
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
		catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
