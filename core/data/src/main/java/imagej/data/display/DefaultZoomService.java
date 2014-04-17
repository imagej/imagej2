/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.data.display;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.thread.ThreadService;
import org.scijava.util.IntCoords;
import org.scijava.util.RealCoords;
import org.scijava.util.RealRect;

/**
 * Default service for performing zoom operations on {@link ImageDisplay}s.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
@Plugin(type = Service.class)
public class DefaultZoomService extends AbstractService implements ZoomService {

	@Parameter
	private ThreadService threadService;

	@Parameter
	private InputService inputService;

	@Parameter
	private OverlayService overlayService;

	@Override
	public void zoomIn(final ImageDisplay display) {
		threadService.queue(new Runnable() {

			@Override
			public void run() {
				if (inputService.getDisplay() == display) {
					// zoom in centered around the mouse cursor
					final int x = inputService.getX();
					final int y = inputService.getY();

					display.getCanvas().zoomIn(new IntCoords(x, y));
				}
				else {
					// no mouse coordinates available; use default behavior
					display.getCanvas().zoomIn();
				}
			}
		});
	}

	@Override
	public void zoomOut(final ImageDisplay display) {
		threadService.queue(new Runnable() {

			@Override
			public void run() {
				if (inputService.getDisplay() == display) {
					// zoom in centered around the mouse cursor
					final int x = inputService.getX();
					final int y = inputService.getY();

					display.getCanvas().zoomOut(new IntCoords(x, y));
				}
				else {
					// no mouse coordinates available; use default behavior
					display.getCanvas().zoomOut();
				}
			}
		});
	}

	@Override
	public void zoomOriginalScale(final ImageDisplay display) {
		threadService.queue(new Runnable() {

			@Override
			public void run() {
				display.getCanvas().setZoom(0);
				display.getCanvas().panReset();
			}
		});
	}

	@Override
	public void zoom100Percent(final ImageDisplay display) {
		threadService.queue(new Runnable() {

			@Override
			public void run() {
				display.getCanvas().setZoom(1);
			}
		});
	}

	@Override
	public void zoomToSelection(final ImageDisplay display) {
		threadService.queue(new Runnable() {

			@Override
			public void run() {
				final RealRect selection = overlayService.getSelectionBounds(display);
				display.getCanvas().zoomToFit(selection);
			}
		});

	}

	@Override
	public void zoomSet(final ImageDisplay display, final double zoomPercent,
		final double centerU, final double centerV)
	{
		threadService.queue(new Runnable() {

			@Override
			public void run() {
				display.getCanvas().setZoomAndCenter(zoomPercent / 100.0,
					new RealCoords(centerU, centerV));
			}
		});
	}

}
