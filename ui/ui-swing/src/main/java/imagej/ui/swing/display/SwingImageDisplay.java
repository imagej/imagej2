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
import imagej.display.DisplayService;
import imagej.display.DisplayView;
import imagej.display.EventDispatcher;
import imagej.display.event.window.WinActivatedEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.ext.plugin.Plugin;
import imagej.tool.ToolService;
import imagej.ui.common.awt.AWTDisplay;
import imagej.ui.common.awt.AWTEventDispatcher;

import java.util.ArrayList;
import java.util.List;


/**
 * A Swing image display plugin, which displays 2D planes in grayscale or
 * composite color.
 * 
 * Added 
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 * @author Barry DeZonia
 */
@Plugin(type = Display.class)
public class SwingImageDisplay extends AbstractDisplay implements AWTDisplay {

	private final JHotDrawImageCanvas imgCanvas;
	private final SwingDisplayWindow imgWindow;

	private boolean willRebuildImgWindow;

	private final Display thisDisplay;

//	private EventSubscriber<WinClosedEvent> winCloseSubscriber;
//	private EventSubscriber<DatasetRestructuredEvent> restructureSubscriber;
//	private EventSubscriber<WinActivatedEvent> winActivatedSubscriber;
	/** Maintain list of subscribers, to avoid garbage collection. */
	private List<EventSubscriber<?>> subscribers;
	//
	private String name;

	public SwingImageDisplay() {

		//final DisplayService displayService = ImageJ.get(DisplayService.class);
		//displayService.setActiveDisplay(this);

		imgCanvas = new JHotDrawImageCanvas(this);
		imgWindow = new SwingDisplayWindow(this);

		final EventDispatcher eventDispatcher = new AWTEventDispatcher(this, false);
		imgCanvas.addEventDispatcher(eventDispatcher);
		imgWindow.addEventDispatcher(eventDispatcher);
		subscribeToEvents();
		
		willRebuildImgWindow = false;
		thisDisplay = this;
	}
	
	/* Unused
	private void redoLayout()
	{
		imgWindow.redoLayout();
	}
	*/
	
	// -- Display methods --

	@Override
	public boolean canDisplay(final Dataset dataset) {
		return true;
	}

	/*
	 * GBH: Regarding naming/id of the display...
	 * For now, we will use the original (first) dataset name
	 */
	
	@Override
	public void display(final Dataset dataset) {
		final String datasetName = dataset.getName();
		createName(datasetName);
		imgWindow.setTitle(this.getName());
		addView(new SwingDatasetView(this, dataset));
		update();
	}

	@Override
	public void display(final Overlay overlay) {
		addView(new SwingOverlayView(this, overlay));		
		update();
	}
	
			
	// Name this display with unique id
	private void createName(String baseName) {
		final DisplayService displayService = ImageJ.get(DisplayService.class);
		String theName = baseName;
		int n = 0;
		while (!displayService.isUniqueName(theName)) {
			n++;
			theName = baseName + "-" + n;
		}
		this.setName(theName);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
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
		for (final DisplayView view : getViews()) {
			view.update();
		}
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
	private void subscribeToEvents(){
		
		subscribers = new ArrayList<EventSubscriber<?>>();
		
		// CTR TODO - listen for imgWindow windowClosing and send
		// DisplayDeletedEvent. Think about how best this should work...
		// Is a display always deleted when its window is closed?
		//  BDZ note - I added some code to winCloseSubscriber below
		//    to do some cleanup work. Fix if needed.

		 EventSubscriber<WinActivatedEvent> winActivatedSubscriber = 
				 new EventSubscriber<WinActivatedEvent>() {
			@Override
			public void onEvent(final WinActivatedEvent event) {
				if (event.getDisplay() != thisDisplay) return;
				//final UserInterface ui = ImageJ.get(UIService.class).getUI();
				//final ToolService toolMgr = ui.getToolBar().getToolService();
				final ToolService toolService = ImageJ.get(ToolService.class);
				imgCanvas.setCursor(toolService.getActiveTool().getCursor());
			}
		};
		subscribers.add(winActivatedSubscriber);
		Events.subscribe(WinActivatedEvent.class, winActivatedSubscriber);

		EventSubscriber<DatasetRestructuredEvent> restructureSubscriber =
			new EventSubscriber<DatasetRestructuredEvent>()
		{
			@Override
			public void onEvent(final DatasetRestructuredEvent event) {
				final Dataset dataset = event.getObject();
				for (final DisplayView view : getViews()) {
					if (dataset == view.getDataObject()) {
						willRebuildImgWindow = true;
						return;
					}
				}
			}
		};
		subscribers.add(restructureSubscriber);
		Events.subscribe(DatasetRestructuredEvent.class, restructureSubscriber);
	}

	@Override
	public void redoWindowLayout() {
		imgWindow.redoLayout();
	}

	//@Override
	//public void close() {
	//	imgWindow.close();
	//}


}
