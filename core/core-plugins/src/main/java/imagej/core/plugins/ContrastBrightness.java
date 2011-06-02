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
package imagej.core.plugins;

import imagej.ImageJ;
import imagej.display.AbstractDatasetView;
import imagej.display.Display;
import imagej.display.DisplayManager;
import imagej.display.DisplayView;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.plugin.PreviewPlugin;
import imagej.plugin.ui.WidgetStyle;

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
@Plugin(menuPath = "Image>Adjust>ContrastBrightness")
public class ContrastBrightness implements ImageJPlugin, PreviewPlugin {

	@Parameter(label = "Minimum", persist = false,
	callback = "adjustMinMax")
	private double min = 0;
	//
	@Parameter(label = "Maximum", persist = false,
	callback = "adjustMinMax")
	private double max = 255;
	//
	@Parameter(callback = "adjustBrightness", persist = false,
	style = WidgetStyle.NUMBER_SCROLL_BAR, min = "0", max = "255")
	private int brightness = 128;
	//
	@Parameter(callback = "adjustContrast", persist = false,
	style = WidgetStyle.NUMBER_SCROLL_BAR, min = "0", max = "255")
	private int contrast = 128;
	//
	int sliderRange = 256;
	double defaultMin, defaultMax;

	public ContrastBrightness() {
		final AbstractDatasetView view = getActiveDisplayView();
		final List<RealLUTConverter<? extends RealType<?>>> converters =
				view.getConverters();
		for (final RealLUTConverter<? extends RealType<?>> conv : converters) {
			min = conv.getMin();
			max = conv.getMax();
			break; // use only first channel, for now
		}
		this.defaultMin = min;
		this.defaultMax = max;
		System.out.println("ValidBitsOOO: " + view.getDataObject().getValidBits());
		System.out.println("default min/max= " + defaultMin + "/" + defaultMax );
		
		
//		if (view.getCompositeDimIndex() >= 0) {
//			int currentChannel = view.getProjector().getIntPosition(view.getCompositeDimIndex());
//			defaultMin = view.getImgPlus().getChannelMinimum(currentChannel);
//			defaultMax = view.getImgPlus().getChannelMaximum(currentChannel);
//		}
	}

	@Override
	public void run() {
		final AbstractDatasetView view = getActiveDisplayView();
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
//		min = defaultMin + min * (defaultMax - defaultMin) / (sliderRange - 1.0);
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
//		max = defaultMin + max * (defaultMax - defaultMin) / (sliderRange - 1.0);
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
		double center = min + (max - min) / 2.0;
		double range = defaultMax - defaultMin;
		double mid = sliderRange / 2;
		int cvalue = contrast;
		if (cvalue <= mid) {
			slope = cvalue / mid;
		} else {
			slope = mid / (sliderRange - cvalue);
		}
		if (slope > 0.0) {
			min = (center - (0.5 * range) / slope);
			max = (center + (0.5 * range) / slope);
		}
	}

	protected void adjustBrightness() {

		double brightCenter = defaultMin + (defaultMax - defaultMin)
				* ((float)(sliderRange - brightness) / (float) sliderRange);
		double width = max - min;
		min = (brightCenter - width / 2.0);
		max = (brightCenter + width / 2.0);
		System.out.println("brightness, brightCenter, width, min, max = " + brightness + ", " +
				brightCenter + ", " + width + ", " + min + ", " + max);
	}

	void updateBrightness() {
		double level = min + (max - min) / 2.0;
		double normalizedLevel = 1.0 - (level - defaultMin) / (defaultMax - defaultMin);
		brightness = ((int) (normalizedLevel * sliderRange));
	}

	void updateContrast() {
		double mid = sliderRange / 2;
		double c = ((defaultMax - defaultMin) / (max - min)) * mid;
		if (c > mid) {
			c = sliderRange - ((max - min) / (defaultMax - defaultMin)) * mid;
		}
		contrast = ((int) c);
	}

	//===================================
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

	private AbstractDatasetView getActiveDisplayView() {
		final DisplayManager manager = ImageJ.get(DisplayManager.class);
		final Display display = manager.getActiveDisplay();
		if (display == null) {
			return null; // headless UI or no open images
		}
		final DisplayView activeView = display.getActiveView();
		return activeView instanceof AbstractDatasetView ?
			(AbstractDatasetView) activeView : null;
	}
}