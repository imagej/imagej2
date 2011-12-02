//
// AbstractOverlay.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.data.roi;

import imagej.data.AbstractData;
import imagej.data.Extents;
import imagej.data.event.OverlayCreatedEvent;
import imagej.data.event.OverlayDeletedEvent;
import imagej.data.event.OverlayRestructuredEvent;
import imagej.data.event.OverlayUpdatedEvent;
import imagej.util.ColorRGB;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.roi.RegionOfInterest;

/**
 * Abstract superclass of {@link Overlay} implementations.
 * 
 * @author Lee Kamentsky
 * @author Curtis Rueden
 */
public class AbstractOverlay extends AbstractData implements Overlay,
	Externalizable
{

	private static final long serialVersionUID = 1L;

	// -- instance variables --

	protected ArrowStyle startArrowStyle;
	protected ArrowStyle endArrowStyle;
	protected ColorRGB fillColor;
	protected int alpha;
	protected ColorRGB lineColor;
	protected double lineWidth;
	protected Overlay.LineStyle lineStyle;

	final protected List<AxisType> axes = new ArrayList<AxisType>();
	final protected List<Double> calibrations = new ArrayList<Double>();

	final protected SortedMap<AxisType, Long> axisPositions =
		new TreeMap<AxisType, Long>(new Comparator<AxisType>() {

			@Override
			public int compare(final AxisType axis1, final AxisType axis2) {
				if ((axis1 instanceof Axes) && (axis2 instanceof Axes)) {
					return new Integer(((Axes) axis1).ordinal()).compareTo(((Axes) axis2)
						.ordinal());
				}
				return axis1.getLabel().compareTo(axis2.getLabel());
			}
		});

	public AbstractOverlay() {
		// TODO: Apply OverlayService's settings to each new overlay.
		// As it stands, each new overlay receives the default settings only.
		this(new OverlaySettings());
	}

	public AbstractOverlay(final OverlaySettings settings) {
		applySettings(settings);
	}

	// -- AbstractData methods --

	@Override
	protected void register() {
		eventService.publish(new OverlayCreatedEvent(this));
	}

	// TODO - Decide whether this should really be public. If not, don't call it
	// elsewhere. But if so, add it to the proper interface.

	@Override
	public void delete() {
		eventService.publish(new OverlayDeletedEvent(this));
	}

	// -- Overlay methods --

	@Override
	public RegionOfInterest getRegionOfInterest() {
		// NB: By default, no associated region of interest.
		return null;
	}

	@Override
	public Long getPosition(final AxisType axis) {
		if (axisPositions.containsKey(axis)) {
			return axisPositions.get(axis);
		}
		return null;
	}

	@Override
	public void setPosition(final AxisType axis, final long position) {
		axisPositions.put(axis, position);
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
		if (!this.lineColor.equals(lineColor)) {
			this.lineColor = lineColor;
		}
	}

	@Override
	public double getLineWidth() {
		return lineWidth;
	}

	@Override
	public void setLineWidth(final double lineWidth) {
		if (this.lineWidth != lineWidth) {
			this.lineWidth = lineWidth;
		}
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

	// -- CalibratedSpace methods --

	@Override
	public int getAxisIndex(final AxisType axis) {
		int index = axes.indexOf(axis);
		if (index >= 0) return index;
		if (axisPositions.containsKey(axis)) {
			index = axes.size();
			for (final AxisType other : axisPositions.keySet()) {
				if (other == axis) return index;
				index++;
			}
		}
		return -1;
	}

	@Override
	public AxisType axis(final int d) {
		if (d < axes.size()) {
			return axes.get(d);
		}
		int index = axes.size();
		for (final AxisType axis : axisPositions.keySet()) {
			if (index++ == d) return axis;
		}
		return null;
	}

	@Override
	public void axes(final AxisType[] axesToFill) {
		for (int i = 0; (i < axesToFill.length) && (i < this.axes.size()); i++) {
			axesToFill[i] = this.axes.get(i);
		}
	}

	@Override
	public void setAxis(final AxisType axis, final int d) {
		while (this.axes.size() <= d) {
			this.axes.add(null);
			this.calibrations.add(1.0);
		}
		this.axes.set(d, axis);
	}

	@Override
	public double calibration(final int d) {
		if (d >= calibrations.size()) return 1.0;
		return calibrations.get(d);
	}

	@Override
	public void calibration(final double[] cal) {
		for (int i = 0; (i < cal.length) && (i < this.calibrations.size()); i++) {
			cal[i] = this.calibrations.get(i);
		}
		if (cal.length > calibrations.size()) {
			Arrays.fill(cal, calibrations.size(), cal.length, 1.0);
		}
	}

	@Override
	public void setCalibration(final double cal, final int d) {
		while (calibrations.size() <= d) {
			calibrations.add(1.0);
		}
		calibrations.set(d, cal);
	}

	// -- EuclideanSpace methods --

	@Override
	public int numDimensions() {
		return axes.size() + axisPositions.size();
	}

	// -- Data methods --

	@Override
	public void update() {
		eventService.publish(new OverlayUpdatedEvent(this));
	}

	@Override
	public void rebuild() {
		eventService.publish(new OverlayRestructuredEvent(this));
	}

	// -- LabeledSpace methods --

	@Override
	public Extents getExtents() {
		// FIXME
		return null;
	}

	// -- Externalizable methods --

	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		out.writeObject(lineColor);
		out.writeDouble(lineWidth);
		out.writeInt(lineStyle.name().length());
		out.writeChars(lineStyle.name());
		out.writeObject(fillColor);
		out.writeInt(alpha);
		out.writeInt(axes.size());
		for (int i = 0; i < axes.size(); i++) {
			writeAxis(out, axes.get(i));
			out.writeDouble(calibrations.get(i));
		}
		out.writeInt(axisPositions.size());
		for (final AxisType axis : axisPositions.keySet()) {
			writeAxis(out, axis);
			out.writeLong(axisPositions.get(axis));
		}
		writeString(out, startArrowStyle.name());
		writeString(out, endArrowStyle.name());
	}

	@Override
	public void readExternal(final ObjectInput in) throws IOException,
		ClassNotFoundException
	{
		lineColor = (ColorRGB) in.readObject();
		lineWidth = in.readDouble();
		final char[] buffer = new char[in.readInt()];
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = in.readChar();
		}
		lineStyle = Overlay.LineStyle.valueOf(new String(buffer));
		fillColor = (ColorRGB) in.readObject();
		alpha = in.readInt();
		final int nAxes = in.readInt();
		this.axes.clear();
		this.calibrations.clear();
		for (int i = 0; i < nAxes; i++) {
			axes.add(readAxis(in));
			calibrations.add(in.readDouble());
		}
		final int nPositions = in.readInt();
		for (int i = 0; i < nPositions; i++) {
			final AxisType axis = readAxis(in);
			axisPositions.put(axis, in.readLong());
		}
		startArrowStyle = ArrowStyle.valueOf(readString(in));
		endArrowStyle = ArrowStyle.valueOf(readString(in));
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

	/** Helper function to write a string to the object output. */
	private void writeString(final ObjectOutput out, final String s)
		throws IOException
	{
		out.writeInt(s.length());
		out.writeChars(s);
	}

	/**
	 * Helper function to read a string.
	 * 
	 * @return string read from in
	 */
	private String readString(final ObjectInput in) throws IOException {
		final int length = in.readInt();
		final char[] buffer = new char[length];
		for (int i = 0; i < length; i++)
			buffer[i] = in.readChar();
		return new String(buffer);
	}

	private void writeAxis(final ObjectOutput out, final AxisType axis)
		throws IOException
	{
		writeString(out, axis.getLabel());
	}

	private AxisType readAxis(final ObjectInput in) throws IOException {
		return Axes.get(new String(readString(in)));
	}

}
