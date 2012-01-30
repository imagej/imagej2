//
// PickerTool.java
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

import imagej.ext.display.event.input.KyPressedEvent;
import imagej.ext.display.event.input.KyReleasedEvent;
import imagej.ext.display.event.input.MsClickedEvent;
import imagej.ext.plugin.Plugin;
import imagej.ext.tool.AbstractTool;
import imagej.ext.tool.Tool;
import imagej.options.OptionsService;
import imagej.options.plugins.OptionsColors;
import imagej.util.ColorRGB;

/**
 * Sets foreground and background values when tool is active and mouse clicked
 * over an image.
 * 
 * @author Barry DeZonia
 */
@Plugin(
	type = Tool.class,
	name = "Picker",
	description = "Picker Tool (sets foreground/background colors/values)",
	iconPath = "/icons/tools/picker.png", priority = PickerTool.PRIORITY)
public class PickerTool extends AbstractTool {

	// -- constants --

	public static final int PRIORITY = -299;

	// -- instance variables --

	private final PixelHelper helper = new PixelHelper();
	private boolean altKeyDown = false;

	// -- Tool methods --

	@Override
	public void onMouseClick(final MsClickedEvent evt) {

		if (!helper.recordEvent(evt)) {
			evt.consume();
			return;
		}

		final OptionsColors options = getOptions();

		final double value = helper.getValue();
		final ColorRGB color = helper.getColor();

		// background case?
		if (altKeyDown) {
			if (helper.isPureRGBCase()) {
				options.setBgColor(color);
				colorMessage("BG", color);
			}
			else {
				options.setBgGray(value);
				grayMessage("BG", value);
			}
		}
		else { // foreground case
			if (helper.isPureRGBCase()) {
				options.setFgColor(color);
				colorMessage("FG", color);
			}
			else {
				options.setFgGray(value);
				grayMessage("FG", value);
			}
		}

		options.save();

		evt.consume();
	}

	@Override
	public void onKeyDown(final KyPressedEvent evt) {
		altKeyDown =
			evt.getModifiers().isAltDown() || evt.getModifiers().isAltGrDown();
		evt.consume();
	}

	@Override
	public void onKeyUp(final KyReleasedEvent evt) {
		altKeyDown =
			evt.getModifiers().isAltDown() || evt.getModifiers().isAltGrDown();
		evt.consume();
	}

	@Override
	public String getDescription() {
		OptionsColors opts = getOptions();
		StringBuilder sb = new StringBuilder();
		sb.append("Picker FG: ");
		ColorRGB fgColor = opts.getFgColor();
		ColorRGB bgColor = opts.getBgColor();
		double fgValue = opts.getFgGray();
		double bgValue = opts.getBgGray();
		sb.append(String.format("(%.3f) (%d,%d,%d)",
			fgValue, fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue()));
		sb.append("  BG: ");
		sb.append(String.format("(%.3f) (%d,%d,%d)",
			bgValue, bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue()));
		return sb.toString();
	}
	
	// -- private interface --

	private void colorMessage(final String label, final ColorRGB color) {
		final String message =
			String.format("%s color = (%d,%d,%d)", label, color.getRed(), color
				.getGreen(), color.getBlue());
		helper.updateStatus(message);
	}

	private void grayMessage(final String label, final double value) {
		String message;
		if (helper.isIntegerCase()) message =
			String.format("%s gray value = %d", label, (long) value);
		else message = String.format("%s gray value = %f", label, value);
		helper.updateStatus(message);
	}

	private OptionsColors getOptions() {
		final OptionsService service =
			getContext().getService(OptionsService.class);

		return service.getOptions(OptionsColors.class);
	}
}
