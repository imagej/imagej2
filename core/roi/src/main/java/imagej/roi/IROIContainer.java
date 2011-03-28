/* IROIContainer.java
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

/**
 * @author leek
 *
 *The IROIContainer interface captures the idea of a possibly
 *hierarchical tree of collections of ROIs, for instance
 *the collection available for picking by the user or
 *the set that are applied to a particular image.
 *
 *The interface can be applied to objects that, by nature
 *are a collection of ROIs instead of containing a collection
 *of ROIs. A segmentation / labeling is one of these: the
 *ROIs correspond to labels in the underlying matrix.
 */
public interface IROIContainer {
	/**
	 * The number of ROIs in the container
	 * @return - a count of ROIs
	 */
	public int getROICount();
	
	/**
	 * @return the names of the ROIs in the container (but not the sub-containers)
	 */
	public Collection<String> getROINames();
	
	/**
	 * @return the ROIs in the container
	 */
	public Collection<ImageJROI> getROIs();
	
	/**
	 * Retrieve a ROI by name
	 * @param name
	 * @return
	 */
	public ImageJROI getROI(String name);

	/**
	 * @return the number of sub-containers within this one
	 */
	public int getSubContainerCount();
	
	/**
	 * @return the names of all sub-containers
	 */
	public Collection<String> getSubContainerNames();
	
	/**
	 * @return all sub-containers
	 */
	public Collection<IROIContainer> getSubContainers();
	
	/**
	 * @param name the name of the container to retrieve
	 * @return the sub-container with the given name
	 */
	public IROIContainer getSubContainer(String name);
}
