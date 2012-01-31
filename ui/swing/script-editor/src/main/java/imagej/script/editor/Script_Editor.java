package fiji.scripting;

import fiji.FijiClassLoader;

import ij.IJ;
import ij.Macro;

import ij.plugin.PlugIn;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.SwingUtilities;

import java.lang.reflect.Method;

import java.net.URL;
import java.net.URLClassLoader;

public class Script_Editor implements PlugIn {
	protected static TextEditor instance;

	public static TextEditor getInstance() {
		return instance;
	}

	public void run(String path) {
		String options = Macro.getOptions();
		if (options != null) {
			if (path == null || path.equals(""))
				path = Macro.getValue(options, "path", null);
			if (path == null)
				path = Macro.getValue(options, "open", null);
			if (path == null && options.indexOf('=') < 0) {
				path = options;
				if (path.endsWith(" "))
					path = path.substring(0,
							path.length() - 1);
			}
		}
		if (instance == null || !instance.isVisible()) {
			instance = new TextEditor(path);
			if (!isToolsJarAvailable())
				instance.installDebugSupportMenuItem();
			SwingUtilities.invokeLater(new Runnable() { public void run() {
				instance.setVisible(true);
			}});
		}
		else {
			instance.open(path);
			instance.toFront();
		}
	}

	final private static String gitwebURL =
		"http://fiji.sc/cgi-bin/gitweb.cgi?p=java/";

	public static String getPlatform() {
		boolean is64bit =
			System.getProperty("os.arch", "").indexOf("64") >= 0;
		String osName = System.getProperty("os.name", "<unknown>");
		if (osName.equals("Linux"))
			return "linux" + (is64bit ? "-amd64" : "");
		if (osName.equals("Mac OS X"))
			return "macosx";
		if (osName.startsWith("Windows"))
			return "win" + (is64bit ? "64" : "32");
		//System.err.println("Unknown platform: " + osName);
		return osName.toLowerCase();
	}

	private String getNewestJavaSubdirectory(String baseURL) {
		try {
			URL url = new URL(baseURL + ";a=tree");
			InputStream input = url.openStream();
			int off = 0, len = 16384;
			byte[] buffer = new byte[len];
			for (;;) {
				int count = input.read(buffer, off, len);
				if (count < 0)
					break;
				off += count;
				len -= count;
			}
			final String content = new String(buffer, 0, off);
			final String key = ";a=tree;f=";
			off = content.indexOf(key);
			if (off >= 0) {
				off += key.length();
				int end = content.indexOf(';', off);
				return content.substring(off, end);
			}
		} catch (IOException e) { e.printStackTrace(); }
		return "";
	}

	public static boolean isToolsJarAvailable() {
		ClassLoader loader = IJ.getClassLoader();
		try {
			if (loader != null)
				return loader.loadClass("com.sun.jdi.VirtualMachine") != null;
			return Class.forName("com.sun.jdi.VirtualMachine") != null;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	public void addToolsJarToClassPath() {
		if (isToolsJarAvailable())
			return;

		try {
			// make sure it is a FijiClassLoader
			FijiClassLoader loader =
				(FijiClassLoader)IJ.getClassLoader();

			File tools_jar =
				new File(System.getProperty("java.home")
				+ "/../lib/tools.jar");
			URL url;
			if (tools_jar.exists())
				url = tools_jar.toURL();
			else {
				String baseURL = gitwebURL + getPlatform()
					+ ".git";
				url = new URL(baseURL + ";a=blob_plain;f="
					+ getNewestJavaSubdirectory(baseURL)
					+ "/lib/tools.jar");
			}
			URL[] urls = new URL[] { url };
			IJ.showStatus("Adding tools.jar from " + url);
			loader.addFallBack(new URLClassLoader(urls));
			return;
		} catch (Exception e) { e.printStackTrace(); }
		IJ.showStatus("Could not find debugging support library");
	}
}
