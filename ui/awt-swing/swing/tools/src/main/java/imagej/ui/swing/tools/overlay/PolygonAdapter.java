//
// PolygonAdapter.java
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

package imagej.ui.swing.tools.overlay;

import imagej.data.display.OverlayView;
import imagej.data.overlay.Overlay;
import imagej.data.overlay.PolygonOverlay;
import imagej.ext.plugin.Plugin;
import imagej.ext.tool.Tool;
import imagej.ui.swing.overlay.JHotDrawOverlayAdapter;
import imagej.util.Log;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.roi.PolygonRegionOfInterest;

import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.BezierFigure;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.handle.BezierNodeHandle;
import org.jhotdraw.draw.handle.BezierOutlineHandle;
import org.jhotdraw.draw.handle.Handle;
import org.jhotdraw.geom.BezierPath.Node;

/**
 * TODO
 * 
 * @author Lee Kamentsky
 */
@Plugin(type = Tool.class, name = "Polygon", description = "Polygon overlays",
	iconPath = "/icons/tools/polygon.png", priority = PolygonAdapter.PRIORITY,
	enabled = true)
@JHotDrawOverlayAdapter(priority = PolygonAdapter.PRIORITY)
public class PolygonAdapter extends
	AbstractJHotDrawOverlayAdapter<PolygonOverlay>
{

	public static final int PRIORITY = EllipseAdapter.PRIORITY - 1;

	static private BezierFigure downcastFigure(final Figure figure) {
		assert figure instanceof BezierFigure;
		return (BezierFigure) figure;
	}

	static private PolygonOverlay downcastOverlay(final Overlay overlay) {
		assert overlay instanceof PolygonOverlay;
		return (PolygonOverlay) overlay;
	}

	/*
	 * The BezierFigure uses a BezierNodeHandle which can change the curve
	 * connecting vertices from a line to a Bezier curve. We subclass both 
	 * the figure and the node handle to defeat this.
	 */
	public static class PolygonNodeHandle extends BezierNodeHandle {

		public PolygonNodeHandle(final BezierFigure owner, final int index,
			final Figure transformOwner)
		{
			super(owner, index, transformOwner);
		}

		public PolygonNodeHandle(final BezierFigure owner, final int index) {
			super(owner, index);
		}

		@Override
		public void trackEnd(final Point anchor, final Point lead,
			final int modifiersEx)
		{
			// Remove the behavior associated with the shift keys
			super.trackEnd(anchor, lead, modifiersEx &
				~(InputEvent.META_DOWN_MASK | InputEvent.CTRL_DOWN_MASK |
					InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		}

	}

	public static class PolygonFigure extends BezierFigure {

		public PolygonFigure() {
			// The constructor makes the BezierFigure a closed figure.
			super(true);
		}

		@Override
		public Collection<Handle> createHandles(final int detailLevel) {
			final LinkedList<Handle> handles = new LinkedList<Handle>();
			if (detailLevel != 0) {
				return super.createHandles(detailLevel);
			}
			handles.add(new BezierOutlineHandle(this));
			for (int i = 0, n = path.size(); i < n; i++) {
				handles.add(new PolygonNodeHandle(this, i));
			}
			return handles;
		}

		private static final long serialVersionUID = 1L;

	}

	@Override
	public boolean supports(final Overlay overlay, final Figure figure) {
		if ((figure != null) && (!(figure instanceof PolygonFigure))) return false;
		return overlay instanceof PolygonOverlay;
	}

	@Override
	public Overlay createNewOverlay() {
		final PolygonOverlay o = new PolygonOverlay();
		return o;
	}

	@Override
	public Figure createDefaultFigure() {
		final BezierFigure figure = new PolygonFigure();
		figure.set(AttributeKeys.FILL_COLOR, getDefaultFillColor());
		figure.set(AttributeKeys.STROKE_COLOR, getDefaultStrokeColor());
		return figure;
	}

	@Override
	public void updateOverlay(final Figure figure, final OverlayView overlay) {
		super.updateOverlay(figure, overlay);
		final BezierFigure b = downcastFigure(figure);
		final PolygonOverlay poverlay = downcastOverlay(overlay.getData());
		final PolygonRegionOfInterest roi = poverlay.getRegionOfInterest();
		final int nodeCount = b.getNodeCount();
		while (roi.getVertexCount() > nodeCount) {
			roi.removeVertex(nodeCount);
			Log.debug("Removed node from overlay.");
		}
		for (int i = 0; i < nodeCount; i++) {
			final Node node = b.getNode(i);
			final double[] position = new double[] { node.x[0], node.y[0] };
			if (roi.getVertexCount() == i) {
				roi.addVertex(i, new RealPoint(position));
				Log.debug("Added node to overlay");
			}
			else {
				if ((position[0] != roi.getVertex(i).getDoublePosition(0)) ||
					(position[1] != roi.getVertex(i).getDoublePosition(1)))
				{
					Log.debug(String.format("Vertex # %d moved to %f,%f", i + 1,
						position[0], position[1]));
				}
				roi.setVertexPosition(i, position);
			}
		}
	}

	@Override
	public void updateFigure(final OverlayView overlay, final Figure figure) {
		super.updateFigure(overlay, figure);
		final BezierFigure b = downcastFigure(figure);
		final PolygonOverlay pOverlay = downcastOverlay(overlay.getData());
		final PolygonRegionOfInterest roi = pOverlay.getRegionOfInterest();
		final int vertexCount = roi.getVertexCount();
		while (b.getNodeCount() > vertexCount)
			b.removeNode(vertexCount);
		for (int i = 0; i < vertexCount; i++) {
			final RealLocalizable vertex = roi.getVertex(i);
			if (b.getNodeCount() == i) {
				final Node node =
					new Node(vertex.getDoublePosition(0), vertex.getDoublePosition(1));
				b.addNode(node);
			}
			else {
				final Node node = b.getNode(i);
				node.mask = 0;
				Arrays.fill(node.x, vertex.getDoublePosition(0));
				Arrays.fill(node.y, vertex.getDoublePosition(1));
			}
		}
	}
}
