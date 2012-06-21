/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.ui.swing.tools.overlay;

import imagej.data.display.ImageDisplay;
import imagej.data.display.OverlayView;
import imagej.data.overlay.Overlay;
import imagej.data.overlay.PointOverlay;
import imagej.ext.MouseCursor;
import imagej.ext.plugin.Plugin;
import imagej.ext.tool.Tool;
import imagej.ui.swing.overlay.IJCreationTool;
import imagej.ui.swing.overlay.JHotDrawOverlayAdapter;
import imagej.ui.swing.overlay.JHotDrawTool;
import imagej.ui.swing.overlay.OverlayCreatedListener;
import imagej.util.ColorRGB;
import imagej.util.awt.AWTColors;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;

import org.jhotdraw.draw.AbstractAttributedFigure;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.handle.AbstractHandle;
import org.jhotdraw.draw.handle.Handle;
import org.jhotdraw.geom.Geom;

/**
 * TODO
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Tool.class, name = "Point", description = "Point overlays",
	iconPath = "/icons/tools/point.png", priority = PointAdapter.PRIORITY,
	enabled = true)
@JHotDrawOverlayAdapter(priority = PointAdapter.PRIORITY)
public class PointAdapter extends AbstractJHotDrawOverlayAdapter<PointOverlay> {

	public static final int PRIORITY = AngleAdapter.PRIORITY - 1;

	@Override
	public boolean supports(final Overlay overlay, final Figure figure) {
		if (!(overlay instanceof PointOverlay)) return false;
		return (figure == null) || (figure instanceof PointFigure);
	}

	@Override
	public PointOverlay createNewOverlay() {
		return new PointOverlay(getContext());
	}

	@Override
	public Figure createDefaultFigure() {
		final PointFigure figure = new PointFigure();
		figure.set(AttributeKeys.STROKE_COLOR, getDefaultStrokeColor());
		// Avoid IllegalArgumentException: miter limit < 1 on the EDT
		figure.set(AttributeKeys.IS_STROKE_MITER_LIMIT_FACTOR, false);
		return figure;
	}

	@Override
	public void updateFigure(final OverlayView overlayView, final Figure figure) {
		super.updateFigure(overlayView, figure);
		assert figure instanceof PointFigure;
		final PointFigure pointFig = (PointFigure) figure;
		final Overlay overlay = overlayView.getData();
		assert overlay instanceof PointOverlay;
		final PointOverlay pointOverlay = (PointOverlay) overlay;
		pointFig.setColor(pointOverlay.getFillColor());
		pointFig.setPoint(pointOverlay.getPoint(0), pointOverlay.getPoint(1));
	}

	@Override
	public void updateOverlay(final Figure figure, final OverlayView overlayView)
	{
		super.updateOverlay(figure, overlayView);
		assert figure instanceof PointFigure;
		final PointFigure point = (PointFigure) figure;
		final Overlay overlay = overlayView.getData();
		assert overlay instanceof PointOverlay;
		final PointOverlay pointOverlay = (PointOverlay) overlay;
		pointOverlay.setPoint(point.getX(), 0);
		pointOverlay.setPoint(point.getY(), 1);
		overlay.update();
	}

	@Override
	public MouseCursor getCursor() {
		return MouseCursor.CROSSHAIR;
	}

	/*
	@Override
	public void onMouseDown(final MsPressedEvent evt) {
		if (evt.getButton() != MsButtonEvent.LEFT_BUTTON) return;
		evt.consume();
	}

	@Override
	public void onMouseDrag(final MsDraggedEvent evt) {
		if (evt.getButton() != MsButtonEvent.LEFT_BUTTON) return;
		evt.consume();
	}

	@Override
	public void onMouseClick(imagej.ext.display.event.input.MsClickedEvent evt) {
		if (evt.getButton() != MsButtonEvent.LEFT_BUTTON) return;
		evt.consume();
	}
	*/

	@Override
	public JHotDrawTool getCreationTool(final ImageDisplay display,
		final OverlayCreatedListener listener)
	{
		return new IJCreationTool(display, this, listener);
	}

	@SuppressWarnings("serial")
	private class PointFigure extends AbstractAttributedFigure {

		protected Rectangle2D.Double bounds;
		private final Rectangle2D.Double rect;
		private Color fillColor = Color.yellow;
		
		/** Creates a new instance. */
		public PointFigure() {
			this(0, 0);
		}

		public PointFigure(final double x, final double y) {
			bounds = new Rectangle2D.Double(x, y, 1, 1);
			rect = new Rectangle2D.Double();
		}

		public void setPoint(final double x, final double y) {
			bounds.x = x;
			bounds.y = y;
		}

		public void setColor(ColorRGB c) {
			if (c == null)
				fillColor = Color.yellow;
			else
				fillColor = AWTColors.getColor(c);
		}
		
		public double getX() {
			return bounds.x;
		}

		public double getY() {
			return bounds.y;
		}

		// DRAWING
		@Override
		protected void drawFill(final Graphics2D g) {
			final Rectangle2D.Double r = (Rectangle2D.Double) bounds.clone();
			final double grow = AttributeKeys.getPerpendicularFillGrowth(this);
			Geom.grow(r, grow, grow);
			g.fill(r);
		}

		@Override
		protected void drawStroke(final Graphics2D g) {
			final Rectangle2D.Double r = (Rectangle2D.Double) bounds.clone();
			final double grow = AttributeKeys.getPerpendicularDrawGrowth(this);
			Geom.grow(r, grow, grow);
			g.draw(r);
		}

		// SHAPE AND BOUNDS
		@Override
		public Rectangle2D.Double getBounds() {
			final Rectangle2D.Double b = (Rectangle2D.Double) bounds.clone();
			return b;
		}

		@Override
		public Rectangle2D.Double getDrawingArea() {
			final Rectangle2D.Double r = (Rectangle2D.Double) bounds.clone();
			final double grow = AttributeKeys.getPerpendicularHitGrowth(this) + 1d;
			Geom.grow(r, grow, grow);
			return r;
		}

		/**
		 * Checks if a Point2D.Double is inside the figure.
		 */
		@Override
		public boolean contains(final Point2D.Double p) {
			final Rectangle2D.Double r = (Rectangle2D.Double) bounds.clone();
			final double grow = AttributeKeys.getPerpendicularHitGrowth(this) + 1d;
			Geom.grow(r, grow, grow);
			return r.contains(p);
		}

		@Override
		public void
			setBounds(final Point2D.Double anchor, final Point2D.Double lead)
		{
			bounds.x = anchor.x;
			bounds.y = anchor.y;
			bounds.width = 1;
			bounds.height = 1;
		}

		/**
		 * Moves the Figure to a new location.
		 * 
		 * @param tx the transformation matrix.
		 */
		@Override
		public void transform(final AffineTransform tx) {
			final Point2D.Double anchor = new Point2D.Double(bounds.x, bounds.y);
			tx.transform(anchor, anchor);
			setBounds(anchor, anchor);
		}

		@Override
		public void restoreTransformTo(final Object geometry) {
			bounds.setRect((Rectangle2D.Double) geometry);
		}

		@Override
		public Object getTransformRestoreData() {
			return bounds.clone();
		}

		@Override
		public PointFigure clone() {
			final PointFigure that = (PointFigure) super.clone();
			that.bounds = (Rectangle2D.Double) this.bounds.clone();
			return that;
		}

		@SuppressWarnings("synthetic-access")
		@Override
		public List<Handle> createHandles(final int detailLevel) {
			final Handle handle = new PointHandle(this);
			return Arrays.asList(handle);
		}

		/* scale invariant version but would be nice if we could avoid scale code
		 * and work in pixels. */
		@Override
		public void draw(final Graphics2D g) {
			final Color origC = g.getColor();
			final double sx = g.getTransform().getScaleX();
			final double sy = g.getTransform().getScaleY();
			final double ctrX = getX();
			final double ctrY = getY();

			g.setColor(Color.black);

			// black outline
			rect.x = ctrX - 2 / sx;
			rect.y = ctrY - 2 / sy;
			rect.width = 5 / sx;
			rect.height = 5 / sy;
			g.fill(rect);

			g.setColor(fillColor);

			// yellow center
			rect.x = ctrX - 1 / sx;
			rect.y = ctrY - 1 / sy;
			rect.width = 3 / sx;
			rect.height = 3 / sy;
			g.fill(rect);

			g.setColor(Color.white);

			// white line # 1
			rect.x = ctrX + 3 / sx;
			rect.y = ctrY;
			rect.width = 4 / sx;
			rect.height = 1 / sy;
			g.fill(rect);

			// white line # 2
			rect.x = ctrX - 6 / sx;
			rect.y = ctrY;
			rect.width = 4 / sx;
			rect.height = 1 / sy;
			g.fill(rect);

			// white line # 3
			rect.x = ctrX;
			rect.y = ctrY - 6 / sy;
			rect.width = 1 / sx;
			rect.height = 4 / sy;
			g.fill(rect);

			// white line # 4
			rect.x = ctrX;
			rect.y = ctrY + 3 / sy;
			rect.width = 1 / sx;
			rect.height = 4 / sy;
			g.fill(rect);

			g.setColor(origC);
		}

	}

	private class PointHandle extends AbstractHandle {

		private final PointFigure figure;

		private PointHandle(final PointFigure fig) {
			super(fig);
			figure = fig;
		}

		@Override
		public void trackEnd(final Point anchor, final Point lead,
			final int modifiers)
		{
			final double currX = figure.getX();
			final double currY = figure.getY();
			final double dx = lead.x - anchor.x;
			final double dy = lead.y - anchor.y;
			figure.setPoint(currX + dx, currY + dy);
		}

		@Override
		public void trackStart(final Point anchor, final int modifiers) {
			// do nothing
		}

		@Override
		public void trackStep(final Point anchor, final Point lead,
			final int modifiers)
		{
			// do nothing
		}

		@Override
		protected Rectangle basicGetBounds() {
			final Rectangle rect = new Rectangle();
			final Rectangle2D.Double bounds = figure.getBounds();
			rect.x = (int) bounds.x;
			rect.y = (int) bounds.y;
			rect.width = (int) bounds.width;
			rect.height = (int) bounds.height;
			return rect;
		}
	}
}
