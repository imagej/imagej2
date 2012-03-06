//
// OptionsChannels.java
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

package imagej.options.plugins;

import imagej.data.ChannelCollection;
import imagej.ext.menu.MenuConstants;
import imagej.ext.module.ItemVisibility;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.options.OptionsPlugin;
import imagej.util.ColorRGB;
import imagej.util.Colors;

/**
 * Runs the Edit::Options::Channels dialog.
 * 
 * @author Barry DeZonia 
 */
@Plugin(type = OptionsPlugin.class, menu = {
	@Menu(label = MenuConstants.EDIT_LABEL, weight = MenuConstants.EDIT_WEIGHT,
		mnemonic = MenuConstants.EDIT_MNEMONIC),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Channels...", weight = 9) })
public class OptionsChannels extends OptionsPlugin {

	@Parameter(label = "Foreground values")
	private String fgValuesString = "255,255,255";
	
	@Parameter(label = "Background values")
	private String bgValuesString = "0,0,0";

	@Parameter(label="Last foreground color",visibility=ItemVisibility.MESSAGE)
	private ColorRGB lastFgColor = Colors.WHITE;
	
	@Parameter(label="Last background color",visibility=ItemVisibility.MESSAGE)
	private ColorRGB lastBgColor = Colors.BLACK;
	
	private ChannelCollection fgValues;
	private ChannelCollection bgValues;
	
	// -- OptionsChannels methods --

	public OptionsChannels() {
		load(); // NB: Load persisted values *after* field initialization.
	}

	@Override
	public void load() {
		super.load();
		fgValues = decode(fgValuesString);
		bgValues = decode(bgValuesString);
	}

	@Override
	public void save() {
		fgValuesString = encode(fgValues);
		bgValuesString = encode(bgValues);
		super.save();
	}
	
	public ChannelCollection getFgValues() {
		return fgValues;
	}
	
	public ChannelCollection getBgValues() {
		return bgValues;
	}

	public ColorRGB getLastFgColor() {
		return lastFgColor;
	}
	
	public ColorRGB getLastBgColor() {
		return lastBgColor;
	}
	
	public void setLastFgColor(ColorRGB c) {
		lastFgColor = c;
	}
	
	public void setLastBgColor(ColorRGB c) {
		lastBgColor = c;
	}
	
	// -- private helpers --
	
	private ChannelCollection decode(String channelString) {
		ChannelCollection collection = new ChannelCollection();
		String[] values = channelString.split(",");
		long i = 0;
		for (String value : values) {
			double val;
			try {
				val = Double.parseDouble(value);
			} catch (NumberFormatException e) {
				val = 0;
			}
			collection.setChannelValue(i++,val);
		}
		return collection;
	}

	private String encode(ChannelCollection chans) {
		StringBuilder builder = new StringBuilder();
		long count = chans.getChannelCount();
		for (long i = 0; i < count; i++) {
			String valString;
			double value = chans.getChannelValue(i);
			if (value == Math.floor(value))
				valString = String.format("%d",(long)value);
			else
				valString = String.format("%.3f",value);
			if (i != 0) builder.append(",");
			builder.append(valString);
		}
		return builder.toString();
	}
}
