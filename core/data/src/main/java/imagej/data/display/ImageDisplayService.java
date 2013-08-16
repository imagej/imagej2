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

package imagej.data.display;

import imagej.data.Data;
import imagej.data.Dataset;
import imagej.display.DisplayService;
import imagej.service.IJService;

import java.util.List;

import org.scijava.event.EventService;
import org.scijava.plugin.PluginService;

/**
 * Interface for services that work with {@link ImageDisplay}s.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 * @author Grant Harris
 */
public interface ImageDisplayService extends IJService {

	EventService getEventService();

	PluginService getPluginService();

	DisplayService getDisplayService();

	/** Creates a new {@link DataView} wrapping the given data object. */
	DataView createDataView(Data data);

	/**
	 * Gets the list of available {@link DataView}s. The list will contain one
	 * uninitialized instance of each {@link DataView} implementation known to the
	 * {@link PluginService}.
	 */
	List<? extends DataView> getDataViews();

	/** Gets the currently active {@link ImageDisplay}. */
	ImageDisplay getActiveImageDisplay();

	/**
	 * Gets the active {@link Dataset}, if any, of the currently active
	 * {@link ImageDisplay}.
	 */
	Dataset getActiveDataset();

	/**
	 * Gets the active {@link DatasetView}, if any, of the currently active
	 * {@link ImageDisplay}.
	 */
	DatasetView getActiveDatasetView();

	/**
	 * Gets the active {@link Dataset}, if any, of the given {@link ImageDisplay}.
	 */
	Dataset getActiveDataset(ImageDisplay display);

	/**
	 * Gets the active {@link DatasetView}, if any, of the given
	 * {@link ImageDisplay}.
	 */
	DatasetView getActiveDatasetView(ImageDisplay display);

	/** Gets a list of all available {@link ImageDisplay}s. */
	List<ImageDisplay> getImageDisplays();

}
