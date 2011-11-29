//
// PixelProbe.java
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

import imagej.ext.display.event.input.MsMovedEvent;
import imagej.ext.tool.AbstractTool;
import imagej.ext.tool.Tool;
import imagej.util.ColorRGB;

/**
 * Displays pixel values under the cursor.
 * 
 * @author Barry DeZonia
 */
@Tool(name = "Probe", alwaysActive = true)
public class PixelProbe extends AbstractTool {

	private PixelHelper helper = new PixelHelper();
	
	// -- ITool methods --

	@Override
	public void onMouseMove(final MsMovedEvent evt) {

		if (!helper.processEvent(evt)) return;

		long cx = helper.getCX();
		long cy = helper.getCY();
		String message;
		if (helper.isPureRGBCase()) {
			final ColorRGB color = helper.getColor();
			message = String.format("x=%d, y=%d, value=%d,%d,%d", cx, cy,
				color.getRed(), color.getGreen(), color.getBlue());
		}
		else {  // gray dataset
			final double value = helper.getValue();
			if (helper.isIntegerCase()) {
				message = String.format("x=%d, y=%d, value=%d", cx, cy, (long) value);
			}
			else { // is float case
				message = String.format("x=%d, y=%d, value=%f", cx, cy, value);
			}
		}
		helper.updateStatus(message);
	}

}
