// PolygonOverlay.java
//
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

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.roi.PolygonRegionOfInterest;

/**
 * @author Lee Kamentsky
 *
 */
public class PolygonOverlay extends AbstractROIOverlay<PolygonRegionOfInterest> {

	public PolygonOverlay() {
		super(new PolygonRegionOfInterest());
		// TODO Auto-generated constructor stub
	}
	private static final long serialVersionUID = 1L;
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		PolygonRegionOfInterest roi = getRegionOfInterest();
		final int vertexCount = roi.getVertexCount(); 
		out.writeInt(vertexCount);
		for (int i=0; i<vertexCount; i++) {
			final RealLocalizable vertex = roi.getVertex(i);
			out.writeDouble(vertex.getDoublePosition(0));
			out.writeDouble(vertex.getDoublePosition(1));
		}
	}
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		PolygonRegionOfInterest roi = getRegionOfInterest();
		while( roi.getVertexCount() > 0) {
			roi.removeVertex(0);
		}
		final int vertexCount = in.readInt();
		for (int i=0; i<vertexCount; i++) {
			RealPoint vertex = new RealPoint(
					new double [] {
							in.readDouble(), in.readDouble()
					});
			roi.addVertex(i, vertex);
		}
	}

}
