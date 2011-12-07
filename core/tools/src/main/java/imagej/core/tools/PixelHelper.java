//
// PixelHelper.java
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

package imagej.core.tools;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.Position;
import imagej.data.display.DataView;
import imagej.data.display.DatasetView;
import imagej.data.display.ImageCanvas;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.event.EventService;
import imagej.event.StatusEvent;
import imagej.ext.display.Display;
import imagej.ext.display.event.input.MsEvent;
import imagej.util.ColorRGB;
import imagej.util.IntCoords;
import imagej.util.RealCoords;
import net.imglib2.RandomAccess;
import net.imglib2.display.ColorTable8;
import net.imglib2.img.Img;
import net.imglib2.meta.Axes;
import net.imglib2.type.numeric.RealType;

/**
 * Gathers pixel information (location, color, value) of pixel associated with a
 * given mouse event.
 * 
 * @author Barry DeZonia
 * @author Rick Lentz
 * @author Grant Harris
 * @author Curtis Rueden
 */
public class PixelHelper {

	// -- instance variables --

	private ColorRGB color = new ColorRGB(0, 0, 0);
	private double value = 0;
	private long cx = 0;
	private long cy = 0;
	private boolean isPureRGBCase = false;
	private boolean isIntegerCase = false;
	private EventService eventService = null;

	// -- public interface --

	public ColorRGB getColor() {
		return color;
	}

	public double getValue() {
		return value;
	}

	public long getCX() {
		return cx;
	}

	public long getCY() {
		return cy;
	}

	public boolean isPureRGBCase() {
		return isPureRGBCase;
	}

	public boolean isIntegerCase() {
		return isIntegerCase;
	}

	public boolean processEvent(final MsEvent evt) {
		final ImageJ context = evt.getContext();
		final ImageDisplayService imageDisplayService =
			context.getService(ImageDisplayService.class);
		eventService = context.getService(EventService.class);

		final Display<?> display = evt.getDisplay();
		if (!(display instanceof ImageDisplay)) return false;
		final ImageDisplay imageDisplay = (ImageDisplay) display;

		final ImageCanvas canvas = imageDisplay.getCanvas();
		final IntCoords mousePos = new IntCoords(evt.getX(), evt.getY());
		if (!canvas.isInImage(mousePos)) {
			eventService.publish(new StatusEvent(null));
			return false;
		}

		// mouse is over image

		// TODO - update tool to probe more than just the active view
		final DataView activeView = imageDisplay.getActiveView();
		final Dataset dataset = imageDisplayService.getActiveDataset(imageDisplay);

		final RealCoords coords = canvas.panelToImageCoords(mousePos);
		cx = coords.getLongX();
		cy = coords.getLongY();

		final Position planePos = activeView.getPlanePosition();

		final Img<? extends RealType<?>> image = dataset.getImgPlus();
		final RandomAccess<? extends RealType<?>> randomAccess =
			image.randomAccess();
		final int xAxis = dataset.getAxisIndex(Axes.X);
		final int yAxis = dataset.getAxisIndex(Axes.Y);

		setPosition(randomAccess, cx, cy, planePos, xAxis, yAxis);

		// color dataset?
		if (dataset.isRGBMerged()) {
			isPureRGBCase = true;
			isIntegerCase = false;
			color = getColor(dataset, randomAccess);
			value = 0;
		}
		else { // gray dataset
			isPureRGBCase = false;
			isIntegerCase = dataset.isInteger();
			value = randomAccess.get().getRealDouble();
			final DatasetView view =
				imageDisplayService.getActiveDatasetView(imageDisplay);
			final ColorTable8 ctab = view.getColorTables().get(0);
			final double min = randomAccess.get().getMinValue();
			final double max = randomAccess.get().getMaxValue();
			final double percent = (value - min) / (max - min);
			final int byteVal = (int) Math.round(255 * percent);
			final int r = ctab.get(0, byteVal);
			final int g = ctab.get(1, byteVal);
			final int b = ctab.get(2, byteVal);
			color = new ColorRGB(r, g, b);
		}
		return true;
	}

	public void updateStatus(final String message) {
		eventService.publish(new StatusEvent(message));
	}

	// -- private helpers --

	private void setPosition(
		final RandomAccess<? extends RealType<?>> randomAccess, final long cx,
		final long cy, final Position planePos, final int xAxis, final int yAxis)
	{
		int i = 0;
		for (int d = 0; d < randomAccess.numDimensions(); d++) {
			if (d == xAxis) randomAccess.setPosition(cx, xAxis);
			else if (d == yAxis) randomAccess.setPosition(cy, yAxis);
			else randomAccess.setPosition(planePos.getLongPosition(i++), d);
		}
	}

	private ColorRGB getColor(final Dataset dataset,
		final RandomAccess<? extends RealType<?>> access)
	{
		final int channelAxis = dataset.getAxisIndex(Axes.CHANNEL);
		access.setPosition(0, channelAxis);
		final int r = (int) access.get().getRealDouble();
		access.setPosition(1, channelAxis);
		final int g = (int) access.get().getRealDouble();
		access.setPosition(2, channelAxis);
		final int b = (int) access.get().getRealDouble();
		return new ColorRGB(r, g, b);
	}

}
