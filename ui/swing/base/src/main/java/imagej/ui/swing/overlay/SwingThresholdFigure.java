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

package imagej.ui.swing.overlay;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D.Double;

import net.imglib2.Cursor;
import net.imglib2.img.ImgPlus;
import net.imglib2.ops.pointset.PointSet;

import org.jhotdraw.draw.AbstractAttributedFigure;

/**
 * 
 * @author Barry DeZonia
 *
 */
public class SwingThresholdFigure extends AbstractAttributedFigure {

	private static final long serialVersionUID = 1L;

	private final ImgPlus<?> imgPlus;
	private final PointSet points;
	
	public SwingThresholdFigure(ImgPlus<?> imgPlus, PointSet points) {
		this.imgPlus = imgPlus;
		this.points = points;
	}
	
	@Override
	public boolean contains(Double arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public java.awt.geom.Rectangle2D.Double getBounds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getTransformRestoreData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void restoreTransformTo(Object arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void transform(AffineTransform arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void drawFill(Graphics2D arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void drawStroke(Graphics2D arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void draw(Graphics2D arg0) {
		final Color origC = arg0.getColor();
		// TODO - use color of associated Overlay
		arg0.setColor(Color.green);
		Cursor<long[]> cursor = points.cursor();
		while (cursor.hasNext()) {
			long[] pos = cursor.next();
			// TODO - only draw points that lay in the currently viewed plane
			arg0.drawLine((int)pos[0], (int)pos[1], (int)pos[0], (int)pos[1]);
		}
		arg0.setColor(origC);
		super.draw(arg0);
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
	public java.awt.geom.Rectangle2D.Double getDrawingArea() {
		return new java.awt.geom.Rectangle2D.Double(0, 0, imgPlus.max(0), imgPlus.max(1));
	}
	
	@Override
	public void setBounds(Double anchor, Double lead) {
		// do nothing
	}
}
