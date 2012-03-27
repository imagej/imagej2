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

package imagej.ext;

import imagej.ext.display.ActiveDisplayPreprocessor;
import imagej.ext.display.DisplayPostprocessor;
import imagej.ext.plugin.AbstractInputHarvesterPlugin;
import imagej.ext.plugin.InitPreprocessor;
import imagej.ext.plugin.Plugin;
import imagej.ext.plugin.ServicePreprocessor;
import imagej.ext.plugin.debug.DebugPostprocessor;
import imagej.ext.plugin.debug.DebugPreprocessor;

/**
 * Constants for specifying an item's priority.
 * 
 * @author Johannes Schindelin
 * @author Curtis Rueden
 * @see Plugin#priority()
 */
public class Priority {

	/**
	 * Priority for processors that must go first in the processor chain.
	 * Examples: {@link DebugPreprocessor}, {@link DebugPostprocessor}
	 */
	public static final double FIRST_PRIORITY = Double.POSITIVE_INFINITY;

	/**
	 * Priority for processors that strongly prefer to be early in the processor
	 * chain. Examples: {@link ActiveDisplayPreprocessor},
	 * {@link ServicePreprocessor}
	 */
	public static final double VERY_HIGH_PRIORITY = +10000;

	/**
	 * Priority for processors that prefer to be earlier in the processor chain.
	 * Example: {@link InitPreprocessor}
	 */
	public static final double HIGH_PRIORITY = +100;

	/** Default priority for processors. */
	public static final double NORMAL_PRIORITY = 0;

	/** Priority for processors that prefer to be later in the processor chain. */
	public static final double LOW_PRIORITY = -100;

	/**
	 * Priority for processors that strongly prefer to be late in the processor
	 * chain. Examples: {@link DisplayPostprocessor}, UI-specific subclasses of
	 * {@link AbstractInputHarvesterPlugin}.
	 */
	public static final double VERY_LOW_PRIORITY = -10000;

	/** Priority for processors that must go at the end of the processor chain. */
	public static final double LAST_PRIORITY = Double.NEGATIVE_INFINITY;

}
