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

import imagej.data.Dataset;
import imagej.data.Position;
import imagej.data.display.DisplayService;
import imagej.data.display.DisplayView;
import imagej.data.display.ImageCanvas;
import imagej.data.display.ImageDisplay;
import imagej.data.event.DatasetDeletedEvent;
import imagej.data.event.DatasetRestructuredEvent;
import imagej.data.event.DatasetUpdatedEvent;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.event.StatusEvent;
import imagej.ext.display.Display;
import imagej.ext.display.MouseCursor;
import imagej.ext.display.event.mouse.MsMovedEvent;
import imagej.tool.AbstractTool;
import imagej.tool.Tool;
import imagej.util.IntCoords;
import imagej.util.RealCoords;

import java.util.ArrayList;

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

	// -- private instance variables --

	private Dataset dataset;
	private RandomAccess<? extends RealType<?>> randomAccess;
	private long[] position;
	private ArrayList<EventSubscriber<?>> subscribers;
	private int xAxis, yAxis;

	// -- constructor --

	public PixelProbe() {
		subscribeToEvents();
	}

	// -- ITool methods --

	@Override
	public void onMouseMove(final MsMovedEvent evt) {
		final DisplayService displayService =
			evt.getContext().getService(DisplayService.class);
		final EventService eventService =
			evt.getContext().getService(EventService.class);

		final Display display = evt.getDisplay();
		if(!(display instanceof ImageDisplay)) return;
		final ImageCanvas canvas = ((ImageDisplay)display).getImageCanvas();
		final IntCoords mousePos = new IntCoords(evt.getX(), evt.getY());
		// mouse not in image ?
		if (!canvas.isInImage(mousePos)) {
			clearWorkingVariables();
			eventService.publish(new StatusEvent(""));
		}
		else { // mouse is over image
			// CTR TODO - update tool to probe more than just the active view
			final DisplayView activeView = ((ImageDisplay)display).getActiveView();
			final Dataset d = displayService.getActiveDataset((ImageDisplay)display);
			setWorkingVariables(d);
			final RealCoords coords = canvas.panelToImageCoords(mousePos);
			// Re: bug #639
			// note - can't use getIntX() and getIntY() since they round and can
			// take the integer coords out of the image bounds. Exceptions happen
			//final int cx = (int) Math.floor(coords.x);
			//final int cy = (int) Math.floor(coords.y);
			// The previous attempt did not fix things. There is a scaling issue
			// in the canvas that needs to be figured out.
			final int cx = coords.getIntX();
			final int cy = coords.getIntY();
			final Position planePos = activeView.getPlanePosition();
			fillCurrentPosition(position, cx, cy, planePos);
			randomAccess.setPosition(position);
			double doubleValue = 0;
			//try {
				doubleValue = randomAccess.get().getRealDouble();
			// Re: bug #639
			//} catch (Exception e) {
			//	System.out.println("Exception happened with position "+
			//		position[xAxis]+","+position[yAxis]);
			//}
			final String statusMessage;
			if (dataset.isInteger()) {
				statusMessage =
					String.format("x=%d, y=%d, value=%d", cx, cy, (long) doubleValue);
			}
			else {
				statusMessage =
					String.format("x=%d, y=%d, value=%f", cx, cy, doubleValue);
			}
			eventService.publish(new StatusEvent(statusMessage));
		}
	}

	@Override
	public MouseCursor getCursor() {
		return MouseCursor.CROSSHAIR;
	}

	// -- private interface --

	private void clearWorkingVariables() {
		position = null;
		randomAccess = null;
		dataset = null;
		xAxis = Integer.MIN_VALUE;
		yAxis = Integer.MIN_VALUE;
	}

	private void setWorkingVariables(final Dataset d) {
		if (d != dataset) {
			clearWorkingVariables();
			dataset = d;
			final Img<? extends RealType<?>> image = d.getImgPlus();
			randomAccess = image.randomAccess();
			position = new long[image.numDimensions()];
			randomAccess.localize(position);
			xAxis = dataset.getAxisIndex(Axes.X);
			yAxis = dataset.getAxisIndex(Axes.Y);
		}
	}

	private void fillCurrentPosition(final long[] pos,
		final long x, final long y,
		final Position planePos)
	{
		int d = 0;
		for (int i = 0; i < pos.length; i++) {
			if (i == xAxis) pos[i] = x;
			else if (i == yAxis) pos[i] = y;
			else pos[i] = planePos.getLongPosition(d++);
		}
	}

	@SuppressWarnings("synthetic-access")
	private void subscribeToEvents() {

		subscribers = new ArrayList<EventSubscriber<?>>();

		// it is possible that underlying data is changed in such a way that
		// probe gets out of sync. force a resync

		final EventSubscriber<DatasetUpdatedEvent> updateSubscriber =
			new EventSubscriber<DatasetUpdatedEvent>() {

				@Override
				public void onEvent(final DatasetUpdatedEvent event) {
					if (event.getObject() == dataset) {
						clearWorkingVariables();
					}
				}
			};
		subscribers.add(updateSubscriber);
		Events.subscribe(DatasetUpdatedEvent.class, updateSubscriber);

		final EventSubscriber<DatasetDeletedEvent> deleteSubscriber =
			new EventSubscriber<DatasetDeletedEvent>() {

				@Override
				public void onEvent(final DatasetDeletedEvent event) {
					if (event.getObject() == dataset) {
						clearWorkingVariables();
					}
				}
			};
		subscribers.add(deleteSubscriber);
		Events.subscribe(DatasetDeletedEvent.class, deleteSubscriber);

		final EventSubscriber<DatasetRestructuredEvent> restructureSubscriber =
			new EventSubscriber<DatasetRestructuredEvent>() {

				@Override
				public void onEvent(final DatasetRestructuredEvent event) {
					if (event.getObject() == dataset) {
						clearWorkingVariables();
					}
				}
			};
		subscribers.add(restructureSubscriber);
		Events.subscribe(DatasetRestructuredEvent.class, restructureSubscriber);
	}

}
