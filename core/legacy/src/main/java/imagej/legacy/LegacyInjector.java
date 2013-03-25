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
		// NB: Override class behavior before class loading gets too far along.
		hacker = new CodeHacker(classLoader);

		// override behavior of ij.ImageJ
		hacker.insertMethod("ij.ImageJ",
			"public java.awt.Point getLocationOnScreen()");
		hacker.insertBeforeMethod("ij.ImageJ",
			"public java.awt.Point getLocationOnScreen()",
			"if ($isLegacyMode()) return super.getLocationOnScreen();");
		hacker.loadClass("ij.ImageJ");

		// override behavior of ij.IJ
		hacker.insertAfterMethod("ij.IJ",
			"public static void showProgress(double progress)");
		hacker.insertAfterMethod("ij.IJ",
			"public static void showProgress(int currentIndex, int finalIndex)");
		hacker.insertAfterMethod("ij.IJ",
			"public static void showStatus(java.lang.String s)");
		hacker.loadClass("ij.IJ");

		// override behavior of ij.ImagePlus
		hacker.insertAfterMethod("ij.ImagePlus", "public void updateAndDraw()");
		hacker.insertAfterMethod("ij.ImagePlus", "public void repaintWindow()");
		hacker.insertAfterMethod("ij.ImagePlus",
			"public void show(java.lang.String statusMessage)");
		hacker.insertAfterMethod("ij.ImagePlus", "public void hide()");
		hacker.insertAfterMethod("ij.ImagePlus", "public void close()");
		hacker.loadClass("ij.ImagePlus");

		// override behavior of ij.gui.ImageWindow
		hacker.insertMethod("ij.gui.ImageWindow",
			"public void setVisible(boolean vis)");
		hacker.insertBeforeMethod("ij.gui.ImageWindow",
			"public void setVisible(boolean vis)",
			"if ($isLegacyMode()) { super.setVisible($1); }");
		hacker.insertMethod("ij.gui.ImageWindow", "public void show()");
		hacker.insertBeforeMethod("ij.gui.ImageWindow",
			"public void show()",
			"if ($isLegacyMode()) { super.show(); }");
		hacker.insertBeforeMethod("ij.gui.ImageWindow", "public void close()");
		hacker.loadClass("ij.gui.ImageWindow");

		// override behavior of PluginClassLoader
		hacker.insertBeforeMethod("ij.io.PluginClassLoader", "void init(java.lang.String path)");
		hacker.loadClass("ij.io.PluginClassLoader");

		// override behavior of ij.macro.Functions
		hacker
			.insertBeforeMethod("ij.macro.Functions",
				"void displayBatchModeImage(ij.ImagePlus imp2)",
				"imagej.legacy.patches.FunctionsMethods.displayBatchModeImageBefore($service, $1);");
		hacker
			.insertAfterMethod("ij.macro.Functions",
				"void displayBatchModeImage(ij.ImagePlus imp2)",
				"imagej.legacy.patches.FunctionsMethods.displayBatchModeImageAfter($service, $1);");
		hacker.loadClass("ij.macro.Functions");

		// override behavior of MacAdapter, if needed
		if (ClassUtils.hasClass("com.apple.eawt.ApplicationListener")) {
			// NB: If com.apple.eawt package is present, override IJ1's MacAdapter.
			hacker.insertBeforeMethod("MacAdapter",
				"public void run(java.lang.String arg)",
				"if (!$isLegacyMode()) return;");
			hacker.loadClass("MacAdapter");
		}

		// override behavior of ij.plugin.frame.RoiManager
		hacker
			.insertMethod("ij.plugin.frame.RoiManager", "public void show()", ";");
		hacker.insertMethod("ij.plugin.frame.RoiManager",
			"public void setVisible(boolean b)", ";");
		hacker.loadClass("ij.plugin.frame.RoiManager");

		// make sure that there is a legacy service
		setLegacyService(new DummyLegacyService());
	}

	void setLegacyService(final LegacyService legacyService) {
		hacker.setLegacyService(legacyService);
	}
}
