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
				+ " return getContext();"
				+ "if (\"ij.IJ.init\".equals($1)) {"
				+ " ij.IJ.init();"
				+ " return null;"
				+ "}");
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

		LegacyExtensions.injectHooks(hacker);

		// commit patches
		hacker.loadClasses();

		// make sure that there is a legacy service
		if (this.hacker != null) {
			setLegacyService(new DummyLegacyService());
		}
	}

	void setLegacyService(final LegacyService legacyService) {
		Context context;
		try {
			context = legacyService.getContext();
		} catch (UnsupportedOperationException e) {
			// DummyLegacyService does not have a context
			context = null;
		}

		try {
			final Class<?> ij = hacker.classLoader.loadClass("ij.IJ");
			Field field = ij.getDeclaredField("_legacyService");
			field.setAccessible(true);
			field.set(null, legacyService);

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

		IJ1Helper.subscribeEvents(context);
	}

}
