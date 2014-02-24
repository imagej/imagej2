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

import ij.ImagePlus;
import imagej.data.display.ImageDisplay;
import imagej.legacy.patches.EssentialLegacyHooks;
import imagej.legacy.patches.LegacyHooks;

import java.io.BufferedWriter;
import java.util.Date;

import org.scijava.Context;

/**
 * The {@link LegacyHooks} encapsulating an active {@link LegacyService} for use within the patched ImageJ 1.x.
 * 
 * @author Johannes Schindelin
 */
public class DefaultLegacyHooks extends EssentialLegacyHooks {

	private LegacyService legacyService;

	public DefaultLegacyHooks(LegacyService legacyService) {
		this.legacyService = legacyService;
	}

	/** @inherit */
	@Override
	public boolean isLegacyMode() {
		return legacyService.isLegacyMode();
	}

	/** @inherit */
	@Override
	public Object getContext() {
		return legacyService.getContext();
	}

	/** @inherit */
	@Override
	public boolean quit() {
		dispose();
		return isLegacyMode();
	}

	/** @inherit */
	@Override
	public void dispose() {
		// TODO: if there are still things open, we should object.
	}

	/** @inherit */
	@Override
	public Object interceptRunPlugIn(String className, String arg) {
		if (LegacyService.class.getName().equals(className))
			return legacyService;
		if (Context.class.getName().equals(className))
			return legacyService == null ? null : legacyService.getContext();
		return null;
	}

	/** Resolution to use when converting double progress to int ratio. */
	private static final int PROGRESS_GRANULARITY = 1000;

	/** @inherit */
	@Override
	public void showProgress(double progress) {
		final int currentIndex = (int) (PROGRESS_GRANULARITY * progress);
		final int finalIndex = PROGRESS_GRANULARITY;
		showProgress(currentIndex, finalIndex);
	}

	/** @inherit */
	@Override
	public void showProgress(int currentIndex, int finalIndex) {
		if (!isLegacyMode()) {
			legacyService.status().showProgress(currentIndex, finalIndex);
		}
	}

	/** @inherit */
	@Override
	public void showStatus(final String status) {
		if (!isInitialized()) {
			return;
		}
		if (!isLegacyMode()) {
			legacyService.status().showStatus(status);
		}
	}

	/** @inherit */
	@Override
	public void registerLegacyImage(final ImagePlus image) {
		// TODO: avoid using ImagePlus here altogether; use ImgLib2-ij's wrap() instead
		if (image == null) return;
		if (!image.isProcessor()) return;
		if (image.getWindow() == null) return;
		if (!isLegacyMode()) {
			if (!Utils.isLegacyThread(Thread.currentThread())) return;
			legacyService.log().debug("register legacy image: " + image);
		}
		try {
			legacyService.getImageMap().registerLegacyImage(image);
		} catch (UnsupportedOperationException e) {
			// ignore: the dummy legacy service does not have an image map
		}
	}

	/** @inherit */
	@Override
	public void unregisterLegacyImage(final ImagePlus image) {
		// TODO: avoid using ImagePlus here altogether; use ImgLib2-ij's wrap() instead
		if (isLegacyMode()) return;
		if (image == null) return;
		if (!Utils.isLegacyThread(Thread.currentThread())) return;
		legacyService.log().debug("ImagePlus.hide(): " + image);
		LegacyOutputTracker.removeOutput(image);
		try {
			ImageDisplay disp = legacyService.getImageMap().lookupDisplay(image);
			if (disp == null) {
				legacyService.getImageMap().unregisterLegacyImage(image);
			}
			else {
				disp.close();
			}
		} catch (UnsupportedOperationException e) {
			// ignore: the dummy legacy service does not have an image map
		}
		// end alternate
	}

	/** @inherit */
	@Override
	public void debug(String string) {
		legacyService.log().debug(string);
	}

	/** @inherit */
	@Override
	public void error(Throwable t) {
		legacyService.log().error(t);
	}

	private boolean isInitialized() {
		return legacyService.isInitialized();
	}

	// if the ij.log.file property is set, log every message to the file pointed to
	private BufferedWriter logFileWriter;

	/** @inherit */
	@Override
	public void log(String message) {
		if (message != null) {
			String logFilePath = System.getProperty("ij.log.file");
			if (logFilePath != null) {
				try {
					if (logFileWriter == null) {
						java.io.OutputStream out = new java.io.FileOutputStream(
								logFilePath, true);
						java.io.Writer writer = new java.io.OutputStreamWriter(
								out, "UTF-8");
						logFileWriter = new java.io.BufferedWriter(writer);
						logFileWriter.write("Started new log on " + new Date() + "\n");
					}
					logFileWriter.write(message);
					if (!message.endsWith("\n"))
						logFileWriter.newLine();
					logFileWriter.flush();
				} catch (Throwable t) {
					t.printStackTrace();
					System.getProperties().remove("ij.log.file");
					logFileWriter = null;
				}
			}
		}
	}
}
