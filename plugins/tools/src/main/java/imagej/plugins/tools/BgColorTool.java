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

package imagej.plugins.tools;

import imagej.data.ChannelCollection;
import imagej.data.options.OptionsChannels;
import imagej.tool.Tool;

import org.scijava.plugin.Plugin;
import org.scijava.util.ColorRGB;
import org.scijava.util.Colors;

/**
 * The tool that displays the current background color.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Tool.class, name = "Background",
	iconPath = "/icons/tools/blankBlack.png", priority = BgColorTool.PRIORITY)
public class BgColorTool extends AbstractColorTool {

	public static final int PRIORITY = BASE_PRIORITY - 1;

	@Override
	ColorRGB getEmptyColor() {
		return Colors.BLACK;
	}

	@Override
	ColorRGB getOutlineColor() {
		return Colors.ORANGE;
	}

	@Override
	ChannelCollection getChannels(final OptionsChannels options) {
		return options.getBgValues();
	}

	@Override
	void
		setChannels(final OptionsChannels options, final ChannelCollection chans)
	{
		options.setBgValues(chans);
	}

	@Override
	void setLastColor(final OptionsChannels options, final ColorRGB color) {
		options.setLastBgColor(color);
	}

	@Override
	String getLabel() {
		return "BG";
	}

}
