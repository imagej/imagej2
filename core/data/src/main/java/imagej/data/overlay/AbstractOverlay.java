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
import imagej.data.AbstractData;
import imagej.data.display.OverlayService;
import imagej.data.event.OverlayCreatedEvent;
import imagej.data.event.OverlayDeletedEvent;
import imagej.data.event.OverlayRestructuredEvent;
import imagej.data.event.OverlayUpdatedEvent;
import imagej.util.ColorRGB;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.Positionable;
import net.imglib2.RealPositionable;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.roi.RegionOfInterest;

/**
 * Abstract superclass of {@link Overlay} implementations.
 * 
 * @author Lee Kamentsky
 * @author Curtis Rueden
 */
public class AbstractOverlay extends AbstractData implements Overlay {

	private static final long serialVersionUID = 1L;

	private final List<AxisType> axes = new ArrayList<AxisType>();
	private final List<Double> cal = new ArrayList<Double>();

	private int alpha;
	private ColorRGB fillColor;
	private ColorRGB lineColor;
	private double lineWidth;
	private Overlay.LineStyle lineStyle;
	private ArrowStyle startArrowStyle;
	private ArrowStyle endArrowStyle;

	public AbstractOverlay(final ImageJ context) {
		super(context);
		final OverlayService overlayService =
			context == null ? null : context.getService(OverlayService.class);
		if (overlayService == null) applySettings(new OverlaySettings());
		else applySettings(overlayService.getDefaultSettings());
		axes.add(Axes.X);
		axes.add(Axes.Y);
		cal.add(1d);
		cal.add(1d);
	}

	// -- AbstractData methods --

	@Override
	protected void register() {
		publish(new OverlayCreatedEvent(this));
	}

	// TODO - Decide whether this should really be public. If not, don't call it
	// elsewhere. But if so, add it to the proper interface.

	@Override
	public void delete() {
		publish(new OverlayDeletedEvent(this));
	}

	// -- Overlay methods --

	@Override
	public RegionOfInterest getRegionOfInterest() {
		// NB: By default, no associated region of interest.
		return null;
	}

	@Override
	public int getAlpha() {
		return alpha;
	}

	@Override
	public void setAlpha(final int alpha) {
		this.alpha = alpha;
	}

	@Override
	public ColorRGB getFillColor() {
		return fillColor;
	}

	@Override
	public void setFillColor(final ColorRGB fillColor) {
		this.fillColor = fillColor;
	}

	@Override
	public ColorRGB getLineColor() {
		return lineColor;
	}

	@Override
	public void setLineColor(final ColorRGB lineColor) {
		this.lineColor = lineColor;
	}

	@Override
	public double getLineWidth() {
		return lineWidth;
	}

	@Override
	public void setLineWidth(final double lineWidth) {
		this.lineWidth = lineWidth;
	}

	@Override
	public LineStyle getLineStyle() {
		return lineStyle;
	}

	@Override
	public void setLineStyle(final LineStyle lineStyle) {
		this.lineStyle = lineStyle;
	}

	@Override
	public ArrowStyle getLineStartArrowStyle() {
		return startArrowStyle;
	}

	@Override
	public void setLineStartArrowStyle(final ArrowStyle style) {
		startArrowStyle = style;
	}

	@Override
	public ArrowStyle getLineEndArrowStyle() {
		return endArrowStyle;
	}

	@Override
	public void setLineEndArrowStyle(final ArrowStyle style) {
		endArrowStyle = style;
	}

	// -- Data methods --

	@Override
	public void update() {
		publish(new OverlayUpdatedEvent(this));
	}

	@Override
	public void rebuild() {
		publish(new OverlayRestructuredEvent(this));
	}

	// -- CalibratedInterval methods --

	@Override
	public boolean isDiscrete() {
		return false;
	}

	// -- CalibratedSpace methods --

	@Override
	public int getAxisIndex(final AxisType axis) {
		return axes.indexOf(axis);
	}

	@Override
	public AxisType axis(final int d) {
		return axes.get(d);
	}

	@Override
	public void axes(final AxisType[] target) {
		for (int i = 0; i < target.length; i++)
			target[i] = axis(i);
	}

	@Override
	public void setAxis(final AxisType axis, final int d) {
		axes.set(d, axis);
	}

	@Override
	public double calibration(final int d) {
		return cal.get(d);
	}

	@Override
	public void calibration(final double[] target) {
		for (int i = 0; i < target.length; i++)
			target[i] = calibration(i);
	}

	@Override
	public void setCalibration(final double value, final int d) {
		cal.set(d, value);
	}

	// -- EuclideanSpace methods --

	@Override
	public int numDimensions() {
		return 2;
	}

	// -- Interval methods --

	@Override
	public long min(final int d) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void min(final long[] min) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void min(final Positionable min) {
		throw new UnsupportedOperationException();
	}

	@Override
	public long max(final int d) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void max(final long[] max) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void max(final Positionable max) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void dimensions(final long[] dimensions) {
		throw new UnsupportedOperationException();
	}

	@Override
	public long dimension(final int d) {
		throw new UnsupportedOperationException();
	}

	// -- RealInterval methods --

	@Override
	public double realMin(final int d) {
		return getRegionOfInterest().realMin(d);
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
		return getRegionOfInterest().realMax(d);
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

	// -- Externalizable methods --

	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		out.writeInt(alpha);
		out.writeObject(fillColor);
		out.writeObject(lineColor);
		out.writeDouble(lineWidth);
		out.writeObject(lineStyle.toString());
		out.writeObject(startArrowStyle.toString());
		out.writeObject(endArrowStyle.toString());
		super.writeExternal(out);
	}

	@Override
	public void readExternal(final ObjectInput in) throws IOException,
		ClassNotFoundException
	{
		alpha = in.readInt();
		fillColor = (ColorRGB) in.readObject();
		lineColor = (ColorRGB) in.readObject();
		lineWidth = in.readDouble();
		lineStyle = Overlay.LineStyle.valueOf((String) in.readObject());
		startArrowStyle = ArrowStyle.valueOf((String) in.readObject());
		endArrowStyle = ArrowStyle.valueOf((String) in.readObject());
		super.readExternal(in);
	}

	// -- Helper methods --

	private void applySettings(final OverlaySettings settings) {
		startArrowStyle = settings.getStartArrowStyle();
		endArrowStyle = settings.getEndArrowStyle();
		fillColor = settings.getFillColor();
		alpha = settings.getAlpha();
		lineColor = settings.getLineColor();
		lineWidth = settings.getLineWidth();
		lineStyle = settings.getLineStyle();
	}

}
