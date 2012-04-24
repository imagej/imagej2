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

package imagej.data.display;

import imagej.data.CalibratedInterval;
import imagej.data.Data;
import imagej.data.Dataset;
import imagej.data.PositionableByAxis;
import imagej.data.overlay.Overlay;
import imagej.ext.display.Display;
import net.imglib2.meta.AxisType;

/**
 * An image display is a {@link Display} for visualizing {@link Data} objects.
 * It operates directly on {@link DataView}s, which wrap the {@link Data}
 * objects and provide display settings for the data.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
public interface ImageDisplay extends Display<DataView>, CalibratedInterval,
	PositionableByAxis
{

	/** Gets the view currently designated as active. */
	DataView getActiveView();

	/** Gets the axis currently designated as active. */
	AxisType getActiveAxis();

	/** Sets the axis currently designated as active. */
	void setActiveAxis(AxisType axis);

	/** Tests whether this display contains the given data object (via a view). */
	boolean containsData(Data data);

	/**
	 * Tests whether the given view should currently be visible in this display. A
	 * view is visible when the current position falls within the bounds of the
	 * view's space, including constant-value dimensions beyond the view's linked
	 * data's space.
	 */
	boolean isVisible(DataView view);

}
