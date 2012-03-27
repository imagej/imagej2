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

package imagej.data;

import imagej.data.display.ImageDisplay;
import imagej.data.overlay.Overlay;
import net.imglib2.Interval;
import net.imglib2.RealInterval;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.CalibratedSpace;
import net.imglib2.meta.Named;

/**
 * A named Euclidean coordinate space interval with labeled dimensional axes.
 * All N-dimensional constructs in ImageJ should implement this interface.
 * <p>
 * If the N-dimensional construct is not expressed over a discrete source
 * domain, the {@link #isDiscrete()} method will return false, and the methods
 * of {@link Interval} will throw {@link UnsupportedOperationException}.
 * </p>
 * 
 * @author Curtis Rueden
 * @see Data
 * @see ImageDisplay
 */
public interface CalibratedInterval extends CalibratedSpace, Interval, Named {

	/** Gets the dimensional axes of the space. */
	AxisType[] getAxes();

	/**
	 * Gets whether the coordinate space provides a discrete sampling of its
	 * values. E.g.: {@link Dataset} and {@link ImageDisplay} do, but
	 * {@link Overlay} does not. If this method returns false, the methods of
	 * {@link Interval} will throw {@link UnsupportedOperationException}. Either
	 * way, the methods of {@link RealInterval} will be usable.
	 */
	boolean isDiscrete();

	/**
	 * Gets the dimensional extents of the space.
	 * 
	 * @throws UnsupportedOperationException if this object does not provide a
	 *           discrete sampling.
	 */
	Extents getExtents();

	/**
	 * Gets the dimensional lengths of the space.
	 * 
	 * @throws UnsupportedOperationException if this object does not provide a
	 *           discrete sampling.
	 */
	long[] getDims();

}
