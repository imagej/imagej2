//
// ProbeTool.java
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

import imagej.Coords;
import imagej.display.MouseCursor;
import imagej.display.event.mouse.MsMovedEvent;
import imagej.event.Events;
import imagej.event.StatusEvent;
import imagej.tool.BaseTool;
import imagej.tool.Tool;

import java.awt.Point;

/**
 * TODO
 * 
 * @author Rick Lentz
 * @author Grant Harris
 */
@Tool(name = "Probe", iconPath = "/tools/probe.png",
	description = "Probe Pixel Tool")
public class ProbeTool extends BaseTool {

	@Override
	public void onMouseMove(final MsMovedEvent evt) {
		final Object plane = evt.getDisplay().getCurrentPlane();
		final int x = evt.getX();
		final int y = evt.getY();
		final Point mousePos = new Point(x, y);
		final Coords coords =
			evt.getDisplay().getImageCanvas().panelToImageCoords(mousePos);
		final int imageWidth =
			evt.getDisplay().getImageCanvas().getImage().getWidth();
		if (evt.getDisplay().getImageCanvas().isInImage(mousePos)) {
			String s = "";
			final int cx = coords.getIntX();
			final int cy = coords.getIntY();
			final int offset = cx + imageWidth * cy;
			if (plane instanceof byte[]) {
				s = "" + (((byte[]) plane)[offset] & 0xFF);
			}
			else if (plane instanceof short[]) {
				s = "" + (((short[]) plane)[offset] & 0xffff);
			}
			else if (plane instanceof int[]) {
				s = "" + ((int[]) plane)[offset];
			}
			else if (plane instanceof float[]) {
				s = "" + ((float[]) plane)[offset];
			}
			else if (plane instanceof double[]) {
				s = "" + ((double[]) plane)[offset];
			}
			else {
				throw new IllegalStateException("Unknown data type: " +
					plane.getClass().getName());
			}
			Events.publish(new StatusEvent("x=" + cx + ", y=" + cy + ", value=" + s));
		}
		else {
			Events.publish(new StatusEvent(""));
		}
	}

	@Override
	public MouseCursor getCursor() {
		return MouseCursor.CROSSHAIR;
	}

}
