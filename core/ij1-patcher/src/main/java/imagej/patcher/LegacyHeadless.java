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
 * Limited headless support for ImageJ 1.x.
 * 
 * <p>
 * <i>Headless operation</i> means: does not require a graphical user interface
 * (GUI). Due to some limitations in Java on Linux and Unix, it is impossible to
 * instantiate GUI elements when there is no way to display them (i.e. when
 * there is no desktop environment) -- even if they are never to be displayed.
 * </p>
 * 
 * <p>
 * Therefore, even when running in batch mode, we have to prevent GUI elements
 * -- such as dialogs -- to be instantiated.
 * </p>
 * 
 * <p>
 * Unfortunately, ImageJ 1.x' macro handling works exclusively by instantiating
 * dialogs (but not displaying them). Macros are executed by overriding the
 * dialog methods to extract the user-specified values.
 * </p>
 * 
 * <p>
 * The limited legacy headless support overrides {@link ij.gui.GenericDialog} to
 * <b>not</b> be a subclass of {@link java.awt.Dialog}. This will always be a
 * fragile solution, especially with plugins trying to add additional GUI
 * elements to the dialog: when those GUI elements are instantiated, Java will
 * throw a {@link java.awt.HeadlessException}. Hence the legacy headless support
 * only works with standard (i.e. non-fancy) plugins; this could only be fixed
 * by overriding the plugin class loader with a version inspecting every plugin
 * class and using Javassist to override such instantiations. Given that
 * ImageJ2's architecture handles headless operation much more gracefully, that
 * enormous effort would have little to gain.
 * </p>
 * 
 * @author Johannes Schindelin
 */
public class LegacyHeadless  {

	private final CodeHacker hacker;

	public LegacyHeadless(final CodeHacker hacker) {
		this.hacker = hacker;
	}

	public void patch() {
		if (hacker.hasSuperclass("ij.gui.GenericDialog", HeadlessGenericDialog.class.getName())) {
			// if we already applied the headless patches, let's not do it again
			return;
		}
		hacker.commitClass(HeadlessGenericDialog.class);
		hacker.replaceWithStubMethods("ij.gui.GenericDialog", "paint", "getInsets", "showHelp");
		hacker.replaceSuperclass("ij.gui.GenericDialog", HeadlessGenericDialog.class.getName());
		hacker.skipAWTInstantiations("ij.gui.GenericDialog");

		hacker.insertAtTopOfMethod("ij.Menus", "void installJarPlugin(java.lang.String jarName, java.lang.String pluginsConfigLine)",
			"int quote = $2.indexOf('\"');"
			+ "if (quote >= 0)"
			+ "  addPluginItem(null, $2.substring(quote));");
		hacker.skipAWTInstantiations("ij.Menus");

		hacker.skipAWTInstantiations("ij.plugin.HyperStackConverter");

		hacker.skipAWTInstantiations("ij.plugin.Duplicator");

		hacker.insertAtTopOfMethod("ij.plugin.filter.ScaleDialog",
			"java.awt.Panel makeButtonPanel(ij.plugin.filter.SetScaleDialog gd)",
			"return null;");
	}

}
