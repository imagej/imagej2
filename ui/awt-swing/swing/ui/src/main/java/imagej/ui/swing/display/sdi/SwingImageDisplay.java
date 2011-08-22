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

package imagej.ui.swing.display.sdi;

import imagej.ui.swing.display.sdi.SwingDisplayWindow;
import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.event.DatasetRestructuredEvent;
import imagej.data.roi.Overlay;
import imagej.display.AbstractImageDisplay;
import imagej.display.ImageDisplay;
import imagej.display.DisplayService;
import imagej.display.DisplayView;
import imagej.display.DisplayWindow;
import imagej.display.ImageCanvas;
import imagej.display.event.window.WinActivatedEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.ext.plugin.Plugin;
import imagej.tool.ToolService;
import imagej.ui.common.awt.AWTDisplay;
import imagej.ui.common.awt.AWTKeyEventDispatcher;
import imagej.ui.common.awt.AWTMouseEventDispatcher;
import imagej.ui.common.awt.AWTWindowEventDispatcher;
import imagej.ui.swing.display.JHotDrawImageCanvas;
import imagej.ui.swing.display.SwingDatasetView;
import imagej.ui.swing.display.SwingDisplayPanel;
import imagej.ui.swing.display.SwingOverlayView;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;

/**
 * A Swing image display plugin, which displays 2D planes in grayscale or
 * composite color.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 * @author Barry DeZonia
 */
@Plugin(type = ImageDisplay.class)
public class SwingImageDisplay extends AbstractImageDisplay implements AWTDisplay {

	private final JHotDrawImageCanvas imgCanvas;
	private final SwingDisplayPanel imgPanel;

	private final ImageDisplay thisDisplay;

//	private EventSubscriber<WinClosedEvent> winCloseSubscriber;
//	private EventSubscriber<DatasetRestructuredEvent> restructureSubscriber;
//	private EventSubscriber<WinActivatedEvent> winActivatedSubscriber;

	/** Maintain list of subscribers, to avoid garbage collection. */
	private List<EventSubscriber<?>> subscribers;

	private String name;

	public SwingImageDisplay() {
		imgCanvas = new JHotDrawImageCanvas(this);
		DisplayWindow window = new SwingDisplayWindow();
		imgPanel = new SwingDisplayPanel(this, window);

		//final EventDispatcher eventDispatcher =new AWTEventDispatcher(this, false);
		imgCanvas.addEventDispatcher(new AWTMouseEventDispatcher(this, false));
		imgPanel.addEventDispatcher(new AWTKeyEventDispatcher(this));
		window.addEventDispatcher(new AWTWindowEventDispatcher(this));
		subscribeToEvents();

		thisDisplay = this;
	}

	// -- ImageDisplay methods --

	@Override
	public boolean canDisplay(final Dataset dataset) {
		return true;
	}

	@Override
	public void display(final Dataset dataset) {
		// GBH: Regarding naming/id of the display...
		// For now, we will use the original (first) dataset name

		final String datasetName = dataset.getName();
		createName(datasetName);
		imgPanel.setTitle(this.getName());
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
		EventQueue.invokeLater(new Runnable() {

			@SuppressWarnings("synthetic-access")
			@Override
			public void run() {

				imgPanel.update();

				// the following code is also done in SwingDisplayPanel::update()
				// (which was just called) so commenting out
				
				//for (final DisplayView view : getViews()) {
				//	view.update();
				//}
			}
		});
	}

	@Override
	public SwingDisplayPanel getDisplayPanel() {
		return imgPanel;
	}

	@Override
	public ImageCanvas getImageCanvas() {
		return imgCanvas;
	}

	@Override
	public void redoWindowLayout() {
		imgPanel.redoLayout();
	}

	// -- Named methods --

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	// -- Helper methods --

	@SuppressWarnings("synthetic-access")
	private void subscribeToEvents() {

		subscribers = new ArrayList<EventSubscriber<?>>();

		// CTR TODO - listen for imgWindow windowClosing and send
		// DisplayDeletedEvent. Think about how best this should work...
		// Is a display always deleted when its window is closed?
		// BDZ note - I added some code to winCloseSubscriber below
		// to do some cleanup work. Fix if needed.

		final EventSubscriber<WinActivatedEvent> winActivatedSubscriber =
			new EventSubscriber<WinActivatedEvent>() {

				@Override
				public void onEvent(final WinActivatedEvent event) {
					if (event.getDisplay() != thisDisplay) return;
					// final UserInterface ui = ImageJ.get(UIService.class).getUI();
					// final ToolService toolMgr = ui.getToolBar().getToolService();
					final ToolService toolService = ImageJ.get(ToolService.class);
					imgCanvas.setCursor(toolService.getActiveTool().getCursor());
				}
			};
		subscribers.add(winActivatedSubscriber);
		Events.subscribe(WinActivatedEvent.class, winActivatedSubscriber);

		final EventSubscriber<DatasetRestructuredEvent> restructureSubscriber =
			new EventSubscriber<DatasetRestructuredEvent>() {

				@Override
				public void onEvent(final DatasetRestructuredEvent event) {
					// NOTE - this code used to just note that a rebuild was necessary
					//   and had the rebuild done in update(). But due to timing of
					//   events it possible to get the update() before this call.
					//   So make this do a rebuild. In some cases update() will be
					//   called twice. Not sure if avoiding this was the reason to
					//   just record and do work in update. Or if that code was to
					//   avoid some other bug. Changing on 8-18-11. Fixed bug #627
					//   and bug #605. BDZ
					final Dataset dataset = event.getObject();
					for (final DisplayView view : getViews()) {
						if (dataset == view.getDataObject()) {
							// NB - if just panReset() we'll be zoomed on wrong part of image
							imgCanvas.setZoom(0); // original scale
							// NB - if x or y dims change without panReset() image panned
							// incorrectly. The panReset() call must happen after the
							// setZoom() call
							imgCanvas.panReset();
							SwingImageDisplay.this.redoWindowLayout();
							SwingImageDisplay.this.update();
							return;
						}
					}
				}
			};
		subscribers.add(restructureSubscriber);
		Events.subscribe(DatasetRestructuredEvent.class, restructureSubscriber);
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
