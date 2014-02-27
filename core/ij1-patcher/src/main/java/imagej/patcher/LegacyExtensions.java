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

package imagej.patcher;


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
				"if (ij.IJ._hooks.handleNoSuchMethodError($e))"
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
		if (hacker.hasMethod("ij.gui.GenericDialog", "public void showDialog()")) {
			hacker.insertAtTopOfMethod("ij.gui.GenericDialog", "public void showDialog()", "if (macro) dispose();");
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
				"ij.plugin.FolderOpener$FolderOpenerDialog", "addCheckbox",
				"$0.addCheckbox(\"Convert to 8-bit Grayscale\", convertToGrayscale);"
				+ "$0.addCheckbox($1, $2);", 1);
			hacker.replaceCallInMethod("ij.plugin.FolderOpener", "boolean showDialog(ij.ImagePlus imp, java.lang.String[] list)",
					"ij.plugin.FolderOpener$FolderOpenerDialog", "getNextBoolean",
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
		insertRefreshMenusHook(hacker);
		overrideStartupMacrosForFiji(hacker);
		handleMacAdapter(hacker);
	}

	/**
	 * Install a hook to optionally run a Runnable at the end of Help>Refresh Menus.
	 * 
	 * <p>
	 * See {@link LegacyExtensions#runAfterRefreshMenus(Runnable)}.
	 * </p>
	 * 
	 * @param hacker the {@link CodeHacker} to use for patching
	 */
	private static void insertRefreshMenusHook(CodeHacker hacker) {
		hacker.insertAtBottomOfMethod("ij.Menus", "public static void updateImageJMenus()",
			"ij.IJ._hooks.runAfterRefreshMenus();");
	}

	private static void addEditorExtensionPoints(final CodeHacker hacker) {
		hacker.insertAtTopOfMethod("ij.io.Opener", "public void open(java.lang.String path)",
			"if (isText($1) && ij.IJ._hooks.openInEditor($1)) return;");
		hacker.dontReturnOnNull("ij.plugin.frame.Recorder", "void createMacro()");
		hacker.replaceCallInMethod("ij.plugin.frame.Recorder", "void createMacro()",
			"ij.IJ", "runPlugIn",
			"$_ = null;");
		hacker.replaceCallInMethod("ij.plugin.frame.Recorder", "void createMacro()",
			"ij.plugin.frame.Editor", "createMacro",
			"if ($1.endsWith(\".txt\")) {"
			+ "  $1 = $1.substring($1.length() - 3) + \"ijm\";"
			+ "}"
			+ "if (!ij.IJ._hooks.createInEditor($1, $2)) {"
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
			+ "if ($1.endsWith(\".ijm\") && ij.IJ._hooks.createInEditor($1, $2)) return;"
			+ "int options = (monospaced ? ij.plugin.frame.Editor.MONOSPACED : 0)"
			+ "  | (menuBar ? ij.plugin.frame.Editor.MENU_BAR : 0);"
			+ "new ij.plugin.frame.Editor(rows, columns, 0, options).create($1, $2);");
		hacker.dontReturnOnNull("ij.plugin.NewPlugin", "public void createPlugin(java.lang.String name, int type, java.lang.String methods)");
		hacker.replaceCallInMethod("ij.plugin.NewPlugin", "public void createPlugin(java.lang.String name, int type, java.lang.String methods)",
			"ij.IJ", "runPlugIn",
			"$_ = null;");
		hacker.replaceCallInMethod("ij.plugin.NewPlugin", "public void createPlugin(java.lang.String name, int type, java.lang.String methods)",
			"ij.plugin.frame.Editor", "create",
			"if (!ij.IJ._hooks.createInEditor($1, $2)) {"
			+ "  ((ij.plugin.frame.Editor)ij.IJ.runPlugIn(\"ij.plugin.frame.Editor\", \"\")).create($1, $2);"
			+ "}");
	}

	/**
	 * Inserts hooks to replace the application name.
	 */
	private static void insertAppNameHooks(final CodeHacker hacker) {
		final String appName = "ij.IJ._hooks.getAppName()";
		final String replace = ".replace(\"ImageJ\", " + appName + ")";
		hacker.insertAtTopOfMethod("ij.IJ", "public void error(java.lang.String title, java.lang.String msg)",
				"if ($1 == null || $1.equals(\"ImageJ\")) $1 = " + appName + ";");
		hacker.insertAtBottomOfMethod("ij.ImageJ", "public java.lang.String version()", "$_ = $_" + replace + ";");
		hacker.replaceAppNameInCall("ij.ImageJ", "public <init>(java.applet.Applet applet, int mode)", "super", 1, appName);
		hacker.replaceAppNameInNew("ij.ImageJ", "public void run()", "ij.gui.GenericDialog", 1, appName);
		hacker.replaceAppNameInCall("ij.ImageJ", "public void run()", "addMessage", 1, appName);
		if (hacker.hasMethod("ij.plugin.CommandFinder", "public void export()")) {
			hacker.replaceAppNameInNew("ij.plugin.CommandFinder", "public void export()", "ij.text.TextWindow", 1, appName);
		}
		hacker.replaceAppNameInCall("ij.plugin.Hotkeys", "public void removeHotkey()", "addMessage", 1, appName);
		hacker.replaceAppNameInCall("ij.plugin.Hotkeys", "public void removeHotkey()", "showStatus", 1, appName);
		if (hacker.existsClass("ij.plugin.AppearanceOptions")) {
			hacker.replaceAppNameInCall("ij.plugin.AppearanceOptions", "void showDialog()", "showMessage", 2, appName);
		} else {
			hacker.replaceAppNameInCall("ij.plugin.Options", "public void appearance()", "showMessage", 2, appName);
		}
		hacker.replaceAppNameInCall("ij.gui.YesNoCancelDialog", "public <init>(java.awt.Frame parent, java.lang.String title, java.lang.String msg)", "super", 2, appName);
		hacker.replaceAppNameInCall("ij.gui.Toolbar", "private void showMessage(int toolId)", "showStatus", 1, appName);
	}

	private static void addIconHooks(final CodeHacker hacker) {
		final String icon = "ij.IJ._hooks.getIconURL()";
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
	 * {@code $HOME/.plugins/}.
	 */
	private static void addExtraPlugins(final CodeHacker hacker) {
		for (final String methodName : new String[] { "addJAR", "addJar" }) {
			if (hacker.hasMethod("ij.io.PluginClassLoader", "private void "
					+ methodName + "(java.io.File file)")) {
				hacker.insertAtTopOfMethod("ij.io.PluginClassLoader",
						"void init(java.lang.String path)",
						extraPluginJarsHandler(methodName + "(file);"));
			}
		}
		// add the extra .jar files to the list of plugin .jar files to be processed.
		hacker.insertAtBottomOfMethod("ij.Menus",
				"public static synchronized java.lang.String[] getPlugins()",
				extraPluginJarsHandler("if (jarFiles == null) jarFiles = new java.util.Vector();" +
						"jarFiles.addElement(file.getAbsolutePath());"));
		// exclude -sources.jar entries generated by Maven.
		hacker.insertAtBottomOfMethod("ij.Menus",
				"public static synchronized java.lang.String[] getPlugins()",
				"if (jarFiles != null) {" +
				"  for (int i = jarFiles.size() - 1; i >= 0; i--) {" +
				"    String entry = (String) jarFiles.elementAt(i);" +
				"    if (entry.endsWith(\"-sources.jar\")) {" +
				"      jarFiles.remove(i);" +
				"    }" +
				"  }" +
				"}");
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
		return "for (java.util.Iterator iter = ij.IJ._hooks.handleExtraPluginJars().iterator();\n" +
				"iter.hasNext(); ) {\n" +
				"\tjava.io.File file = (java.io.File)iter.next();\n" +
				code + "\n" +
				"}\n";
	}

	private static void overrideStartupMacrosForFiji(CodeHacker hacker) {
		hacker.replaceCallInMethod("ij.Menus", "void installStartupMacroSet()", "java.io.File", "<init>",
				"if ($1.endsWith(\"StartupMacros.txt\")) {" +
				" java.lang.String fijiPath = $1.substring(0, $1.length() - 3) + \"fiji.ijm\";" +
				" java.io.File fijiFile = new java.io.File(fijiPath);" +
				" $_ = fijiFile.exists() ? fijiFile : new java.io.File($1);" +
				"} else $_ = new java.io.File($1);");
		hacker.replaceCallInMethod("ij.Menus", "void installStartupMacroSet()", "ij.plugin.MacroInstaller", "installFile",
				"if ($1.endsWith(\"StartupMacros.txt\")) {" +
				" java.lang.String fijiPath = $1.substring(0, $1.length() - 3) + \"fiji.ijm\";" +
				" java.io.File fijiFile = new java.io.File(fijiPath);" +
				" $0.installFile(fijiFile.exists() ? fijiFile.getPath() : $1);" +
				"} else $0.installFile($1);");
	}

	private static void handleMacAdapter(final CodeHacker hacker) {
		// Without the ApplicationListener, MacAdapter cannot load, and hence CodeHacker would fail
		// to load it if we patched the class.
		if (!hacker.existsClass("com.apple.eawt.ApplicationListener")) return;

		hacker.insertAtTopOfMethod("MacAdapter", "public void run(java.lang.String arg)",
			"return;");
	}

}
