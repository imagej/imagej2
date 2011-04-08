//
// SimpleImageDisplay.java
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

package imagej.ui.swing.display;

import imagej.awt.AWTDisplay;
import imagej.awt.AWTDisplayController;
import imagej.awt.AWTEventDispatcher;
import imagej.data.Dataset;
import imagej.data.event.DatasetChangedEvent;
import imagej.display.ActiveDisplay;
import imagej.display.Display;
import imagej.display.DisplayController;
import imagej.display.EventDispatcher;
import imagej.display.event.DisplayCreatedEvent;
import imagej.display.event.window.WinActivatedEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.plugin.Plugin;
import imagej.util.IntCoords;
import imagej.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple Swing image display plugin.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
@Plugin(type = Display.class)
public class SwingImageDisplay implements AWTDisplay
{
	private SwingImageDisplayWindow imgWindow;
	private SwingNavigableImageCanvas imgCanvas;
	private Dataset theDataset;
	private DisplayController controller;
	private int[] lastKnownDimensions;
	private List<EventSubscriber<?>> subscribers;

	public SwingImageDisplay() {
		// CTR FIXME - listen for imgWindow windowClosing and send
		// DisplayDeletedEvent. Think about how best this should work...
		// Is a display always deleted when its window is closed?
		subscribers = new ArrayList<EventSubscriber<?>>();
		
		EventSubscriber<DatasetChangedEvent> dsChangeSubscriber =
			new EventSubscriber<DatasetChangedEvent>() {
				@Override
				public void onEvent(DatasetChangedEvent event) {
					if (theDataset == event.getObject()) {
						update();
					}
				}
			};
		EventSubscriber<WinActivatedEvent> windowSubscriber = 
			new EventSubscriber<WinActivatedEvent>() {
				@Override
				public void onEvent(WinActivatedEvent event) {
					ActiveDisplay.set(event.getDisplay());
					Log.debug("**** active window changed ****");
				}
			};
			
		subscribers.add(dsChangeSubscriber);
		subscribers.add(windowSubscriber);
		
		Events.subscribe(DatasetChangedEvent.class, dsChangeSubscriber);
		Events.subscribe(WinActivatedEvent.class, windowSubscriber);
		
		Events.publish(new DisplayCreatedEvent(this));

		ActiveDisplay.set(this);
	}

	@Override
	public boolean canDisplay(final Dataset dataset) {
		return true;
	}

	@Override
	public void display(final Dataset dataset) {
		theDataset = dataset;
		lastKnownDimensions = dataset.getImage().getDimensions();
		// imgCanvas = new ImageCanvasSwing();
		imgCanvas = new SwingNavigableImageCanvas();
		imgWindow = new SwingImageDisplayWindow(imgCanvas);
		controller = new AWTDisplayController(this);
		imgWindow.setDisplayController(controller);
		// FIXME - this is a second call to set the Dataset. An earlier call is
		// contained in the AWTDisplayController constructor. If the Dataset not
		// reset here then image width will not fill the zoom window. Will debug
		// further but patch for now in preparation of release of alpha 1.
		controller.setDataset(dataset);
		// FIXME - this pack() call an experiment to avoid it in
		//   imgWindow.setDisplayController(). Works but controller probably
		//   should pack.
		//imgWindow.pack();
		final EventDispatcher dispatcher = new AWTEventDispatcher(this);
		imgCanvas.addEventDispatcher(dispatcher);
		imgCanvas.subscribeToToolEvents();
		imgWindow.addEventDispatcher(dispatcher);
		// TODO - use DisplayView instead of Dataset directly
		// imageFrame.setDataset(dataset);
		// ((NavigableImageJFrame)imgWindow).pack();
		imgWindow.setVisible(true);
	}

	@Override
	public Dataset getDataset() {
		return theDataset;
	}

	@Override
	public void update() {
		// did the shape of the dataset change?
		int[] currDimensions = theDataset.getImage().getDimensions();
		// TODO - maybe this should be handled in the onEvent(DatasetChangedEvent) handler
		if (!Arrays.equals(lastKnownDimensions, currDimensions)) {
			lastKnownDimensions = currDimensions;
			controller.setDataset(theDataset);
			imgCanvas.setZoom(1.0);
			imgCanvas.resetImageOrigin();
		}
		controller.update();
	}

	@Override
	public SwingImageDisplayWindow getImageDisplayWindow() {
		return imgWindow;
	}

	@Override
	public SwingNavigableImageCanvas getImageCanvas() {
		return imgCanvas;
	}

	@Override
	public Object getCurrentPlane() {
		return controller.getCurrentPlane();
	}

	@Override
	public int[] getCurrentPlanePosition() {
		return controller.getPos();
	}

	@Override
	public void pan(final float x, final float y) {
		imgCanvas.pan((int) x, (int) y);
	}

	// TODO
	@Override
	public float getPanX() {
		return 0f;
		// throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public float getPanY() {
		return 0f;
		// throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setZoom(final float factor, final float centerX,
		final float centerY)
	{
		imgCanvas.setZoom(factor, new IntCoords((int)centerX, (int)centerY));
	}

	@Override
	public void zoomIn(final float centerX, final float centerY) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void zoomOut(final float centerX, final float centerY) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void zoomToFit(final int w, final int h) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public float getZoom() {
		return 1f;
		// throw new UnsupportedOperationException("Not supported yet.");
	}
}
