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
import imagej.display.event.mouse.MsMovedEvent;
import imagej.event.Events;
import imagej.event.StatusEvent;
import imagej.tool.BaseTool;
import imagej.tool.Tool;

//FIXME - cannot use AWT in ij-core-tools
import java.awt.Point;

/**
 * TODO
 * 
 * @author Rick Lentz
 * @author Grant Harris
 */
@Tool(name = "Probe",
iconPath = "/tools/probe.png",
description = "Probe Pixel Tool")
public class ProbeTool extends BaseTool {

	@Override
	public void onMouseMove(MsMovedEvent evt) {
		Object plane = evt.getDisplay().getCurrentPlane();
		int x = evt.getX();
		int y = evt.getY();
		Point mousePos = new Point(x,y);
		Coords coords = evt.getDisplay().getImageCanvas().panelToImageCoords(mousePos);
		int imageWidth = evt.getDisplay().getImageCanvas().getImage().getWidth();
		if (evt.getDisplay().getImageCanvas().isInImage(mousePos)) {
			String s = "";
			int offset = coords.getIntX() + imageWidth * coords.getIntY();
			if (plane instanceof byte[]) {
				s = "" + (((byte[]) plane)[offset] & 0xFF);
			} else if (plane instanceof short[]) {
				s = "" + (((short[]) plane)[offset] & 0xffff);
			} else if (plane instanceof int[]) {
				s = "" + ((int[]) plane)[offset];
			} else if (plane instanceof float[]) {
				s = "" + ((float[]) plane)[offset];
			} else if (plane instanceof double[]) {
				s = "" + ((double[]) plane)[offset];
			} else {
				throw new IllegalStateException("Unknown data type: "
						+ plane.getClass().getName());
			}
			Events.publish(new StatusEvent(s + "  (" + x + ", " + y + ")"));
		} else {
			Events.publish(new StatusEvent(""));
		}
	}

	// TODO

}
