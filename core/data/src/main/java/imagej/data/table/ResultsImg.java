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

package imagej.data.table;

import java.util.Iterator;

import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.IterableRealInterval;
import net.imglib2.Positionable;
import net.imglib2.RandomAccess;
import net.imglib2.RealPositionable;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * Expresses a {@link ResultsTable} as an {@link Img}.
 * 
 * @author Curtis Rueden
 */
public class ResultsImg implements Img<DoubleType> {

	private final ResultsTable table;

	public ResultsImg(final ResultsTable table) {
		this.table = table;
	}

	// -- RandomAccessible methods --

	@Override
	public RandomAccess<DoubleType> randomAccess() {
		// TODO
		throw new UnsupportedOperationException("Unimplemented");
	}

	@Override
	public RandomAccess<DoubleType> randomAccess(final Interval interval) {
		// TODO
		throw new UnsupportedOperationException("Unimplemented");
	}

	// -- EuclideanSpace methods --

	@Override
	public int numDimensions() {
		return 2;
	}

	// -- Interval methods --

	@Override
	public long min(final int d) {
		if (d >= 0 && d <= 1) return 0;
		throw new IllegalArgumentException("Invalid dimension: " + d);
	}

	@Override
	public void min(final long[] min) {
		min[0] = min[1] = 0;
	}

	@Override
	public void min(final Positionable min) {
		min.setPosition(0, 0);
		min.setPosition(0, 1);
	}

	@Override
	public long max(final int d) {
		if (d == 0) return max0();
		if (d == 1) return max1();
		throw new IllegalArgumentException("Invalid dimension: " + d);
	}

	@Override
	public void max(final long[] max) {
		max[0] = max0();
		max[1] = max1();
	}

	@Override
	public void max(final Positionable max) {
		max.setPosition(0, max0());
		max.setPosition(0, max1());
	}

	// -- RealInterval methods --

	@Override
	public double realMin(final int d) {
		return min(d);
	}

	@Override
	public void realMin(final double[] min) {
		min[0] = min[1] = 0;
	}

	@Override
	public void realMin(final RealPositionable min) {
		min(min);
	}

	@Override
	public double realMax(final int d) {
		return max(d);
	}

	@Override
	public void realMax(final double[] max) {
		max[0] = table.getColumnCount();
		max[1] = table.getRowCount();
	}

	@Override
	public void realMax(final RealPositionable max) {
		max(max);
	}

	// -- Dimensions methods --

	@Override
	public void dimensions(final long[] dimensions) {
		dimensions[0] = dim0();
		dimensions[1] = dim1();
	}

	@Override
	public long dimension(final int d) {
		if (d == 0) return dim0();
		if (d == 1) return dim1();
		throw new IllegalArgumentException("Invalid dimension: " + d);
	}

	// -- IterableRealInterval methods --

	@Override
	public Cursor<DoubleType> cursor() {
		// TODO
		throw new UnsupportedOperationException("Unimplemented");
	}

	@Override
	public Cursor<DoubleType> localizingCursor() {
		// TODO
		throw new UnsupportedOperationException("Unimplemented");
	}

	@Override
	public long size() {
		return (long) dim0() * dim1();
	}

	@Override
	public DoubleType firstElement() {
		return new DoubleType(table.get(0, 0));
	}

	@Override
	public Object iterationOrder() {
		// TODO
		throw new UnsupportedOperationException("Unimplemented");
	}

	@Override
	@Deprecated
	public boolean equalIterationOrder(final IterableRealInterval<?> f) {
		// TODO
		throw new UnsupportedOperationException("Unimplemented");
	}

	// -- Iterable methods --

	@Override
	public Iterator<DoubleType> iterator() {
		// TODO
		throw new UnsupportedOperationException("Unimplemented");
	}

	// -- Img methods --

	@Override
	public ImgFactory<DoubleType> factory() {
		// TODO
		throw new UnsupportedOperationException("Unimplemented");
	}

	@Override
	public Img<DoubleType> copy() {
		// TODO
		throw new UnsupportedOperationException("Unimplemented");
	}

	// -- Helper methods --

	private int dim0() {
		return table.getColumnCount();
	}

	private int dim1() {
		return table.getRowCount();
	}

	private int max0() {
		return dim0() - 1;
	}

	private int max1() {
		return dim1() - 1;
	}

}
