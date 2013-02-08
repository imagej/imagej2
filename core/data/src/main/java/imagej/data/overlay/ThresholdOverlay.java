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

package imagej.data.overlay;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.display.Displayable;
import imagej.util.ColorRGB;
import imagej.util.Colors;
import net.imglib2.img.ImgPlus;
import net.imglib2.meta.AxisType;
import net.imglib2.ops.condition.Condition;
import net.imglib2.ops.condition.WithinRangeCondition;
import net.imglib2.ops.function.Function;
import net.imglib2.ops.function.real.RealImageFunction;
import net.imglib2.ops.pointset.ConditionalPointSet;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.ops.pointset.PointSetRegionOfInterest;
import net.imglib2.roi.RegionOfInterest;
import net.imglib2.type.numeric.RealType;

/**
 * A {@link ThresholdOverlay} is an {@link Overlay} that represents the set of
 * points whose data values are in a range prescribed by API user.
 * 
 * @author Barry DeZonia
 */
public class ThresholdOverlay extends AbstractOverlay {

	// -- instance variables --

	private Function<long[], RealType<?>> function;
	private RealType<?> variable;
	private Displayable figure;
	private final Dataset dataset;
	private final ConditionalPointSet pointsLess;
	private final ConditionalPointSet pointsGreater;
	private final ConditionalPointSet pointsWithin;
	private final FunctionLessCondition<? extends RealType<?>> conditionLess;
	private final FunctionGreaterCondition<? extends RealType<?>> conditionGreater;
	private final WithinRangeCondition<? extends RealType<?>> conditionWithin;
	private final RegionOfInterest regionAdapter;
	private ColorRGB colorLess;
	private ColorRGB colorGreater;

	// -- ThresholdOverlay methods --

	/**
	 * Construct a {@link ThresholdOverlay} on a {@link Dataset} given an
	 * {@link ImageJ} context.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ThresholdOverlay(ImageJ context, Dataset dataset)
	{
		setContext(context);
		this.dataset = dataset;
		ImgPlus<? extends RealType<?>> imgPlus = dataset.getImgPlus();
		function = new RealImageFunction(imgPlus, imgPlus.firstElement());
		variable = function.createOutput();
		conditionWithin =
			new WithinRangeCondition(function, Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY);
		conditionLess =
			new FunctionLessCondition(function, Double.POSITIVE_INFINITY);
		conditionGreater =
			new FunctionGreaterCondition(function, Double.NEGATIVE_INFINITY);
		long[] dims = new long[imgPlus.numDimensions()];
		imgPlus.dimensions(dims);
		HyperVolumePointSet volume = new HyperVolumePointSet(dims);
		pointsLess = new ConditionalPointSet(volume, conditionLess);
		pointsGreater = new ConditionalPointSet(volume, conditionGreater);
		pointsWithin = new ConditionalPointSet(volume, conditionWithin);
		regionAdapter = new PointSetRegionOfInterest(pointsWithin);
		figure = null;
		updateName();
		setAlpha(255);
		setFillColor(Colors.RED);
		setLineColor(Colors.RED);
		setLineEndArrowStyle(ArrowStyle.NONE);
		setLineStartArrowStyle(ArrowStyle.NONE);
		setLineStyle(LineStyle.NONE);
		setLineWidth(1);
		initColors();
		resetThreshold();
	}
	
	/**
	 * Construct a {@link ThresholdOverlay} on a {@link Dataset} given an
	 * {@link ImageJ} context, and a numeric range within which the data values of
	 * interest exist.
	 */
	public ThresholdOverlay(ImageJ context, Dataset ds, double min, double max)
	{
		this(context, ds);
		setRange(min,max);
	}

	/**
	 * Helper method used by services to tie this overlay to its graphic
	 * representation. Sometimes this overlay needs to redraw its graphics in
	 * response to changes in the threshold values. Various services may use this
	 * method but this method is not for general consumption.
	 */
	public void setFigure(Displayable figure) {
		this.figure = figure;
	}

	/**
	 * Returns the {@link Displayable} figure associated with this overlay.
	 */
	public Displayable getFigure() {
		return figure;
	}

	/**
	 * Sets the range of interest for this overlay. As a side effect the name of
	 * the overlay is updated.
	 */
	public void setRange(double min, double max) {
		conditionWithin.setMin(min);
		conditionWithin.setMax(max);
		conditionLess.setValue(min);
		conditionGreater.setValue(max);
		// make sure all pointsets know they've changed
		pointsGreater.setCondition(conditionGreater);
		pointsLess.setCondition(conditionLess);
		pointsWithin.setCondition(conditionWithin);
		updateName();
	}

	/**
	 * Gets the lower end of the range of interest for this overlay.
	 */
	public double getRangeMin() {
		return conditionWithin.getMin();
	}

	/**
	 * Gets the upper end of the range of interest for this overlay.
	 */
	public double getRangeMax() {
		return conditionWithin.getMax();
	}

	/**
	 * Resets the range of interest of this overlay to default values.
	 */
	public void resetThreshold() {
		// TODO - this is hacky. Maybe we need actual data values but scanning is
		// slow. Or maybe we delete threshold? No, we use it in constructor.
		RealType<?> type = dataset.getType();
		double min = type.getMinValue();
		double max = type.getMaxValue();
		if (min < 20000) min = 20000;
		if (max > 20000) max = 20000;
		setRange(min, max / 2);
	}

	/**
	 * Returns the set of points whose data values are within the range of
	 * interest.
	 */
	public PointSet getPointsWithin() {
		return pointsWithin;
	}

	/**
	 * Returns the set of points whose data values are less than the range of
	 * interest.
	 */
	public PointSet getPointsLess() {
		return pointsLess;
	}

	/**
	 * Returns the set of points whose data values are greater than the range of
	 * interest.
	 */
	public PointSet getPointsGreater() {
		return pointsGreater;
	}
	
	/**
	 * Returns the {@link ThresholdOverlay}'s {@link Condition} for including
	 * points that are within the threshold. This is used by others (like the
	 * rendering code) to quickly iterate the portion of the data points they are
	 * interested in.
	 * <p>
	 * By design the return value is not a specialized version of a Condition.
	 * Users must not poke the threshold values via this Condition. This would
	 * bypass internal communication. API users should call setRange(min, max) on
	 * this {@link ThresholdOverlay} if they want to manipulate the display range.
	 */
	public Condition<long[]> getConditionWithin() {
		return conditionWithin;
	}

	/**
	 * Returns the {@link ThresholdOverlay}'s {@link Condition} for including
	 * points that are less than the threshold. This is used by others (like the
	 * rendering code) to quickly iterate the portion of the data points they are
	 * interested in.
	 * <p>
	 * By design the return value is not a specialized version of a Condition.
	 * Users must not poke the threshold values via this Condition. This would
	 * bypass internal communication. API users should call setRange(min, max) on
	 * this {@link ThresholdOverlay} if they want to manipulate the display range.
	 */
	public Condition<long[]> getConditionLess() {
		return conditionLess;
	}

	/**
	 * Returns the {@link ThresholdOverlay}'s {@link Condition} for including
	 * points that are greater than the threshold. This is used by others (like
	 * the rendering code) to quickly iterate the portion of the data points they
	 * are interested in.
	 * <p>
	 * By design the return value is not a specialized version of a Condition.
	 * Users must not poke the threshold values via this Condition. This would
	 * bypass internal communication. API users should call setRange(min, max) on
	 * this {@link ThresholdOverlay} if they want to manipulate the display range.
	 */
	public Condition<long[]> getConditionGreater() {
		return conditionGreater;
	}

	/**
	 * Gets the color used when rendering points less than the threshold range.
	 */
	public ColorRGB getColorLess() {
		return colorLess;
	}

	/**
	 * Gets the color used when rendering points greater than the threshold range.
	 */
	public ColorRGB getColorGreater() {
		return colorGreater;
	}

	/**
	 * Gets the color used when rendering points within the threshold range.
	 */
	public ColorRGB getColorWithin() {
		return getFillColor();
	}

	/**
	 * Sets the color used when rendering points less than the threshold range.
	 */
	public void setColorLess(ColorRGB c) {
		colorLess = c;
	}

	/**
	 * Sets the color used when rendering points greater than the threshold range.
	 */
	public void setColorGreater(ColorRGB c) {
		colorGreater = c;
	}

	/**
	 * Sets the color used when rendering points within the threshold range.
	 */
	public void setColorWithin(ColorRGB c) {
		setFillColor(c);
	}

	/**
	 * Determines the relationship between the value of the underlying data and
	 * the threshold range. The data is evaluated at the given point. Data points
	 * whose value is less than the threshold range are classified as -1. Data
	 * points whose value is within the threshold range are classified as 0. And
	 * data points whose value is greater than the threshold range are classified
	 * as 1.
	 * 
	 * @param point The coordinate point at which to test the underlying data
	 * @return -1, 0, or 1
	 */
	public int classify(long[] point) {
		function.compute(point, variable);
		double val = variable.getRealDouble();
		if (val < conditionWithin.getMin()) return -1;
		if (val > conditionWithin.getMax()) return 1;
		return 0;
	}

	// -- Overlay methods --

	@Override
	public void update() {
		if (figure != null) figure.draw();
	}

	@Override
	public void rebuild() {
		update(); // TODO - is this all we need to do? I think so.
	}

	@Override
	public boolean isDiscrete() {
		return true;
	}

	@Override
	public int getAxisIndex(AxisType axis) {
		return dataset.getAxisIndex(axis);
	}

	@Override
	public AxisType axis(int d) {
		return dataset.axis(d);
	}

	@Override
	public void axes(AxisType[] axes) {
		dataset.axes(axes);
	}

	@Override
	public void setAxis(AxisType axis, int d) {
		dataset.setAxis(axis, d);
	}

	// TODO these two calibration methods are inconsistent. Decide what is best.

	@Override
	public double calibration(int d) {
		return dataset.calibration(d);
	}

	@Override
	public void setCalibration(double cal, int d) {
		if (cal == 1 && (d == 0 || d == 1)) return;
		throw new IllegalArgumentException(
			"Cannot set calibration of a ThresholdOverlay");
	}

	@Override
	public int numDimensions() {
		return pointsWithin.numDimensions();
	}

	@Override
	public long min(int d) {
		return pointsWithin.min(d);
	}

	@Override
	public long max(int d) {
		return pointsWithin.max(d);
	}

	@Override
	public double realMin(int d) {
		return min(d);
	}

	@Override
	public double realMax(int d) {
		return max(d);
	}

	@Override
	public void dimensions(long[] dimensions) {
		pointsWithin.dimensions(dimensions);
	}

	@Override
	public long dimension(int d) {
		return pointsWithin.dimension(d);
	}

	@Override
	public RegionOfInterest getRegionOfInterest() {
		return regionAdapter;
	}

	@Override
	public ThresholdOverlay duplicate() {
		double min = getRangeMin();
		double max = getRangeMax();
		ThresholdOverlay overlay =
			new ThresholdOverlay(getContext(), dataset, min, max);
		overlay.setColorWithin(getColorWithin());
		overlay.setColorLess(getColorLess());
		overlay.setColorGreater(getColorGreater());
		return overlay;
	}

	@Override
	public void move(double[] deltas) {
		// do nothing - thresholds don't move though space
	}

	// -- helpers --

	private void updateName() {
		setName("Threshold: " + conditionWithin.getMin() + " to " +
			conditionWithin.getMax());
	}

	private void initColors() {
		setColorWithin(Colors.RED);
		setColorLess(null);
		setColorGreater(null);
	}

	private abstract class FunctionCondition<T extends RealType<T>> implements
		Condition<long[]>
	{

		protected final Function<long[], T> func;
		protected double value;
		private final T var;

		abstract boolean relationTrue(double fVal);

		public FunctionCondition(Function<long[], T> func, double value) {
			this.func = func;
			this.value = value;
			var = func.createOutput();
		}

		@Override
		public boolean isTrue(long[] val) {
			func.compute(val, var);
			return relationTrue(var.getRealDouble());
		}

		public double getValue() {
			return value;
		}

		public void setValue(double value) {
			this.value = value;
		}

	}

	private class FunctionLessCondition<T extends RealType<T>> extends
		FunctionCondition<T>
	{

		public FunctionLessCondition(Function<long[], T> func, double value) {
			super(func, value);
		}

		@Override
		public FunctionLessCondition<T> copy() {
			return new FunctionLessCondition<T>(func.copy(), value);
		}

		@Override
		boolean relationTrue(double fVal) {
			return fVal < value;
		}
	}

	private class FunctionLessEqualCondition<T extends RealType<T>> extends
		FunctionCondition<T>
	{

		public FunctionLessEqualCondition(Function<long[], T> func, double value)
		{
			super(func, value);
		}

		@Override
		public FunctionLessEqualCondition<T> copy() {
			return new FunctionLessEqualCondition<T>(func.copy(), value);
		}

		@Override
		boolean relationTrue(double fVal) {
			return fVal <= value;
		}
	}

	private class FunctionGreaterCondition<T extends RealType<T>> extends
		FunctionCondition<T>
	{

		public FunctionGreaterCondition(Function<long[], T> func, double value) {
			super(func, value);
		}

		@Override
		public FunctionGreaterCondition<T> copy() {
			return new FunctionGreaterCondition<T>(func.copy(), value);
		}

		@Override
		boolean relationTrue(double fVal) {
			return fVal > value;
		}
	}

	private class FunctionGreaterEqualCondition<T extends RealType<T>> extends
		FunctionCondition<T>
	{

		public FunctionGreaterEqualCondition(Function<long[], T> func, double value)
		{
			super(func, value);
		}

		@Override
		public FunctionGreaterEqualCondition<T> copy() {
			return new FunctionGreaterEqualCondition<T>(func.copy(), value);
		}

		@Override
		boolean relationTrue(double fVal) {
			return fVal >= value;
		}
	}

	private class FunctionEqualCondition<T extends RealType<T>> extends
		FunctionCondition<T>
	{

		public FunctionEqualCondition(Function<long[], T> func, double value) {
			super(func, value);
		}

		@Override
		public FunctionEqualCondition<T> copy() {
			return new FunctionEqualCondition<T>(func.copy(), value);
		}

		@Override
		boolean relationTrue(double fVal) {
			return fVal == value;
		}
	}

	private class FunctionNotEqualCondition<T extends RealType<T>> extends
		FunctionCondition<T>
	{

		public FunctionNotEqualCondition(Function<long[], T> func, double value) {
			super(func, value);
		}

		@Override
		public FunctionNotEqualCondition<T> copy() {
			return new FunctionNotEqualCondition<T>(func.copy(), value);
		}

		@Override
		boolean relationTrue(double fVal) {
			return fVal != value;
		}
	}
}
