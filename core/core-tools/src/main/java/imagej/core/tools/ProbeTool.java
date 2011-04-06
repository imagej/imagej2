//
// ProbeTool.java
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

package imagej.core.tools;

import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import imagej.data.Dataset;
import imagej.display.Display;
import imagej.display.MouseCursor;
import imagej.display.NavigableImageCanvas;
import imagej.display.event.mouse.MsMovedEvent;
import imagej.event.Events;
import imagej.event.StatusEvent;
import imagej.tool.BaseTool;
import imagej.tool.Tool;
import imagej.util.IntCoords;
import imagej.util.RealCoords;

/**
 * @author Barry DeZonia
 * @author Rick Lentz
 * @author Grant Harris
 */
@Tool(name = "Probe", iconPath = "/tools/probe.png",
	description = "Probe Pixel Tool", priority = ProbeTool.PRIORITY)
public class ProbeTool extends BaseTool {

	// -- constants --
	
	public static final int PRIORITY = 204;

	// -- private instance variables --
	
	private Dataset currentDataset;
	private LocalizableByDimCursor<? extends RealType<?>> currentCursor;
	private int[] currentPosition;
	
	// -- ITool methods --

	@Override
	public void onMouseMove(final MsMovedEvent evt) {
		final Display display = evt.getDisplay();
		final Dataset dataset = display.getDataset();
		final NavigableImageCanvas canvas = display.getImageCanvas();
		final int x = evt.getX();
		final int y = evt.getY();
		final IntCoords mousePos = new IntCoords(x, y);
		final RealCoords coords = canvas.panelToImageCoords(mousePos);
		// mouse not in image ?
		if ( ! canvas.isInImage(mousePos) ) {
			clearWorkingVariables();
			Events.publish(new StatusEvent(""));
		}
		else {  // mouse is over image
			setWorkingVariables(display.getDataset());
			final int cx = coords.getIntX();
			final int cy = coords.getIntY();
			final int[] currPlanePos = display.getCurrentPlanePosition();
			fillCurrentPosition(cx, cy, currPlanePos);
			currentCursor.setPosition(currentPosition);
			final double doubleValue = currentCursor.getType().getRealDouble();
			String valString;
			if (dataset.isFloat())
				valString = "" + doubleValue;
			else
				valString = "" + ((long) doubleValue);
			Events.publish(
				new StatusEvent("x=" + cx + ", y=" + cy + ", value=" + valString));
		}
	}

	@Override
	public MouseCursor getCursor() {
		return MouseCursor.CROSSHAIR;
	}

	// -- private interface --
	
	// TODO - If someone positions the probe over an image and then they close
	//   the image via any means other than using the mouse then this method
	//   will not get called. This leaves a cursor open and thus an Image
	//   reference may be kept around. This could keep some memory from freeing
	//   up. Test and if so figure out a workaround. Maybe it could subscribe
	//   to an event that is fired when images are closed. That event handler
	//   could just call this method. (Note that this might be a case to not
	//   worry about. If the Probe is moved after the image close it should
	//   clean up unless there are no displays open at all).

	private void clearWorkingVariables() {
		currentPosition = null;
		if (currentCursor != null) {
			currentCursor.close();
			currentCursor = null;
		}
		currentDataset = null;
	}
	
	private void setWorkingVariables(Dataset dataset) {
		if (dataset != currentDataset) {
			clearWorkingVariables();
			currentDataset = dataset;
			final Image<? extends RealType<?>> image =
				(Image<? extends RealType<?>>) dataset.getImage();
			currentCursor = image.createLocalizableByDimCursor();
			currentPosition = currentCursor.createPositionArray();
		}
	}
	
	private void fillCurrentPosition(int x, int y, int[] planePos) {
		// TODO - FIXME - assumes x & y axes are first two
		currentPosition[0] = x;
		currentPosition[1] = y;
		for (int i = 2; i < currentPosition.length; i++)
			currentPosition[i] = planePos[i-2];
	}
}
