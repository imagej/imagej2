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
import imagej.data.event.DatasetRestructuredEvent;
import imagej.display.Displayable;
import imagej.event.EventHandler;
import imagej.util.ColorRGB;
import imagej.util.Colors;
import net.imglib2.img.ImgPlus;
import net.imglib2.meta.AxisType;
import net.imglib2.ops.condition.Condition;
import net.imglib2.ops.condition.FunctionGreaterCondition;
import net.imglib2.ops.condition.FunctionLessCondition;
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
	private FunctionLessCondition<? extends RealType<?>> conditionLess;
	private FunctionGreaterCondition<? extends RealType<?>> conditionGreater;
	private WithinRangeCondition<? extends RealType<?>> conditionWithin;
	private RegionOfInterest regionAdapter;
	private ColorRGB colorLess;
	private ColorRGB colorGreater;
	private String defaultName;

	// -- ThresholdOverlay methods --

	/**
	 * Construct a {@link ThresholdOverlay} on a {@link Dataset} given an
	 * {@link ImageJ} context.
	 */
	public ThresholdOverlay(ImageJ context, Dataset dataset)
	{
		setContext(context);
		this.dataset = dataset;
		this.figure = null;
		init(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		initAttributes();
		resetThreshold();
		setDefaultName();
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
		setDefaultName();
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

	@Override
	public String getName() {
		String name = super.getName();
		if (name != null) return name;
		return defaultName;
	}

	// -- Event handlers --

	@EventHandler
	public void onEvent(DatasetRestructuredEvent evt) {
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
		long[] dims = new long[imgPlus.numDimensions()];
		imgPlus.dimensions(dims);
		HyperVolumePointSet volume = new HyperVolumePointSet(dims);
		pointsLess = new ConditionalPointSet(volume, conditionLess);
		pointsGreater = new ConditionalPointSet(volume, conditionGreater);
		pointsWithin = new ConditionalPointSet(volume, conditionWithin);
		regionAdapter = new PointSetRegionOfInterest(pointsWithin);
		setDefaultName();
	}

	// Updates the guts of the various members of this class to reflect a changed
	// Dataset. It makes sure that anyone using references from before should be
	// fine (as long as not concurrently accessing while changes made).

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void reinit() {
		ImgPlus<? extends RealType<?>> imgPlus = dataset.getImgPlus();
		function = new RealImageFunction(imgPlus, imgPlus.firstElement());
		conditionWithin.setFunction( (Function) function);
		conditionLess.setFunction( (Function) function);
		conditionGreater.setFunction( (Function) function);
		long[] dims = new long[imgPlus.numDimensions()];
		imgPlus.dimensions(dims);
		HyperVolumePointSet volume = new HyperVolumePointSet(dims);
		pointsWithin.setPointSet(volume);
		pointsLess.setPointSet(volume);
		pointsGreater.setPointSet(volume);
		pointsWithin.setCondition(conditionWithin);
		pointsLess.setCondition(conditionLess);
		pointsGreater.setCondition(conditionGreater);
		// regionAdapter does not need any changes
		setDefaultName();
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

	private void setDefaultName() {
		defaultName =
			"Threshold: " + conditionWithin.getMin() + " to " +
				conditionWithin.getMax();
	}

}
