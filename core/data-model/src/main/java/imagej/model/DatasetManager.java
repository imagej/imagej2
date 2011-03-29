//
//
// DatasetManager.java

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

package imagej.model;

import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.manager.Manager;
import imagej.manager.ManagerComponent;
import imagej.manager.Managers;
import imagej.model.event.DatasetCreatedEvent;
import imagej.model.event.DatasetDeletedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager component for keeping track of active datasets.
 *
 * @author Curtis Rueden
 */
@Manager(priority = Managers.HIGH_PRIORITY)
public class DatasetManager implements ManagerComponent {

	// TODO - Is using dataset name as a key really a good idea?
	// Shouldn't we support multiple datasets with the same name?

	private final Map<String, Dataset> datasets =
		new ConcurrentHashMap<String, Dataset>();

	public void addDataset(final Dataset dataset) {
		datasets.put(dataset.getMetadata().getName(), dataset);
	}

	public void removeDataset(final Dataset dataset) {
		datasets.get(dataset.getMetadata().getName());
	}

	/** Gets a list of active datasets, sorted by name. */
	public List<Dataset> getDatasets() {
		final List<Dataset> datasetList =
			new ArrayList<Dataset>(datasets.values());
		Collections.sort(datasetList);
		return datasetList;
	}

	/** Gets the dataset with the given name. */
	public Dataset getDataset(final String name) {
		return datasets.get(name);
	}

	// -- ManagerComponent methods --

	@Override
	public void initialize() {
		Events.subscribe(DatasetCreatedEvent.class,
			new EventSubscriber<DatasetCreatedEvent>()
		{
			@Override
			public void onEvent(final DatasetCreatedEvent event) {
				addDataset(event.getDataset());
			}			
		});
		Events.subscribe(DatasetDeletedEvent.class,
			new EventSubscriber<DatasetDeletedEvent>()
		{
			@Override
			public void onEvent(final DatasetDeletedEvent event) {
				removeDataset(event.getDataset());
			}
		});
	}

}
