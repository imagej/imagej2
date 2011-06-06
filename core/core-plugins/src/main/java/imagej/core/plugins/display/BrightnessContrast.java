//
// ContrastBrightness.java
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

package imagej.core.plugins.display;

import imagej.ImageJ;
import imagej.display.DatasetView;
import imagej.display.Display;
import imagej.display.DisplayManager;
import imagej.display.DisplayView;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.plugin.PreviewPlugin;
import imagej.plugin.ui.WidgetStyle;
import imagej.util.Log;

import java.util.List;

import net.imglib2.display.RealLUTConverter;
import net.imglib2.type.numeric.RealType;

/**
 * Plugin that sets the minimum and maximum for scaling of display values. Sets
 * the same min/max for each channel.
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 */
@Plugin(menu = { @Menu(label = "Image"), @Menu(label = "Adjust"),
	@Menu(label = "Brightness/Contrast", accelerator = "control shift C") })
public class BrightnessContrast implements ImageJPlugin, PreviewPlugin {

	private static final int SLIDER_RANGE = 256;
	private static final String SLIDER_MAX = "" + (SLIDER_RANGE - 1);

	// TODO - Use DisplayView (DatasetView?) parameter instead of getting the
	// active display from the DisplayManager.

	@Parameter(label = "Minimum", persist = false, callback = "adjustMinMax")
	private double min = 0;

	@Parameter(label = "Maximum", persist = false, callback = "adjustMinMax")
	private double max = 255;

	@Parameter(callback = "adjustBrightness", persist = false,
		style = WidgetStyle.NUMBER_SCROLL_BAR, min = "0", max = SLIDER_MAX)
	private int brightness = SLIDER_RANGE / 2;

	@Parameter(callback = "adjustContrast", persist = false,
		style = WidgetStyle.NUMBER_SCROLL_BAR, min = "0", max = SLIDER_MAX)
	private int contrast = SLIDER_RANGE / 2;

	private final double defaultMin, defaultMax;

	public BrightnessContrast() {
		final DatasetView view = getActiveDisplayView();
		final List<RealLUTConverter<? extends RealType<?>>> converters =
			view.getConverters();
		for (final RealLUTConverter<? extends RealType<?>> conv : converters) {
			min = conv.getMin();
			max = conv.getMax();
			break; // use only first channel, for now
		}
		this.defaultMin = min;
		this.defaultMax = max;
		Log.debug("ValidBitsOOO: " + view.getDataObject().getValidBits());
		Log.debug("default min/max= " + defaultMin + "/" + defaultMax);

//		if (view.getCompositeDimIndex() >= 0) {
//			int currentChannel = view.getProjector().getIntPosition(view.getCompositeDimIndex());
//			defaultMin = view.getImgPlus().getChannelMinimum(currentChannel);
//			defaultMax = view.getImgPlus().getChannelMaximum(currentChannel);
//		}
	}

	@Override
	public void run() {
		final DatasetView view = getActiveDisplayView();
		final List<RealLUTConverter<? extends RealType<?>>> converters =
			view.getConverters();
		for (final RealLUTConverter<? extends RealType<?>> conv : converters) {
			conv.setMin(min);
			conv.setMax(max);
		}
		view.getProjector().map();
		view.update();
	}

	@Override
	public void preview() {
		run();
	}

	void adjustMinMax() {
//		min = defaultMin + min * (defaultMax - defaultMin) / (SLIDER_RANGE - 1.0);
//		if (max > defaultMax) {
//			max = defaultMax;
//		}
//		if (min > max) {
//			max = min;
//		}
		updateBrightness();
		updateContrast();
	}

	void adjustMax() {
//		max = defaultMin + max * (defaultMax - defaultMin) / (SLIDER_RANGE - 1.0);
//		//IJ.log("adjustMax: "+maxvalue+"  "+max);
//		if (min < defaultMin) {
//			min = defaultMin;
//		}
//		if (max < min) {
//			min = max;
//		}
//		updateBrightness();
//		updateContrast();
	}

	protected void adjustContrast() {
		double slope;
		final double center = min + (max - min) / 2.0;
		final double range = defaultMax - defaultMin;
		final double mid = SLIDER_RANGE / 2;
		final int cvalue = contrast;
		if (cvalue <= mid) {
			slope = cvalue / mid;
		}
		else {
			slope = mid / (SLIDER_RANGE - cvalue);
		}
		if (slope > 0.0) {
			min = (center - (0.5 * range) / slope);
			max = (center + (0.5 * range) / slope);
		}
	}

	protected void adjustBrightness() {
		final double brightCenter =
			defaultMin + (defaultMax - defaultMin) *
				((float) (SLIDER_RANGE - brightness) / (float) SLIDER_RANGE);
		final double width = max - min;
		min = (brightCenter - width / 2.0);
		max = (brightCenter + width / 2.0);
		Log.debug("brightness, brightCenter, width, min, max = " + brightness +
			", " + brightCenter + ", " + width + ", " + min + ", " + max);
	}

	void updateBrightness() {
		final double level = min + (max - min) / 2.0;
		final double normalizedLevel =
			1.0 - (level - defaultMin) / (defaultMax - defaultMin);
		brightness = ((int) (normalizedLevel * SLIDER_RANGE));
	}

	void updateContrast() {
		final double mid = SLIDER_RANGE / 2;
		double c = ((defaultMax - defaultMin) / (max - min)) * mid;
		if (c > mid) {
			c = SLIDER_RANGE - ((max - min) / (defaultMax - defaultMin)) * mid;
		}
		contrast = ((int) c);
	}

	public double getMinimum() {
		return min;
	}

	public void setMinimum(final double min) {
		this.min = min;
	}

	public double getMaximum() {
		return max;
	}

	public void setMaximum(final double max) {
		this.max = max;
	}

	public int getBrightness() {
		return brightness;
	}

	public void setBrightness(final int brightness) {
		this.brightness = brightness;
	}

	public int getContrast() {
		return contrast;
	}

	public void setContrast(final int contrast) {
		this.contrast = contrast;
	}

	private DatasetView getActiveDisplayView() {
		final DisplayManager manager = ImageJ.get(DisplayManager.class);
		final Display display = manager.getActiveDisplay();
		if (display == null) {
			return null; // headless UI or no open images
		}
		final DisplayView activeView = display.getActiveView();
		return activeView instanceof DatasetView ? (DatasetView) activeView : null;
	}
}
