//
// ImageDisplay.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.data.display;

import imagej.data.Data;
import imagej.data.Dataset;
import imagej.data.LabeledSpace;
import imagej.data.roi.Overlay;
import imagej.ext.display.Display;
import net.imglib2.img.Axis;

/**
 * An image display is a {@link Display} for visualizing {@link Data} objects.
 * It operates directly on {@link DataView}s, which wrap the {@link Data}
 * objects and provide display settings for the data.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
public interface ImageDisplay extends Display<DataView>, LabeledSpace {

	/** Gets the view currently designated as active. */
	DataView getActiveView();

	/** Gets the axis currently designated as active. */
	Axis getActiveAxis();

	/** Sets the axis currently designated as active. */
	void setActiveAxis(Axis axis);

	long getAxisPosition(Axis axis);

	void setAxisPosition(final Axis axis, final long position);

	/** Gets the image canvas upon which this display's output is painted. */
	ImageCanvas getCanvas();

	/** Tests whether this display contains the given data object (via a view). */
	boolean containsData(Data data);

	// CTR TODO - eliminate the methods below.

	@Deprecated
	void display(Dataset dataset);

	@Deprecated
	void display(Overlay overlay);

	String makeLabel();

}
