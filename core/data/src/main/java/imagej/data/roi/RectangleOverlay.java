//
// RectangleROI.java
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
package imagej.data.roi;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import net.imglib2.roi.RectangleRegionOfInterest;

/**
 * @author leek
 *
 * A rectangular region of interest
 */
public class RectangleOverlay extends AbstractROIOverlay<RectangleRegionOfInterest> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public RectangleOverlay() {
		super(new RectangleRegionOfInterest(new double [] { 0, 0}, new double [] {0,0}));
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		RectangleRegionOfInterest roi = getRegionOfInterest();
		out.writeDouble(roi.getOrigin(0));
		out.writeDouble(roi.getOrigin(1));
		out.writeDouble(roi.getExtent(0));
		out.writeDouble(roi.getExtent(1));
	}
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		RectangleRegionOfInterest roi = getRegionOfInterest();
		roi.setOrigin(in.readDouble(), 0);
		roi.setOrigin(in.readDouble(), 1);
		roi.setExtent(in.readDouble(), 0);
		roi.setExtent(in.readDouble(), 1);
	}
}
