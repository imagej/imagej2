package imagej.ui.swing.tools.overlay;
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

import imagej.data.display.OverlayService;
import imagej.data.overlay.OverlaySettings;
import imagej.util.ColorRGB;
import imagej.util.awt.AWTColors;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;

import org.jhotdraw.draw.AbstractAttributedFigure;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.handle.Handle;
import org.jhotdraw.geom.Geom;

@SuppressWarnings("serial")
public class SwingPointFigure extends AbstractAttributedFigure {

	private final SwingPointTool swingPointTool;
	protected Rectangle2D.Double bounds;
	private final Rectangle2D.Double rect;
	private Color fillColor = Color.yellow;
	private Color lineColor = Color.white;

	/** Creates a new instance.
	 * @param swingPointTool TODO*/
	public SwingPointFigure(SwingPointTool swingPointTool) {
		this(swingPointTool, 0, 0);
	}

	public SwingPointFigure(SwingPointTool swingPointTool, final double x, final double y) {
		this.swingPointTool = swingPointTool;
		bounds = new Rectangle2D.Double(x, y, 1, 1);
		rect = new Rectangle2D.Double();
	}

	public void setPoint(final double x, final double y) {
		bounds.x = x;
		bounds.y = y;
	}

	public void setFillColor(final ColorRGB c) {
		if (c == null) {
			OverlayService srv = this.swingPointTool.getContext().getService(OverlayService.class);
			OverlaySettings settings = srv.getDefaultSettings();
			fillColor = AWTColors.getColor(settings.getFillColor());
		}
		else
			fillColor = AWTColors.getColor(c);
	}

	public void setLineColor(final ColorRGB c) {
		if (c == null) {
			OverlayService srv = this.swingPointTool.getContext().getService(OverlayService.class);
			OverlaySettings settings = srv.getDefaultSettings();
			lineColor = AWTColors.getColor(settings.getLineColor());
		}
		else
			lineColor = AWTColors.getColor(c);
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
	public SwingPointFigure clone() {
		final SwingPointFigure that = (SwingPointFigure) super.clone();
		that.bounds = (Rectangle2D.Double) this.bounds.clone();
		return that;
	}

	@Override
	public List<Handle> createHandles(final int detailLevel) {
		final Handle handle = new SwingPointHandle(this);
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