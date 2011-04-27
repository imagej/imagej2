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

import imagej.display.Display;
import imagej.display.event.key.KyPressedEvent;
import imagej.display.event.mouse.MsButtonEvent;
import imagej.display.event.mouse.MsPressedEvent;
import imagej.display.event.mouse.MsReleasedEvent;
import imagej.display.event.mouse.MsWheelEvent;
import imagej.tool.BaseTool;
import imagej.tool.Tool;
import imagej.util.IntCoords;
import imagej.util.Rect;

/**
 * Tool for zooming in and out of a display.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
@Tool(name = "Zoom", description = "Image Zoom Tool",
	iconPath = "/tools/zoom.png", priority = ZoomTool.PRIORITY)
//
public class ZoomTool extends BaseTool {

	public static final int PRIORITY = 202;
	private IntCoords startPt;
	private final int minMouseDrag = 8;

	// -- ITool methods --

	@Override
	public void onMouseDown(final MsPressedEvent evt) {
		startPt = new IntCoords(evt.getX(), evt.getY());
	}

	@Override
	public void onMouseUp(final MsReleasedEvent evt) {
		System.out.println("onMouseUp");
		final Display display = evt.getDisplay();
		final IntCoords endPt = new IntCoords(evt.getX(), evt.getY());

		// mouse moved more than a lot - a rectangle was dragged
		if ((Math.abs(endPt.x - startPt.x) > minMouseDrag) ||
			(Math.abs(endPt.y - startPt.y) > minMouseDrag))
		{
			final int ox = Math.min(endPt.x, startPt.x);
			final int oy = Math.min(endPt.y, startPt.y);
			final int w = Math.abs(endPt.x - startPt.x) + 1;
			final int h = Math.abs(endPt.y - startPt.y) + 1;
			final Rect dragRegion = new Rect(ox, oy, w, h);
			display.zoomToFit(dragRegion);
		}
		else {
			// mouse barely moved : just zoom
			if (evt.getButton() == MsButtonEvent.LEFT_BUTTON) {
				display.zoomIn(evt.getX(), evt.getY());
			}
			else {
				display.zoomOut(evt.getX(), evt.getY());
			}
		}
	}

	@Override
	public void onKeyDown(final KyPressedEvent evt) {
		final Display display = evt.getDisplay();
		final char c = evt.getCharacter();
		if (c == '=' || c == '+') {
			display.zoomIn();
		}
		else if (c == '-') {
			display.zoomOut();
		}
	}

	@Override
	public void onMouseWheel(final MsWheelEvent evt) {
		final Display display = evt.getDisplay();
		// final IntCoords center = new IntCoords(evt.getX(), evt.getY());
		if (evt.getWheelRotation() > 0) {
			display.zoomIn(evt.getX(), evt.getY());
		}
		else {
			display.zoomOut(evt.getX(), evt.getY());
		}
	}

}
