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

package imagej.data.overlay;

import imagej.data.Dataset;
import imagej.data.event.DatasetRestructuredEvent;
import imagej.display.Displayable;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.CalibratedAxis;
import net.imglib2.meta.ImgPlus;
import net.imglib2.ops.condition.Condition;
import net.imglib2.ops.condition.FunctionGreaterCondition;
import net.imglib2.ops.condition.FunctionLessCondition;
import net.imglib2.ops.condition.OrCondition;
import net.imglib2.ops.condition.WithinRangeCondition;
import net.imglib2.ops.function.Function;
import net.imglib2.ops.function.real.RealImageFunction;
import net.imglib2.ops.pointset.ConditionalPointSet;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.ops.pointset.PointSetRegionOfInterest;
import net.imglib2.roi.RegionOfInterest;
import net.imglib2.type.numeric.RealType;

import org.scijava.Context;
import org.scijava.event.EventHandler;
import org.scijava.util.ColorRGB;
import org.scijava.util.Colors;

/**
 * A {@link ThresholdOverlay} is an {@link Overlay} that represents the set of
 * points whose data values are in a range prescribed by API user.
 * 
 * @author Barry DeZonia
 */
public class ThresholdOverlay extends AbstractOverlay
{

	// -- instance variables --

	private final Dataset dataset;
	private Function<long[], RealType<?>> function;
	private RealType<?> variable;
	private Displayable figure;
	private ConditionalPointSet pointsLess;
	private ConditionalPointSet pointsGreater;
	private ConditionalPointSet pointsWithin;
	private ConditionalPointSet pointsOutside;
	private FunctionLessCondition<? extends RealType<?>> conditionLess;
	private FunctionGreaterCondition<? extends RealType<?>> conditionGreater;
	private WithinRangeCondition<? extends RealType<?>> conditionWithin;
	private OrCondition<long[]> conditionOutside;
	private RegionOfInterest regionAdapter;
	private ColorRGB colorLess;
	private ColorRGB colorWithin;
	private ColorRGB colorGreater;
	private String defaultName;

	// -- ThresholdOverlay methods --

	/**
	 * Construct a {@link ThresholdOverlay} on a {@link Dataset} given an
	 * {@link Context} context.
	 */
	public ThresholdOverlay(Context context, Dataset dataset)
	{
		// TODO FIXME
		// This blows up
		// super(context, dataset);
		// and this does not blow up
		super(dataset);
		setContext(context);

		this.dataset = dataset;
		this.figure = null;
		init(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		initAttributes();
		resetThreshold();
		setDefaultName(false);
	}
	
	/**
	 * Construct a {@link ThresholdOverlay} on a {@link Dataset} given an
	 * {@link Context} context, and a numeric range within which the data values of
	 * interest exist.
	 */
	public ThresholdOverlay(Context context, Dataset ds, double min, double max)
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
		boolean changed =
			(min != conditionWithin.getMin() || max != conditionWithin.getMax());
		conditionWithin.setMin(min);
		conditionWithin.setMax(max);
		conditionLess.setValue(min);
		conditionGreater.setValue(max);
		// make sure all pointsets know they've changed
		pointsGreater.setCondition(conditionGreater);
		pointsLess.setCondition(conditionLess);
		pointsWithin.setCondition(conditionWithin);
		pointsOutside.setCondition(conditionOutside);
		setDefaultName(changed);
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
		if (min < -20000) min = -20000;
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
	 * Returns the set of points whose data values are outside the range of
	 * interest.
	 */
	public PointSet getPointsOutside() {
		return pointsOutside;
	}

	/**
	 * Returns the {@link ThresholdOverlay}'s {@link Condition} used to determine
	 * which points are within than the threshold.
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
	 * Returns the {@link ThresholdOverlay}'s {@link Condition} used to determine
	 * which points are less than the threshold.
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
	 * Returns the {@link ThresholdOverlay}'s {@link Condition} used to determine
	 * which points are greater than the threshold.
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
	 * Returns the {@link ThresholdOverlay}'s {@link Condition} used to determine
	 * which points are outside than the threshold.
	 * <p>
	 * By design the return value is not a specialized version of a Condition.
	 * Users must not poke the threshold values via this Condition. This would
	 * bypass internal communication. API users should call setRange(min, max) on
	 * this {@link ThresholdOverlay} if they want to manipulate the display range.
	 */
	public Condition<long[]> getConditionOutside() {
		return conditionOutside;
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
		return colorWithin;
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
		colorWithin = c;
	}

	/**
	 * Determines the relationship between the value of the underlying data and
	 * the threshold range. The data is evaluated at the given point. Data points
	 * whose value is less than the threshold range are classified as -1. Data
	 * points whose value is within the threshold range are classified as 0. And
	 * data points whose value is greater than the threshold range are classified
	 * as 1. NaN data values are classified as Integer.MAX_VALUE.
	 * <p>
	 * This method is used by the renderers to quickly determine a point's status
	 * and can be used generally by interested parties.
	 * 
	 * @param point The coordinate point at which to test the underlying data
	 * @return -1, 0, or 1
	 */
	public int classify(long[] point) {
		function.compute(point, variable);
		double val = variable.getRealDouble();
		if (Double.isNaN(val)) return Integer.MAX_VALUE;
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
	public int dimensionIndex(AxisType axis) {
		return dataset.dimensionIndex(axis);
	}

	@Override
	public CalibratedAxis axis(int d) {
		return dataset.axis(d);
	}

	@Override
	public void axes(CalibratedAxis[] axes) {
		dataset.axes(axes);
	}

	@Override
	public void setAxis(CalibratedAxis axis, int d) {
		dataset.setAxis(axis, d);
	}

	@Override
	public int numDimensions() {
		return pointsWithin.numDimensions();
	}

	@Override
	public double realMin(int d) {
		return pointsWithin.min(d);
	}

	@Override
	public double realMax(int d) {
		return pointsWithin.max(d);
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

	@Override
	public String getName() {
		String name = super.getName();
		if (name != null) return name;
		return defaultName;
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(DatasetRestructuredEvent evt) {
		if (evt.getObject() == dataset) {
			reinit();
			rebuild();
		}
	}

	// -- helpers --

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void init(double min, double max) {
		ImgPlus<? extends RealType<?>> imgPlus = dataset.getImgPlus();
		function = new RealImageFunction(imgPlus, imgPlus.firstElement());
		variable = function.createOutput();
		conditionWithin = new WithinRangeCondition(function, min, max);
		conditionLess = new FunctionLessCondition(function, min);
		conditionGreater = new FunctionGreaterCondition(function, max);
		conditionOutside = new OrCondition<long[]>(conditionLess, conditionGreater);
		long[] dims = new long[imgPlus.numDimensions()];
		imgPlus.dimensions(dims);
		HyperVolumePointSet volume = new HyperVolumePointSet(dims);
		pointsLess = new ConditionalPointSet(volume, conditionLess);
		pointsGreater = new ConditionalPointSet(volume, conditionGreater);
		pointsWithin = new ConditionalPointSet(volume, conditionWithin);
		pointsOutside = new ConditionalPointSet(volume, conditionOutside);
		regionAdapter = new PointSetRegionOfInterest(pointsWithin);
		setDefaultName(false);
	}

	// Updates the guts of the various members of this class to reflect a changed
	// Dataset. It makes sure that anyone using references from before should be
	// fine (as long as not concurrently accessing while changes made).

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void reinit() {
		ImgPlus<? extends RealType<?>> imgPlus = dataset.getImgPlus();
		// FIXME: This generic typing is broken. Stop using raw types.
		function = new RealImageFunction(imgPlus, imgPlus.firstElement());
		// NB: Separate variable is necessary to avoid javac compile errors.
		final Function f = function;
		conditionWithin.setFunction(f);
		conditionLess.setFunction(f);
		conditionGreater.setFunction(f);
		// no change needed for conditionOutside
		long[] dims = new long[imgPlus.numDimensions()];
		imgPlus.dimensions(dims);
		HyperVolumePointSet volume = new HyperVolumePointSet(dims);
		pointsWithin.setPointSet(volume);
		pointsLess.setPointSet(volume);
		pointsGreater.setPointSet(volume);
		// let ConditionalPointSets know they need bounds recalc via setCondition()
		pointsWithin.setCondition(conditionWithin);
		pointsLess.setCondition(conditionLess);
		pointsGreater.setCondition(conditionGreater);
		pointsOutside.setCondition(conditionOutside);
		// regionAdapter does not need any changes
		setDefaultName(false);
	}

	private void initAttributes() {
		setAlpha(255);
		setFillColor(Colors.RED);
		setLineColor(Colors.RED);
		setLineEndArrowStyle(ArrowStyle.NONE);
		setLineStartArrowStyle(ArrowStyle.NONE);
		setLineStyle(LineStyle.NONE);
		setLineWidth(1);
		setColorWithin(Colors.RED);
		setColorLess(null);
		setColorGreater(null);
	}

	private void setDefaultName(boolean emitEvent) {
		defaultName =
			"Threshold: " + conditionWithin.getMin() + " to " +
				conditionWithin.getMax();
		if (emitEvent) update();
	}

}
