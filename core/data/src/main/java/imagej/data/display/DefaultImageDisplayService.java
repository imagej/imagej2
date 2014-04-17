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

import imagej.data.Data;
import imagej.data.Dataset;
import imagej.data.Position;

import java.util.List;

import org.scijava.display.DisplayService;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Default service for working with {@link ImageDisplay}s.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 * @author Grant Harris
 */
@Plugin(type = Service.class)
public final class DefaultImageDisplayService extends AbstractService
	implements ImageDisplayService
{

	@Parameter
	private EventService eventService;

	@Parameter
	private PluginService pluginService;

	@Parameter
	private DisplayService displayService;

	// -- ImageDisplayService methods --

	@Override
	public EventService getEventService() {
		return eventService;
	}

	@Override
	public PluginService getPluginService() {
		return pluginService;
	}

	@Override
	public DisplayService getDisplayService() {
		return displayService;
	}

	@Override
	public DataView createDataView(final Data data) {
		for (final DataView dataView : getDataViews()) {
			if (dataView.isCompatible(data)) {
				dataView.initialize(data);
				return dataView;
			}
		}
		throw new IllegalArgumentException("No data view found for data: " + data);
	}

	@Override
	public List<? extends DataView> getDataViews() {
		return pluginService.createInstancesOfType(DataView.class);
	}

	@Override
	public ImageDisplay getActiveImageDisplay() {
		return displayService.getActiveDisplay(ImageDisplay.class);
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
	public Position getActivePosition() {
		return getActivePosition(getActiveImageDisplay());
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
	public Position getActivePosition(final ImageDisplay display) {
		if (display == null) return null;
		final DatasetView activeDatasetView = this.getActiveDatasetView(display);
		if(activeDatasetView == null) return null;
		return activeDatasetView.getPlanePosition();
	}

	@Override
	public List<ImageDisplay> getImageDisplays() {
		return displayService.getDisplaysOfType(ImageDisplay.class);
	}

}
