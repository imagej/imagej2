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

package imagej.legacy;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageWindow;
import imagej.data.display.ImageDisplay;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.scijava.util.FileUtils;

/**
 * A helper class to interact with ImageJ 1.x.
 * 
 * The DefaultLegacyService needs to patch ImageJ 1.x' classes before they
 * are loaded. Unfortunately, this is tricky: if the DefaultLegacyService
 * already uses those classes, it is a matter of luck whether we can get
 * the patches in before those classes are loaded.
 * 
 * Therefore, we put as much interaction with ImageJ 1.x as possible into
 * this class and keep a reference to it in the DefaultLegacyService.
 * 
 * @author Johannes Schindelin
 */
public class IJ1Helper {

	/** A reference to the legacy service, just in case we need it */
	private final DefaultLegacyService legacyService;

	public IJ1Helper(final DefaultLegacyService legacyService) {
		this.legacyService = legacyService;
	}

	public void initialize() {
		// initialize legacy ImageJ application
		if (IJ.getInstance() == null) try {
			new ImageJ(ImageJ.NO_SHOW);
		}
		catch (final Throwable t) {
			legacyService.getLogService().warn("Failed to instantiate IJ1.", t);
		} else {
			final LegacyImageMap imageMap = legacyService.getImageMap();
			for (int i = 1; i <= WindowManager.getImageCount(); i++) {
				imageMap.registerLegacyImage(WindowManager.getImage(i));
			}
		}
	}

	public void dispose() {
		final ImageJ ij = IJ.getInstance();
		if (ij != null) {
			// close out all image windows, without dialog prompts
			while (true) {
				final ImagePlus imp = WindowManager.getCurrentImage();
				if (imp == null) break;
				imp.changes = false;
				imp.close();
			}

			// close any remaining (non-image) windows
			WindowManager.closeAllWindows();

			// quit legacy ImageJ on the same thread
			ij.run();
		}
	}

	public void setVisible(boolean toggle) {
		final ImageJ ij = IJ.getInstance();
		if (ij != null) {
			if (toggle) ij.pack();
			ij.setVisible(toggle);
		}

		// hide/show the legacy ImagePlus instances
		final LegacyImageMap imageMap = legacyService.getImageMap();
		for (final ImagePlus imp : imageMap.getImagePlusInstances()) {
			final ImageWindow window = imp.getWindow();
			if (window != null) window.setVisible(toggle);
		}
	}

	public void syncActiveImage(final ImageDisplay activeDisplay) {
		final LegacyImageMap imageMap = legacyService.getImageMap();
		final ImagePlus activeImagePlus = imageMap.lookupImagePlus(activeDisplay);
		// NB - old way - caused probs with 3d Project
		// WindowManager.setTempCurrentImage(activeImagePlus);
		// NB - new way - test thoroughly
		if (activeImagePlus == null) WindowManager.setCurrentWindow(null);
		else WindowManager.setCurrentWindow(activeImagePlus.getWindow());
	}

	public void setKeyDown(int keyCode) {
		IJ.setKeyDown(keyCode);
	}

	public void setKeyUp(int keyCode) {
		IJ.setKeyUp(keyCode);
	}

	public boolean hasInstance() {
		return IJ.getInstance() != null;
	}

	public String getVersion() {
		return IJ.getVersion();
	}

	public boolean isMacintosh() {
		return IJ.isMacintosh();
	}

	public static boolean handleNoSuchMethodError(NoSuchMethodError error) {
		String message = error.getMessage();
		int paren = message.indexOf("(");
		if (paren < 0) return false;
		int dot = message.lastIndexOf(".", paren);
		if (dot < 0) return false;
		String path = message.substring(0, dot).replace('.', '/') + ".class";
		Set<String> urls = new LinkedHashSet<String>();
		try {
			Enumeration<URL> e = IJ.getClassLoader().getResources(path);
			while (e.hasMoreElements()) {
				urls.add(e.nextElement().toString());
			}
			e = IJ.getClassLoader().getResources("/" + path);
			while (e.hasMoreElements()) {
				urls.add(e.nextElement().toString());
			}
		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}

		if (urls.size() == 0) return false;
		StringBuilder buffer = new StringBuilder();
		buffer.append("There was a problem with the class ");
		buffer.append(message.substring(0, dot));
		buffer.append(" which can be found here:\n");
		for (String url : urls) {
			if (url.startsWith("jar:")) url = url.substring(4);
			if (url.startsWith("file:")) url = url.substring(5);
			int bang = url.indexOf("!");
			if (bang < 0) buffer.append(url);
			else buffer.append(url.substring(0, bang));
			buffer.append("\n");
		}
		if (urls.size() > 1) {
			buffer.append("\nWARNING: multiple locations found!\n");
		}

		StringWriter writer = new StringWriter();
		error.printStackTrace(new PrintWriter(writer));
		buffer.append(writer.toString());

		IJ.log(buffer.toString());
		IJ.error("Could not find method " + message + "\n(See Log for details)\n");
		return true;
	}

	public static List<File> handleExtraPluginJars() {
		final List<File> result = new ArrayList<File>();
		final String extraPluginDirs = System.getProperty("ij1.plugin.dirs");
		if (extraPluginDirs != null) {
			for (final String dir : extraPluginDirs.split(File.pathSeparator)) {
				handleExtraPluginJars(new File(dir), result);
			}
			return result;
		}
		final String userHome = System.getProperty("user.home");
		if (userHome != null) handleExtraPluginJars(new File(userHome, ".plugins"), result);
		return result;
	}

	private static void handleExtraPluginJars(final File directory, final List<File> result) {
		final File[] list = directory.listFiles();
		if (list == null) return;
		for (final File file : list) {
			if (file.isDirectory()) handleExtraPluginJars(file, result);
			else if (file.isFile() && file.getName().endsWith(".jar")) {
				result.add(file);
			}
		}
	}

	/**
	 * A minimal interface for the editor to use instead of ImageJ 1.x' limited AWT-based one.
	 * 
	 * @author Johannes Schindelin
	 */
	public interface LegacyEditorPlugin {
		public boolean open(final File path);
		public boolean create(final String title, final String content);
	}

	private static LegacyEditorPlugin editor;

	/**
	 * Sets the legacy editor to use instead of ImageJ 1.x' built-in one.
	 * 
	 * @param plugin the editor to set, or null if ImageJ 1.x' built-in editor should be used
	 */
	public static void setLegacyEditor(final LegacyEditorPlugin plugin) {
		editor = plugin;
	}

	/**
	 * Opens the given path in the registered legacy editor, if any.
	 * 
	 * @param path the path of the file to open
	 * @return whether the file was opened successfully
	 */
	public static boolean openInLegacyEditor(final String path) {
		if (editor == null) return false;
		if (path.indexOf("://") > 0) return false;
		if ("".equals(FileUtils.getExtension(path))) return false;
		if (stackTraceContains(IJ1Helper.class.getName() + ".openEditor(")) return false;
		final File file = new File(path);
		if (!file.exists()) return false;
		if (isBinaryFile(file)) return false;
		return editor.open(file);
	}

	/**
	 * Creates the given file in the registered legacy editor, if any.
	 * 
	 * @param title the title of the file to create
	 * @param content the text of the file to be created
	 * @return whether the fule was opened successfully
	 */
	public static boolean createInLegacyEditor(final String title, final String content) {
		if (editor == null) return false;
		return editor.create(title, content);
	}

	/**
	 * Determines whether the current stack trace contains the specified string.
	 * 
	 * @param needle the text to find
	 * @return whether the stack trace contains the text
	 */
	private static boolean stackTraceContains(String needle) {
		final StringWriter writer = new StringWriter();
		final PrintWriter out = new PrintWriter(writer);
		new Exception().printStackTrace(out);
		out.close();
		return writer.toString().indexOf(needle) >= 0;
	}

    /**
     * Determines whether a file is binary or text.
     * 
     * This just checks for a NUL in the first 1024 bytes.
     * Not the best test, but a pragmatic one.
     * 
     * @param file the file to test
     * @return whether it is binary
     */
	private static boolean isBinaryFile(final File file) {
        try {
			InputStream in = new FileInputStream(file);
			byte[] buffer = new byte[1024];
			int offset = 0;
			while (offset < buffer.length) {
				int count = in.read(buffer, offset, buffer.length - offset);
				if (count < 0)
					break;
				else
					offset += count;
			}
			in.close();
			while (offset > 0)
				if (buffer[--offset] == 0)
					return true;
		} catch (IOException e) {
		}
        return false;
    }

}
