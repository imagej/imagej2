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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.scijava.util.FileUtils;

/**
 * Assorted legacy patches / extension points for use in the legacy mode.
 * 
 * <p>
 * The Fiji distribution of ImageJ accumulated patches and extensions to ImageJ
 * 1.x over the years.
 * </p>
 * 
 * <p>
 * However, there was a lot of overlap with the ImageJ2 project, so it was
 * decided to focus Fiji more on the life-science specific part and move all the
 * parts that are of more general use into ImageJ2. That way, it is pretty
 * clear-cut what goes into Fiji and what goes into ImageJ2.
 * </p>
 * 
 * <p>
 * This class contains the extension points (such as being able to override the
 * macro editor) ported from Fiji as well as the code for runtime patching
 * ImageJ 1.x needed both for the extension points and for more backwards
 * compatibility than ImageJ 1.x wanted to provide (e.g. when public methods or
 * classes that were used by Fiji plugins were removed all of a sudden, without
 * being deprecated first).
 * </p>
 * 
 * <p>
 * The code in this class is only used in the legacy mode.
 * </p>
 * 
 * @author Johannes Schindelin
 */
public class LegacyExtensions {

	/*
	 * Extension points
	 */

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
	private static String appName = "ImageJ";
	private static URL iconURL;

	/**
	 * Sets the application name for ImageJ 1.x.
	 * 
	 * @param name the name to display instead of <i>ImageJ</i>.
	 */
	public static void setAppName(final String name) {
		appName = name;
	}

	/**
	 * Returns the application name for use with ImageJ 1.x.
	 * @return the application name
	 */
	public static String getAppName() {
		return appName;
	}

	/**
	 * Sets the icon for ImageJ 1.x.
	 * 
	 * @param file
	 *            the {@link File} of the icon to use in ImageJ 1.x
	 */
	public static void setIcon(final File file) {
		if (file != null && file.exists()) try {
			iconURL = file.toURI().toURL();
			return;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		iconURL = null;
	}

	/**
	 * Returns the icon for use with ImageJ 1.x.
	 * 
	 * @return the application name
	 */
	public static URL getIconURL() {
		return iconURL;
	}

	/**
	 * Sets the legacy editor to use instead of ImageJ 1.x' built-in one.
	 * 
	 * @param plugin the editor to set, or null if ImageJ 1.x' built-in editor should be used
	 */
	public static void setLegacyEditor(final LegacyEditorPlugin plugin) {
		editor = plugin;
	}

	/*
	 * Helper functions intended to be called by runtime-patched ImageJ 1.x
	 */

	public static boolean handleNoSuchMethodError(NoSuchMethodError error) {
		String message = error.getMessage();
		int paren = message.indexOf("(");
		if (paren < 0) return false;
		int dot = message.lastIndexOf(".", paren);
		if (dot < 0) return false;
		String path = message.substring(0, dot).replace('.', '/') + ".class";
		Set<String> urls = new LinkedHashSet<String>();
		try {
			Enumeration<URL> e = IJ1Helper.getClassLoader().getResources(path);
			while (e.hasMoreElements()) {
				urls.add(e.nextElement().toString());
			}
			e = IJ1Helper.getClassLoader().getResources("/" + path);
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

		IJ1Helper.log(buffer.toString());
		IJ1Helper.error("Could not find method " + message + "\n(See Log for details)\n");
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
	 * Opens the given path in the registered legacy editor, if any.
	 * 
	 * @param path the path of the file to open
	 * @return whether the file was opened successfully
	 */
	public static boolean openInLegacyEditor(final String path) {
		if (editor == null) return false;
		if (path.indexOf("://") > 0) return false;
		if ("".equals(FileUtils.getExtension(path))) return false;
		if (stackTraceContains(LegacyExtensions.class.getName() + ".openEditor(")) return false;
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

	/*
	 * Runtime patches (using CodeHacker for patching)
	 */

	/**
	 * Applies runtime patches to ImageJ 1.x for backwards-compatibility and extension points.
	 * 
	 * <p>
	 * These patches enable a patched ImageJ 1.x to call a different script editor or to override
	 * the application icon.
	 * </p>
	 * 
	 * <p>
	 * This method is called by {@link LegacyInjector#injectHooks(ClassLoader)}.
	 * </p>
	 * 
	 * @param hacker the {@link CodeHacker} instance
	 */
	public static void injectHooks(final CodeHacker hacker) {
		//
		// Below are patches to make ImageJ 1.x more backwards-compatible
		//

		// add back the (deprecated) killProcessor(), and overlay methods
		final String[] imagePlusMethods = {
				"public void killProcessor()",
				"{}",
				"public void setDisplayList(java.util.Vector list)",
				"getCanvas().setDisplayList(list);",
				"public java.util.Vector getDisplayList()",
				"return getCanvas().getDisplayList();",
				"public void setDisplayList(ij.gui.Roi roi, java.awt.Color strokeColor,"
				+ " int strokeWidth, java.awt.Color fillColor)",
				"setOverlay(roi, strokeColor, strokeWidth, fillColor);"
		};
		for (int i = 0; i < imagePlusMethods.length; i++) try {
			hacker.insertNewMethod("ij.ImagePlus",
					imagePlusMethods[i], imagePlusMethods[++i]);
		} catch (Exception e) { /* ignore */ }

		// make sure that ImageJ has been initialized in batch mode
		hacker.insertAtTopOfMethod("ij.IJ",
				"public static java.lang.String runMacro(java.lang.String macro, java.lang.String arg)",
				"if (ij==null && ij.Menus.getCommands()==null) init();");

		try {
			hacker.insertNewMethod("ij.CompositeImage",
				"public ij.ImagePlus[] splitChannels(boolean closeAfter)",
				"ij.ImagePlus[] result = ij.plugin.ChannelSplitter.split(this);"
				+ "if (closeAfter) close();"
				+ "return result;");
			hacker.insertNewMethod("ij.plugin.filter.RGBStackSplitter",
				"public static ij.ImagePlus[] splitChannelsToArray(ij.ImagePlus imp, boolean closeAfter)",
				"if (!imp.isComposite()) {"
				+ "  ij.IJ.error(\"splitChannelsToArray was called on a non-composite image\");"
				+ "  return null;"
				+ "}"
				+ "ij.ImagePlus[] result = ij.plugin.ChannelSplitter.split(imp);"
				+ "if (closeAfter)"
				+ "  imp.close();"
				+ "return result;");
		} catch (IllegalArgumentException e) {
			final Throwable cause = e.getCause();
			if (cause != null && !cause.getClass().getName().endsWith("DuplicateMemberException")) {
				throw e;
			}
		}

		// handle mighty mouse (at least on old Linux, Java mistakes the horizontal wheel for a popup trigger)
		for (String fullClass : new String[] {
				"ij.gui.ImageCanvas",
				"ij.plugin.frame.RoiManager",
				"ij.text.TextPanel",
				"ij.gui.Toolbar"
		}) {
			hacker.handleMightyMousePressed(fullClass);
		}

		// tell IJ#runUserPlugIn to catch NoSuchMethodErrors
		final String runUserPlugInSig = "static java.lang.Object runUserPlugIn(java.lang.String commandName, java.lang.String className, java.lang.String arg, boolean createNewLoader)";
		hacker.addCatch("ij.IJ", runUserPlugInSig, "java.lang.NoSuchMethodError",
				"if (" + LegacyExtensions.class.getName() + ".handleNoSuchMethodError($e))"
				+ "  throw new RuntimeException(ij.Macro.MACRO_CANCELED);"
				+ "throw $e;");

		// tell IJ#runUserPlugIn to be more careful about catching NoClassDefFoundError
		hacker.insertPrivateStaticField("ij.IJ", String.class, "originalClassName");
		hacker.insertAtTopOfMethod("ij.IJ", runUserPlugInSig, "originalClassName = $2;");
		hacker.insertAtTopOfExceptionHandlers("ij.IJ", runUserPlugInSig, "java.lang.NoClassDefFoundError",
				"java.lang.String realClassName = $1.getMessage();"
							+ "int spaceParen = realClassName.indexOf(\" (\");"
							+ "if (spaceParen > 0) realClassName = realClassName.substring(0, spaceParen);"
							+ "if (!originalClassName.replace('.', '/').equals(realClassName)) {"
							+ " if (realClassName.startsWith(\"javax/vecmath/\") || realClassName.startsWith(\"com/sun/j3d/\") || realClassName.startsWith(\"javax/media/j3d/\"))"
							+ "  ij.IJ.error(\"The class \" + originalClassName + \" did not find Java3D (\" + realClassName + \")\\nPlease call Plugins>3D Viewer to install\");"
							+ " else"
							+ "  ij.IJ.handleException($1);"
							+ " return null;"
							+ "}");

		// let the plugin class loader find stuff in $HOME/.plugins, too
		addExtraPlugins(hacker);

		// make sure that the GenericDialog is disposed in macro mode
		try {
			hacker.insertAtTopOfMethod("ij.gui.GenericDialog", "public void showDialog()", "if (macro) dispose();");
		} catch (IllegalArgumentException e) {
			// ignore if the headless patcher renamed the method away
			if (e.getCause() == null || !e.getCause().getClass().getName().endsWith("NotFoundException")) {
				throw e;
			}
		}

		// make sure NonBlockingGenericDialog does not wait in macro mode
		hacker.replaceCallInMethod("ij.gui.NonBlockingGenericDialog", "public void showDialog()", "java.lang.Object", "wait", "if (isShowing()) wait();");

		// tell the showStatus() method to show the version() instead of empty status
		hacker.insertAtTopOfMethod("ij.ImageJ", "void showStatus(java.lang.String s)", "if ($1 == null || \"\".equals($1)) $1 = version();");

		// handle custom icon (e.g. for Fiji)
		addIconHooks(hacker);

		// optionally disallow batch mode from calling System.exit()
		hacker.insertPrivateStaticField("ij.ImageJ", Boolean.TYPE, "batchModeMayExit");
		hacker.insertAtTopOfMethod("ij.ImageJ", "public static void main(java.lang.String[] args)",
			"batchModeMayExit = true;"
			+ "for (int i = 0; i < $1.length; i++) {"
			+ "  if (\"-batch-no-exit\".equals($1[i])) {"
			+ "    batchModeMayExit = false;"
			+ "    $1[i] = \"-batch\";"
			+ "  }"
			+ "}");
		hacker.replaceCallInMethod("ij.ImageJ", "public static void main(java.lang.String[] args)", "java.lang.System", "exit",
			"if (batchModeMayExit) System.exit($1);"
			+ "if ($1 == 0) return;"
			+ "throw new RuntimeException(\"Exit code: \" + $1);");

		// do not use the current directory as IJ home on Windows
		String prefsDir = System.getenv("IJ_PREFS_DIR");
		if (prefsDir == null && System.getProperty("os.name").startsWith("Windows")) {
			prefsDir = System.getenv("user.home");
		}
		if (prefsDir != null) {
			hacker.overrideFieldWrite("ij.Prefs", "public java.lang.String load(java.lang.Object ij, java.applet.Applet applet)",
				"prefsDir", "$_ = \"" + prefsDir + "\";");
		}

		// tool names can be prefixes of other tools, watch out for that!
		hacker.replaceCallInMethod("ij.gui.Toolbar", "public int getToolId(java.lang.String name)", "java.lang.String", "startsWith",
			"$_ = $0.equals($1) || $0.startsWith($1 + \"-\") || $0.startsWith($1 + \" -\");");

		// make sure Rhino gets the correct class loader
		hacker.insertAtTopOfMethod("JavaScriptEvaluator", "public void run()",
			"Thread.currentThread().setContextClassLoader(ij.IJ.getClassLoader());");

		// make sure that the check for Bio-Formats is correct
		hacker.addToClassInitializer("ij.io.Opener",
			"try {"
			+ "    ij.IJ.getClassLoader().loadClass(\"loci.plugins.LociImporter\");"
			+ "    bioformats = true;"
			+ "} catch (ClassNotFoundException e) {"
			+ "    bioformats = false;"
			+ "}");

		// make sure that symbolic links are *not* resolved (because then the parent info in the FileInfo would be wrong)
		hacker.replaceCallInMethod("ij.plugin.DragAndDrop", "public void openFile(java.io.File f)", "java.io.File", "getCanonicalPath",
			"$_ = $0.getAbsolutePath();");

		// make sure no dialog is opened in headless mode
		hacker.insertAtTopOfMethod("ij.macro.Interpreter", "void showError(java.lang.String title, java.lang.String msg, java.lang.String[] variables)",
			"if (ij.IJ.getInstance() == null) {"
			+ "  java.lang.System.err.println($1 + \": \" + $2);"
			+ "  return;"
			+ "}");

		// let IJ.handleException override the macro interpreter's call()'s exception handling
		hacker.insertAtTopOfExceptionHandlers("ij.macro.Functions", "java.lang.String call()", "java.lang.reflect.InvocationTargetException",
			"ij.IJ.handleException($1);"
			+ "return null;");

		// Add back the "Convert to 8-bit Grayscale" checkbox to Import>Image Sequence
		if (!hacker.hasField("ij.plugin.FolderOpener", "convertToGrayscale")) {
			hacker.insertPrivateStaticField("ij.plugin.FolderOpener", Boolean.TYPE, "convertToGrayscale");
			hacker.replaceCallInMethod("ij.plugin.FolderOpener", "public void run(java.lang.String arg)", "ij.io.Opener", "openImage",
				"$_ = $0.openImage($1, $2);"
				+ "if (convertToGrayscale)"
				+ "  ij.IJ.run($_, \"8-bit\", \"\");");
			hacker.replaceCallInMethod("ij.plugin.FolderOpener", "boolean showDialog(ij.ImagePlus imp, java.lang.String[] list)",
				"ij.gui.GenericDialog", "addCheckbox",
				"i$0.addCheckbox(\"Convert to 8-bit Grayscale\", convertToGrayscale);"
				+ "$0.addCheckbox($1, $2);", 1);
			hacker.replaceCallInMethod("ij.plugin.FolderOpener", "boolean showDialog(ij.ImagePlus imp, java.lang.String[] list)",
					"ij.gui.GenericDialog", "getNextBoolean",
					"convertToGrayscale = $0.getNextBoolean();"
					+ "$_ = $0.getNextBoolean();"
					+ "if (convertToGrayscale && $_) {"
					+ "  ij.IJ.error(\"Cannot convert to grayscale and RGB at the same time.\");"
					+ "  return false;"
					+ "}", 1);
		}

		// handle HTTPS in addition to HTTP
		hacker.handleHTTPS("ij.macro.Functions", "java.lang.String exec()");
		hacker.handleHTTPS("ij.plugin.DragAndDrop", "public void drop(java.awt.dnd.DropTargetDropEvent dtde)");
		hacker.handleHTTPS(hacker.existsClass("ij.plugin.PluginInstaller") ? "ij.plugin.PluginInstaller" : "ij.io.PluginInstaller", "public boolean install(java.lang.String path)");
		hacker.handleHTTPS("ij.plugin.ListVirtualStack", "public void run(java.lang.String arg)");
		hacker.handleHTTPS("ij.plugin.ListVirtualStack", "java.lang.String[] open(java.lang.String path)");

		addEditorExtensionPoints(hacker);
		insertAppNameHooks(hacker);
	}

	private static void addEditorExtensionPoints(final CodeHacker hacker) {
		hacker.insertAtTopOfMethod("ij.io.Opener", "public void open(java.lang.String path)",
			"if (isText($1) && " + LegacyExtensions.class.getName() + ".openInLegacyEditor($1)) return;");
		hacker.dontReturnOnNull("ij.plugin.frame.Recorder", "void createMacro()");
		hacker.replaceCallInMethod("ij.plugin.frame.Recorder", "void createMacro()",
			"ij.plugin.frame.Editor", "runPlugIn",
			"$_ = null;");
		hacker.replaceCallInMethod("ij.plugin.frame.Recorder", "void createMacro()",
			"ij.plugin.frame.Editor", "createMacro",
			"if ($1.endsWith(\".txt\")) {"
			+ "  $1 = $1.substring($1.length() - 3) + \"ijm\";"
			+ "}"
			+ "if (!" + LegacyExtensions.class.getName() + ".createInLegacyEditor($1, $2)) {"
			+ "  ((ij.plugin.frame.Editor)ij.IJ.runPlugIn(\"ij.plugin.frame.Editor\", \"\")).createMacro($1, $2);"
			+ "}");
		hacker.insertPublicStaticField("ij.plugin.frame.Recorder", String.class, "nameForEditor", null);
		hacker.insertAtTopOfMethod("ij.plugin.frame.Recorder", "void createPlugin(java.lang.String text, java.lang.String name)",
			"this.nameForEditor = $2;");
		hacker.replaceCallInMethod("ij.plugin.frame.Recorder", "void createPlugin(java.lang.String text, java.lang.String name)",
			"ij.IJ", "runPlugIn",
			"$_ = null;"
			+ "new ij.plugin.NewPlugin().createPlugin(this.nameForEditor, ij.plugin.NewPlugin.PLUGIN, $2);"
			+ "return;");
		hacker.replaceCallInMethod("ij.plugin.NewPlugin", "public void createMacro(java.lang.String name)",
			"ij.plugin.frame.Editor", "<init>",
			"$_ = null;");
		hacker.replaceCallInMethod("ij.plugin.NewPlugin",  "public void createMacro(java.lang.String name)",
			"ij.plugin.frame.Editor", "create",
			"if ($1.endsWith(\".txt\")) {"
			+ "  $1 = $1.substring(0, $1.length() - 3) + \"ijm\";"
			+ "}"
			+ "if ($1.endsWith(\".ijm\") && " + LegacyExtensions.class.getName() + ".createInLegacyEditor($1, $2)) return;"
			+ "int options = (monospaced ? ij.plugin.frame.Editor.MONOSPACED : 0)"
			+ "  | (menuBar ? ij.plugin.frame.Editor.MENU_BAR : 0);"
			+ "new ij.plugin.frame.Editor(rows, columns, 0, options).create($1, $2);");
		hacker.dontReturnOnNull("ij.plugin.NewPlugin", "public void createPlugin(java.lang.String name, int type, java.lang.String methods)");
		hacker.replaceCallInMethod("ij.plugin.NewPlugin", "public void createPlugin(java.lang.String name, int type, java.lang.String methods)",
			"ij.IJ", "runPlugIn",
			"$_ = null;");
		hacker.replaceCallInMethod("ij.plugin.NewPlugin", "public void createPlugin(java.lang.String name, int type, java.lang.String methods)",
			"ij.plugin.frame.Editor", "create",
			"if (!" + LegacyExtensions.class.getName() + ".createInLegacyEditor($1, $2)) {"
			+ "  ((ij.plugin.frame.Editor)ij.IJ.runPlugIn(\"ij.plugin.frame.Editor\", \"\")).create($1, $2);"
			+ "}");
	}

	/**
	 * Inserts hooks to replace the application name.
	 */
	private static void insertAppNameHooks(final CodeHacker hacker) {
		final String appName = LegacyExtensions.class.getName() + ".getAppName()";
		hacker.insertAtTopOfMethod("ij.IJ", "public void error(java.lang.String title, java.lang.String msg)",
				"if ($1 == null || $1.equals(\"ImageJ\")) $1 = " + appName + ";");
		hacker.insertAtBottomOfMethod("ij.ImageJ", "public java.lang.String version()", "$_ = $_.replace(\"ImageJ\", " + appName + ");");
		hacker.replaceParameterInCall("ij.ImageJ", "public <init>(java.applet.Applet applet, int mode)", "super", 1, appName);
		hacker.replaceParameterInNew("ij.ImageJ", "public void run()", "ij.gui.GenericDialog", 1, appName);
		hacker.replaceParameterInCall("ij.ImageJ", "public void run()", "addMessage", 1, appName);
		hacker.replaceParameterInNew("ij.plugin.CommandFinder", "public void export()", "ij.text.TextWindow", 1, appName);
		hacker.replaceParameterInCall("ij.plugin.Hotkeys", "public void removeHotkey()", "addMessage", 1, appName);
		hacker.replaceParameterInCall("ij.plugin.Hotkeys", "public void removeHotkey()", "showStatus", 1, appName);
		hacker.replaceParameterInCall("ij.plugin.Options", "public void appearance()", "showMessage", 2, appName);
		hacker.replaceParameterInCall("ij.gui.YesNoCancelDialog", "public <init>(java.awt.Frame parent, java.lang.String title, java.lang.String msg)", "super", 2, appName);
		hacker.replaceParameterInCall("ij.gui.Toolbar", "private void showMessage(int toolId)", "showStatus", 1, appName);
	}

	private static void addIconHooks(final CodeHacker hacker) {
		final String icon = LegacyExtensions.class.getName() + ".getIconURL()";
		hacker.replaceCallInMethod("ij.ImageJ", "void setIcon()", "java.lang.Class", "getResource",
			"java.net.URL _iconURL = " + icon + ";\n" +
			"if (_iconURL == null) $_ = $0.getResource($1);" +
			"else $_ = _iconURL;");
		hacker.insertAtTopOfMethod("ij.ImageJ", "public <init>(java.applet.Applet applet, int mode)",
				"if ($2 != 2 /* ij.ImageJ.NO_SHOW */) setIcon();");
		hacker.insertAtTopOfMethod("ij.WindowManager", "public void addWindow(java.awt.Frame window)",
			"java.net.URL _iconURL = " + icon + ";\n"
			+ "if (_iconURL != null && $1 != null) {"
			+ "  java.awt.Image img = $1.createImage((java.awt.image.ImageProducer)_iconURL.getContent());"
			+ "  if (img != null) {"
			+ "    $1.setIconImage(img);"
			+ "  }"
			+ "}");
	}

	/**
	 * Makes sure that the legacy plugin class loader finds stuff in
	 * $HOME/.plugins/
	 * 
	 * @param directory
	 *            a directory where additional plugins can be found
	 */
	private static void addExtraPlugins(final CodeHacker hacker) {
		hacker.insertAtTopOfMethod("ij.io.PluginClassLoader", "void init(java.lang.String path)",
				extraPluginJarsHandler("addJAR(file);"));
		hacker.insertAtBottomOfMethod("ij.Menus",
				"public static synchronized java.lang.String[] getPlugins()",
				extraPluginJarsHandler("if (jarFiles == null) jarFiles = new java.util.Vector();" +
						"jarFiles.addElement(file.getAbsolutePath());"));
		// force IJ.getClassLoader() to instantiate a PluginClassLoader
		hacker.replaceCallInMethod(
				"ij.IJ",
				"public static ClassLoader getClassLoader()",
				"java.lang.System",
				"getProperty",
				"$_ = System.getProperty($1);\n"
						+ "if ($_ == null && $1.equals(\"plugins.dir\")) $_ = \"/non-existant/\";");
	}

	private static String extraPluginJarsHandler(final String code) {
		return "for (java.util.Iterator iter = " + LegacyExtensions.class.getName() + ".handleExtraPluginJars().iterator();\n" +
				"iter.hasNext(); ) {\n" +
				"\tjava.io.File file = (java.io.File)iter.next();\n" +
				code + "\n" +
				"}\n";
	}

}