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

package imagej.data.threshold;

import imagej.data.display.ImageDisplay;
import imagej.data.overlay.ThresholdOverlay;
import imagej.service.ImageJService;

import java.util.List;
import java.util.Map;

import org.scijava.plugin.SingletonService;

/**
 * Interface for service that works with thresholds.
 * 
 * @author Barry DeZonia
 * @see ThresholdOverlay
 * @see ThresholdMethod
 */
public interface ThresholdService extends SingletonService<ThresholdMethod>,
	ImageJService
{

	/**
	 * Returns true if a {@link ThresholdOverlay} is defined for a given display.
	 */
	boolean hasThreshold(ImageDisplay display);

	/**
	 * Gets the {@link ThresholdOverlay} associated with a display. If one does
	 * not yet exist it is created.
	 */
	ThresholdOverlay getThreshold(ImageDisplay display);

	/** Removes the {@link ThresholdOverlay} associated with a display. */
	void removeThreshold(ImageDisplay display);

	/** Returns a map of available {@link ThresholdMethod}s, indexed by name. */
	Map<String, ThresholdMethod> getThresholdMethods();

	/**
	 * Returns the names of all available {@link ThresholdMethod}s, ordered by
	 * priority.
	 */
	List<String> getThresholdMethodNames();

	/**
	 * Returns the {@link ThresholdMethod} associated with the given name.
	 */
	ThresholdMethod getThresholdMethod(String name);

}
