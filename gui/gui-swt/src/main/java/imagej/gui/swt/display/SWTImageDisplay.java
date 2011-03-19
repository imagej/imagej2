//
// SWTImageDisplay.java
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

package imagej.gui.swt.display;

import imagej.display.Display;
import imagej.display.ImageCanvas;
import imagej.display.ImageDisplayWindow;
import imagej.display.NavigableImageCanvas;
import imagej.model.Dataset;
import imagej.plugin.Plugin;

/**
 * TODO
 *
 * @author Curtis Rueden
 */
@Plugin(type = Display.class)
public class SWTImageDisplay implements Display {

	private SWTImageFrame imageFrame;

	@Override
	public boolean canDisplay(Dataset dataset) {
		return true;
	}

	@Override
	public void display(final Dataset dataset) {
		imageFrame = new SWTImageFrame();

		// TODO - listen for user input

		// TODO - use DisplayView instead of Dataset directly
		imageFrame.setDataset(dataset);
		imageFrame.open();
	}

	@Override
	public void pan(float x, float y) {
		// TODO
	}

	@Override
	public float getPanX() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public float getPanY() {
		throw new UnsupportedOperationException("Not supported yet.");
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
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Dataset getDataset() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public ImageDisplayWindow getImageDisplayWindow() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public NavigableImageCanvas getImageCanvas() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Object getCurrentPlane() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/*
	Viewer	API ideas
	From Aivar March 15

// Scales to fit within arbitrary screen dimensions.
scaleToFit(Dimension dim)

// Gets screen dimensions.
Dimension getScreenDimension()

// Scales (closest) to arbitrary factor.
scaleFactor(float factor)

// Gets scale factor.
float getScaleFactor()

// Pans in screen increments, as much as possible.
//
// Used when you drag the mouse with the hand tool selected.
pan(int x, int y)

// Zooms in or out, if possible.
// This assumes that the Viewer
// Used when you press '+' or '-'.
void zoom(boolean in)

// Zooms in or out, if possible, and re-centers, in screen coordinates.
//
// Used when you click on the image with the zoom tool selected.
zoom(boolean in, int x, int y)

// Converts scaled screen coordinates to raw image coordinates.
//
// Coordinate is just some class to encapsulate x, y.
Coordinate screenToImage(Coordinate coord);

// Converts raw image coordinates to scaled screen coordinates.
//
// Can be negative or greater than screen window dimensions.
Coordinate imageToScreen(Coordinate coord);

// Invalidate image rectangles.
//
// Not called from the UI but called after ROI-based or otherwise.
// localized image changes. Forces retile and redraw.
void invalidateRects(Rectangle[] rects);

// Invalidate whole image.
//
// Called after image changes. Forces retile and redraw.
void invalidate();
*/

}
