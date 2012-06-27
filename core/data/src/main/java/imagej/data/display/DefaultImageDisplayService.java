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

package imagej.data.display;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.event.EventService;
import imagej.ext.display.Display;
import imagej.ext.display.DisplayService;
import imagej.service.AbstractService;
import imagej.service.Service;

import java.util.List;

/**
 * Default service for working with {@link ImageDisplay}s.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 * @author Grant Harris
 */
@Service
public final class DefaultImageDisplayService extends AbstractService
	implements ImageDisplayService
{

	private final EventService eventService;
	private final DisplayService displayService;

	public DefaultImageDisplayService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public DefaultImageDisplayService(final ImageJ context,
		final EventService eventService, final DisplayService displayService)
	{
		super(context);
		this.eventService = eventService;
		this.displayService = displayService;
	}

	// -- ImageDisplayService methods --

	@Override
	public EventService getEventService() {
		return eventService;
	}

	@Override
	public DisplayService getDisplayService() {
		return displayService;
	}

	// -- DisplayService methods - display discovery --

	@Override
	public ImageDisplay getActiveImageDisplay() {
		final Display<?> activeDisplay = displayService.getActiveDisplay();
		if (activeDisplay == null || !(activeDisplay instanceof ImageDisplay)) {
			return null;
		}
		return (ImageDisplay) activeDisplay;
	}

	@Override
	public Dataset getActiveDataset() {
		return getActiveDataset(getActiveImageDisplay());
	}

	@Override
	public DatasetView getActiveDatasetView() {
		return getActiveDatasetView(getActiveImageDisplay());
	}

	@Override
	public Dataset getActiveDataset(final ImageDisplay display) {
		final DatasetView activeDatasetView = getActiveDatasetView(display);
		return activeDatasetView == null ? null : activeDatasetView.getData();
	}

	@Override
	public DatasetView getActiveDatasetView(final ImageDisplay display) {
		if (display == null) return null;
		final DataView activeView = display.getActiveView();
		if (activeView instanceof DatasetView) {
			return (DatasetView) activeView;
		}
		return null;
	}

	@Override
	public List<ImageDisplay> getImageDisplays() {
		return displayService.getDisplaysOfType(ImageDisplay.class);
	}

}
