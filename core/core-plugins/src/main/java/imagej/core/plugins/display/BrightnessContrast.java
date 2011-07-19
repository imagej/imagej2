//
// BrightnessContrast.java
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
import imagej.display.DisplayService;
import imagej.ext.module.ui.WidgetStyle;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.ext.plugin.PreviewPlugin;
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
@Plugin(menu = {
	@Menu(label = "Image"),
	@Menu(label = "Adjust"),
	@Menu(label = "Brightness/Contrast", accelerator = "control shift C",
		weight = 0) }, iconPath = "/icons/plugins/contrast.png")
public class BrightnessContrast implements ImageJPlugin, PreviewPlugin {

	private static final int SLIDER_RANGE = 256;
	private static final String SLIDER_MAX = "" + (SLIDER_RANGE - 1);

	@Parameter
	private DatasetView view = ImageJ.get(DisplayService.class)
		.getActiveDatasetView();

	@Parameter(label = "Minimum", persist = false, callback = "minMaxChanged")
	private double min = 0;

	@Parameter(label = "Maximum", persist = false, callback = "minMaxChanged")
	private double max = 255;

	@Parameter(callback = "brightnessChanged", persist = false,
		style = WidgetStyle.NUMBER_SCROLL_BAR, min = "0", max = SLIDER_MAX)
	private int brightness = SLIDER_RANGE / 2;

	@Parameter(callback = "contrastChanged", persist = false,
		style = WidgetStyle.NUMBER_SCROLL_BAR, min = "0", max = SLIDER_MAX)
	private int contrast = SLIDER_RANGE / 2;

	private final double defaultMin, defaultMax;

	public BrightnessContrast() {
		if (view != null) initializeMinMax();
		this.defaultMin = min;
		this.defaultMax = max;
		Log.debug("default min/max= " + defaultMin + "/" + defaultMax);

//		if (view.getCompositeDimIndex() >= 0) {
//			int currentChannel = view.getProjector().getIntPosition(view.getCompositeDimIndex());
//			defaultMin = view.getImgPlus().getChannelMinimum(currentChannel);
//			defaultMax = view.getImgPlus().getChannelMaximum(currentChannel);
//		}
	}

	@Override
	public void run() {
		if (view == null) return;
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

	public DatasetView getView() {
		return view;
	}

	public void setView(final DatasetView view) {
		this.view = view;
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

	// -- Callback methods --

	protected void minMaxChanged() {
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

	protected void contrastChanged() {
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

	protected void brightnessChanged() {
		final double brightCenter =
			defaultMin + (defaultMax - defaultMin) *
				((float) (SLIDER_RANGE - brightness) / (float) SLIDER_RANGE);
		final double width = max - min;
		min = (brightCenter - width / 2.0);
		max = (brightCenter + width / 2.0);
		Log.debug("brightness, brightCenter, width, min, max = " + brightness +
			", " + brightCenter + ", " + width + ", " + min + ", " + max);
	}

	// -- Helper methods --

	private void initializeMinMax() {
		final List<RealLUTConverter<? extends RealType<?>>> converters =
			view.getConverters();
		for (final RealLUTConverter<? extends RealType<?>> conv : converters) {
			min = conv.getMin();
			max = conv.getMax();
			break; // use only first channel, for now
		}
		Log.debug("BrightnessContrast: valid bits = " +
			view.getDataObject().getValidBits());
	}

	private void updateBrightness() {
		final double level = min + (max - min) / 2.0;
		final double normalizedLevel =
			1.0 - (level - defaultMin) / (defaultMax - defaultMin);
		brightness = ((int) (normalizedLevel * SLIDER_RANGE));
	}

	private void updateContrast() {
		final double mid = SLIDER_RANGE / 2;
		double c = ((defaultMax - defaultMin) / (max - min)) * mid;
		if (c > mid) {
			c = SLIDER_RANGE - ((max - min) / (defaultMax - defaultMin)) * mid;
		}
		contrast = ((int) c);
	}

}
