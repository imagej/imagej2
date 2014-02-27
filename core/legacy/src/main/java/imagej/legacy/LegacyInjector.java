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

package imagej.legacy;

import imagej.legacy.patches.EssentialLegacyHooks;
import imagej.legacy.patches.LegacyHooks;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;

import org.scijava.util.ClassUtils;

/**
 * Overrides class behavior of ImageJ1 classes using bytecode manipulation. This
 * class uses the {@link CodeHacker} (which uses Javassist) to inject method
 * hooks, which are implemented in the {@link imagej.legacy.patches} package.
 * 
 * @author Curtis Rueden
 */
public class LegacyInjector {

	/** Overrides class behavior of ImageJ1 classes by injecting method hooks. */
	public void injectHooks(final ClassLoader classLoader) {
		injectHooks(classLoader, GraphicsEnvironment.isHeadless());
	}

	/** Overrides class behavior of ImageJ1 classes by injecting method hooks. */
	public void injectHooks(final ClassLoader classLoader, boolean headless) {
		final CodeHacker hacker = new CodeHacker(classLoader);

		// NB: Override class behavior before class loading gets too far along.
		hacker.insertPublicStaticField("ij.IJ", LegacyHooks.class, "_hooks", null);

		if (headless) {
			new LegacyHeadless(hacker).patch();
		}

		// override behavior of ij.ImageJ
		hacker.insertAtTopOfMethod("ij.ImageJ", "public void quit()",
			"if (!ij.IJ._hooks.quit()) return;");

		// override behavior of ij.IJ
		hacker.insertAtBottomOfMethod("ij.IJ",
			"public static void showProgress(double progress)",
			"ij.IJ._hooks.showProgress($1);");
		hacker.insertAtBottomOfMethod("ij.IJ",
			"public static void showProgress(int currentIndex, int finalIndex)",
			"ij.IJ._hooks.showProgress($1, $2);");
		hacker.insertAtBottomOfMethod("ij.IJ",
			"public static void showStatus(java.lang.String status)",
			"ij.IJ._hooks.showStatus($1);");
		hacker.insertAtTopOfMethod("ij.IJ",
				"public static Object runPlugIn(java.lang.String className, java.lang.String arg)",
				" if (classLoader != null) Thread.currentThread().setContextClassLoader(classLoader);"
				+ "Object o = _hooks.interceptRunPlugIn($1, $2);"
				+ "if (o != null) return o;"
				+ "if (\"ij.IJ.init\".equals($1)) {"
				+ " ij.IJ.init();"
				+ " return null;"
				+ "}");
		hacker.insertAtTopOfMethod("ij.IJ",
				"public static void log(java.lang.String message)",
				"ij.IJ._hooks.log($1);");
		hacker.insertAtTopOfMethod("ij.IJ",
			"static java.lang.Object runUserPlugIn(java.lang.String commandName, java.lang.String className, java.lang.String arg, boolean createNewLoader)",
			"if (classLoader != null) Thread.currentThread().setContextClassLoader(classLoader);");

		// override behavior of ij.ImagePlus
		hacker.insertAtBottomOfMethod("ij.ImagePlus",
			"public void updateAndDraw()",
			"ij.IJ._hooks.registerImage(this);");
		hacker.insertAtBottomOfMethod("ij.ImagePlus",
			"public void repaintWindow()",
			"ij.IJ._hooks.registerImage(this);");
		hacker.insertAtBottomOfMethod("ij.ImagePlus",
			"public void show(java.lang.String statusMessage)",
			"ij.IJ._hooks.registerImage(this);");
		hacker.insertAtBottomOfMethod("ij.ImagePlus",
			"public void hide()",
			"ij.IJ._hooks.unregisterImage(this);");
		hacker.insertAtBottomOfMethod("ij.ImagePlus",
			"public void close()",
			"ij.IJ._hooks.unregisterImage(this);");

		// override behavior of ij.gui.ImageWindow
		hacker.insertNewMethod("ij.gui.ImageWindow",
			"public void setVisible(boolean vis)",
			"if ($1) ij.IJ._hooks.registerImage(this.getImagePlus());"
			+ "if (ij.IJ._hooks.isLegacyMode()) { super.setVisible($1); }");
		hacker.insertNewMethod("ij.gui.ImageWindow",
			"public void show()",
			"ij.IJ._hooks.registerImage(this.getImagePlus());"
			+ "if (ij.IJ._hooks.isLegacyMode()) { super.show(); }");
		hacker.insertAtTopOfMethod("ij.gui.ImageWindow",
			"public void close()",
			"ij.IJ._hooks.unregisterImage(this.getImagePlus());");

		// override behavior of PluginClassLoader
		hacker.insertNewMethod("ij.io.PluginClassLoader",
			"void addRecursively(java.io.File directory)",
			"java.io.File[] list = $1.listFiles();"
			+ "if (list == null) return;"
			+ "for (int i = 0; i < list.length; i++) {"
			+ "  java.io.File file = list[i];"
			+ "  if (file.isDirectory()) addRecursively(file);"
			+ "  else if (file.getName().endsWith(\".jar\")) addURL(file.toURI().toURL());"
			+ "}");
		hacker.insertAtTopOfMethod("ij.io.PluginClassLoader",
			"void init(java.lang.String path)",
			"java.io.File plugins = new java.io.File($1);"
			+ "if (plugins.getName().equals(\"plugins\")) {"
			+ "  java.io.File root = plugins.getParentFile();"
			+ "  if (root != null) addRecursively(new java.io.File(root, \"jars\"));"
			+ "}");

		// override behavior of MacAdapter, if needed
		if (ClassUtils.hasClass("com.apple.eawt.ApplicationListener")) {
			// NB: If com.apple.eawt package is present, override IJ1's MacAdapter.
			hacker.insertAtTopOfMethod("MacAdapter",
				"public void run(java.lang.String arg)",
				"if (!ij.IJ._hooks.isLegacyMode()) return;");
		}

		// override behavior of ij.plugin.frame.RoiManager
		hacker.insertNewMethod("ij.plugin.frame.RoiManager",
			"public void show()",
			"if (ij.IJ._hooks.isLegacyMode()) { super.show(); }");
		hacker.insertNewMethod("ij.plugin.frame.RoiManager",
			"public void setVisible(boolean b)",
			"if (ij.IJ._hooks.isLegacyMode()) { super.setVisible($1); }");

		LegacyExtensions.injectHooks(hacker);

		// commit patches
		hacker.loadClasses();

		// make sure that the legacy hooks are in place
		installHooks(classLoader, null);
	}

	public static void installHooks(final ClassLoader classLoader, LegacyHooks hooks) throws UnsupportedOperationException {
		if (hooks == null) hooks = new EssentialLegacyHooks();
		try {
			final Field hooksField = classLoader.loadClass("ij.IJ").getField("_hooks");
			final LegacyHooks previous = (LegacyHooks)hooksField.get(null);
			if (previous != null) {
				previous.dispose();
			}
			hooksField.set(null, hooks);
			hooks.installed();
		}
		catch (Throwable t) {
			throw new UnsupportedOperationException(t);
		}
	}

}
