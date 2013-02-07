/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package imagej.ui.swing.overlay;

import imagej.data.display.ImageDisplay;
import imagej.data.overlay.ThresholdOverlay;
import imagej.display.Displayable;
import imagej.util.awt.AWTColors;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;

import net.imglib2.Cursor;
import net.imglib2.img.ImgPlus;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSet;

import org.jhotdraw.draw.AbstractAttributedFigure;
import org.jhotdraw.draw.AttributeKeys;

/**
 * TODO
 * 
 * @author Barry DeZonia
 */
public class SwingThresholdFigure extends AbstractAttributedFigure implements
	Displayable
{

	private static final long serialVersionUID = 1L;

	private final ImageDisplay display;
	private final ImgPlus<?> imgPlus;
	private final ThresholdOverlay overlay;
	private final Rectangle2D.Double rect;
	
	public SwingThresholdFigure(ImageDisplay display, ImgPlus<?> imgPlus,
		ThresholdOverlay overlay)
	{
		this.display = display;
		this.imgPlus = imgPlus;
		this.overlay = overlay;
		this.rect = new Rectangle2D.Double();
		setAttributeEnabled(AttributeKeys.FILL_COLOR, true);
		setAttributeEnabled(AttributeKeys.STROKE_COLOR, false);
		setAttributeEnabled(AttributeKeys.TEXT_COLOR, false);
	}
	
	@Override
	public boolean contains(Double arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Rectangle2D.Double getBounds() {
		return new Rectangle2D.Double(0, 0, imgPlus.max(0), imgPlus.max(1));
	}

	@Override
	public Object getTransformRestoreData() {
		return new Object();
	}

	@Override
	public void restoreTransformTo(Object arg0) {
		// do nothing
	}

	@Override
	public void transform(AffineTransform arg0) {
		// do nothing
	}

	@Override
	protected void drawStroke(Graphics2D arg0) {
		// do nothing
	}

	@Override
	public Double getStartPoint() {
		return new Double(imgPlus.min(0), imgPlus.min(1));
	}

	@Override
	public Double getEndPoint() {
		return new Double(imgPlus.max(0), imgPlus.max(1));
	}
	
	@Override
	public Rectangle2D.Double getDrawingArea() {
		return new Rectangle2D.Double(0, 0, imgPlus.max(0), imgPlus.max(1));
	}
	
	@Override
	public void setBounds(Double anchor, Double lead) {
		// do nothing
	}

	// NB - not using a ConditionalPointSet directly. ConditionalPointSet may
	// encompass a huge hypervolume and we are only interested in the points in
	// the displayed plane. So we define a smaller hypervolume of just the viewed
	// plane, iterate it and then classify the point directly. This is
	// much faster for display purposes.

	@Override
	protected void drawFill(final Graphics2D g) {
		final Color origC = g.getColor();
		Color withinColor = AWTColors.getColor(overlay.getColorWithin());
		Color lessColor = AWTColors.getColor(overlay.getColorLess());
		Color greaterColor = AWTColors.getColor(overlay.getColorGreater());
		Color color = null;
		Color lastColor = null;
		rect.width = 1;
		rect.height = 1;
		// only iterate currently viewed plane
		Cursor<long[]> cursor = getViewedPlane().cursor();
		while (cursor.hasNext()) {
			long[] pos = cursor.next();
			// only draw points that satisfy the threshold conditions
			color = null;
			int classification = overlay.classify(pos);
			if (classification < 0) {
				color = lessColor;
			}
			else if (classification > 0) {
				color = greaterColor;
			}
			else color = withinColor;
			if (color != null) {
				if (color != lastColor) {
					g.setColor(color);
					lastColor = color;
				}
				rect.x = pos[0];
				rect.y = pos[1];
				g.fill(rect);
			}
		}
		g.setColor(origC);
	}

	// -- Displayable --

	@Override
	public void draw() {
		// OLD WAY : likely wrong
		// fireFigureChanged();
		// NEW WAY
		fireAreaInvalidated();
	}

	// -- helpers --

	// TODO - there is an assumption here that display and data coords map 1:1.
	// If we have a display with multiple datasets in it this code isn't quite
	// correct.

	private PointSet getViewedPlane() {
		long[] pt1 = new long[display.numDimensions()];
		long[] pt2 = new long[display.numDimensions()];
		for (int i = 2; i < pt1.length; i++) {
			pt1[i] = pt2[i] = display.getLongPosition(i);
		}
		pt2[0] = display.dimension(0) - 1;
		pt2[1] = display.dimension(1) - 1;
		return new HyperVolumePointSet(pt1, pt2);
	}
}
