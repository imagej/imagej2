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

package imagej.core.commands.misc;

import imagej.command.Command;
import imagej.data.display.DatasetView;
import imagej.data.display.ImageDisplay;
import imagej.data.lut.LutService;
import imagej.log.LogService;
import imagej.module.ItemIO;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import net.imglib2.display.ColorTable;
import net.imglib2.meta.Axes;

/**
 * Reads a lookup table from a URL and applies it to the current view of the
 * current {@link ImageDisplay}. Designed to be used by menu code. Lookup tables
 * are discovered and menu entries are constructed using their URLS with
 * instances of this class.
 * 
 * @author Barry DeZonia
 */
@Plugin
public class ApplyLookupTablePlugin implements Command {

	// -- Parameters --

	@Parameter(type = ItemIO.BOTH)
	private ImageDisplay display;

	@Parameter(type = ItemIO.INPUT)
	private DatasetView view;

	@Parameter(type = ItemIO.INPUT)
	private String tableURL;
	// = "file:///Users/bdezonia/Desktop/ImageJ/luts/6_shades.lut";

	@Parameter
	private LogService logService;

	@Parameter
	private LutService lutService;


	// -- Command methods --

	@Override
	public void run() {
		if (tableURL == null) {
			logService.warn("ApplyLookupTablePlugin: no url string provided.");
			return;
		}
		ColorTable colorTable = lutService.loadLut(tableURL);
		if (colorTable == null) {
			logService.error("ApplyLookupTablePlugin: could not load color table - " +
				tableURL);
			return;
		}
		int channel = (int) view.getLongPosition(Axes.CHANNEL);
		view.setColorTable(colorTable, channel);
	}

}
