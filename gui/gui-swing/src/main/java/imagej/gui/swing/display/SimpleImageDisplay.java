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
package imagej.gui.swing.display;

import imagej.display.NavigableImageCanvas;
import imagej.display.Display;
import imagej.display.DisplayController;
import imagej.display.DisplayView;
import imagej.display.EventDispatcher;
import imagej.display.ImageCanvas;
import imagej.display.ImageDisplayWindow;
import imagej.display.LayeredDisplay;
import imagej.model.Dataset;
import imagej.plugin.Plugin;

/**
 * TODO
 *
 * @author Curtis Rueden
 * @author Grant Harris
 */
@Plugin(type = Display.class)
public class SimpleImageDisplay implements Display {

	private ImageDisplayWindow imgWindow;
	private NavigableImageCanvas imgCanvas;
	private Dataset dataset;
	private DisplayController controller;

	public SimpleImageDisplay() {
	}

	@Override
	public boolean canDisplay(Dataset dataset) {
		return true;
	}

	@Override
	public void display(Dataset dataset) {
		this.dataset = dataset;
		//imgCanvas = new ImageCanvasSwing();
		imgCanvas = new NavigableImagePanel();
		imgWindow = new NavigableImageJFrame(imgCanvas);
		controller = new DisplayController(this);
		imgWindow.setDisplayController(controller);
		EventDispatcher dispatcher = new AWTEventDispatcher(this);
		imgCanvas.addEventDispatcher(dispatcher);
		imgCanvas.subscribeToToolEvents();
		imgWindow.addEventDispatcher(dispatcher);
		// TODO - use DisplayView instead of Dataset directly
		//imageFrame.setDataset(dataset);
		//((NavigableImageJFrame)imgWindow).pack();
		((NavigableImageJFrame)imgWindow).setVisible(true);
	}


	@Override
	public Dataset getDataset() {
		return dataset;
	}

	@Override
	public ImageDisplayWindow getImageDisplayWindow() {
		return imgWindow;
	}

	@Override
	public NavigableImageCanvas getImageCanvas() {
		return imgCanvas;
	}

	@Override
	public Object getCurrentPlane() {
		return controller.getCurrentPlane();
	}

	@Override
	public void pan(float x, float y) {
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
	public void setZoom(float factor, float centerX, float centerY) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void zoomIn(float centerX, float centerY) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void zoomOut(float centerX, float centerY) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void zoomToFit(int w, int h) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public float getZoom() {
		return 1f;
		//throw new UnsupportedOperationException("Not supported yet.");
	}


}
