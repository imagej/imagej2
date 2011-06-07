//
// DisplayManager.java
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

package imagej.display;

import imagej.ImageJ;
import imagej.Manager;
import imagej.ManagerComponent;
import imagej.data.DataObject;
import imagej.data.Dataset;
import imagej.object.ObjectManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager component for working with {@link Display}s.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
@Manager(priority = Manager.NORMAL_PRIORITY)
public final class DisplayManager implements ManagerComponent {

	private Display activeDisplay;

	// -- DisplayManager methods --

	public Display getActiveDisplay() {
		return activeDisplay;
	}

	public void setActiveDisplay(final Display display) {
		activeDisplay = display;
	}

	public Dataset getActiveDataset() {
		if (activeDisplay == null) return null;
		final DisplayView activeView = activeDisplay.getActiveView();
		if (activeView == null) return null;
		final DataObject dataObject = activeView.getDataObject();
		if (dataObject instanceof Dataset) return (Dataset) dataObject;
		return null;
	}

	public DatasetView getActiveDatasetView() {
		if (activeDisplay == null) return null;
		final DisplayView activeView = activeDisplay.getActiveView();
		if (activeView instanceof DatasetView) return (DatasetView) activeView;
		return null;
	}

	/** Gets a list of all active {@link Display}s. */
	public List<Display> getDisplays() {
		final ObjectManager objectManager = ImageJ.get(ObjectManager.class);
		return objectManager.getObjects(Display.class);
	}

	/**
	 * Gets a list of {@link Display}s containing the given {@link DataObject}.
	 */
	public List<Display> getDisplays(final DataObject dataObject) {
		final ArrayList<Display> displays = new ArrayList<Display>();
		for (final Display display : getDisplays()) {
			// check whether data object is present in this display
			for (final DisplayView view : display.getViews()) {
				if (dataObject == view.getDataObject()) {
					displays.add(display);
					break;
				}
			}
		}
		return displays;
	}

	// -- ManagerComponent methods --

	@Override
	public void initialize() {
		activeDisplay = null;
	}

}
