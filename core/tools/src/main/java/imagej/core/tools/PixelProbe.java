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

import net.imglib2.meta.Axes;
import imagej.ImageJ;
import imagej.data.ChannelCollection;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.ext.display.event.input.MsMovedEvent;
import imagej.ext.plugin.Plugin;
import imagej.ext.tool.AbstractTool;
import imagej.ext.tool.Tool;

/**
 * Displays pixel values under the cursor.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Tool.class, name = "Probe", alwaysActive = true)
public class PixelProbe extends AbstractTool {

	private final PixelHelper helper = new PixelHelper(false);

	// -- Tool methods --

	// TODO - this tool does not consume the events. Not sure this is correct.
	
	@Override
	public void onMouseMove(final MsMovedEvent evt) {
		
		final ImageDisplayService service = ImageJ.get(ImageDisplayService.class);
		final ImageDisplay disp = service.getActiveImageDisplay();
		if (disp == null) return;
		final int channelIndex = disp.getAxisIndex(Axes.CHANNEL);

		if (!helper.recordEvent(evt)) return;

		final long cx = helper.getCX();
		final long cy = helper.getCY();
		ChannelCollection values = helper.getValues();
		StringBuilder builder = new StringBuilder();
		builder.append("x=");
		builder.append(cx);
		builder.append(", y=");
		builder.append(cy);
		builder.append(", value=");
		// single channel image (no channel axis)
		if (channelIndex == -1) {
			if (helper.getDataset().isInteger())
				builder.append((long)values.getChannelValue(0));
			else
				builder.append(String.format("%f", values.getChannelValue(0)));
		}
		else { // has a channel axis
			int currChannel = disp.getIntPosition(channelIndex);
			String value = valueString(values.getChannelValue(currChannel));
			builder.append(value);
			builder.append(" from (");
			for (int i = 0; i < values.getChannelCount(); i++) {
				value = valueString(values.getChannelValue(i));
				if (i > 0) builder.append(",");
				builder.append(value);
			}
			builder.append(")");
		}
		helper.updateStatus(builder.toString());
	}
	
	// -- helpers --
	
	private String valueString(double value) {
		if (helper.getDataset().isInteger())
			return String.format("%d",(long)value);
		return String.format("%f", value);
	}

}
