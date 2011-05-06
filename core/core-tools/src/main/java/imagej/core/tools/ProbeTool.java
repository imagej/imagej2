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

import imagej.data.DataObject;
import imagej.data.Dataset;
import imagej.display.Display;
import imagej.display.DisplayView;
import imagej.display.ImageCanvas;
import imagej.display.MouseCursor;
import imagej.display.event.mouse.MsMovedEvent;
import imagej.event.Events;
import imagej.event.StatusEvent;
import imagej.tool.BaseTool;
import imagej.tool.Tool;
import imagej.util.IntCoords;
import imagej.util.RealCoords;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

/**
 * TODO
 * 
 * @author Barry DeZonia
 * @author Rick Lentz
 * @author Grant Harris
 * @author Curtis Rueden
 */
@Tool(name = "Probe", iconPath = "/tools/probe.png",
	description = "Probe Pixel Tool", priority = ProbeTool.PRIORITY)
public class ProbeTool extends BaseTool {

	// -- constants --

	public static final int PRIORITY = 204;

	// -- private instance variables --

	private Dataset dataset;
	private RandomAccess<? extends RealType<?>> randomAccess;
	private long[] position;

	// -- ITool methods --

	@Override
	public void onMouseMove(final MsMovedEvent evt) {
		final Display display = evt.getDisplay();
		final ImageCanvas canvas = display.getImageCanvas();
		final IntCoords mousePos = new IntCoords(evt.getX(), evt.getY());
		// mouse not in image ?
		if (!canvas.isInImage(mousePos)) {
			clearWorkingVariables();
			Events.publish(new StatusEvent(""));
		}
		else { // mouse is over image
			// CTR TODO - update tool to probe more than just the active view
			final DisplayView activeView = display.getActiveView();
			final DataObject dataObject = activeView.getDataObject();
			final Dataset d = dataObject instanceof Dataset ?
				(Dataset) dataObject : null;
			setWorkingVariables(d);
			final RealCoords coords = canvas.panelToImageCoords(mousePos);
			final int cx = coords.getIntX();
			final int cy = coords.getIntY();
			final long[] planePos = activeView.getPlanePosition();
			fillCurrentPosition(cx, cy, planePos);
			randomAccess.setPosition(position);
			final double doubleValue = randomAccess.get().getRealDouble();
			final String statusMessage;
			if (dataset.isInteger()) {
				statusMessage =
					String.format("x=%d, y=%d, value=%d", cx, cy, (long) doubleValue);
			}
			else {
				statusMessage =
					String.format("x=%d, y=%d, value=%f", cx, cy, doubleValue);
			}
			Events.publish(new StatusEvent(statusMessage));
		}
	}

	@Override
	public MouseCursor getCursor() {
		return MouseCursor.CROSSHAIR;
	}

	// -- private interface --

	// TODO - If someone positions the probe over an image and then they close
	// the image via any means other than using the mouse then this method
	// will not get called. This leaves a cursor open and thus an Image
	// reference may be kept around. This could keep some memory from freeing
	// up. Test and if so figure out a workaround. Maybe it could subscribe
	// to an event that is fired when images are closed. That event handler
	// could just call this method. (Note that this might be a case to not
	// worry about. If the Probe is moved after the image close it should
	// clean up unless there are no displays open at all).

	private void clearWorkingVariables() {
		position = null;
		randomAccess = null;
		dataset = null;
	}

	private void setWorkingVariables(final Dataset d) {
		if (d != dataset) {
			clearWorkingVariables();
			dataset = d;
			final Img<? extends RealType<?>> image = d.getImgPlus();
			randomAccess = image.randomAccess();
			position = new long[image.numDimensions()];
			randomAccess.localize(position);
		}
	}

	private void fillCurrentPosition(final long x, final long y,
		final long[] planePos)
	{
		// TODO - FIXME - assumes x & y axes are first two
		position[0] = x;
		position[1] = y;
		for (int i = 2; i < position.length; i++) {
			position[i] = planePos[i - 2];
		}
	}

}
