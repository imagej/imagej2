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

import ij.Executer;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageWindow;
import ij.io.Opener;
import imagej.data.display.ImageDisplay;
import imagej.platform.event.AppAboutEvent;
import imagej.platform.event.AppOpenFilesEvent;
import imagej.platform.event.AppPreferencesEvent;
import imagej.platform.event.AppQuitEvent;
import imagej.platform.event.ApplicationEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.scijava.AbstractContextual;
import org.scijava.Context;
import org.scijava.event.EventHandler;

/**
 * A helper class to interact with ImageJ 1.x.
 * 
 * The DefaultLegacyService needs to patch ImageJ 1.x' classes before they
 * are loaded. Unfortunately, this is tricky: if the DefaultLegacyService
 * already uses those classes, it is a matter of luck whether we can get
 * the patches in before those classes are loaded.
 * 
 * Therefore, we put as much interaction with ImageJ 1.x as possible into
 * this class and keep a reference to it in the DefaultLegacyService.
 * 
 * @author Johannes Schindelin
 */
public class IJ1Helper {

	/** A reference to the legacy service, just in case we need it */
	private final DefaultLegacyService legacyService;

	public IJ1Helper(final DefaultLegacyService legacyService) {
		this.legacyService = legacyService;
	}

	public void initialize() {
		// initialize legacy ImageJ application
		if (IJ.getInstance() == null) try {
			new ImageJ(ImageJ.NO_SHOW);
		}
		catch (final Throwable t) {
			legacyService.getLogService().warn("Failed to instantiate IJ1.", t);
		} else {
			final LegacyImageMap imageMap = legacyService.getImageMap();
			for (int i = 1; i <= WindowManager.getImageCount(); i++) {
				imageMap.registerLegacyImage(WindowManager.getImage(i));
			}
		}
	}

	public void dispose() {
		final ImageJ ij = IJ.getInstance();
		if (ij != null) {
			// close out all image windows, without dialog prompts
			while (true) {
				final ImagePlus imp = WindowManager.getCurrentImage();
				if (imp == null) break;
				imp.changes = false;
				imp.close();
			}

			// close any remaining (non-image) windows
			WindowManager.closeAllWindows();

			// quit legacy ImageJ on the same thread
			ij.run();
		}
	}

	public void setVisible(boolean toggle) {
		final ImageJ ij = IJ.getInstance();
		if (ij != null) {
			if (toggle) ij.pack();
			ij.setVisible(toggle);
		}

		// hide/show the legacy ImagePlus instances
		final LegacyImageMap imageMap = legacyService.getImageMap();
		for (final ImagePlus imp : imageMap.getImagePlusInstances()) {
			final ImageWindow window = imp.getWindow();
			if (window != null) window.setVisible(toggle);
		}
	}

	public void syncActiveImage(final ImageDisplay activeDisplay) {
		final LegacyImageMap imageMap = legacyService.getImageMap();
		final ImagePlus activeImagePlus = imageMap.lookupImagePlus(activeDisplay);
		// NB - old way - caused probs with 3d Project
		// WindowManager.setTempCurrentImage(activeImagePlus);
		// NB - new way - test thoroughly
		if (activeImagePlus == null) WindowManager.setCurrentWindow(null);
		else WindowManager.setCurrentWindow(activeImagePlus.getWindow());
	}

	public void setKeyDown(int keyCode) {
		IJ.setKeyDown(keyCode);
	}

	public void setKeyUp(int keyCode) {
		IJ.setKeyUp(keyCode);
	}

	public boolean hasInstance() {
		return IJ.getInstance() != null;
	}

	public String getVersion() {
		return IJ.getVersion();
	}

	public boolean isMacintosh() {
		return IJ.isMacintosh();
	}

	/**
	 * Delegator for {@link IJ#getClassLoader()}.
	 * 
	 * <p>
	 * This method allows the {@link LegacyExtensions} class to be loaded
	 * without loading any of ImageJ 1.x.
	 * </p>
	 * 
	 * @return ImageJ 1.x' current plugin class loader
	 */
	public static ClassLoader getClassLoader() {
		return IJ.getClassLoader();
	}

	/**
	 * Delegator for {@link IJ#log(String)}.
	 * 
	 * <p>
	 * This method allows the {@link LegacyExtensions} class to be loaded
	 * without loading any of ImageJ 1.x.
	 * </p>
	 */
	public static void log(final String message) {
		IJ.log(message);
	}

	/**
	 * Delegator for {@link IJ#error(String)}.
	 * 
	 * <p>
	 * This method allows the {@link LegacyExtensions} class to be loaded
	 * without loading any of ImageJ 1.x.
	 * </p>
	 */
	public static void error(final String message) {
		IJ.log(message);
	}

	private static class LegacyEventDelegator extends AbstractContextual {

		// -- MacAdapter re-implementations --

		@EventHandler
		protected void onEvent(final AppAboutEvent event) {
			if (isLegacyMode(event)) {
				IJ.run("About ImageJ...");
			}
		}

		@EventHandler
		protected void onEvent(final AppOpenFilesEvent event) {
			if (isLegacyMode(event)) {
				final List<File> files = new ArrayList<File>(event.getFiles());
				for (final File file : files) {
					new Opener().openAndAddToRecent(file.getAbsolutePath());
				}
			}
		}

		@EventHandler
		protected void onEvent(final AppQuitEvent event) {
			if (isLegacyMode(event)) {
				new Executer("Quit", null); // works with the CommandListener
			}
		}

		@EventHandler
		protected void onEvent(final AppPreferencesEvent event) {
			if (isLegacyMode(event)) {
				IJ.error("The ImageJ preferences are in the Edit>Options menu.");
			}
		}

		private static boolean isLegacyMode(final ApplicationEvent event) {
			final LegacyService legacyService = event.getContext().getService(LegacyService.class);
			return legacyService != null && legacyService.isLegacyMode();
		}

	}

	private static LegacyEventDelegator eventDelegator;

	public static void subscribeEvents(final Context context) {
		if (context == null) {
			eventDelegator = null;
		} else {
			eventDelegator = new LegacyEventDelegator();
			eventDelegator.setContext(context);
		}
	}

}
