// PolygonAdapter.java
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
package imagej.ui.swing.tools.roi;

import java.util.Arrays;

import imagej.data.roi.Overlay;
import imagej.data.roi.PolygonOverlay;
import imagej.tool.Tool;
import imagej.util.Log;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.roi.PolygonRegionOfInterest;

import org.jhotdraw.draw.BezierFigure;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.geom.BezierPath.Node;

/**
 * @author leek
 *
 */
@Tool(name = "Polygon", iconPath = "/tools/polygon.png",
		priority = PolygonAdapter.PRIORITY, enabled = true)
@JHotDrawOverlayAdapter(priority = PolygonAdapter.PRIORITY)
public class PolygonAdapter extends AbstractShapeOverlayAdapter<BezierFigure, PolygonRegionOfInterest> {
	final static public int PRIORITY = 150;

	static private BezierFigure downcastFigure(Figure figure) {
		assert figure instanceof BezierFigure;
		return (BezierFigure) figure;
	}
	
	static private PolygonOverlay downcastOverlay(Overlay overlay) {
		assert overlay instanceof PolygonOverlay;
		return (PolygonOverlay)overlay;
	}
	
	/* (non-Javadoc)
	 * @see imagej.ui.swing.tools.roi.IJHotDrawOverlayAdapter#supports(imagej.data.roi.Overlay, org.jhotdraw.draw.Figure)
	 */
	@Override
	public boolean supports(Overlay overlay, Figure figure) {
		if ((figure != null) && (!(figure instanceof BezierFigure))) return false;
		return overlay instanceof PolygonOverlay;
	}

	/* (non-Javadoc)
	 * @see imagej.ui.swing.tools.roi.IJHotDrawOverlayAdapter#createNewOverlay()
	 */
	@Override
	public Overlay createNewOverlay() {
		return new PolygonOverlay();
	}

	/* (non-Javadoc)
	 * @see imagej.ui.swing.tools.roi.IJHotDrawOverlayAdapter#createDefaultFigure()
	 */
	@Override
	public Figure createDefaultFigure() {
		final BezierFigure figure = new BezierFigure(true);
		return figure;
	}

	/* (non-Javadoc)
	 * @see imagej.ui.swing.tools.roi.IJHotDrawOverlayAdapter#updateOverlay(org.jhotdraw.draw.Figure, imagej.data.roi.Overlay)
	 */
	@Override
	public void updateOverlay(Figure figure, Overlay overlay) {
		BezierFigure b = downcastFigure(figure);
		PolygonRegionOfInterest roi = downcastOverlay(overlay).getShapeRegionOfInterest();
		int nodeCount = b.getNodeCount();
		while(roi.getVertexCount() > nodeCount) {
			roi.removeVertex(nodeCount);
			Log.debug("Removed node from overlay.");
		}
		for (int i=0; i < nodeCount; i++) {
			Node node = b.getNode(i);
			double [] position = new double[] { node.x[0], node.y[0] };
			if (roi.getVertexCount() == i) {
				roi.addVertex(i, new RealPoint(position));
				Log.debug("Added node to overlay");
			} else {
				if ((position[0] != roi.getVertex(i).getDoublePosition(0)) ||
					(position[1] != roi.getVertex(i).getDoublePosition(1))) {
					Log.debug(String.format("Vertex # %d moved to %f,%f", i+1, position[0], position[1] ));
				}
				roi.setVertexPosition(i, position);
			}
		}
	}

	/* (non-Javadoc)
	 * @see imagej.ui.swing.tools.roi.IJHotDrawOverlayAdapter#updateFigure(imagej.data.roi.Overlay, org.jhotdraw.draw.Figure)
	 */
	@Override
	public void updateFigure(Overlay overlay, Figure figure) {
		BezierFigure b = downcastFigure(figure);
		PolygonRegionOfInterest roi = downcastOverlay(overlay).getShapeRegionOfInterest();
		int vertexCount = roi.getVertexCount();
		while(b.getNodeCount() > vertexCount) b.removeNode(vertexCount);
		for (int i=0; i<vertexCount; i++) {
			RealLocalizable vertex = roi.getVertex(i);
			if (b.getNodeCount() == i) {
				Node node = new Node(vertex.getDoublePosition(0),
									 vertex.getDoublePosition(1));
				b.addNode(node);
			} else {
				Node node = b.getNode(i);
				node.mask = 0;
				Arrays.fill(node.x, vertex.getDoublePosition(0));
				Arrays.fill(node.y, vertex.getDoublePosition(1));
			}
		}
	}
}
