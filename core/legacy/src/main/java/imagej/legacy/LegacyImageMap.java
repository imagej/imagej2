//
// LegacyImageMap.java
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

package imagej.legacy;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageWindow;

import imagej.data.Dataset;
import imagej.data.event.DatasetDeletedEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO
 *
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public class LegacyImageMap {

	// -- instance variables --
	
	//private Object lock;
	private Map<ImagePlus, Dataset> imageTable;
	private ImageTranslator imageTranslator;
	private ArrayList<EventSubscriber<?>> subscribers;

	// -- public interface --

	/** default constructor */
	public LegacyImageMap() {
		//lock = new Object();
		//imageTable = new WeakHashMap<ImagePlus, Dataset>();
		imageTable = new ConcurrentHashMap<ImagePlus, Dataset>();
		imageTranslator = new DefaultImageTranslator();
		subscribers = new ArrayList<EventSubscriber<?>>();
		subscribeToEvents();
	}

	/** returns the result of a lookup for a given ImagePlus */
	public Dataset findDataset(ImagePlus imp) {
		//synchronized(lock) {
			return imageTable.get(imp);
		//}
	}

	/** returns the result of a lookup for a given Dataset */
	public ImagePlus findImagePlus(Dataset ds) {
		//synchronized(lock) {
			for (ImagePlus imp : imageTable.keySet()) {
				if (imageTable.get(imp) == ds)
					return imp;
			}
			return null;
		//}
	}
	
	/** returns the ImageTranslator used by the LegacyImageMap */
	public ImageTranslator getTranslator() {
		//synchronized(lock) {
			return imageTranslator;
		//}
	}

	/**
	 * Ensures that the given dataset has a corresponding legacy image.
	 *
	 * @return the {@link ImagePlus} object shadowing this dataset.
	 */
	public ImagePlus registerDataset(Dataset dataset) {
		//synchronized(lock) {
			// find image window
			ImagePlus imp = null;
			for (final ImagePlus key : imageTable.keySet()) {
				final Dataset value = imageTable.get(key);
				if (dataset == value) {
					imp = key;
					break;
				}
			}
			if (imp == null) {
				// mirror dataset to image window
				imp = imageTranslator.createLegacyImage(dataset);
				imageTable.put(imp, dataset);
			}
			return imp;
		//}
	}
	
	/**
	 * Ensures that the given legacy image has a corresponding dataset.
	 *
	 * @return the {@link Dataset} object shadowing this legacy image.
	 */
	public Dataset registerLegacyImage(ImagePlus imp) {
		//synchronized(lock) {
			Dataset dataset = imageTable.get(imp);
			if (dataset == null) {
				// mirror image window to dataset
				dataset = imageTranslator.createDataset(imp);
				imageTable.put(imp, dataset);
			}
			else {  // dataset was already existing
				// do nothing
			}
			return dataset;
		//}
	}

	/** removes the mapping associated with a given Dataset */
	public void unregisterDataset(Dataset ds) {
		//synchronized(lock) {
			for (final ImagePlus imp : imageTable.keySet()) {
				final Dataset dataset = imageTable.get(imp);
				if (dataset == ds) {
					imageTable.remove(imp);
					removeImagePlusFromIJ1(imp);
				}
			}
		//}
	}
	
	// TODO - hatched as maybe useful
	/** removes the mapping associated with a given ImagePlus */
	public void unregisterLegacyImage(ImagePlus imp) {
		//synchronized(lock) {
			imageTable.remove(imp);
			removeImagePlusFromIJ1(imp);
		//}
	}

	// -- helpers --

	private void removeImagePlusFromIJ1(ImagePlus imp) {
		ImagePlus savedImagePlus = WindowManager.getCurrentImage();
		if (savedImagePlus != imp)
			WindowManager.setTempCurrentImage(imp);
		ImageWindow ij1Window = WindowManager.getCurrentWindow();
		if (ij1Window != null) {
			imp.changes = false;
			ij1Window.close();
		}
		WindowManager.setTempCurrentImage(null);
		if (savedImagePlus != imp)
			WindowManager.setTempCurrentImage(savedImagePlus);
	}
	
	private void subscribeToEvents() {
		final EventSubscriber<DatasetDeletedEvent> deletionSubscriber =
			new EventSubscriber<DatasetDeletedEvent>() {

				@Override
				public void onEvent(DatasetDeletedEvent event) {
					unregisterDataset(event.getObject());
				}
			};
		subscribers.add(deletionSubscriber);
		Events.subscribe(DatasetDeletedEvent.class, deletionSubscriber);
	}
}
