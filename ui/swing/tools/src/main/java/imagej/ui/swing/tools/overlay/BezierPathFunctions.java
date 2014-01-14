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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;

import javax.swing.JFrame;
import javax.swing.JPanel;

import net.imglib2.roi.GeneralPathRegionOfInterest;

import org.jhotdraw.geom.BezierPath;

/**
 * Utility functions for {@link GeneralPathJHotDrawAdapter}.
 * 
 * @author Johannes Schindelin
 */
public class BezierPathFunctions {

	public static BezierPath toBezierPath(final PathIterator iterator) {
		final BezierPath path = new BezierPath();
		final double[] segment = new double[6];
		for (; !iterator.isDone(); iterator.next()) {
			final int type = iterator.currentSegment(segment);
			switch (type) {
				case PathIterator.SEG_MOVETO:
					path.moveTo(segment[0], segment[1]);
					break;
				case PathIterator.SEG_LINETO:
					path.lineTo(segment[0], segment[1]);
					break;
				case PathIterator.SEG_QUADTO:
					path.quadTo(segment[0], segment[1], segment[2], segment[3]);
					break;
				case PathIterator.SEG_CUBICTO:
					path.curveTo(segment[0], segment[1], segment[2], segment[3],
						segment[4], segment[5]);
					break;
				case PathIterator.SEG_CLOSE:
					path.setClosed(true);
					break;
			}
		}
		return path;
	}

	public static void addToRegionOfInterest(final BezierPath path,
		final GeneralPathRegionOfInterest roi)
	{
		addToRegionOfInterest(path.toGeneralPath().getPathIterator(null), roi);
	}

	public static void addToRegionOfInterest(final PathIterator iterator,
		final GeneralPathRegionOfInterest roi)
	{
		final double[] segment = new double[6];
		for (; !iterator.isDone(); iterator.next()) {
			final int type = iterator.currentSegment(segment);
			switch (type) {
				case PathIterator.SEG_MOVETO:
					roi.moveTo(segment[0], segment[1]);
					break;
				case PathIterator.SEG_LINETO:
					roi.lineTo(segment[0], segment[1]);
					break;
				case PathIterator.SEG_QUADTO:
					roi.quadTo(segment[0], segment[1], segment[2], segment[3]);
					break;
				case PathIterator.SEG_CUBICTO:
					roi.cubicTo(segment[0], segment[1], segment[2], segment[3],
						segment[4], segment[5]);
					break;
				case PathIterator.SEG_CLOSE:
					roi.close();
					break;
				default:
					throw new UnsupportedOperationException();
			}
		}
	}

	public enum OP {
		ADD, XOR, INTERSECT, SUBTRACT
	}

	public static BezierPath add(final BezierPath path1, final BezierPath path2)
	{
		return op(path1, path2, OP.ADD);
	}

	public static BezierPath exclusiveOr(final BezierPath path1,
		final BezierPath path2)
	{
		return op(path1, path2, OP.XOR);
	}

	public static BezierPath intersect(final BezierPath path1,
		final BezierPath path2)
	{
		return op(path1, path2, OP.INTERSECT);
	}

	public static BezierPath subtract(final BezierPath path1,
		final BezierPath path2)
	{
		return op(path1, path2, OP.SUBTRACT);
	}

	public static BezierPath op(final BezierPath path1, final BezierPath path2,
		final OP op)
	{
		final Area area1 = new Area(path1.toGeneralPath());
		final Area area2 = new Area(path2.toGeneralPath());
		switch (op) {
			case ADD:
				area1.add(area2);
				break;
			case XOR:
				area1.exclusiveOr(area2);
				break;
			case INTERSECT:
				area1.intersect(area2);
				break;
			case SUBTRACT:
				area1.subtract(area2);
				break;
		}
		return toBezierPath(area1.getPathIterator(new AffineTransform()));
	}

	@SuppressWarnings("unused")
	private static void show(final PathIterator iterator) {
		show(toBezierPath(iterator).toGeneralPath());
	}

	@SuppressWarnings("serial")
	private static void show(final Shape shape) {
		final JFrame frame = new JFrame("debug");
		final JPanel panel = new JPanel() {

			@Override
			public void paint(final Graphics graphics) {
				final Graphics2D graphics2D = (Graphics2D) graphics;
				graphics2D.draw(shape);
			}
		};
		frame.getContentPane().add(panel);
		final Rectangle bounds = shape.getBounds();
		frame.setSize(bounds.x + bounds.width, bounds.y + bounds.height);
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(final String[] args) {
		BezierPath path1 = new BezierPath();
		path1.moveTo(0, 0);
		path1.lineTo(100, 0);
		path1.lineTo(100, 100);
		path1.lineTo(0, 0);

		final GeneralPath path2 = new GeneralPath();
		path2.moveTo(0, 0);
		path2.lineTo(100, 0);
		path2.lineTo(100, 100);
		path2.closePath();

		// path1 = toBezierPath(path2.getPathIterator(new AffineTransform()));

		final BezierPath path3 = new BezierPath();
		path3.moveTo(0, 100);
		path3.lineTo(100, 0);
		path3.lineTo(100, 100);
		path3.lineTo(0, 100);

		path1 = subtract(path1, path3);

		show(path1.toGeneralPath());
	}

}
