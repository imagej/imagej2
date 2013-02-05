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

package imagej.data.overlay;

import imagej.ImageJ;
import imagej.display.Displayable;
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
 * 
 * @author Barry DeZonia
 *
 */
public class ThresholdOverlay extends AbstractOverlay {

	private Displayable figure;
	private final ImgPlus<? extends RealType<?>> imgPlus;
	private final ConditionalPointSet points;
	private final WithinRangeCondition<? extends RealType<?>> condition;
	private final RegionOfInterest regionAdapter;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ThresholdOverlay(ImageJ context, ImgPlus<? extends RealType<?>> imgPlus)
	{
		setContext(context);
		this.imgPlus = imgPlus;
		Function<long[], ? extends RealType<?>> function =
			new RealImageFunction(imgPlus, imgPlus.firstElement());
		condition =
			new WithinRangeCondition(function, Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY);
		long[] dims = new long[imgPlus.numDimensions()];
		imgPlus.dimensions(dims);
		HyperVolumePointSet volume = new HyperVolumePointSet(dims);
		points = new ConditionalPointSet(volume, condition);
		regionAdapter = new PointSetRegionOfInterest(points);
		figure = null;
		setName();
		setAlpha(255);
		setFillColor(Colors.RED);
		setLineColor(Colors.RED);
		setLineEndArrowStyle(ArrowStyle.NONE);
		setLineStartArrowStyle(ArrowStyle.NONE);
		setLineStyle(LineStyle.NONE);
		setLineWidth(1);
	}
	
	public ThresholdOverlay(ImageJ context,
		ImgPlus<? extends RealType<?>> imgPlus, double min, double max)
	{
		this(context, imgPlus);
		setRange(min,max);
	}

	public void setFigure(Displayable figure) {
		this.figure = figure;
	}
	
	public Displayable getFigure() {
		return figure;
	}

	public void setRange(double min, double max) {
		condition.setMin(min);
		condition.setMax(max);
		points.setCondition(condition); // this lets PointSet know it is changed
		setName();
	}
	
	public void resetThreshold() {
		setRange(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public PointSet getPoints() {
		return points;
	}
	
	/**
	 * Returns the {@link Condition} of the {@link ThresholdOverlay}. This is used
	 * by others (like the rendering code) to quickly iterate the portion of the
	 * data points they are interested in.
	 * <p>
	 * By design the return value is not a {@link WithinRangeCondition}. Users
	 * cannot poke the threshold values via this Condition. This would bypass
	 * internal communication. API users should call setRange(min, max) on this
	 * {@link ThresholdOverlay} if they want to manipulate the display range.
	 */
	public Condition<long[]> getCondition() {
		return condition;
	}

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
		return imgPlus.getAxisIndex(axis);
	}

	@Override
	public AxisType axis(int d) {
		return imgPlus.axis(d);
	}

	@Override
	public void axes(AxisType[] axes) {
		imgPlus.axes(axes);
	}

	@Override
	public void setAxis(AxisType axis, int d) {
		imgPlus.setAxis(axis, d);
	}

	@Override
	public double calibration(int d) {
		return imgPlus.calibration(d);
	}

	@Override
	public void setCalibration(double cal, int d) {
		if (cal == 1 && (d == 0 || d == 1)) return;
		throw new IllegalArgumentException(
			"Cannot set calibration of a ThresholdOverlay");
	}

	@Override
	public int numDimensions() {
		return points.numDimensions();
	}

	@Override
	public long min(int d) {
		return points.min(d);
	}

	@Override
	public long max(int d) {
		return points.max(d);
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
		points.dimensions(dimensions);
	}

	@Override
	public long dimension(int d) {
		return points.dimension(d);
	}

	@Override
	public RegionOfInterest getRegionOfInterest() {
		return regionAdapter;
	}

	@Override
	public ThresholdOverlay duplicate() {
		return new ThresholdOverlay(getContext(), imgPlus, condition.getMin(),
			condition.getMax());
	}

	@Override
	public void move(double[] deltas) {
		// do nothing - thresholds don't move though space
	}

	private void setName() {
		setName("Threshold: " + condition.getMin() + " to " + condition.getMax());
	}
}
