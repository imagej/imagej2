//
// SwingImageDisplay.java
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
import imagej.awt.AWTEventDispatcher;
import imagej.awt.AWTImageTools;
import imagej.data.Dataset;
import imagej.data.event.DatasetRestructuredEvent;
import imagej.display.DatasetView;
import imagej.display.Display;
import imagej.display.DisplayManager;
import imagej.display.DisplayView;
import imagej.display.EventDispatcher;
import imagej.display.event.DisplayCreatedEvent;
import imagej.display.event.window.WinActivatedEvent;
import imagej.display.event.window.WinClosedEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.plugin.Plugin;
import imagej.util.Log;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.imglib2.display.ARGBScreenImage;

/**
 * A Swing image display plugin, which displays 2D planes in grayscale or
 * composite color.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
@Plugin(type = Display.class)
public class SwingImageDisplay implements AWTDisplay {

	private final ArrayList<DisplayView> views;
	private final List<EventSubscriber<?>> subscribers;

	private SwingImageCanvas imgCanvas;
	private SwingDisplayWindow imgWindow;
	
	private boolean willRebuildImgWindow;

	public SwingImageDisplay() {
		views = new ArrayList<DisplayView>();
		subscribers = new ArrayList<EventSubscriber<?>>();

		final DisplayManager displayManager = ImageJ.get(DisplayManager.class);
		displayManager.setActiveDisplay(this);
		subscribeToEvents(displayManager);

		imgCanvas = new SwingImageCanvas();
		imgWindow = new SwingDisplayWindow(this);

		final EventDispatcher eventDispatcher = new AWTEventDispatcher(this);
		imgCanvas.addEventDispatcher(eventDispatcher);
		imgWindow.addEventDispatcher(eventDispatcher);

		imgWindow.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				for (DisplayView view : getViews()) {
					view.dispose();
				}
			}
		});
		
		willRebuildImgWindow = false;
		
		Events.publish(new DisplayCreatedEvent(this));
	}

	// -- Display methods --

	@Override
	public boolean canDisplay(final Dataset dataset) {
		return true;
	}

	@Override
	public void display(final Dataset dataset) {
		addView(new DatasetView(this, dataset));
//		addView(new OverlayView(this, new Overlay()));
	}

	@Override
	public void update() {
		// compute width and height needed to contain all view images
		int width = 0, height = 0;
		for (final DisplayView view : views) {
			final int w = view.getImageWidth();
			final int h = view.getImageHeight();
			if (w > width) width = w;
			if (h > height) height = h;
		}

		// paint view images onto result image
		final BufferedImage result = AWTImageTools.createImage(width, height);
		final Graphics gfx = result.getGraphics();
		for (final DisplayView view : views) {
			final Object image = view.getImage();
			// TODO - Fix this hack. Perhaps AWTDisplayView could extend DisplayView
			// and narrow the getImage() method to return a java.awt.Image?
			final Image awtImage;
			if (image instanceof Image) {
				awtImage = (Image) image;
			}
			else if (image instanceof ARGBScreenImage) {
				awtImage = ((ARGBScreenImage) image).image();
			}
			else {
				awtImage = null;
				Log.warn("Unsupported DisplayView: " + view);
				continue;
			}
			gfx.drawImage(awtImage, 0, 0, null);
		}
		gfx.dispose();
		imgCanvas.setImage(result);
		if ( ! willRebuildImgWindow ) {
			imgWindow.update();
		}
		else { // rebuild image window
		  // NB - if pan to be reset below we'll be zoomed on wrong part of image 
			imgCanvas.setZoom(0);  // original scale
		  // NB - if x or y dims change without this image panned incorrectly
			//   Must happen after setZoom() call
			imgCanvas.panReset();
			imgWindow.redoLayout();
			willRebuildImgWindow = false;
		}
	}

	@Override
	public void addView(final DisplayView view) {
		views.add(view);
		update();
		imgWindow.redoLayout();
	}

	@Override
	public void removeView(final DisplayView view) {
		views.remove(view);
		update();
		imgWindow.redoLayout();
	}

	@Override
	public void removeAllViews() {
		views.clear();
		update();
		imgWindow.redoLayout();
	}

	@Override
	public List<DisplayView> getViews() {
		return Collections.unmodifiableList(views);
	}

	@Override
	public DisplayView getActiveView() {
		// CTR TODO - do better than hardcoding first view
		return views.get(0);
	}

	@Override
	public SwingDisplayWindow getDisplayWindow() {
		return imgWindow;
	}

	@Override
	public SwingImageCanvas getImageCanvas() {
		return imgCanvas;
	}

	// -- Helper methods --

	@SuppressWarnings("synthetic-access")
	private DisplayManager subscribeToEvents(final DisplayManager displayManager)
	{
		// CTR TODO - listen for imgWindow windowClosing and send
		// DisplayDeletedEvent. Think about how best this should work...
		// Is a display always deleted when its window is closed?

		final EventSubscriber<WinActivatedEvent> winActSubscriber =
			new EventSubscriber<WinActivatedEvent>()
		{
			@Override
			public void onEvent(final WinActivatedEvent event) {
				displayManager.setActiveDisplay(event.getDisplay());
			}
		};
		Events.subscribe(WinActivatedEvent.class, winActSubscriber);
		subscribers.add(winActSubscriber);

		final EventSubscriber<WinClosedEvent> winCloseSubscriber =
			new EventSubscriber<WinClosedEvent>()
		{
			@Override
			public void onEvent(final WinClosedEvent event) {
				displayManager.setActiveDisplay(null);
			}
		};
		Events.subscribe(WinClosedEvent.class, winCloseSubscriber);
		subscribers.add(winCloseSubscriber);

		final EventSubscriber<DatasetRestructuredEvent> restructureSubscriber =
			new EventSubscriber<DatasetRestructuredEvent>()
		{
			@Override
			public void onEvent(DatasetRestructuredEvent event) {
				Dataset dataset = event.getObject();
				for (DisplayView view : views) {
					if (dataset == view.getDataObject()) {
						willRebuildImgWindow = true;
						return;
					}
				}
			}
		};
		Events.subscribe(DatasetRestructuredEvent.class, restructureSubscriber);
		subscribers.add(restructureSubscriber);
		
		return displayManager;
	}

}
