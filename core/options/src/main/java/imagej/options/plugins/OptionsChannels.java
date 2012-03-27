/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.options.plugins;

import java.util.LinkedList;
import java.util.List;

import imagej.data.ChannelCollection;
import imagej.event.EventService;
import imagej.ext.menu.MenuConstants;
import imagej.ext.module.ItemVisibility;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.options.OptionsPlugin;
import imagej.options.event.OptionsEvent;
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

	// TODO - this should become a List<Double> when that widget is supported
	@Parameter(label = "Foreground values")
	private String fgValuesString = "255,255,255";
	
	// TODO - this should become a List<Double> when that widget is supported
	@Parameter(label = "Background values")
	private String bgValuesString = "0,0,0";

	// TODO
	// Ideally this would be truly invisible and persisted. We use it to
	// set IJ1 colors to IJ2 values.
	@Parameter(label="Last foreground color",visibility=ItemVisibility.MESSAGE)
	private ColorRGB lastFgColor = Colors.WHITE;
	
	// TODO
	// Ideally this would be truly invisible and persisted. We use it to
	// set IJ1 colors to IJ2 values.
	@Parameter(label="Last background color",visibility=ItemVisibility.MESSAGE)
	private ColorRGB lastBgColor = Colors.BLACK;
	
	// -- instance variables that are not Parameters --

	private ChannelCollection fgValues;  // used by IJ2 consumers
	private ChannelCollection bgValues;  // used by IJ2 consumers
	
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
		cleanStrings();
		super.save();
	}

	// NB - TEMP - FIXME This is a way to generate events when we have not been
	// run by the plugin service. When not run by plugin service our eventService
	// instance variable is NULL.
	
	public void setEventService(EventService s) {
		eventService = s;
	}
	
	public ChannelCollection getFgValues() {
		return fgValues;
	}
	
	public ChannelCollection getBgValues() {
		return bgValues;
	}

	public void setFgValues(ChannelCollection vals) {
		fgValues = vals;
		fgValuesString = encode(fgValues);
	}
	
	public void setBgValues(ChannelCollection vals) {
		bgValues = vals;
		bgValuesString = encode(bgValues);
	}

	public ColorRGB getLastFgColor() {
		return lastFgColor;
	}
	
	public ColorRGB getLastBgColor() {
		return lastBgColor;
	}
	
	public void setLastFgColor(ColorRGB c) {
		lastFgColor = c;
		save(); // must do in case interested parties need persisted info
		// make sure IJ1 knows about this change if possible
		if (eventService != null)
			eventService.publish(new OptionsEvent(this));
		// TODO FIXME - find a way to get a handle on the current EventService so
		// we can always publish those events.
	}
	
	public void setLastBgColor(ColorRGB c) {
		lastBgColor = c;
		save(); // must do in case interested parties need persisted info
		// make sure IJ1 knows about this change if possible
		if (eventService != null)
			eventService.publish(new OptionsEvent(this));
		// TODO FIXME - find a way to get a handle on the current EventService so
		// we can always publish those events.
	}
	
	// -- private helpers --
	
	private ChannelCollection decode(String channelString) {
		final List<Double> collection = new LinkedList<Double>();
		final String[] values = channelString.split(",");
		for (final String value : values) {
			double val;
			try {
				val = Double.parseDouble(value);
			} catch (NumberFormatException e) {
				val = 0;
			}
			collection.add(val);
		}
		return new ChannelCollection(collection);
	}

	private String encode(ChannelCollection chans) {
		final StringBuilder builder = new StringBuilder();
		final long count = chans.getChannelCount();
		for (long i = 0; i < count; i++) {
			final String valString;
			final double value = chans.getChannelValue(i);
			if (value == Math.floor(value))
				valString = String.format("%d",(long)value);
			else
				valString = String.format("%f",value);
			if (i != 0) builder.append(",");
			builder.append(valString);
		}
		return builder.toString();
	}

	private void cleanStrings() {
		if (goodFormat(fgValuesString))
			fgValuesString = noWhitespace(fgValuesString);
		else
			fgValuesString = encode(fgValues);
		if (goodFormat(bgValuesString))
			bgValuesString = noWhitespace(bgValuesString);
		else
			bgValuesString = encode(bgValues);
	}
	
	private boolean goodFormat(String valuesString) {
		final String[] values = valuesString.split(",");
		for (final String value : values) {
			try {
				Double.parseDouble(value);
			} catch (NumberFormatException e) {
				return false;
			}
		}
		return true;
	}
	
	private String noWhitespace(String str) {
		StringBuilder builder = new StringBuilder();
		int len = str.length();
		for (int i = 0; i < len; i++) {
			char ch = str.charAt(i);
			if (!Character.isWhitespace(ch)) builder.append(ch);
		}
		return builder.toString();
	}
}
