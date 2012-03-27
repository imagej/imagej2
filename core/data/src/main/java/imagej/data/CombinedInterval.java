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

package imagej.data;

import java.util.ArrayList;
import java.util.HashMap;

import net.imglib2.Positionable;
import net.imglib2.RealPositionable;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;

/**
 * A combined interval is an aggregation of other {@link CalibratedInterval}s,
 * with common axes merged as appropriate. For example, combining three
 * intervals with dimensions (X, Y, Z, CHANNEL), (X, Y, CHANNEL, TIME) and (X,
 * Z, LIFETIME, TIME) will result in a coordinate space with dimensions (X, Y,
 * Z, CHANNEL, TIME, LIFETIME).
 * <p>
 * No reconciliation is done to ensure that overlapping axes have equal
 * dimensional lengths or calibrations, although discrete
 * {@link CalibratedInterval}s are given priority over those without a discrete
 * sampling (see {@link CalibratedInterval#isDiscrete()} for more information).
 * As long as all interval dimensions belong to least one discrete interval, the
 * combined interval will also be discrete, even if a subset of the constituent
 * intervals are not.
 * </p>
 * 
 * @author Curtis Rueden
 */
public class CombinedInterval extends ArrayList<CalibratedInterval> implements
	CalibratedInterval
{

	/**
	 * List of axis mappings from combined interval into constituent intervals.
	 * Each axis maps to a particular axis index of one of the constituent
	 * {@link CalibratedInterval} objects. This data structure answers the
	 * question: "For the (e.g.) 2nd axis of this combined interval, which axis of
	 * which constituent {@link CalibratedInterval} is used?"
	 */
	private final ArrayList<DimensionMapping> mappings =
		new ArrayList<DimensionMapping>();

	/**
	 * Mapping from axis type to axis index. This data structure answers the
	 * question: "Which axis of this combined interval is (e.g.)
	 * {@link Axes#CHANNEL}?"
	 */
	private final HashMap<AxisType, Integer> indices =
		new HashMap<AxisType, Integer>();

	/** Number of dimensions with a discrete sampling. */
	private boolean discrete;

	/** Name of the combined interval. */
	private String name;

	// -- CombinedInterval methods --

	public void update() {
		// CTR TODO - reconcile multiple copies of same axis with different lengths
		// or calibrations.
		mappings.clear();
		indices.clear();
		for (final CalibratedInterval interval : this) {
			for (int d = 0; d < interval.numDimensions(); d++) {
				final AxisType axis = interval.axis(d);
				if (!indices.containsKey(axis)) {
					// new axis; add to mappings
					final DimensionMapping mapping = new DimensionMapping();
					mapping.interval = interval;
					mapping.index = d;
					indices.put(axis, mappings.size());
					mappings.add(mapping);
				}
				final int index = indices.get(axis);
				final DimensionMapping mapping = mappings.get(index);
				if (interval.isDiscrete() && !mapping.interval.isDiscrete()) {
					// replace interval without sampling with an interval that has one
					mapping.interval = interval;
					mapping.index = d;
				}
			}
		}

		// count number of dimensions with a discrete sampling
		int discreteCount = 0;
		for (final DimensionMapping mapping : mappings) {
			if (mapping.interval.isDiscrete()) discreteCount++;
		}
		discrete = discreteCount == mappings.size();
	}

	// -- CalibratedInterval methods --

	@Override
	public AxisType[] getAxes() {
		final AxisType[] axes = new AxisType[numDimensions()];
		axes(axes);
		return axes;
	}

	@Override
	public boolean isDiscrete() {
		return discrete;
	}

	@Override
	public Extents getExtents() {
		if (!isDiscrete()) throw new UnsupportedOperationException();
		final long[] min = new long[numDimensions()];
		final long[] max = new long[numDimensions()];
		min(min);
		max(max);
		return new Extents(min, max);
	}

	@Override
	public long[] getDims() {
		if (!isDiscrete()) throw new UnsupportedOperationException();
		final long[] dims = new long[numDimensions()];
		dimensions(dims);
		return dims;
	}

	// -- CalibratedSpace methods --

	@Override
	public int getAxisIndex(final AxisType axis) {
		return indices.containsKey(axis) ? indices.get(axis) : -1;
	}

	@Override
	public AxisType axis(final int d) {
		final DimensionMapping mapping = mappings.get(d);
		return mapping.interval.axis(mapping.index);
	}

	@Override
	public void axes(final AxisType[] axes) {
		for (int i = 0; i < axes.length; i++)
			axes[i] = axis(i);
	}

	@Override
	public void setAxis(final AxisType axis, final int d) {
		throw new UnsupportedOperationException();
	}

	@Override
	public double calibration(final int d) {
		final DimensionMapping mapping = mappings.get(d);
		return mapping.interval.calibration(mapping.index);
	}

	@Override
	public void calibration(final double[] cal) {
		for (int i = 0; i < cal.length; i++)
			cal[i] = calibration(i);
	}

	@Override
	public void setCalibration(final double cal, final int d) {
		throw new UnsupportedOperationException();
	}

	// -- EuclideanSpace methods --

	@Override
	public int numDimensions() {
		return mappings.size();
	}

	// -- Interval methods --

	@Override
	public long min(final int d) {
		final DimensionMapping mapping = mappings.get(d);
		return mapping.interval.min(mapping.index);
	}

	@Override
	public void min(final long[] min) {
		for (int i = 0; i < min.length; i++)
			min[i] = min(i);
	}

	@Override
	public void min(final Positionable min) {
		for (int i = 0; i < min.numDimensions(); i++)
			min.setPosition(min(i), i);
	}

	@Override
	public long max(final int d) {
		final DimensionMapping mapping = mappings.get(d);
		return mapping.interval.max(mapping.index);
	}

	@Override
	public void max(final long[] max) {
		for (int i = 0; i < max.length; i++)
			max[i] = max(i);
	}

	@Override
	public void max(final Positionable max) {
		for (int i = 0; i < max.numDimensions(); i++)
			max.setPosition(max(i), i);
	}

	@Override
	public void dimensions(final long[] dimensions) {
		for (int i = 0; i < dimensions.length; i++)
			dimensions[i] = dimension(i);
	}

	@Override
	public long dimension(final int d) {
		final DimensionMapping mapping = mappings.get(d);
		return mapping.interval.dimension(mapping.index);
	}

	// -- RealInterval methods --

	@Override
	public double realMin(final int d) {
		final DimensionMapping mapping = mappings.get(d);
		return mapping.interval.realMin(mapping.index);
	}

	@Override
	public void realMin(final double[] min) {
		for (int i = 0; i < min.length; i++)
			min[i] = realMin(i);
	}

	@Override
	public void realMin(final RealPositionable min) {
		for (int i = 0; i < min.numDimensions(); i++)
			min.setPosition(realMin(i), i);
	}

	@Override
	public double realMax(final int d) {
		final DimensionMapping mapping = mappings.get(d);
		return mapping.interval.realMax(mapping.index);
	}

	@Override
	public void realMax(final double[] max) {
		for (int i = 0; i < max.length; i++)
			max[i] = realMax(i);
	}

	@Override
	public void realMax(final RealPositionable max) {
		for (int i = 0; i < max.numDimensions(); i++)
			max.setPosition(realMax(i), i);
	}

	// -- Named methods --

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	// -- Helper classes --

	protected class DimensionMapping {

		public CalibratedInterval interval;
		public int index;
	}

}
