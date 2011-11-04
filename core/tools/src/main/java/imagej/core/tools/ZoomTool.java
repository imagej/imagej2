//
// ZoomTool.java
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

import imagej.data.display.ImageDisplay;
import imagej.ext.display.Display;
import imagej.ext.display.event.input.MsButtonEvent;
import imagej.ext.display.event.input.MsMovedEvent;
import imagej.ext.display.event.input.MsPressedEvent;
import imagej.ext.display.event.input.MsReleasedEvent;
import imagej.ext.display.event.input.MsWheelEvent;
import imagej.ext.tool.AbstractTool;
import imagej.ext.tool.Tool;
import imagej.util.IntCoords;

/**
 * Tool for zooming in and out of a display.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
@Tool(name = "Zoom",
	description = "Magnifying glass (or use \"+\" and \"-\" keys)",
	iconPath = "/icons/tools/zoom.png", priority = ZoomTool.PRIORITY)
public class ZoomTool extends AbstractTool {

	public static final int PRIORITY = 200;

	private static final int DRAG_THRESHOLD = 8;

	private final IntCoords mousePos = new IntCoords(0, 0);
	private final IntCoords mouseDown = new IntCoords(0, 0);
	private final IntCoords mouseUp = new IntCoords(0, 0);

	// -- ITool methods --

	// NB
	//   removed onKeyDown() method because zoom events were doubling.
	//   make sure elsewhere that the zoom plugins get called appropriately.
	
	@Override
	public void onMouseDown(final MsPressedEvent evt) {
		mouseDown.x = evt.getX();
		mouseDown.y = evt.getY();
		evt.consume();
	}

	@Override
	public void onMouseUp(final MsReleasedEvent evt) {
		final Display<?> display = evt.getDisplay();
		if (!(display instanceof ImageDisplay)) return;
		final ImageDisplay imageDisplay = (ImageDisplay) display;

		mouseUp.x = evt.getX();
		mouseUp.y = evt.getY();
		final int xDist = Math.abs(mouseUp.x - mouseDown.x);
		final int yDist = Math.abs(mouseUp.y - mouseDown.y);

		// ensure mouse movement exceeds threshold
		if (xDist > DRAG_THRESHOLD || yDist > DRAG_THRESHOLD) {
			// over threshold: zoom to rectangle
			if (mouseUp.x < mouseDown.x) {
				// swap X coordinates
				final int x = mouseUp.x;
				mouseUp.x = mouseDown.x;
				mouseDown.x = x;
			}
			if (mouseUp.y < mouseDown.y) {
				// swap Y coordinates
				final int y = mouseUp.y;
				mouseUp.y = mouseDown.y;
				mouseDown.y = y;
			}
			imageDisplay.getCanvas().zoomToFit(mouseDown, mouseUp);
		}
		else {
			// under threshold: just zoom
			final boolean zoomOut =
				evt.getButton() != MsButtonEvent.LEFT_BUTTON ||
					evt.getModifiers().isCtrlDown();
			if (zoomOut) {
				imageDisplay.getCanvas().zoomOut(mouseDown);
			}
			else {
				imageDisplay.getCanvas().zoomIn(mouseDown);
			}
		}
		evt.consume();
	}

	@Override
	public void onMouseMove(final MsMovedEvent evt) {
		mousePos.x = evt.getX();
		mousePos.y = evt.getY();
		evt.consume();
	}

	@Override
	public void onMouseWheel(final MsWheelEvent evt) {
		final Display<?> display = evt.getDisplay();
		if (!(display instanceof ImageDisplay)) return;
		final ImageDisplay imageDisplay = (ImageDisplay) display;

		final IntCoords center = new IntCoords(evt.getX(), evt.getY());
		if (evt.getWheelRotation() < 0) {
			imageDisplay.getCanvas().zoomIn(center);
		}
		else {
			imageDisplay.getCanvas().zoomOut(center);
		}
		evt.consume();
	}

}
