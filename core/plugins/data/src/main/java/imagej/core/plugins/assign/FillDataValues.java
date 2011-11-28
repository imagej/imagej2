//
// FillDataValues.java
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

package imagej.core.plugins.assign;

import imagej.core.tools.FGTool;
import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.ext.tool.ToolService;
import imagej.util.ColorRGB;
import imagej.util.RealRect;
import net.imglib2.Cursor;
import net.imglib2.img.ImgPlus;
import net.imglib2.meta.Axes;
import net.imglib2.ops.Real;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.ops.operation.unary.real.RealConstant;
import net.imglib2.type.numeric.RealType;

/**
 * Fills the selected region of an input Dataset with the foreground value.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = { @Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Fill", weight = 28, accelerator = "control F") })
public class FillDataValues implements ImageJPlugin {

	// -- instance variables that are Parameters --

	@Parameter(required = true, persist = false)
	private OverlayService overlayService;
	
	@Parameter(required = true, persist = false)
	private ToolService toolService;
	
	@Parameter(required = true, persist = false)
	private ImageDisplayService dispService;
	
	@Parameter(required = true, persist = false)
	private ImageDisplay display;

	// -- public interface --

	@Override
	public void run() {
		final FGTool cp = toolService.getTool(FGTool.class);
		if (cp == null) return;
		Dataset dataset = dispService.getActiveDataset(display);
		if (dataset.isRGBMerged()) {
			final ColorRGB color = cp.getFgColor(); 
			fillSelectedRegionWithColor(dataset, color);
		}
		else { // gray data
			final double value = cp.getFgValue();
			fillSelectedRegionWithValue(display, value);
		}
	}

	public ImageDisplay getDisplay() {
		return display;
	}

	public void setDisplay(final ImageDisplay display) {
		this.display = display;
	}

	// -- private helpers --
	
	private void fillSelectedRegionWithValue(ImageDisplay disp, double value) {
		final UnaryOperation<Real, Real> op = new RealConstant(value);
		final InplaceUnaryTransform transform =
				new InplaceUnaryTransform(disp, op);
		transform.run();
	}
	
	// TODO - make something like this Dataset API maybe. or somewhere else.
	
	private void fillSelectedRegionWithColor(Dataset dataset, ColorRGB color) {
		RealRect bounds = overlayService.getSelectionBounds(display);
		long minX = (long) bounds.x;
		long minY = (long) bounds.y;
		long maxX = (long) (bounds.x + bounds.width - 1);
		long maxY = (long) (bounds.y + bounds.height - 1);
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		long[] pos = new long[dataset.numDimensions()];
		int xIndex = dataset.getAxisIndex(Axes.X);
		int yIndex = dataset.getAxisIndex(Axes.Y);
		int chIndex = dataset.getAxisIndex(Axes.CHANNEL);
		ImgPlus<? extends RealType<?>> imgPlus = dataset.getImgPlus();
		Cursor<? extends RealType<?>> cursor = imgPlus.localizingCursor();
		while (cursor.hasNext()) {
			RealType<?> pixRef = cursor.next();
			cursor.localize(pos);
			if ((pos[xIndex] < minX) || (pos[xIndex] > maxX)) continue;
			if ((pos[yIndex] < minY) || (pos[yIndex] > maxY)) continue;
			if (pos[chIndex] == 0)
				pixRef.setReal(r);
			else if (pos[chIndex] == 1)
				pixRef.setReal(g);
			else
				pixRef.setReal(b);
		}
		dataset.update();
	}
}
