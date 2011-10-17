//
// PanTool.java
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
import imagej.ext.display.MouseCursor;
import imagej.ext.display.event.input.KyPressedEvent;
import imagej.ext.display.event.input.MsDraggedEvent;
import imagej.ext.display.event.input.MsPressedEvent;
import imagej.ext.tool.AbstractTool;
import imagej.ext.tool.Tool;
import imagej.util.IntCoords;

/**
 * Tool for panning the display.
 * 
 * @author Rick Lentz
 * @author Grant Harris
 * @author Curtis Rueden
 */
@Tool(name = "Pan",
	description = "Scrolling tool (or press space bar and drag)",
	iconPath = "/icons/tools/pan.png", priority = PanTool.PRIORITY)
public class PanTool extends AbstractTool {

	public static final int PRIORITY = ZoomTool.PRIORITY + 1;

	private static final int PAN_AMOUNT = 10;

	// TODO - Add customization to set pan amount

	private int lastX, lastY;

	@Override
	public boolean onKeyDown(final KyPressedEvent evt) {
		final Display<?> display = evt.getDisplay();
		if (!(display instanceof ImageDisplay)) return false;
		final ImageDisplay imageDisplay = (ImageDisplay) display;

		switch (evt.getCode()) {
			case UP:
				imageDisplay.getCanvas().pan(new IntCoords(0, -PAN_AMOUNT));
				break;
			case DOWN:
				imageDisplay.getCanvas().pan(new IntCoords(0, PAN_AMOUNT));
				break;
			case LEFT:
				imageDisplay.getCanvas().pan(new IntCoords(-PAN_AMOUNT, 0));
				break;
			case RIGHT:
				imageDisplay.getCanvas().pan(new IntCoords(PAN_AMOUNT, 0));
				break;
			default:
		}
		return false;
	}

	@Override
	public boolean onMouseDown(final MsPressedEvent evt) {
		lastX = evt.getX();
		lastY = evt.getY();
		return true;
	}

	@Override
	public boolean onMouseDrag(final MsDraggedEvent evt) {
		final Display<?> display = evt.getDisplay();
		if (!(display instanceof ImageDisplay)) return false;
		final ImageDisplay imageDisplay = (ImageDisplay) display;

		final int xDelta = lastX - evt.getX();
		final int yDelta = lastY - evt.getY();
		imageDisplay.getCanvas().pan(new IntCoords(xDelta, yDelta));
		lastX = evt.getX();
		lastY = evt.getY();
		return true;
	}

	@Override
	public MouseCursor getCursor() {
		return MouseCursor.HAND;
	}

}
