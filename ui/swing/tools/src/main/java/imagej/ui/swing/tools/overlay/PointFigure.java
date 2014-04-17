/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.ui.swing.tools.overlay;

import imagej.util.awt.AWTColors;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jhotdraw.draw.AbstractAttributedFigure;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.handle.DragHandle;
import org.jhotdraw.draw.handle.Handle;
import org.jhotdraw.geom.Geom;
import org.scijava.util.ColorRGB;

/**
 * Implementation of a point as a JHotDraw figure.
 * 
 * @author Johannes Schindelin
 * @author Barry DeZonia
 */
public class PointFigure extends AbstractAttributedFigure {

	protected Rectangle2D.Double bounds;
	private final Rectangle2D.Double rect;
	private final List<double[]> points;
	private Color fillColor = Color.yellow;
	private Color lineColor = Color.white;

	public PointFigure() {
		this(new double[2]);
	}

	public PointFigure(double[] pt) {
		this(Arrays.asList(pt));
	}

	public PointFigure(List<double[]> pts) {
		bounds = new Rectangle2D.Double();
		rect = new Rectangle2D.Double();
		points = new ArrayList<double[]>();
		setPoints(pts);
	}

	public void setPoints(List<double[]> pts) {
		points.clear();
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		for (double[] pt : pts) {
			points.add(pt.clone());
			if (pt[0] < minX) minX = pt[0];
			if (pt[0] > maxX) maxX = pt[0];
			if (pt[1] < minY) minY = pt[1];
			if (pt[1] > maxY) maxY = pt[1];
		}
		bounds.x = minX;
		bounds.y = minY;
		bounds.width = maxX - minX + 0.1;
		bounds.height = maxY - minY + 0.1;
	}

	public void setFillColor(final ColorRGB c) {
		fillColor = AWTColors.getColor(c);
	}

	public void setLineColor(final ColorRGB c) {
		lineColor = AWTColors.getColor(c);
	}

	public double getX() {
		return bounds.x;
	}

	public double getY() {
		return bounds.y;
	}

	public List<double[]> getPoints() {
		return points;
	}

	public void move(double dx, double dy) {
		bounds.x += dx;
		bounds.y += dy;
		for (double[] pt : points) {
			pt[0] += dx;
			pt[1] += dy;
		}
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
	 * Checks if a Point2D.Double is near any point contained in the figure
	 */
	@Override
	public boolean contains(final Point2D.Double p) {
		final Rectangle2D.Double r = new Rectangle2D.Double();
		for (double[] pt : points) {
			r.x = pt[0];
			r.y = pt[1];
			// NB - 0.1 works, 1.0 works, even 0.0 works but selection harder
			r.width = 1.0;
			r.height = 1.0;
			final double grow = AttributeKeys.getPerpendicularHitGrowth(this) + 1d;
			Geom.grow(r, grow, grow);
			if (r.contains(p)) return true;
		}
		return false;
	}

	@Override
	public void
		setBounds(final Point2D.Double anchor, final Point2D.Double lead)
	{
		double dx = anchor.x - bounds.x;
		double dy = anchor.y - bounds.y;
		move(dx, dy);
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
		setBounds((Point2D.Double) geometry, (Point2D.Double) geometry);
	}

	@Override
	public Object getTransformRestoreData() {
		return new Point2D.Double(bounds.x, bounds.y);
	}

	@Override
	public PointFigure clone() {
		final PointFigure that = (PointFigure) super.clone();
		that.bounds = (Rectangle2D.Double) this.bounds.clone();
		return that;
	}

	@Override
	public List<Handle> createHandles(final int detailLevel) {

		// NB - original code that was not dragging correctly - BDZ
		// final Handle handle = new SwingPointHandle(this);

		// NB - new approach here:
		final Handle handle = new DragHandle(this);

		return Arrays.asList(handle);
	}

	/* scale invariant version but would be nice if we could avoid scale code
	 * and work in pixels. */
	@Override
	public void draw(final Graphics2D g) {
		final Color origC = g.getColor();
		final double sx = g.getTransform().getScaleX();
		final double sy = g.getTransform().getScaleY();

		for (double[] pt : points) {
			final double ctrX = pt[0];
			final double ctrY = pt[1];

			g.setColor(Color.black);

			// black outline around center region
			rect.x = ctrX - 2 / sx;
			rect.y = ctrY - 2 / sy;
			rect.width = 5 / sx;
			rect.height = 5 / sy;
			g.fill(rect);

			g.setColor(fillColor);

			// center region
			rect.x = ctrX - 1 / sx;
			rect.y = ctrY - 1 / sy;
			rect.width = 3 / sx;
			rect.height = 3 / sy;
			g.fill(rect);

			g.setColor(lineColor);

			// tick mark line # 1
			rect.x = ctrX + 3 / sx;
			rect.y = ctrY;
			rect.width = 4 / sx;
			rect.height = 1 / sy;
			g.fill(rect);

			// tick mark line # 2
			rect.x = ctrX - 6 / sx;
			rect.y = ctrY;
			rect.width = 4 / sx;
			rect.height = 1 / sy;
			g.fill(rect);

			// tick mark line # 3
			rect.x = ctrX;
			rect.y = ctrY - 6 / sy;
			rect.width = 1 / sx;
			rect.height = 4 / sy;
			g.fill(rect);

			// tick mark line # 4
			rect.x = ctrX;
			rect.y = ctrY + 3 / sy;
			rect.width = 1 / sx;
			rect.height = 4 / sy;
			g.fill(rect);

			g.setColor(origC);
		}
	}


}
