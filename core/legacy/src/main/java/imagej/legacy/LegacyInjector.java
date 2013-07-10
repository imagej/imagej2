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

import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;
import java.net.URL;

import javassist.NotFoundException;
import javassist.bytecode.DuplicateMemberException;

import org.scijava.Context;
import org.scijava.util.ClassUtils;

/**
 * Overrides class behavior of ImageJ1 classes using bytecode manipulation. This
 * class uses the {@link CodeHacker} (which uses Javassist) to inject method
 * hooks, which are implemented in the {@link imagej.legacy.patches} package.
 * 
 * @author Curtis Rueden
 */
public class LegacyInjector {
	private CodeHacker hacker;

	/** Overrides class behavior of ImageJ1 classes by injecting method hooks. */
	public void injectHooks(final ClassLoader classLoader) {
		hacker = new CodeHacker(classLoader);
		injectHooks(hacker);
	}

	/** Overrides class behavior of ImageJ1 classes by injecting method hooks. */
	protected void injectHooks(final CodeHacker hacker) {
		// NB: Override class behavior before class loading gets too far along.

		if (GraphicsEnvironment.isHeadless()) {
			new LegacyHeadless(hacker).patch();
		}

		// override behavior of ij.ImageJ
		hacker.insertNewMethod("ij.ImageJ",
			"public java.awt.Point getLocationOnScreen()");
		hacker.insertAtTopOfMethod("ij.ImageJ",
			"public java.awt.Point getLocationOnScreen()",
			"if ($isLegacyMode()) return super.getLocationOnScreen();");
		hacker.insertAtTopOfMethod("ij.ImageJ", "public void quit()",
			"if (!($service instanceof imagej.legacy.DummyLegacyService)) $service.getContext().dispose();"
			+ "if (!$isLegacyMode()) return;");

		// override behavior of ij.IJ
		hacker.insertAtTopOfMethod("ij.IJ",
			"public static java.lang.Object runPlugIn(java.lang.String className, java.lang.String arg)",
			"if (\"MacAdapter\".equals(className)) return null;");
		hacker.insertAtBottomOfMethod("ij.IJ",
			"public static void showProgress(double progress)");
		hacker.insertAtBottomOfMethod("ij.IJ",
			"public static void showProgress(int currentIndex, int finalIndex)");
		hacker.insertAtBottomOfMethod("ij.IJ",
			"public static void showStatus(java.lang.String s)");
		hacker.insertPrivateStaticField("ij.IJ", Context.class, "_context");
		hacker.insertNewMethod("ij.IJ",
			"public synchronized static org.scijava.Context getContext()",
			"if (_context == null) _context = new org.scijava.Context();"
			+ "return _context;");
		hacker.insertAtTopOfMethod("ij.IJ",
				"public static Object runPlugIn(java.lang.String className, java.lang.String arg)",
				"if (\"" + LegacyService.class.getName() + "\".equals($1))"
				+ " return getLegacyService();"
				+ "if (\"" + Context.class.getName() + "\".equals($1))"
				+ " return getContext();");
		hacker.insertAtTopOfMethod("ij.IJ", "public static void log(java.lang.String message)");
		hacker.insertAtTopOfMethod("ij.IJ",
			"static java.lang.Object runUserPlugIn(java.lang.String commandName, java.lang.String className, java.lang.String arg, boolean createNewLoader)",
			"if (classLoader != null) Thread.currentThread().setContextClassLoader(classLoader);");

		// override behavior of ij.ImagePlus
		hacker.insertAtBottomOfMethod("ij.ImagePlus", "public void updateAndDraw()");
		hacker.insertAtBottomOfMethod("ij.ImagePlus", "public void repaintWindow()");
		hacker.insertAtBottomOfMethod("ij.ImagePlus",
			"public void show(java.lang.String statusMessage)");
		hacker.insertAtBottomOfMethod("ij.ImagePlus", "public void hide()");
		hacker.insertAtBottomOfMethod("ij.ImagePlus", "public void close()");

		// override behavior of ij.gui.ImageWindow
		hacker.insertNewMethod("ij.gui.ImageWindow",
			"public void setVisible(boolean vis)");
		hacker.insertAtTopOfMethod("ij.gui.ImageWindow",
			"public void setVisible(boolean vis)",
			"if ($isLegacyMode()) { super.setVisible($1); }");
		hacker.insertNewMethod("ij.gui.ImageWindow", "public void show()");
		hacker.insertAtTopOfMethod("ij.gui.ImageWindow",
			"public void show()",
			"if ($isLegacyMode()) { super.show(); }");
		hacker.insertAtTopOfMethod("ij.gui.ImageWindow", "public void close()");

		// override behavior of PluginClassLoader
		hacker.insertAtTopOfMethod("ij.io.PluginClassLoader", "void init(java.lang.String path)");

		// override behavior of ij.macro.Functions
		hacker
			.insertAtTopOfMethod("ij.macro.Functions",
				"void displayBatchModeImage(ij.ImagePlus imp2)",
				"imagej.legacy.patches.FunctionsMethods.displayBatchModeImageBefore($service, $1);");
		hacker
			.insertAtBottomOfMethod("ij.macro.Functions",
				"void displayBatchModeImage(ij.ImagePlus imp2)",
				"imagej.legacy.patches.FunctionsMethods.displayBatchModeImageAfter($service, $1);");

		// override behavior of MacAdapter, if needed
		if (ClassUtils.hasClass("com.apple.eawt.ApplicationListener")) {
			// NB: If com.apple.eawt package is present, override IJ1's MacAdapter.
			hacker.insertAtTopOfMethod("MacAdapter",
				"public void run(java.lang.String arg)",
				"if (!$isLegacyMode()) return;");
		}

		// override behavior of ij.plugin.frame.RoiManager
		hacker.insertNewMethod("ij.plugin.frame.RoiManager",
			"public void show()",
			"if ($isLegacyMode()) { super.show(); }");
		hacker.insertNewMethod("ij.plugin.frame.RoiManager",
			"public void setVisible(boolean b)",
			"if ($isLegacyMode()) { super.setVisible($1); }");

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
			if (cause != null && !(cause instanceof DuplicateMemberException)) {
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
				"if (" + IJ1Helper.class.getName() + ".handleNoSuchMethodError($e))"
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
			if (e.getCause() == null || !(e.getCause() instanceof NotFoundException)) {
				throw e;
			}
		}

		// make sure NonBlockingGenericDialog does not wait in macro mode
		hacker.replaceCallInMethod("ij.gui.NonBlockingGenericDialog", "public void showDialog()", "java.lang.Object", "wait", "if (isShowing()) wait();");

		// tell the showStatus() method to show the version() instead of empty status
		hacker.insertAtTopOfMethod("ij.ImageJ", "void showStatus(java.lang.String s)", "if ($1 == null || \"\".equals($1)) $1 = version();");

		// handle custom icon (e.g. for Fiji)
		if (!hacker.hasField("ij.IJ", "_iconURL")) { // Fiji will already have called CodeHacker#setIcon(File icon)
			hacker.insertPublicStaticField("ij.IJ", URL.class, "_iconURL", null);
		}
		hacker.replaceCallInMethod("ij.ImageJ", "void setIcon()", "java.lang.Class", "getResource",
			"if (ij.IJ._iconURL == null) $_ = $0.getResource($1);" +
			"else $_ = ij.IJ._iconURL;");
		hacker.insertAtTopOfMethod("ij.ImageJ", "public <init>(java.applet.Applet applet, int mode)",
				"if ($2 != 2 /* ij.ImageJ.NO_SHOW */) setIcon();");
		hacker.insertAtTopOfMethod("ij.WindowManager", "public void addWindow(java.awt.Frame window)",
			"if (ij.IJ._iconURL != null && $1 != null) {"
			+ "  java.awt.Image img = $1.createImage((java.awt.image.ImageProducer)ij.IJ._iconURL.getContent());"
			+ "  if (img != null) {"
			+ "    $1.setIconImage(img);"
			+ "  }"
			+ "}");

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

		// commit patches
		hacker.loadClasses();

		// make sure that there is a legacy service
		if (this.hacker != null) {
			setLegacyService(new DummyLegacyService());
		}
	}

	private static void addEditorExtensionPoints(final CodeHacker hacker) {
		hacker.insertAtTopOfMethod("ij.io.Opener", "public void open(java.lang.String path)",
			"if (isText($1) && " + IJ1Helper.class.getName() + ".openInLegacyEditor($1)) return;");
		hacker.dontReturnOnNull("ij.plugin.frame.Recorder", "void createMacro()");
		hacker.replaceCallInMethod("ij.plugin.frame.Recorder", "void createMacro()",
			"ij.plugin.frame.Editor", "runPlugIn",
			"$_ = null;");
		hacker.replaceCallInMethod("ij.plugin.frame.Recorder", "void createMacro()",
			"ij.plugin.frame.Editor", "createMacro",
			"if ($1.endsWith(\".txt\")) {"
			+ "  $1 = $1.substring($1.length() - 3) + \"ijm\";"
			+ "}"
			+ "if (!" + IJ1Helper.class.getName() + ".createInLegacyEditor($1, $2)) {"
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
			+ "if ($1.endsWith(\".ijm\") && " + IJ1Helper.class.getName() + ".createInLegacyEditor($1, $2)) return;"
			+ "int options = (monospaced ? ij.plugin.frame.Editor.MONOSPACED : 0)"
			+ "  | (menuBar ? ij.plugin.frame.Editor.MENU_BAR : 0);"
			+ "new ij.plugin.frame.Editor(rows, columns, 0, options).create($1, $2);");
		hacker.dontReturnOnNull("ij.plugin.NewPlugin", "public void createPlugin(java.lang.String name, int type, java.lang.String methods)");
		hacker.replaceCallInMethod("ij.plugin.NewPlugin", "public void createPlugin(java.lang.String name, int type, java.lang.String methods)",
			"ij.IJ", "runPlugIn",
			"$_ = null;");
		hacker.replaceCallInMethod("ij.plugin.NewPlugin", "public void createPlugin(java.lang.String name, int type, java.lang.String methods)",
			"ij.plugin.frame.Editor", "create",
			"if (!" + IJ1Helper.class.getName() + ".createInLegacyEditor($1, $2)) {"
			+ "  ((ij.plugin.frame.Editor)ij.IJ.runPlugIn(\"ij.plugin.frame.Editor\", \"\")).create($1, $2);"
			+ "}");
	}

	/**
	 * Inserts hooks to replace the application name.
	 */
	private void insertAppNameHooks(final CodeHacker hacker) {
		final String appName = IJ1Helper.class.getName() + ".getAppName()";
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

	/**
	 * Makes sure that the legacy plugin class loader finds stuff in
	 * $HOME/.plugins/
	 * 
	 * @param directory
	 *            a directory where additional plugins can be found
	 */
	private void addExtraPlugins(final CodeHacker hacker) {
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

	private String extraPluginJarsHandler(final String code) {
		return "for (java.util.Iterator iter = " + IJ1Helper.class.getName() + ".handleExtraPluginJars().iterator();\n" +
				"iter.hasNext(); ) {\n" +
				"\tjava.io.File file = (java.io.File)iter.next();\n" +
				code + "\n" +
				"}\n";
	}

	void setLegacyService(final LegacyService legacyService) {
		try {
			final Class<?> ij = hacker.classLoader.loadClass("ij.IJ");
			Field field = ij.getDeclaredField("_legacyService");
			field.setAccessible(true);
			field.set(null, legacyService);

			Context context;
			try {
				context = legacyService.getContext();
			} catch (UnsupportedOperationException e) {
				// DummyLegacyService does not have a context
				context = null;
			}
			field = ij.getDeclaredField("_context");
			field.setAccessible(true);
			field.set(null, context);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Cannot find ij.IJ", e);
		} catch (SecurityException e) {
			throw new IllegalArgumentException("Cannot find ij.IJ", e);
		} catch (NoSuchFieldException e) {
			throw new IllegalArgumentException("Cannot find field in ij.IJ", e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Cannot access field in ij.IJ", e);
		}
	}

}
