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

package imagej.core.plugins.assign;

import imagej.data.Dataset;
import imagej.data.Position;
import imagej.data.display.DatasetView;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.data.overlay.Overlay;
import imagej.ext.menu.MenuConstants;
import imagej.ext.module.ItemVisibility;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.ui.UIService;
import net.imglib2.img.Img;
import net.imglib2.meta.Axes;
import net.imglib2.ops.Function;
import net.imglib2.ops.InputIteratorFactory;
import net.imglib2.ops.Tuple2;
import net.imglib2.ops.condition.UVInsideRoiCondition;
import net.imglib2.ops.image.ImageAssignment;
import net.imglib2.ops.input.PointInputIteratorFactory;
import net.imglib2.ops.parse.RealEquationFunctionParser;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * Fills a region of a Dataset with the point by point calculation of a user
 * specified equation.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = MenuConstants.PROCESS_LABEL,
		weight = MenuConstants.PROCESS_WEIGHT,
		mnemonic = MenuConstants.PROCESS_MNEMONIC),
	@Menu(label = "Math", mnemonic = 'm'), @Menu(label = "Equation...", weight = 20) },
	headless = true)
public class EquationDataValues<T extends RealType<T>> implements ImageJPlugin {

	// -- instance variables that are Parameters --

	@Parameter(persist = false)
	OverlayService overlayService;
	
	@Parameter(persist = false)
	ImageDisplayService imgDispService;
	
	@Parameter(persist = false)
	UIService uiService;
	
	@Parameter(persist = false)
	private ImageDisplay display;

	@Parameter(label = "Format examples", visibility=ItemVisibility.MESSAGE)
	private String title = "";

	@Parameter(label = "", visibility=ItemVisibility.MESSAGE)
	private String ex1 = "img + 12";
	
	@Parameter(label = "", visibility=ItemVisibility.MESSAGE)
	private String ex2 = "[x,y], x^2 + y^2";
	
	@Parameter(label = "", visibility=ItemVisibility.MESSAGE)
	private String ex3 = "[u,v,w] , -2.003*u - 3.41*w + E + PI";
	
	@Parameter(label = "", visibility=ItemVisibility.MESSAGE)
	private String ex4 = "[x,y,c,z,t], cos(t) + sin(z)";
	
	@Parameter(label = "Apply to all planes")
	private boolean allPlanes;

	@Parameter(label = "Equation")
	private String equationString;
	
	private Dataset dataset;
	private long[] origin;
	private long[] span;
	private UVInsideRoiCondition condition;

	// -- public interface --

	@Override
	public void run() {
		setRegion(display, allPlanes);
		RealEquationFunctionParser parser = new RealEquationFunctionParser();
		Tuple2<Function<long[],DoubleType>, String> result =
				parser.parse(equationString, dataset.getImgPlus());
		if (result.get2() != null) {
			uiService.showDialog(result.get2(), "Equation parsing error");
			return;
		}
		InputIteratorFactory<long[]> factory = new PointInputIteratorFactory();
		Function<long[],DoubleType> function = result.get1();
		ImageAssignment<T,DoubleType,long[]> assigner =
				new ImageAssignment<T, DoubleType, long[]>(
						(Img<T>)dataset.getImgPlus(), origin, span,
						function, condition, factory);
		assigner.assign();
		dataset.update();
	}

	public ImageDisplay getDisplay() {
		return display;
	}

	public void setDisplay(final ImageDisplay display) {
		this.display = display;
	}

	public boolean isAllPlanes() {
		return allPlanes;
	}
	
	public void setAllPlanes(boolean value) {
		this.allPlanes = value;
	}
	
	public String getEquation() {
		return equationString;
	}
	
	public void setEquation(String equationString) {
		this.equationString = equationString;
	}
	
	// -- private helpers --

	private void setRegion(final ImageDisplay disp, boolean allPlanes) {
		dataset = imgDispService.getActiveDataset(disp);
		final Overlay overlay = overlayService.getActiveOverlay(disp);
		final DatasetView view = imgDispService.getActiveDatasetView(disp);
		
		// check dimensions of Dataset
		final int xIndex = dataset.getAxisIndex(Axes.X);
		final int yIndex = dataset.getAxisIndex(Axes.Y);
		if ((xIndex < 0) || (yIndex < 0))
			throw new IllegalArgumentException(
				"display does not have XY planes");
		
		// calc XY outline boundary
		final long[] dims = dataset.getDims();
		final long x,y,w,h;
		if (overlay == null) {
			x = 0;
			y = 0;
			w = dims[xIndex];
			h = dims[yIndex];
		}
		else {
			x = (long)overlay.realMin(0);
			y = (long)overlay.realMin(1);
			w = (long) Math.round(overlay.realMax(0) - x);
			h = (long) Math.round(overlay.realMax(1) - y);
		}

		// calc origin and span values
		origin = new long[dims.length];
		span = new long[dims.length];
		Position pos = view.getPlanePosition();
		int p = 0;
		for (int i = 0; i < dims.length; i++) {
			if (i == xIndex) {
				origin[xIndex] = x;
				span[xIndex] = w;
			}
			else if (i == yIndex) {
				origin[yIndex] = y;
				span[yIndex] = h;
			}
			else if (allPlanes) {
				origin[i] = 0;
				span[i] = dims[i];
			}
			else {
				origin[i] = pos.getLongPosition(p++);
				span[i] = 1;
			}
		}
		
		condition = null;
		if (overlay != null)
			condition = new UVInsideRoiCondition(overlay.getRegionOfInterest());
	}
}
