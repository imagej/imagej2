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

package imagej.data.overlay;

import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.data.event.OverlayDeletedEvent;
import imagej.display.Display;
import imagej.display.event.DisplayDeletedEvent;
import imagej.options.OptionsService;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.scijava.Priority;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Provides functionality related to {@link ThresholdOverlay}s.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Service.class, priority = Priority.NORMAL_PRIORITY)
public class ThresholdService extends AbstractService
{

	// TODO - eliminate direct Prefs hacking by utilizing OptionsThreshold.
	// Unfortunately I could not because ij-options is not part of core or data
	// subprojects.

	// -- parameters --

	@Parameter
	private EventService eventService;

	@Parameter
	private ImageDisplayService displayService;

	@Parameter
	private OverlayService overlayService;

	@Parameter
	private OptionsService optionsService;

	// -- instance variables --

	private final ConcurrentHashMap<ImageDisplay, ThresholdOverlay> map =
		new ConcurrentHashMap<ImageDisplay, ThresholdOverlay>();

	// -- ThresholdService methods --

	@Override
	public void initialize() {
		super.initialize();
		eventService.subscribe(this);
	}

	/**
	 * Returns true if a {@link ThresholdOverlay} is defined for a given display.
	 */
	public boolean hasThreshold(ImageDisplay display) {
		return map.get(display) != null;
	}

	/**
	 * Gets the {@link ThresholdOverlay} associated with a display. If one does
	 * not yet exist it is created.
	 */
	public ThresholdOverlay getThreshold(ImageDisplay display) {
		ThresholdOverlay overlay = map.get(display);
		if (overlay == null) {
			Dataset dataset = displayService.getActiveDataset(display);
			if (dataset == null) {
				throw new IllegalArgumentException(
					"expected ImageDisplay to have active dataset");
			}
			overlay = new ThresholdOverlay(getContext(), dataset);
			map.put(display, overlay);
			display.display(overlay);
			// NOTE - the call on prev line did a rebuild() but not necessarily an
			// update(). So graphics might not be up to date! This may be a bug in
			// display code. Anyhow this next line makes sure that the display is
			// updated. This fixes the problem where you adjust threshold and in
			// dialog you delete the thresh and then go into min or max field and make
			// a change. Correct behavior is for new thresh to immediately appear.
			// Without this update() call the thresh overlay exists but doesn't get
			// displayed.
			display.update(); // TEMP HACK
		}
		return overlay;
	}

	/**
	 * Removes the {@link ThresholdOverlay} associated with a display.
	 */
	public void removeThreshold(ImageDisplay display) {
		ThresholdOverlay overlay = map.get(display);
		if (overlay != null) {
			overlayService.removeOverlay(display, overlay);
			map.remove(display);
		}
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
	protected void onEvent(OverlayDeletedEvent evt) {
		Overlay overlay = evt.getObject();
		if (overlay instanceof ThresholdOverlay) {
			for (Entry<ImageDisplay, ThresholdOverlay> entry : map.entrySet()) {
				if (entry.getValue() == overlay) removeThreshold(entry.getKey());
			}
		}
	}
}
