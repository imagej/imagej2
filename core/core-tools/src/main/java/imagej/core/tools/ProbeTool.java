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

import java.util.ArrayList;

import imagej.data.DataObject;
import imagej.data.Dataset;
import imagej.data.event.DatasetDeletedEvent;
import imagej.data.event.DatasetRestructuredEvent;
import imagej.data.event.DatasetUpdatedEvent;
import imagej.display.Display;
import imagej.display.DisplayView;
import imagej.display.ImageCanvas;
import imagej.display.MouseCursor;
import imagej.display.event.mouse.MsMovedEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.event.StatusEvent;
import imagej.tool.AbstractTool;
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
@Tool(name = "Probe", iconPath = "/icons/tools/probe.png",
	description = "Probe Pixel Tool", priority = ProbeTool.PRIORITY)
public class ProbeTool extends AbstractTool {

	// -- constants --

	public static final int PRIORITY = 200;

	// -- private instance variables --

	private Dataset dataset;
	private RandomAccess<? extends RealType<?>> randomAccess;
	private long[] position;
	private ArrayList<EventSubscriber<?>> subscribers;
	
	// -- constructor --
	
	public ProbeTool() {
		subscribeToEvents();
	}
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
	
	private void subscribeToEvents() {

		subscribers = new ArrayList<EventSubscriber<?>>();
		
		// it is possible that underlying data is changed in such a way that
		// probe gets out of sync. force a resync
		
		EventSubscriber<DatasetUpdatedEvent> updateSubscriber =
			new EventSubscriber<DatasetUpdatedEvent>() {

				@Override
				public void onEvent(DatasetUpdatedEvent event) {
					if (event.getObject() == dataset) {
						clearWorkingVariables();
					}
				}
		};
		subscribers.add(updateSubscriber);
		Events.subscribe(DatasetUpdatedEvent.class, updateSubscriber);

		EventSubscriber<DatasetDeletedEvent> deleteSubscriber =
			new EventSubscriber<DatasetDeletedEvent>() {

				@Override
				public void onEvent(DatasetDeletedEvent event) {
					if (event.getObject() == dataset) {
						clearWorkingVariables();
					}
				}
		};
		subscribers.add(deleteSubscriber);
		Events.subscribe(DatasetDeletedEvent.class, deleteSubscriber);

		EventSubscriber<DatasetRestructuredEvent> restructureSubscriber =
			new EventSubscriber<DatasetRestructuredEvent>() {

				@Override
				public void onEvent(DatasetRestructuredEvent event) {
					if (event.getObject() == dataset) {
						clearWorkingVariables();
					}
				}
		};
		subscribers.add(restructureSubscriber);
		Events.subscribe(DatasetRestructuredEvent.class, restructureSubscriber);
	}

}
