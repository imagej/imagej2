/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
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

package imagej.legacy.patches;

import ij.ImagePlus;
import imagej.ImageJ;
import imagej.legacy.LegacyOutputTracker;
import imagej.legacy.LegacyService;
import imagej.util.Log;

/**
 * Overrides {@link ImagePlus} methods.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public final class ImagePlusMethods {

	private ImagePlusMethods() {
		// prevent instantiation of utility class
	}

	/** Appends {@link ImagePlus#updateAndDraw()}. */
	public static void updateAndDraw(final ImagePlus obj) {
		Log.debug("ImagePlus.updateAndDraw(): " + obj);
		final LegacyService legacyService = ImageJ.get(LegacyService.class);
		legacyService.legacyImageChanged(obj);
	}

	/** Appends {@link ImagePlus#repaintWindow()}. */
	public static void repaintWindow(final ImagePlus obj) {
		Log.debug("ImagePlus.repaintWindow(): " + obj);
		final LegacyService legacyService = ImageJ.get(LegacyService.class);
		legacyService.legacyImageChanged(obj);
	}

	/** Appends {@link ImagePlus#show(String message)}. */
	public static void show(final ImagePlus obj,
		@SuppressWarnings("unused") final String message)
	{
		Log.debug("ImagePlus.show(): " + obj);
		final LegacyService legacyService = ImageJ.get(LegacyService.class);
		legacyService.legacyImageChanged(obj);
	}

	/** Appends {@link ImagePlus#hide()}. */
	public static void hide(final ImagePlus obj) {
		Log.debug("ImagePlus.hide(): " + obj);
		LegacyOutputTracker.getOutputImps().remove(obj);
		LegacyOutputTracker.getClosedImps().add(obj);
	}

	/** Appends {@link ImagePlus#close()}. */
	public static void close(final ImagePlus obj) {
		if ((obj != null) && (!LegacyOutputTracker.isBeingClosedbyIJ2(obj))) LegacyOutputTracker
			.getClosedImps().add(obj);
	}
}
