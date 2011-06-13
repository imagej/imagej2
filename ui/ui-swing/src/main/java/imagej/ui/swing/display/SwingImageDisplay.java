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
import imagej.data.Dataset;
import imagej.data.event.DatasetRestructuredEvent;
import imagej.data.roi.Overlay;
import imagej.display.AbstractDisplay;
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
import imagej.tool.ToolManager;
import imagej.ui.UIManager;
import imagej.ui.UserInterface;
import imagej.ui.common.awt.AWTDisplay;
import imagej.ui.common.awt.AWTEventDispatcher;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A Swing image display plugin, which displays 2D planes in grayscale or
 * composite color.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
@Plugin(type = Display.class)
public class SwingImageDisplay extends AbstractDisplay implements AWTDisplay {

	private final ArrayList<DisplayView> views;
	private final List<EventSubscriber<?>> subscribers;

	private final JHotDrawImageCanvas imgCanvas;
	private final SwingDisplayWindow imgWindow;

	private boolean willRebuildImgWindow;

	private final Display thisDisplay;

	public SwingImageDisplay() {
		views = new ArrayList<DisplayView>();
		subscribers = new ArrayList<EventSubscriber<?>>();

		final DisplayManager displayManager = ImageJ.get(DisplayManager.class);
		displayManager.setActiveDisplay(this);
		subscribeToEvents(displayManager);

		imgCanvas = new JHotDrawImageCanvas(this);
		imgWindow = new SwingDisplayWindow(this);

		final EventDispatcher eventDispatcher = new AWTEventDispatcher(this, false);
		imgCanvas.addEventDispatcher(eventDispatcher);
		imgWindow.addEventDispatcher(eventDispatcher);

		imgWindow.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(final WindowEvent e) {
				for (final DisplayView view : new ArrayList<DisplayView>(getViews())) {
					view.dispose();
				}
			}
		});

		willRebuildImgWindow = false;
		thisDisplay = this;

		Events.publish(new DisplayCreatedEvent(this));
	}
	
	private void redoLayout()
	{
		imgWindow.redoLayout();
	}

	// -- Display methods --

	@Override
	public boolean canDisplay(final Dataset dataset) {
		return true;
	}

	@Override
	public void display(final Dataset dataset) {
		addView(new SwingDatasetView(this, dataset));
		update();
	}

	@Override
	public void display(final Overlay overlay) {
		addView(new SwingOverlayView(this, overlay));		
		update();
	}

	@Override
	public void update() {
		if (!willRebuildImgWindow) {
			imgWindow.update();
		}
		else { // rebuild image window
			// NB - if pan to be reset below we'll be zoomed on wrong part of image
			imgCanvas.setZoom(0); // original scale
			// NB - if x or y dims change without this image panned incorrectly
			// Must happen after setZoom() call
			imgCanvas.panReset();
			imgWindow.redoLayout();
			imgWindow.update();
			willRebuildImgWindow = false;
		}
		for (final DisplayView view : views) {
			view.update();
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
		view.dispose();
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
		return views.size() > 0 ? views.get(0) : null;
	}

	@Override
	public SwingDisplayWindow getDisplayWindow() {
		return imgWindow;
	}

	@Override
	public JHotDrawImageCanvas getImageCanvas() {
		return imgCanvas;
	}

	// -- Helper methods --

	@SuppressWarnings("synthetic-access")
	private DisplayManager
		subscribeToEvents(final DisplayManager displayManager)
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
				if (event.getDisplay() != thisDisplay) return;
				final UserInterface ui = ImageJ.get(UIManager.class).getUI();
				final ToolManager toolMgr = ui.getToolBar().getToolManager();
				imgCanvas.setCursor(toolMgr.getActiveTool().getCursor());
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
			public void onEvent(final DatasetRestructuredEvent event) {
				final Dataset dataset = event.getObject();
				for (final DisplayView view : views) {
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
