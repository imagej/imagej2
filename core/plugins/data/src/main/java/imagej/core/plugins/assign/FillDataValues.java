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

import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.ext.menu.MenuConstants;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.options.OptionsService;
import imagej.options.plugins.OptionsColors;
import imagej.util.ColorRGB;
import imagej.util.RealRect;
import net.imglib2.Cursor;
import net.imglib2.img.ImgPlus;
import net.imglib2.meta.Axes;
import net.imglib2.ops.operation.unary.real.RealConstant;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * Fills the selected region of an input Dataset with the foreground value.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = MenuConstants.EDIT_LABEL, weight = MenuConstants.EDIT_WEIGHT,
		mnemonic = MenuConstants.EDIT_MNEMONIC),
	@Menu(label = "Fill", weight = 28, accelerator = "control F") })
public class FillDataValues<T extends RealType<T>> implements ImageJPlugin {

	// -- instance variables that are Parameters --

	@Parameter(persist = false)
	private OverlayService overlayService;

	@Parameter(persist = false)
	private ImageDisplayService dispService;

	@Parameter(persist = false)
	private OptionsService optionsService;

	@Parameter(persist = false)
	private ImageDisplay display;

	// -- public interface --

	@Override
	public void run() {
		final OptionsColors opts = optionsService.getOptions(OptionsColors.class);
		if (opts == null) return;
		final Dataset dataset = dispService.getActiveDataset(display);
		if (dataset.isRGBMerged()) {
			final ColorRGB color = opts.getFgColor();
			fillSelectedRegionWithColor(dataset, color);
		}
		else { // gray data
			final double value = opts.getFgGray();
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

	private void fillSelectedRegionWithValue(final ImageDisplay disp,
		final double value)
	{
		final RealConstant<DoubleType, DoubleType> op =
				new RealConstant<DoubleType,DoubleType>(value);
		final InplaceUnaryTransform<T,DoubleType> transform =
			new InplaceUnaryTransform<T,DoubleType>(
					disp, op, new DoubleType());
		transform.run();
	}

	// TODO - make something like this Dataset API maybe. or somewhere else.

	private void fillSelectedRegionWithColor(final Dataset dataset,
		final ColorRGB color)
	{
		final RealRect bounds = overlayService.getSelectionBounds(display);
		final long minX = (long) bounds.x;
		final long minY = (long) bounds.y;
		final long maxX = (long) (bounds.x + bounds.width - 1);
		final long maxY = (long) (bounds.y + bounds.height - 1);
		final int r = color.getRed();
		final int g = color.getGreen();
		final int b = color.getBlue();
		final long[] pos = new long[dataset.numDimensions()];
		final int xIndex = dataset.getAxisIndex(Axes.X);
		final int yIndex = dataset.getAxisIndex(Axes.Y);
		final int chIndex = dataset.getAxisIndex(Axes.CHANNEL);
		final ImgPlus<? extends RealType<?>> imgPlus = dataset.getImgPlus();
		final Cursor<? extends RealType<?>> cursor = imgPlus.localizingCursor();
		while (cursor.hasNext()) {
			final RealType<?> pixRef = cursor.next();
			cursor.localize(pos);
			if ((pos[xIndex] < minX) || (pos[xIndex] > maxX)) continue;
			if ((pos[yIndex] < minY) || (pos[yIndex] > maxY)) continue;
			if (pos[chIndex] == 0) pixRef.setReal(r);
			else if (pos[chIndex] == 1) pixRef.setReal(g);
			else pixRef.setReal(b);
		}
		dataset.update();
	}
}
