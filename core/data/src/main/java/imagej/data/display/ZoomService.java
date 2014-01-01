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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.data.display;

import imagej.service.ImageJService;

/**
 * Interface for services that perform zoom operations on {@link ImageDisplay}s.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public interface ZoomService extends ImageJService {

	/**
	 * Zooms in on the given image display. Zoom multiplier is taken from the
	 * current ImageDisplay's zoom factor value.
	 */
	void zoomIn(ImageDisplay display);

	/**
	 * Zooms out on the given image display. Zoom multiplier is taken from the
	 * current ImageDisplay's zoom factor value.
	 */
	void zoomOut(ImageDisplay display);

	/** Zooms the given image display at the scale it was originally viewed at. */
	void zoomOriginalScale(ImageDisplay display);

	/** Zooms the given image display to 100% resolution. */
	void zoom100Percent(ImageDisplay display);

	/**
	 * Zooms in on the given image display's currently selected region.
	 */
	void zoomToSelection(ImageDisplay display);

	/**
	 * Zooms in on the center of the given image display at the specified
	 * magnification level and center coordinates.
	 */
	void zoomSet(ImageDisplay display, double zoomPercent, double centerU,
		double centerV);

}
