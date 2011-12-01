//
// FGTool.java
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

import imagej.ext.display.event.input.MsClickedEvent;
import imagej.ext.tool.AbstractTool;
import imagej.ext.tool.Tool;
import imagej.util.ColorRGB;

// TODO - this code adapted from PixelProbe. Update both this and that to
// share some code.

/**
 * Sets foreground value when tool is active and mouse clicked over an image.
 * 
 * @author Barry DeZonia
 */
@Tool(name = "FGTool",
	description = "Drawing value tool (sets foreground color/value)",
	iconPath = "/icons/tools/fgtool.png",
	priority = FGTool.PRIORITY)
public class FGTool extends AbstractTool {

	// -- constants --
	
	public static final int PRIORITY = -299;

	// -- instance variables --
	
	private PixelHelper helper = new PixelHelper();
	private ColorRGB fgColor = new ColorRGB(0,0,0);
	private double fgValue = 0;

	// -- ValueTool methods --
	
	public ColorRGB getFgColor() {
		return new ColorRGB(
			fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue());
	}

	public double getFgValue() {
		return fgValue;
	}

	// -- ITool methods --

	@Override
	public void onMouseClick(final MsClickedEvent evt) {

		if (!helper.processEvent(evt)) return;

		fgColor = helper.getColor();
		if (!helper.isPureRGBCase()) {
			fgValue = helper.getValue();
		}
		String message = String.format("(%d,%d,%d)",
			fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue());
		helper.updateStatus(message);
	}

	// -- private interface --

}
