//
// LegacyInjector.java
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

package imagej.legacy;

/**
 * Overrides class behavior of ImageJ1 classes using bytecode manipulation. This
 * class uses the {@link CodeHacker} (which uses Javassist) to inject method
 * hooks, which are implemented in the {@link imagej.legacy.patches} package.
 * 
 * @author Curtis Rueden
 */
public class LegacyInjector {

	/** Overrides class behavior of ImageJ1 classes by injecting method hooks. */
	public void injectHooks() {
		// NB: Override class behavior before class loading gets too far along.
		final CodeHacker hacker = new CodeHacker();

		// override behavior of ij.ImageJ
		hacker.insertMethod("ij.ImageJ",
			"public java.awt.Point getLocationOnScreen()");
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
		hacker.loadClass("ij.ImagePlus");

		// override behavior of ij.gui.ImageWindow
		hacker.insertMethod("ij.gui.ImageWindow",
			"public void setVisible(boolean vis)");
		hacker.insertMethod("ij.gui.ImageWindow", "public void show()");
		hacker.insertBeforeMethod("ij.gui.ImageWindow", "public void close()");
		hacker.loadClass("ij.gui.ImageWindow");

		// override behavior of ij.macro.Functions
		hacker
			.insertBeforeMethod("ij.macro.Functions",
				"void displayBatchModeImage(ij.ImagePlus imp2)",
				"imagej.legacy.patches.FunctionsMethods.displayBatchModeImageBefore(imp2);");
		hacker
			.insertAfterMethod("ij.macro.Functions",
				"void displayBatchModeImage(ij.ImagePlus imp2)",
				"imagej.legacy.patches.FunctionsMethods.displayBatchModeImageAfter(imp2);");
		hacker.loadClass("ij.macro.Functions");

		// override behavior of MacAdapter
		hacker.replaceMethod("MacAdapter",
			"public void run(java.lang.String arg)", ";");
		hacker.loadClass("MacAdapter");
	}

}
