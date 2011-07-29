//
// AxisUtils.java
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

package imagej.core.plugins.axispos;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.display.event.AxisPositionEvent;
import imagej.event.Events;
import net.imglib2.img.Axis;

/**
 * Utility methods used within the axis position subpackage.
 * 
 * @author Barry DeZonia
 */
public final class AxisUtils {

	private AxisUtils() {
		// utility class : uninstantiable
	}

	/**
	 * Publishes a new AxisPositionEvent
	 * 
	 * @param change - the size of the change to make to the axis position
	 * @param relative - whether the change is a relative or absolute amount
	 */
	public static void changeCurrentAxisPosition(final Display display,
		final long change, final boolean relative)
	{
		final DisplayService displayService = ImageJ.get(DisplayService.class);
		if (display == null) return; // headless UI or no open images
		final Axis axis = display.getActiveAxis();
		if (axis == null) return;
		if (display.getAxisIndex(axis) < 0) return;
		final Dataset ds = displayService.getActiveDataset(display);
		final int axisIndex = ds.getAxisIndex(axis);
		if (axisIndex < 0) return;
		final long max = ds.getExtents().dimension(axisIndex);
		Events
			.publish(new AxisPositionEvent(display, axis, change, max, relative));
	}

}
