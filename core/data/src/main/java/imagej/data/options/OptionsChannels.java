/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package imagej.data.options;

import imagej.data.ChannelCollection;
import imagej.menu.MenuConstants;
import imagej.options.OptionsPlugin;
import imagej.options.event.OptionsEvent;

import java.util.LinkedList;
import java.util.List;

import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.ColorRGB;
import org.scijava.util.Colors;
import org.scijava.util.Prefs;

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

	private static final String LAST_FG_RED =   "lastFgColor.red";
	private static final String LAST_FG_GREEN = "lastFgColor.green";
	private static final String LAST_FG_BLUE =  "lastFgColor.blue";
	private static final String LAST_BG_RED =   "lastBgColor.red";
	private static final String LAST_BG_GREEN = "lastBgColor.green";
	private static final String LAST_BG_BLUE =  "lastBgColor.blue";

	// TODO - this should become a List<Double> when that widget is supported
	@Parameter(label = "Foreground values")
	private String fgValuesString = "255,255,255";

	// TODO - this should become a List<Double> when that widget is supported
	@Parameter(label = "Background values")
	private String bgValuesString = "0,0,0";

	private ColorRGB lastFgColor = Colors.WHITE;

	private ColorRGB lastBgColor = Colors.BLACK;

	// -- instance variables that are not Parameters --

	private ChannelCollection fgValues; // used by modern ImageJ consumers
	private ChannelCollection bgValues; // used by modern ImageJ consumers

	// -- OptionsChannels methods --

	public ChannelCollection getFgValues() {
		return fgValues;
	}

	public ChannelCollection getBgValues() {
		return bgValues;
	}

	public void setFgValues(final ChannelCollection vals) {
		fgValues = vals;
		fgValuesString = encode(fgValues);
	}

	public void setBgValues(final ChannelCollection vals) {
		bgValues = vals;
		bgValuesString = encode(bgValues);
	}

	public ColorRGB getLastFgColor() {
		return lastFgColor;
	}

	public ColorRGB getLastBgColor() {
		return lastBgColor;
	}

	public void setLastFgColor(final ColorRGB c) {
		setLastFgColor(c, true);
	}

	public void setLastFgColor(final ColorRGB c, boolean notifyIJ1) {
		lastFgColor = c;
		// CTR FIXME: Find another way to take care of this.
//		save(); // must do in case interested parties need persisted info
		// make sure IJ1 knows about this change if possible
		if (notifyIJ1) eventService.publish(new OptionsEvent(this));
	}
	
	public void setLastBgColor(final ColorRGB c) {
		setLastBgColor(c, true);
	}
	
	public void setLastBgColor(final ColorRGB c, boolean notifyIJ1) {
		lastBgColor = c;
		// CTR FIXME: Find another way to take care of this.
//		save(); // must do in case interested parties need persisted info
		// make sure IJ1 knows about this change if possible
		if (notifyIJ1) eventService.publish(new OptionsEvent(this));
	}

	// -- OptionsPlugin methods --

	@Override
	public void load() {
		super.load();
		fgValues = decode(fgValuesString);
		bgValues = decode(bgValuesString);
		int r,g,b;
		r = Prefs.getInt(getClass(), LAST_FG_RED, 255);
		g = Prefs.getInt(getClass(), LAST_FG_GREEN, 255);
		b = Prefs.getInt(getClass(), LAST_FG_BLUE, 255);
		lastFgColor = new ColorRGB(r,g,b);
		r = Prefs.getInt(getClass(), LAST_BG_RED, 0);
		g = Prefs.getInt(getClass(), LAST_BG_GREEN, 0);
		b = Prefs.getInt(getClass(), LAST_BG_BLUE, 0);
		lastBgColor = new ColorRGB(r,g,b);
	}

	@Override
	public void save() {
		Prefs.put(getClass(), LAST_FG_RED,   lastFgColor.getRed());
		Prefs.put(getClass(), LAST_FG_GREEN, lastFgColor.getGreen());
		Prefs.put(getClass(), LAST_FG_BLUE,  lastFgColor.getBlue());
		Prefs.put(getClass(), LAST_BG_RED,   lastBgColor.getRed());
		Prefs.put(getClass(), LAST_BG_GREEN, lastBgColor.getGreen());
		Prefs.put(getClass(), LAST_BG_BLUE,  lastBgColor.getBlue());
		cleanStrings();
		super.save();
	}

	// -- private helpers --

	private ChannelCollection decode(final String channelString) {
		final List<Double> collection = new LinkedList<Double>();
		final String[] values = channelString.split(",");
		for (final String value : values) {
			double val;
			try {
				val = Double.parseDouble(value);
			}
			catch (final NumberFormatException e) {
				val = 0;
			}
			collection.add(val);
		}
		return new ChannelCollection(collection);
	}

	private String encode(final ChannelCollection chans) {
		final StringBuilder builder = new StringBuilder();
		final long count = chans.getChannelCount();
		for (long i = 0; i < count; i++) {
			final String valString;
			final double value = chans.getChannelValue(i);
			if (value == Math.floor(value)) {
				valString = String.format("%d", (long) value);
			}
			else valString = String.format("%f", value);
			if (i != 0) builder.append(",");
			builder.append(valString);
		}
		return builder.toString();
	}

	private void cleanStrings() {
		if (goodFormat(fgValuesString)) {
			fgValuesString = noWhitespace(fgValuesString);
		}
		else fgValuesString = encode(fgValues);
		if (goodFormat(bgValuesString)) {
			bgValuesString = noWhitespace(bgValuesString);
		}
		else bgValuesString = encode(bgValues);
	}

	private boolean goodFormat(final String valuesString) {
		final String[] values = valuesString.split(",");
		for (final String value : values) {
			try {
				Double.parseDouble(value);
			}
			catch (final NumberFormatException e) {
				return false;
			}
		}
		return true;
	}

	private String noWhitespace(final String str) {
		final StringBuilder builder = new StringBuilder();
		final int len = str.length();
		for (int i = 0; i < len; i++) {
			final char ch = str.charAt(i);
			if (!Character.isWhitespace(ch)) builder.append(ch);
		}
		return builder.toString();
	}

}
