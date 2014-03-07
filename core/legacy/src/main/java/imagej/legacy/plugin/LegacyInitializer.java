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

package imagej.legacy.plugin;

import ij.IJ;
import ij.ImageJ;
import imagej.patcher.LegacyHooks;

import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.ImageProducer;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import javax.swing.SwingUtilities;

import org.scijava.Context;

/**
 * The <i>ij1-patcher</i> defaults to running this class whenever a new
 * {@code PluginClassLoader} is initialized.
 * 
 * @author Johannes Schindelin
 */
public class LegacyInitializer implements Runnable {

	@Override
	public void run() {
		final ClassLoader loader = IJ.getClassLoader();
		Thread.currentThread().setContextClassLoader(loader);
		if (!GraphicsEnvironment.isHeadless() &&
			!SwingUtilities.isEventDispatchThread()) try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					Thread.currentThread().setContextClassLoader(loader);
				}
			});
		}
		catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			// ignore
		}

		try {
			/*
			 * Instantiate a Context if there is none; IJ.runPlugIn() will be intercepted
			 * by the legacy hooks if they are installed and return the current Context.
			 * If no legacy hooks are installed, ImageJ 1.x will instantiate the Context using
			 * the PluginClassLoader and the LegacyService will install the legacy hooks. 
			 */
			IJ.runPlugIn(Context.class.getName(), null);
		} catch (Throwable t) {
			// do nothing; we're not in the PluginClassLoader's class path
			return;
		}

		// make sure that the Event Dispatch Thread's class loader is set
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setContextClassLoader(IJ.getClassLoader());
			}
		});

		// set icon and title of main window (which are instantiated before the initializer is called)
		final ImageJ ij = IJ.getInstance();
		if (ij != null) try {
			final LegacyHooks hooks = (LegacyHooks) IJ.class.getField("_hooks").get(null);
			ij.setTitle(hooks.getAppName());
			final URL iconURL = hooks.getIconURL();
			if (iconURL != null) try {
				Object producer = iconURL.getContent();
				Image image = ij.createImage((ImageProducer)producer);
				ij.setIconImage(image);
				if (IJ.isMacOSX()) try {
					// NB: We also need to set the dock icon
					final Class<?> clazz = Class.forName("com.apple.eawt.Application");
					final Object app = clazz.getMethod("getApplication").invoke(null);
					clazz.getMethod("setDockIconImage", Image.class).invoke(app, image);
				} catch (Throwable t) {
					t.printStackTrace();
				}
			} catch (IOException e) {
				IJ.handleException(e);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

}
