//
// PixelProbe.java
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

package imagej.core.tools.global;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.Position;
import imagej.data.display.DataView;
import imagej.data.display.ImageCanvas;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.event.EventService;
import imagej.event.StatusEvent;
import imagej.ext.display.Display;
import imagej.ext.display.event.mouse.MsMovedEvent;
import imagej.ext.tool.AbstractTool;
import imagej.ext.tool.Tool;
import imagej.util.IntCoords;
import imagej.util.RealCoords;
import net.imglib2.RandomAccess;
import net.imglib2.img.Axes;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

/**
 * Displays pixel values under the cursor.
 * 
 * @author Barry DeZonia
 * @author Rick Lentz
 * @author Grant Harris
 * @author Curtis Rueden
 */
@Tool(name = "Probe", global = true)
public class PixelProbe extends AbstractTool {

	// -- ITool methods --

	@Override
	public void onMouseMove(final MsMovedEvent evt) {
		final ImageJ context = evt.getContext();
		final ImageDisplayService imageDisplayService =
			context.getService(ImageDisplayService.class);
		final EventService eventService = context.getService(EventService.class);

		final Display<?> display = evt.getDisplay();
		if (!(display instanceof ImageDisplay)) return;
		final ImageDisplay imageDisplay = (ImageDisplay) display;

		final ImageCanvas canvas = imageDisplay.getCanvas();
		final IntCoords mousePos = new IntCoords(evt.getX(), evt.getY());
		if (!canvas.isInImage(mousePos)) {
			eventService.publish(new StatusEvent(null));
			return;
		}

		// mouse is over image

		// TODO - update tool to probe more than just the active view
		final DataView activeView = imageDisplay.getActiveView();
		final Dataset dataset = imageDisplayService.getActiveDataset(imageDisplay);

		final RealCoords coords = canvas.panelToImageCoords(mousePos);
		final int cx = coords.getIntX();
		final int cy = coords.getIntY();

		final Position planePos = activeView.getPlanePosition();

		final Img<? extends RealType<?>> image = dataset.getImgPlus();
		final RandomAccess<? extends RealType<?>> randomAccess =
			image.randomAccess();
		final int xAxis = dataset.getAxisIndex(Axes.X);
		final int yAxis = dataset.getAxisIndex(Axes.Y);

		setPosition(randomAccess, cx, cy, planePos, xAxis, yAxis);

		final double value = randomAccess.get().getRealDouble();
		final String message;
		if (dataset.isInteger()) {
			message = String.format("x=%d, y=%d, value=%d", cx, cy, (long) value);
		}
		else {
			message = String.format("x=%d, y=%d, value=%f", cx, cy, value);
		}
		eventService.publish(new StatusEvent(message));
	}

	// -- private interface --

	private void setPosition(
		final RandomAccess<? extends RealType<?>> randomAccess, final int cx,
		final int cy, final Position planePos, final int xAxis, final int yAxis)
	{
		int i = 0;
		for (int d = 0; d < randomAccess.numDimensions(); d++) {
			if (d == xAxis) randomAccess.setPosition(cx, d);
			else if (d == yAxis) randomAccess.setPosition(cy, d);
			else randomAccess.setPosition(planePos.getLongPosition(i++), d);
		}
	}

}
