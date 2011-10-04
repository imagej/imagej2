//
// AbstractSwingImageDisplay.java
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
import imagej.data.display.AbstractImageDisplay;
import imagej.data.display.DataView;
import imagej.data.display.DisplayWindow;
import imagej.data.event.DatasetRestructuredEvent;
import imagej.data.roi.Overlay;
import imagej.event.EventSubscriber;
import imagej.ext.display.DisplayService;
import imagej.ext.display.event.window.WinActivatedEvent;
import imagej.ext.tool.ToolService;
import imagej.ui.common.awt.AWTMouseEventDispatcher;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;

/**
 * A Swing image display plugin, which displays 2D planes in grayscale or
 * composite color. Intended to be subclassed by a concrete implementation that
 * provides a {@link DisplayWindow} in which the display should be housed.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 * @author Barry DeZonia
 */
public abstract class AbstractSwingImageDisplay extends AbstractImageDisplay {

	protected final DisplayWindow window;
	protected final SwingDisplayPanel imgPanel;
	
	private final JHotDrawImageCanvas imgCanvas;

	/** Maintain list of subscribers, to avoid garbage collection. */
	private List<EventSubscriber<?>> subscribers;

	public AbstractSwingImageDisplay(final DisplayWindow window) {
		this.window = window;
		
		imgCanvas = new JHotDrawImageCanvas(this);
		imgPanel = new SwingDisplayPanel(this, window);

		imgCanvas.addEventDispatcher(new AWTMouseEventDispatcher(this, eventService, false));
		subscribeToEvents();
	}
	
	// -- ImageDisplay methods --

	@Override
	public void display(final Dataset dataset) {
		// GBH: Regarding naming/id of the display...
		// For now, we will use the original (first) dataset name
		final String datasetName = dataset.getName();
		createName(datasetName);
		imgPanel.setTitle(this.getName());
		add(new SwingDatasetView(this, dataset));
		redoWindowLayout();
		update();
	}

	@Override
	public void display(final Overlay overlay) {
		add(new SwingOverlayView(this, overlay));
		redoWindowLayout();
		update();
	}

	@Override
	public JHotDrawImageCanvas getImageCanvas() {
		return imgCanvas;
	}

	@Override
	public void redoWindowLayout() {
		imgPanel.redoLayout();
	}

	// -- Display methods --

	@Override
	public void update() {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				imgPanel.update();
			}
		});
	}

	@Override
	public SwingDisplayPanel getDisplayPanel() {
		return imgPanel;
	}

	// -- Helper methods --

	@SuppressWarnings("synthetic-access")
	private void subscribeToEvents() {

		subscribers = new ArrayList<EventSubscriber<?>>();

		// CTR TODO - listen for imgWindow windowClosing and send
		// DisplayDeletedEvent. Think about how best this should work...
		// Is a display always deleted when its window is closed?

		final EventSubscriber<WinActivatedEvent> winActivatedSubscriber =
			new EventSubscriber<WinActivatedEvent>() {

				@Override
				public void onEvent(final WinActivatedEvent event) {
					if (event.getDisplay() != AbstractSwingImageDisplay.this) return;
					// final UserInterface ui = ImageJ.get(UIService.class).getUI();
					// final ToolService toolMgr = ui.getToolBar().getToolService();
					final ToolService toolService = ImageJ.get(ToolService.class);
					imgCanvas.setCursor(toolService.getActiveTool().getCursor());
				}
			};
		subscribers.add(winActivatedSubscriber);
		eventService.subscribe(WinActivatedEvent.class, winActivatedSubscriber);

		final EventSubscriber<DatasetRestructuredEvent> restructureSubscriber =
			new EventSubscriber<DatasetRestructuredEvent>() {

				@Override
				public void onEvent(final DatasetRestructuredEvent event) {
					// NOTE - this code used to just note that a rebuild was necessary
					// and had the rebuild done in update(). But due to timing of
					// events it is possible to get the update() before this call.
					// So make this do a rebuild. In some cases update() will be
					// called twice. Not sure if avoiding this was the reason we used
					// to just record and do work in update. Or if that code was to
					// avoid some other bug. Changing on 8-18-11. Fixed bug #627
					// and bug #605. BDZ
					final Dataset dataset = event.getObject();
					for (final DataView view : AbstractSwingImageDisplay.this) {
						if (dataset == view.getData()) {
							// BDZ - calls to imgCanvas.setZoom(0) followed by
							// imgCanvas.panReset() removed from here to fix bug #797.
							AbstractSwingImageDisplay.this.redoWindowLayout();
							AbstractSwingImageDisplay.this.update();
							return;
						}
					}
				}
			};
		subscribers.add(restructureSubscriber);
		eventService.subscribe(DatasetRestructuredEvent.class, restructureSubscriber);
	}

	/** Name this display with unique id. */
	private void createName(final String baseName) {
		final DisplayService displayService = ImageJ.get(DisplayService.class);
		String theName = baseName;
		int n = 0;
		while (!displayService.isUniqueName(theName)) {
			n++;
			theName = baseName + "-" + n;
		}
		this.setName(theName);
	}

}
