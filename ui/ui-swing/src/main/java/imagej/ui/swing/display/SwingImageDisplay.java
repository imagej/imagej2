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

import imagej.ImageJ;
import imagej.awt.AWTDisplay;
import imagej.awt.AWTDisplayController;
import imagej.awt.AWTEventDispatcher;
import imagej.data.Dataset;
import imagej.data.event.DatasetChangedEvent;
import imagej.display.Display;
import imagej.display.DisplayController;
import imagej.display.DisplayManager;
import imagej.display.EventDispatcher;
import imagej.display.event.DisplayCreatedEvent;
import imagej.display.event.window.WinActivatedEvent;
import imagej.display.event.window.WinClosedEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.plugin.Plugin;
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
public class SwingImageDisplay implements AWTDisplay {
	protected Dataset theDataset;

	private SwingImageDisplayWindow imgWindow;
	private SwingNavigableImageCanvas imgCanvas;
	private DisplayController controller;
	private int[] lastKnownDimensions;
	private List<EventSubscriber<?>> subscribers;

	public SwingImageDisplay() {
		// CTR FIXME - listen for imgWindow windowClosing and send
		// DisplayDeletedEvent. Think about how best this should work...
		// Is a display always deleted when its window is closed?
		
		final DisplayManager displayManager = ImageJ.get(DisplayManager.class);
		
		EventSubscriber<DatasetChangedEvent> dsChangeSubscriber =
			new EventSubscriber<DatasetChangedEvent>() {
				@Override
				public void onEvent(DatasetChangedEvent event) {
					if (theDataset == event.getObject()) {
						update();
					}
				}
			};
		EventSubscriber<WinActivatedEvent> winActSubscriber = 
			new EventSubscriber<WinActivatedEvent>() {
				@Override
				public void onEvent(WinActivatedEvent event) {
					displayManager.setActiveDisplay(event.getDisplay());
					Log.debug("**** active display set to "+event.getDisplay()+" ****");
				}
			};
		EventSubscriber<WinClosedEvent> winCloseSubscriber = 
			new EventSubscriber<WinClosedEvent>() {
				@Override
				public void onEvent(WinClosedEvent event) {
					displayManager.setActiveDisplay(null);
					Log.debug("**** active display set to null ****");
				}
			};
			
		subscribers = new ArrayList<EventSubscriber<?>>();
		subscribers.add(dsChangeSubscriber);
		subscribers.add(winActSubscriber);
		subscribers.add(winCloseSubscriber);
		
		Events.subscribe(DatasetChangedEvent.class, dsChangeSubscriber);
		Events.subscribe(WinActivatedEvent.class, winActSubscriber);
		Events.subscribe(WinClosedEvent.class, winCloseSubscriber);
		
		Events.publish(new DisplayCreatedEvent(this));

		displayManager.setActiveDisplay(this);
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
			imgCanvas.resetPan();
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
	public void pan(final double x, final double y) {
		imgCanvas.pan(x, y);
	}

	@Override
	public void panReset() {
		imgCanvas.resetPan();
	}
	
	// TODO
	@Override
	public double getPanX() {
		return imgCanvas.getPanX();
	}

	@Override
	public double getPanY() {
		return imgCanvas.getPanY();
	}


	@Override
	public void setZoom(double newZoom) {
		imgCanvas.setZoom(newZoom);
	}
	
	@Override
	public void setZoom(final double newZoom, final double centerX,
			final double centerY) {
		imgCanvas.setZoom(newZoom, centerX, centerY);
	}

	@Override
	public void zoomIn() {
		imgCanvas.zoomIn();
	}
	
	@Override
	public void zoomIn(final double centerX, final double centerY) {
		imgCanvas.zoomIn(centerX, centerY);
	}

	@Override
	public void zoomOut() {
		imgCanvas.zoomOut();
	}
	
	@Override
	public void zoomOut(final double centerX, final double centerY) {
		imgCanvas.zoomOut(centerX, centerY);
	}

	@Override
	public void zoomToFit(final int w, final int h) {
		imgCanvas.zoomToFit(w,h);
	}

	@Override
	public double getZoomFactor() {
		return imgCanvas.getZoom();
	}
	
	@Override
	public double getZoomCtrX() {
		return imgCanvas.getZoomCtrX();
	}
	
	@Override
	public double getZoomCtrY() {
		return imgCanvas.getZoomCtrY();
	}
}
