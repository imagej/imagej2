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

package imagej;

/**
 * Constants for specifying an item's priority.
 * 
 * @author Johannes Schindelin
 * @author Curtis Rueden
 * @see imagej.Prioritized#getPriority()
 * @see imagej.plugin.Plugin#priority()
 */
public final class Priority {

	private Priority() {
		// prevent instantiation of utility class
	}

	/**
	 * Priority for items that must go first in the chain. Examples:
	 * {@link imagej.plugin.DebugPreprocessor},
	 * {@link imagej.plugin.DebugPostprocessor}
	 */
	public static final double FIRST_PRIORITY = Double.POSITIVE_INFINITY;

	/**
	 * Priority for items that strongly prefer to be early in the chain. Examples:
	 * {@link imagej.display.ActiveDisplayPreprocessor},
	 * {@link imagej.plugin.ServicePreprocessor}
	 */
	public static final double VERY_HIGH_PRIORITY = +10000;

	/**
	 * Priority for items that prefer to be earlier in the chain. Example:
	 * {@link imagej.plugin.InitPreprocessor}
	 */
	public static final double HIGH_PRIORITY = +100;

	/** Default priority for items. */
	public static final double NORMAL_PRIORITY = 0;

	/** Priority for items that prefer to be later in the chain. */
	public static final double LOW_PRIORITY = -100;

	/**
	 * Priority for items that strongly prefer to be late in the chain.
	 * Example: {@link imagej.display.DisplayPostprocessor}
	 */
	public static final double VERY_LOW_PRIORITY = -10000;

	/** Priority for items that must go at the end of the chain. */
	public static final double LAST_PRIORITY = Double.NEGATIVE_INFINITY;

	/** Compares the two {@link Prioritized} objects. */
	public static int compare(final Prioritized p1, final Prioritized p2) {
		final double priority1 = p1.getPriority();
		final double priority2 = p2.getPriority();
		if (priority1 == priority2) return 0;
		// NB: We invert the ordering here, so that large values come first,
		// rather than the typical natural ordering of smaller values first.
		return priority1 > priority2 ? -1 : 1;
	}

}
