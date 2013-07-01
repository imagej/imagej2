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
		hacker.addExtraPlugins();

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

		// commit patches
		hacker.loadClasses();

		// make sure that there is a legacy service
		if (this.hacker != null) {
			setLegacyService(new DummyLegacyService());
		}
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
