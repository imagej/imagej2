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

import static org.jhotdraw.draw.AttributeKeys.FILL_COLOR;
import static org.jhotdraw.draw.AttributeKeys.STROKE_COLOR;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jhotdraw.draw.AbstractAttributedFigure;
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.BezierFigure;
import org.jhotdraw.draw.event.FigureEvent;
import org.jhotdraw.draw.event.FigureListener;
import org.jhotdraw.draw.handle.Handle;
import org.jhotdraw.geom.BezierPath;

/**
 * A composite figure, made up of possibly-overlapping {@link BezierFigure}s.
 * 
 * The described figures are <b>always</b> winding rule even/odd.
 * 
 * @author Johannes Schindelin
 */
@SuppressWarnings("hiding")
public class SwingGeneralPathFigure extends AbstractAttributedFigure {

	private List<BezierFigure> figures;
	private transient GeneralPath path;

	public SwingGeneralPathFigure(final BezierFigure... list) {
		figures = new ArrayList<BezierFigure>() {
			@Override
			public boolean add(final BezierFigure figure) {
				figure.restoreAttributesTo(SwingGeneralPathFigure.this.getAttributesRestoreData());
				figure.addFigureListener(new FigureListener() {
					@Override
					public void areaInvalidated(FigureEvent e) {
						invalidate();
						fireAreaInvalidated();
					}

					@Override
					public void attributeChanged(FigureEvent e) {
						invalidate();
						fireAttributeChanged(e.getAttribute(), e.getOldValue(), e.getNewValue());
					}

					@Override
					public void figureHandlesChanged(FigureEvent e) {
						invalidate();
						fireFigureHandlesChanged();
					}

					@Override
					public void figureChanged(FigureEvent e) {
						invalidate();
						fireFigureChanged();
					}

					@Override
					public void figureAdded(FigureEvent e) {
						invalidate();
						fireFigureAdded();
					}

					@Override
					public void figureRemoved(FigureEvent e) {
						invalidate();
						fireFigureRemoved();
					}

					@Override
					public void figureRequestRemove(FigureEvent e) {
						invalidate();
						fireFigureRequestRemove();
					}
				});
				return super.add(figure);
			}
		};
		final Color color = get(STROKE_COLOR);
		set(FILL_COLOR, new Color(color.getRed(), color.getGreen(), color.getBlue(), 127));
		for (final BezierFigure figure : list) {
			figures.add(figure);
		}
	}

	/*
		// The constructor makes the BezierFigure a closed figure.
		new BezierFigure(true);
	*/

	private static final long serialVersionUID = 1L;

	/* -- implemented abstract methods */

	@Override
	public boolean contains(Double point) {
		final GeneralPath path = getGeneralPath();
		return path.contains(point);
	}

	@Override
	public Rectangle2D.Double getBounds() {
		Rectangle2D.Double result = new Rectangle2D.Double();
		for (final BezierFigure figure : figures) {
			Rectangle2D.union(result, figure.getBounds(), result);
		}
		return result;
	}

	@Override
	public Object getTransformRestoreData() {
		final List<BezierFigure> result = new ArrayList<BezierFigure>();
		for (final BezierFigure figure : figures) {
			result.add(figure.clone());
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void restoreTransformTo(Object geometry) {
		figures = (List<BezierFigure>) geometry;
	}

	@Override
	public void transform(AffineTransform tx) {
		for (final BezierFigure figure : figures) {
			figure.getBezierPath().transform(tx);
		}
		invalidate();
	}

	@Override
	protected void drawFill(Graphics2D g) {
		g.setColor(get(FILL_COLOR));
		g.fill(getGeneralPath());
	}

	@Override
	protected void drawStroke(Graphics2D g) {
		for (final BezierFigure figure : figures) {
			g.draw(figure.getBezierPath());
		}
	}

	/* -- overridden methods -- */

	@Override
	public Collection<Handle> createHandles(final int detailLevel) {
		final LinkedList<Handle> handles = new LinkedList<Handle>();
		for (final BezierFigure figure : figures) {
			handles.addAll(figure.createHandles(detailLevel));
		}
		return handles;
	}

	@Override
	public synchronized void invalidate() {
		path = null;
		super.invalidate();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void setAttributeEnabled(AttributeKey key, boolean b) {
		super.setAttributeEnabled(key, b);
		for (final BezierFigure figure : figures) {
			figure.setAttributeEnabled(key, b);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void setAttributes(Map<AttributeKey, Object> map) {
		super.setAttributes(map);
		for (final BezierFigure figure : figures) {
			figure.setAttributes(map);
		}
	}

	@Override
	public void restoreAttributesTo(Object restoreData) {
		super.restoreAttributesTo(restoreData);
		for (final BezierFigure figure : figures) {
			figure.restoreAttributesTo(restoreData);
		}
	}

	/**
	 * Sets an attribute of the figure.
	 * AttributeKey name and semantics are defined by the class implementing
	 * the figure interface.
	 */
	@Override
	public <T> void set(AttributeKey<T> key, T newValue) {
		super.set(key, newValue);
		for (final BezierFigure figure : figures) {
			figure.set(key, newValue);
		}
	}

    /* -- public methods -- */

	@SuppressWarnings("null")
	public synchronized void setGeneralPath(final GeneralPath path) {
		this.path = path;
		figures.clear();
		BezierPath bezierPath = null;
		final PathIterator iterator = path.getPathIterator(null);
		final double[] segment = new double[6];
		for (; !iterator.isDone(); iterator.next()) {
			int type = iterator.currentSegment(segment);
			switch (type) {
			case PathIterator.SEG_MOVETO:
				if (bezierPath != null) add(bezierPath, false);
				bezierPath = new BezierPath();
				bezierPath.moveTo(segment[0], segment[1]);
				break;
			case PathIterator.SEG_LINETO:
				bezierPath.lineTo(segment[0], segment[1]);
				break;
			case PathIterator.SEG_QUADTO:
				bezierPath.quadTo(segment[0], segment[1], segment[2], segment[3]);
				break;
			case PathIterator.SEG_CUBICTO:
				bezierPath.curveTo(segment[0], segment[1], segment[2], segment[3], segment[4], segment[5]);
				break;
			case PathIterator.SEG_CLOSE:
				add(bezierPath, true);
				bezierPath = null;
				break;
			}
		}
		if (bezierPath != null) add(bezierPath, false);
	}

	public synchronized GeneralPath getGeneralPath() {
		if (path == null) {
			path = new GeneralPath(Path2D.WIND_EVEN_ODD);
			for (final BezierFigure figure : figures) {
				path.append(figure.getBezierPath(), false);
			}
		}
		return path;
	}

	/* -- helper methods -- */

	private boolean add(final BezierPath bezierPath, boolean isClosed) {
		bezierPath.setClosed(isClosed);
		BezierFigure figure = new BezierFigure(isClosed);
		figure.setBezierPath(bezierPath);
		return figures.add(figure);
	}

}
