/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.lut.LUTService;
import imagej.data.view.DatasetView;
import imagej.display.DisplayService;

import java.io.IOException;
import java.net.URL;

import net.imglib2.RandomAccess;
import net.imglib2.display.ColorTable;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.numeric.RealType;

import org.scijava.ItemIO;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * <p>
 * Reads a lookup table from a URL. If there is an active {@link ImageDisplay}
 * the lookup table is applied to its current view. Otherwise it creates a new
 * {@link ImageDisplay} in which in can display the lookup table.
 * <p>
 * Designed to be used by menu code. Lookup tables are discovered and menu
 * entries are constructed using their URLs with instances of this class.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class)
public class ApplyLookupTable implements Command {

	// -- Parameters --

	@Parameter
	private LogService logService;

	@Parameter
	private LUTService lutService;

	@Parameter
	private DisplayService displayService;

	@Parameter
	private ImageDisplayService imageDisplayService;

	@Parameter
	private DatasetService datasetService;

	@Parameter(required = false)
	private ImageDisplay display;

	@Parameter(required = false)
	private DatasetView view;

	@Parameter
	private URL tableURL;
	// = "file:///Users/bdezonia/Desktop/ImageJ/luts/6_shades.lut";

	@Parameter(type = ItemIO.OUTPUT)
	private ImageDisplay output;

	// -- constants --

	private static final int WIDTH = 256;
	private static final int HEIGHT = 32;

	// -- Command methods --

	@Override
	public void run() {
		if (tableURL == null) {
			logService.warn("ApplyLookupTable: no URL string provided.");
			return;
		}
		final ColorTable colorTable;
		try {
			colorTable = lutService.loadLUT(tableURL);
		}
		catch (IOException exc) {
			logService.error("ApplyLookupTable: error loading color table at URL: " +
				tableURL, exc);
			return;
		}
		if (colorTable == null) {
			logService.error("ApplyLookupTable: no color table at URL: " +
				tableURL);
			return;
		}
		if (display == null) {
			Dataset ds = makeData();
			display = (ImageDisplay) displayService.createDisplay(ds);
		}
		if (view == null) {
			view = imageDisplayService.getActiveDatasetView(display);
		}
		int channel = (int) view.getLongPosition(Axes.CHANNEL);
		view.setColorTable(colorTable, channel);
		output = display;
	}

	// -- private helpers --

	private Dataset makeData() {
		String urlString;
		try {
			urlString = tableURL.toURI().getPath();
		}
		catch (Exception e) {
			urlString = tableURL.getPath();
		}
		String name =
			urlString.substring(urlString.lastIndexOf("/") + 1, urlString.length());
		long[] dims = new long[] { WIDTH, HEIGHT };
		AxisType[] axes = new AxisType[] { Axes.X, Axes.Y };
		int bitsPerPixel = 8;
		boolean signed = false;
		boolean floating = false;
		Dataset ds =
			datasetService.create(dims, name, axes, bitsPerPixel, signed, floating);
		ramp(ds);
		return ds;
	}

	private void ramp(Dataset ds) {
		RandomAccess<? extends RealType<?>> accessor =
			ds.getImgPlus().randomAccess();
		for (int x = 0; x < WIDTH; x++) {
			accessor.setPosition(x, 0);
			for (int y = 0; y < HEIGHT; y++) {
				accessor.setPosition(y, 1);
				accessor.get().setReal(x);
			}
		}
	}
}
