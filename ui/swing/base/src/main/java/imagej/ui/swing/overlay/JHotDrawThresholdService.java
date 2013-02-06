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

package imagej.ui.swing.overlay;

import imagej.Priority;
import imagej.data.Data;
import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.data.event.DataDeletedEvent;
import imagej.data.overlay.ThresholdOverlay;
import imagej.data.overlay.ThresholdService;
import imagej.display.Display;
import imagej.display.event.DisplayDeletedEvent;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.service.AbstractService;
import imagej.service.Service;
import imagej.util.Prefs;

import java.util.HashMap;
import java.util.Map.Entry;

import net.imglib2.img.ImgPlus;
import net.imglib2.type.numeric.RealType;

/**
 * JHotDraw implementation of a ThresholdService.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Service.class, priority = Priority.NORMAL_PRIORITY)
public class JHotDrawThresholdService extends AbstractService implements
	ThresholdService
{

	// -- constants --

	private static final String MIN = "imagej.service.threshold.min";
	private static final String MAX = "imagej.service.threshold.max";

	// -- parameters --

	@Parameter
	private EventService eventService;

	@Parameter
	private ImageDisplayService displayService;

	@Parameter
	private OverlayService overlayService;

	// -- instance variables --

	// TODO - if these doubles are @Parameters the service cannot initialize. This
	// may be a bug in the service init code (i.e. it tries to invoke a Double
	// service maybe). So use Prefs directly as a workaround.

	private double defaultMinThresh = Prefs.getDouble(MIN,
		Double.NEGATIVE_INFINITY);

	private double defaultMaxThresh = Prefs.getDouble(MAX,
		Double.POSITIVE_INFINITY);

	// -- instance variables --

	private final HashMap<ImageDisplay, ThresholdOverlay> map =
		new HashMap<ImageDisplay, ThresholdOverlay>();

	// -- ThresholdService methods --

	@Override
	public void initialize() {
		super.initialize();
		eventService.subscribe(this);
	}

	@Override
	public boolean hasThreshold(ImageDisplay display) {
		return map.get(display) != null;
	}

	@Override
	public ThresholdOverlay getThreshold(ImageDisplay display) {
		ThresholdOverlay overlay = map.get(display);
		if (overlay == null) {
			Dataset dataset = displayService.getActiveDataset(display);
			if (dataset == null) {
				throw new IllegalArgumentException(
					"expected ImageDisplay to have active dataset");
			}
			ImgPlus<? extends RealType<?>> imgPlus = dataset.getImgPlus();
			overlay = new ThresholdOverlay(getContext(), imgPlus);
			overlay.setRange(defaultMinThresh, defaultMaxThresh);
			map.put(display, overlay);
			display.display(overlay);
		}
		return overlay;
	}

	@Override
	public void removeThreshold(ImageDisplay display) {
		ThresholdOverlay overlay = map.get(display);
		if (overlay != null) {
			overlayService.removeOverlay(display, overlay);
			map.remove(display);
		}
	}

	@Override
	public void setDefaultThreshold(double min, double max) {
		if (min > max) {
			throw new IllegalArgumentException(
				"threshold definition error: min > max");
		}
		defaultMinThresh = min;
		defaultMaxThresh = max;
		Prefs.put(MIN, defaultMinThresh);
		Prefs.put(MAX, defaultMaxThresh);
	}

	@Override
	public double getDefaultMinThreshold() {
		return defaultMinThresh;
	}

	@Override
	public double getDefaultMaxThreshold() {
		return defaultMaxThresh;
	}


	// -- event handlers --

	@EventHandler
	protected void onEvent(DisplayDeletedEvent evt) {
		Display<?> display = evt.getObject();
		if (display instanceof ImageDisplay) {
			removeThreshold((ImageDisplay) display);
		}
	}

	@EventHandler
	protected void onEvent(DataDeletedEvent evt) {
		Data object = evt.getObject();
		if (object instanceof ThresholdOverlay) {
			for (Entry<ImageDisplay, ThresholdOverlay> entry : map.entrySet()) {
				if (entry.getValue() == object) removeThreshold(entry.getKey());
			}
		}
	}
}
