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

package imagej.ui.swing.tools.overlay;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import org.jhotdraw.draw.handle.AbstractHandle;

// FIXME : I think this class is no longer necessary. Currently it is avoided
// in SwingPointFigure by using a DragHandle instead. BDZ 12-18-12

/**
 * TODO
 * 
 * @author Johannes Schindelin
 */
public class PointHandle extends AbstractHandle {

	private final PointFigure figure;

	public PointHandle(final PointFigure fig) {
		super(fig);
		figure = fig;
	}

	@Override
	public void trackEnd(final Point anchor, final Point lead,
		final int modifiers)
	{
		final double dx = lead.x - anchor.x;
		final double dy = lead.y - anchor.y;
		figure.move(dx, dy);
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

	@Override
	public void draw(Graphics2D g) {
		// do nothing
	}
}
