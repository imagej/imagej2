/* ROIContainer.java
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

package imagej.roi;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author leek
 *The default container for ROIs
 */
public class ROIContainer implements IMutableROIContainer {

	protected Map<String, ImageJROI> roiMap = new HashMap<String, ImageJROI>();
	protected Map<String, IROIContainer> containerMap = new HashMap<String, IROIContainer>();
	@Override
	public int getROICount() {
		return roiMap.size();
	}

	@Override
	public Collection<String> getROINames() {
		return Collections.unmodifiableSet(roiMap.keySet());
	}

	@Override
	public Collection<ImageJROI> getROIs() {
		return Collections.unmodifiableCollection(roiMap.values());
	}

	@Override
	public ImageJROI getROI(String name) {
		return roiMap.get(name);
	}

	@Override
	public int getSubContainerCount() {
		return containerMap.size();
	}

	@Override
	public Collection<String> getSubContainerNames() {
		return Collections.unmodifiableSet(containerMap.keySet());
	}

	@Override
	public Collection<IROIContainer> getSubContainers() {
		return Collections.unmodifiableCollection(containerMap.values());
	}

	@Override
	public IROIContainer getSubContainer(String name) {
		return containerMap.get(name);
	}

	@Override
	public void addROI(ImageJROI roi, String name) {
		roiMap.put(name, roi);
	}

	@Override
	public void removeROI(String name) {
		roiMap.remove(name);
	}

	@Override
	public void addSubContainer(IROIContainer container, String name) {
		containerMap.put(name, container);
	}

	@Override
	public void removeSubContainer(String name) {
		containerMap.remove(name);
	}

}
