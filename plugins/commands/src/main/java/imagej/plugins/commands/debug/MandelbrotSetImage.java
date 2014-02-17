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

package imagej.plugins.commands.debug;

import imagej.command.Command;
import imagej.command.InteractiveCommand;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.display.ColorTables;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.data.lut.LUTService;
import imagej.data.overlay.Overlay;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.menu.MenuConstants;
import imagej.widget.Button;

import java.net.URL;
import java.util.List;
import java.util.Map;

import net.imglib2.RandomAccess;
import net.imglib2.display.ColorTable;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.CalibratedAxis;
import net.imglib2.meta.ImgPlus;
import net.imglib2.meta.axis.DefaultLinearAxis;
import net.imglib2.type.numeric.RealType;

import org.scijava.ItemIO;
import org.scijava.ItemVisibility;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Makes a color image that represents a Mandelbrot set using specified
 * parameters.
 * 
 * @author Barry DeZonia
 */
@Plugin(
	type = Command.class,
	initializer = "init",
	menu = {
		@Menu(label = MenuConstants.PLUGINS_LABEL,
			weight = MenuConstants.PLUGINS_WEIGHT,
			mnemonic = MenuConstants.PLUGINS_MNEMONIC),
		@Menu(label = "Sandbox", mnemonic = 's'),
		@Menu(label = "Mandelbrot Set", mnemonic = 'm', accelerator = "shift ^NUM0") })
public class MandelbrotSetImage extends InteractiveCommand {

	// -- constants --

	private static final String NAME = "Mandelbrot Set";
	private static final double CUTOFF = 1.8;
	private static final double DEFAULT_ORIGIN_X = -1.6;
	private static final double DEFAULT_ORIGIN_Y = -1.2;
	private static final double DEFAULT_EXTENT_X = 2.2;
	private static final double DEFAULT_EXTENT_Y = 2.2;

	// -- Parameters --

	@Parameter(visibility = ItemVisibility.MESSAGE, persist = false)
	private final String msg =
		"Draw a ROI on the Mandelbrot Set image and press Magnify.";

	@Parameter(label = "Reset", callback = "reset")
	private Button resetButton;

	@Parameter(label = "Magnify", callback = "preview")
	private Button magnifyButton;

	@Parameter(type = ItemIO.OUTPUT)
	private ImageDisplay display;

	@Parameter
	private ImageDisplayService imageDisplayService;

	@Parameter
	private DisplayService displayService;

	@Parameter
	private DatasetService datasetService;

	@Parameter
	private LUTService lutService;

	@Parameter
	private OverlayService overlayService;

	// -- other fields --

	private double originX = DEFAULT_ORIGIN_X;
	private double originY = DEFAULT_ORIGIN_Y;
	private double extentX = DEFAULT_EXTENT_X;
	private double extentY = DEFAULT_EXTENT_Y;

	// -- Command methods --

	@Override
	public void preview() {
		final Overlay o = overlayService.getActiveOverlay(display);
		setWindow(o);
		updateDisplay();
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
	}

	@Override
	public void run() {}

	// -- initializers --

	protected void init() {
		final List<Display<?>> displays = displayService.getDisplays();
		for (final Display<?> disp : displays) {
			if (disp.getName().startsWith(NAME)) {
				display = (ImageDisplay) disp;
				final Overlay o = overlayService.getActiveOverlay(display);
				setWindow(o);
				removeOverlays();
				return;
			}
		}
		setWindow(null);
		display = (ImageDisplay) createDisplay();
	}

	// -- callbacks --

	protected void reset() {
		setWindow(null);
		updateDisplay();
	}

	// -- helpers --

	private Display<?> createDisplay() {
		return displayService.createDisplay(dataset());
	}

	private void updateDisplay() {
		final Dataset ds = imageDisplayService.getActiveDataset(display);
		final ImgPlus<? extends RealType<?>> imgPlus = dataset().getImgPlus();
		ds.setImgPlus(imgPlus);
		removeOverlays();
	}

	private Dataset dataset() {
		final Dataset data = makeDataset();
		fillDataset(data);
		calibrate(data);
		applyLUT(data);
		return data;
	}

	private Dataset makeDataset() {
		final long SIZE = 768;
		final long[] dims =
			new long[] { Math.round(SIZE * extentX / extentY), SIZE };
		String name = NAME;
		name += " [" + originX + "," + originY + "] ";
		name += " [" + (originX + extentX) + "," + (originY + extentY) + "]";
		final AxisType[] axes = new AxisType[] { Axes.X, Axes.Y };
		final int bitsPerPixel = 8;
		final boolean signed = false;
		final boolean floating = false;
		return datasetService.create(dims, name, axes, bitsPerPixel, signed,
			floating);
	}

	private void fillDataset(final Dataset ds) {
		final RandomAccess<? extends RealType<?>> accessor =
			ds.getImgPlus().randomAccess();
		final double dx = extentX / ds.dimension(0);
		final double dy = extentY / ds.dimension(1);
		double cx = 0;
		for (long x = 0; x < ds.dimension(0); x++, cx += dx) {
			accessor.setPosition(x, 0);
			double cy = 0;
			for (long y = 0; y < ds.dimension(1); y++, cy += dy) {
				accessor.setPosition(y, 1);
				final short value = value(originX + cx, originY + cy);
				accessor.get().setReal(value);
			}
		}
	}

	// return a value from 0 to 255

	private short value(final double cx, final double cy) {
		double zx = cx;
		double zy = cy;
		short value = 255;
		while (value > 0) {
			// square self via complex multiply
			final double tx = zx;
			final double ty = zy;
			zx = tx * tx - ty * ty;
			zy = ty * tx + tx * ty;
			// and then add self
			zx += cx;
			zy += cy;
			if (Math.sqrt((zx * zx) + (zy * zy)) > CUTOFF) return value;
			value--;
		}
		return value;
	}

	private void applyLUT(final Dataset ds) {
		ds.initializeColorTables(1);
		ds.setColorTable(lut(), 0);
	}

	private ColorTable lut() {
		final Map<String, URL> luts = lutService.findLUTs();
		final URL lutURL = luts.get("WCIF/Rainbow RGB.lut");
		if (lutURL != null) {
			try {
				return lutService.loadLUT(lutURL);
			}
			catch (final Exception e) {
				// fall through
			}
		}
		return ColorTables.FIRE;
	}

	private void calibrate(final Dataset ds) {
		final CalibratedAxis xAxis =
			new DefaultLinearAxis(Axes.X, extentX / ds.dimension(0), originX);
		final CalibratedAxis yAxis =
			new DefaultLinearAxis(Axes.Y, extentY / ds.dimension(1), originY);
		ds.setAxis(xAxis, 0);
		ds.setAxis(yAxis, 1);
	}

	private void setWindow(final Overlay o) {
		if (o == null) {
			originX = DEFAULT_ORIGIN_X;
			originY = DEFAULT_ORIGIN_Y;
			extentX = DEFAULT_EXTENT_X;
			extentY = DEFAULT_EXTENT_Y;
		}
		else {
			final Dataset ds = imageDisplayService.getActiveDataset(display);
			final CalibratedAxis xAxis = ds.axis(0);
			final CalibratedAxis yAxis = ds.axis(1);
			originX = xAxis.calibratedValue(o.realMin(0));
			originY = yAxis.calibratedValue(o.realMin(1));
			final double cornerX = xAxis.calibratedValue(o.realMax(0));
			final double cornerY = yAxis.calibratedValue(o.realMax(1));
			extentX = cornerX - originX;
			extentY = cornerY - originY;
		}
	}

	private void removeOverlays() {
		for (final Overlay ovr : overlayService.getOverlays(display)) {
			overlayService.removeOverlay(display, ovr);
		}
	}
}
