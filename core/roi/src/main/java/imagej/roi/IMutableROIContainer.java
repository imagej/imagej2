/* IMutableROIContainer.java
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

/**
 * @author leek
 * A ROI container where you can add and remove ROIs and sub-containers
 */
public interface IMutableROIContainer extends IROIContainer {
	/**
	 * Add a ROI to the container. Insert if there is not a
	 * ROI with the same name, replace if there is a ROI with the same name.
	 * 
	 * @param roi
	 * @param name - user-visible name of the ROI
	 */
	public void addROI(ImageJROI roi, String name);
	
	/**
	 * Remove a ROI by name
	 * @param name
	 */
	public void removeROI(String name);
	
	/**
	 * Add a sub-container to this one
	 * @param container
	 * @param name - the name of the container, for retrieval
	 */
	public void addSubContainer(IROIContainer container, String name);
	
	/**
	 * Remove a sub-container by name
	 * @param name
	 */
	public void removeSubContainer(String name);
}
