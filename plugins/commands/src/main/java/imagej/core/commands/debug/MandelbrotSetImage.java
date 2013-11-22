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

package imagej.core.commands.debug;

import imagej.command.Command;
import imagej.command.ContextCommand;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.display.ColorTables;
import imagej.data.lut.LUTFinder;
import imagej.data.lut.LUTService;
import imagej.widget.Button;

import java.net.URL;
import java.util.Map;

import net.imglib2.RandomAccess;
import net.imglib2.display.ColorTable;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.CalibratedAxis;
import net.imglib2.meta.axis.DefaultLinearAxis;
import net.imglib2.type.numeric.RealType;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Makes a color image that represents a Mandelbrot set using specified
 * parameters.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, menuPath = "Plugins>Sandbox>Mandelbrot Set")
public class MandelbrotSetImage extends ContextCommand {

	// TODO
	// make a DynamicPlugin
	// When launched it determines if a Mandelbrot Set Dataset exists.
	// if not then hatch one with default extents
	// else make existing one the active dataset
	// Dialog should have one button that changes existing dataset in place
	// User places a Rectangular roi
	// Pressing button determines user coords of roi and rebuilds a new image
	// and updates active mandelbrot set image in place.
	// Dialog stays active until closed.

	// -- constants --

	private static final double CUTOFF = 1.8;
	private static final double DEFAULT_ORIGIN_X = -1.6;
	private static final double DEFAULT_ORIGIN_Y = -1.2;
	private static final double DEFAULT_EXTENT_X = 2.2;
	private static final double DEFAULT_EXTENT_Y = 2.2;

	// -- Parameters --

	@Parameter(label = "X origin")
	private double originX = DEFAULT_ORIGIN_X;

	@Parameter(label = "Y origin")
	private double originY = DEFAULT_ORIGIN_Y;

	@Parameter(label = "X extent", min = "0.0000001")
	private double extentX = DEFAULT_EXTENT_X;

	@Parameter(label = "Y extent", min = "0.0000001")
	private double extentY = DEFAULT_EXTENT_Y;

	@Parameter(label = "Reset defaults", callback = "reset")
	private Button resetButton;

	@Parameter(type = ItemIO.OUTPUT)
	private Dataset data;

	@Parameter
	private DatasetService datasetService;

	@Parameter
	private LUTService lutService;

	// -- Command methods --

	@Override
	public void run() {
		data = makeDataset();
		fillDataset(data);
		calibrate(data);
		applyLUT(data);
	}

	// -- callbacks --

	protected void reset() {
		originX = DEFAULT_ORIGIN_X;
		originY = DEFAULT_ORIGIN_Y;
		extentX = DEFAULT_EXTENT_X;
		extentY = DEFAULT_EXTENT_Y;
	}

	// -- helpers --

	private Dataset makeDataset() {
		long SIZE = 2048;
		long[] dims = new long[] { Math.round(SIZE * extentX / extentY), SIZE };
		String name = "Mandelbrot Set";
		name += " [" + originX + "," + originY + "] ";
		name += " [" + (originX+extentX) + "," + (originY+extentY) + "]";
		AxisType[] axes = new AxisType[]{Axes.X, Axes.Y};
		int bitsPerPixel = 8;
		boolean signed = false;
		boolean floating = false;
		return datasetService.create(dims, name, axes, bitsPerPixel, signed,
			floating);
	}

	private void fillDataset(Dataset ds) {
		RandomAccess<? extends RealType<?>> accessor =
			ds.getImgPlus().randomAccess();
		double dx = extentX / ds.dimension(0);
		double dy = extentY / ds.dimension(1);
		double cx = 0;
		for (long x = 0; x < ds.dimension(0); x++, cx += dx) {
			accessor.setPosition(x, 0);
			double cy = 0;
			for (long y = 0; y < ds.dimension(1); y++, cy += dy) {
				accessor.setPosition(y, 1);
				short value = value(originX + cx, originY + cy);
				accessor.get().setReal(value);
			}
		}
	}

	// return a value from 0 to 255

	private short value(double cx, double cy) {
		double zx = cx;
		double zy = cy;
		short value = 255;
		while (value > 0) {
			// complex multiply
			double tx = zx;
			double ty = zy;
			zx = tx * tx - ty * ty;
			zy = ty * tx + tx * ty;
			// and then add
			zx += cx;
			zy += cy;
			if (Math.sqrt((zx * zx) + (zy * zy)) > CUTOFF) return value;
			value--;
		}
		return value;
	}

	private void applyLUT(Dataset ds) {
		ds.initializeColorTables(1);
		ds.setColorTable(lut(), 0);
	}

	private ColorTable lut() {
		LUTFinder finder = new LUTFinder();
		Map<String, URL> luts = finder.findLUTs();
		URL lutURL = luts.get("WCIF/Rainbow RGB.lut");
		if (lutURL != null) {
			try {
				return lutService.loadLUT(lutURL);
			}
			catch (Exception e) {
				// fall through
			}
		}
		return ColorTables.FIRE;
	}

	private void calibrate(Dataset ds) {
		CalibratedAxis xAxis =
			new DefaultLinearAxis(Axes.X, extentX / ds.dimension(0), originX);
		CalibratedAxis yAxis =
			new DefaultLinearAxis(Axes.Y, extentY / ds.dimension(1), originY);
		ds.setAxis(xAxis, 0);
		ds.setAxis(yAxis, 1);
	}
}
