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

package net.imagej.legacy;

import net.imagej.ImageJ;

import org.scijava.Context;

/**
 * A utility class providing a static entry point into the ImageJ2 API, for use
 * by legacy ImageJ 1.x code transitioning to ImageJ2.
 * 
 * @author Curtis Rueden
 */
public final class IJ2 {

	private IJ2() {
		// prevent instantiation of utility class
	}

	/** Cached ImageJ application gateway. */
	private static ImageJ ij;

	/**
	 * Gets an ImageJ gateway for accessing the ImageJ2 API. <strong>
	 * <em>This method is only intended for use from ImageJ 1.x plugins.</em>
	 * </strong>
	 * <p>
	 * The normal and better way to access ImageJ2 services is to use the
	 * {@code @Parameter} annotation on fields, and the values will be injected
	 * appropriately. For example:
	 * </p>
	 * 
	 * <pre>
	 * import net.imagej.command.Command;
	 * import org.scijava.plugin.Parameter;
	 * import org.scijava.plugin.Plugin;
	 * import org.scijava.status.StatusService;
	 * import org.scijava.ui.UIService;
	 * 
	 * &#064;Plugin(type = Command.class)
	 * public class MyCommand implements Command {
	 * 
	 * 	&#064;Parameter
	 * 	private StatusService statusService;
	 * 
	 * 	&#064;Parameter
	 * 	private UIService uiService;
	 * 
	 * 	&#064;Override
	 * 	public void run() {
	 * 		statusService.showStatus(&quot;You can write to the status bar!&quot;);
	 * 		uiService.showDialog(&quot;You can pop up a dialog!&quot;);
	 * 	}
	 * 
	 * }
	 * </pre>
	 */
	public static ImageJ getInstance() {
		final Context context = IJ1Helper.getLegacyContext();
		if (context == null) return null;
		if (ij == null || ij.getContext() != context) {
			ij = new ImageJ(context);
		}
		return ij;
	}

}
