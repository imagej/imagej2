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

import imagej.data.AbstractDataObject;
import imagej.data.event.OverlayCreatedEvent;
import imagej.data.event.OverlayDeletedEvent;
import imagej.data.event.OverlayRestructuredEvent;
import imagej.data.event.OverlayUpdatedEvent;
import imagej.event.Events;
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

import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
import net.imglib2.roi.RegionOfInterest;

/**
 * Abstract superclass of {@link Overlay} implementations.
 * 
 * @author Curtis Rueden
 */
public class AbstractOverlay extends AbstractDataObject implements Overlay, Externalizable {

	public final static ColorRGB defaultLineColor = new ColorRGB(255,255,0);
	public final static ColorRGB defaultFillColor = new ColorRGB(0, 255, 0);
	protected ArrowStyle startArrowStyle = ArrowStyle.NONE;
	protected ArrowStyle endArrowStyle = ArrowStyle.NONE;
	private static final long serialVersionUID = 1L;
	protected ColorRGB fillColor = defaultFillColor;
	protected int alpha = 0;
	protected ColorRGB lineColor = defaultLineColor;
	protected double lineWidth = 1.0;
	protected Overlay.LineStyle lineStyle = Overlay.LineStyle.SOLID;
	final protected List<Axis> axes = new ArrayList<Axis>();
	final protected List<Double> calibrations = new ArrayList<Double>();
	final protected SortedMap<Axis, Long> axisPositions = new TreeMap<Axis, Long>(new Comparator<Axis>() {

		@Override
		public int compare(Axis axis1, Axis axis2) {
			if ((axis1 instanceof Axes) && (axis2 instanceof Axes)) {
				return new Integer(((Axes)axis1).ordinal()).compareTo(((Axes)axis2).ordinal());
			}
			return axis1.getLabel().compareTo(axis2.getLabel());
		}});

	// -- Overlay methods --

	@Override
	public RegionOfInterest getRegionOfInterest() {
		// NB: By default, no associated region of interest.
		return null;
	}

	// -- DataObject methods --

	@Override
	public void update() {
		Events.publish(new OverlayUpdatedEvent(this));
	}

	@Override
	public void rebuild() {
		Events.publish(new OverlayRestructuredEvent(this));
	}

	@Override
	public void register() {
		Events.publish(new OverlayCreatedEvent(this));		
	}

	@Override
	public void delete() {
		Events.publish(new OverlayDeletedEvent(this));
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
	public int getAlpha() {
		return alpha;
	}

	@Override
	public void setAlpha(final int alpha) {
		this.alpha = alpha;
	}

	@Override
	public ColorRGB getLineColor() {
		return lineColor;
	}

	@Override
	public void setLineColor(ColorRGB lineColor) {
		if (! this.lineColor.equals(lineColor)) {
			this.lineColor = lineColor;
		}
	}

	@Override
	public double getLineWidth() {
		return lineWidth;
	}

	/**
	 * @param lineWidth the width to be used when painting lines and shape borders, in pixels.
	 */
	@Override
	public void setLineWidth(double lineWidth) {
		if (this.lineWidth != lineWidth) {
			this.lineWidth = lineWidth;
		}
	}
	
	@Override
	public LineStyle getLineStyle() {
		return lineStyle;
	}

	@Override
	public void setLineStyle(LineStyle lineStyle) {
		this.lineStyle = lineStyle;
	}

	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		out.writeObject(lineColor);
		out.writeDouble(lineWidth);
		out.writeInt(lineStyle.name().length());
		out.writeChars(lineStyle.name());
		out.writeObject(fillColor);
		out.writeInt(alpha);
		out.writeInt(axes.size());
		for (int i=0; i<axes.size(); i++){
			writeAxis(out, axes.get(i));
			out.writeDouble(calibrations.get(i));
		}
		out.writeInt(axisPositions.size());
		for (Axis axis:axisPositions.keySet()) {
			writeAxis(out, axis);
			out.writeLong(axisPositions.get(axis));
		}
		writeString(out, startArrowStyle.name());
		writeString(out, endArrowStyle.name());
	}
	
	/**
	 * Helper function to write a string to the object output
	 * @param out
	 * @param s
	 * @throws IOException
	 */
	static protected void writeString(final ObjectOutput out, final String s) throws IOException {
		out.writeInt(s.length());
		out.writeChars(s);
		
	}
	
	/**
	 * Helper function to read a string
	 * @param in
	 * @return string read from in
	 * @throws IOException
	 */
	static protected String readString(final ObjectInput in) throws IOException {
		int length = in.readInt();
		char [] buffer = new char[length];
		for (int i=0; i<length; i++) buffer[i] = in.readChar();
		return new String(buffer);
	}
	
	static private void writeAxis(final ObjectOutput out, final Axis axis) throws IOException {
		writeString(out, axis.getLabel());
	}
	
	@Override
	public void readExternal(final ObjectInput in) throws IOException,
		ClassNotFoundException
	{
		lineColor = (ColorRGB) in.readObject();
		lineWidth = in.readDouble();
		char [] buffer = new char[in.readInt()];
		for (int i=0; i<buffer.length; i++) buffer[i] = in.readChar();
		lineStyle = Overlay.LineStyle.valueOf(new String(buffer));
		fillColor = (ColorRGB) in.readObject();
		alpha = in.readInt();
		final int nAxes = in.readInt();
		this.axes.clear();
		this.calibrations.clear();
		for (int i=0; i<nAxes; i++) {
			axes.add(readAxis(in));
			calibrations.add(in.readDouble());
		}
		final int nPositions = in.readInt();
		for (int i=0; i<nPositions; i++) {
			Axis axis = readAxis(in);
			axisPositions.put(axis, in.readLong());
		}
		startArrowStyle = ArrowStyle.valueOf(readString(in));
		endArrowStyle = ArrowStyle.valueOf(readString(in));
	}

	static private Axis readAxis(final ObjectInput in) throws IOException {
		return Axes.get(new String(readString(in)));
	}
	@Override
	public int getAxisIndex(Axis axis) {
		int index = axes.indexOf(axis);
		if (index >= 0) return index;
		if (axisPositions.containsKey(axis)) {
			index = axes.size();
			for (Axis other:axisPositions.keySet()) {
				if (other == axis) return index;
				index++;
			}
		}
		return -1;
	}

	@Override
	public Axis axis(int d) {
		if (d < axes.size()) {
			return axes.get(d);
		}
		int index = axes.size();
		for (Axis axis:axisPositions.keySet()) {
			if (index++ == d) return axis;
		}
		return null;
	}

	@Override
	public void axes(Axis[] axesToFill) {
		for (int i=0; (i < axesToFill.length) &&
									(i < this.axes.size()); i++ )
		{
			axesToFill[i] = this.axes.get(i);
		}
	}

	@Override
	public void setAxis(Axis axis, int d) {
		while(this.axes.size() <= d) {
			this.axes.add(null);
			this.calibrations.add(1.0);
		}
		this.axes.set(d, axis);
	}

	@Override
	public double calibration(int d) {
		if (d >= calibrations.size()) return 1.0;
		return calibrations.get(d);
	}

	@Override
	public void calibration(double[] cal) {
		for (int i=0; (i < cal.length) && (i < this.calibrations.size()); i++ ) {
			cal[i] = this.calibrations.get(i);
		}
		if (cal.length > calibrations.size()) {
			Arrays.fill(cal, calibrations.size(), cal.length, 1.0);
		}
	}

	@Override
	public void setCalibration(double cal, int d) {
		while (calibrations.size() <= d) {
			calibrations.add(1.0);
		}
		calibrations.set(d, cal);
	}

	@Override
	public int numDimensions() {
		return axes.size() + axisPositions.size();
	}

	/* (non-Javadoc)
	 * @see imagej.data.roi.Overlay#setPosition(net.imglib2.img.Axis, long)
	 */
	@Override
	public void setPosition(Axis axis, long position) {
		axisPositions.put(axis, position);
	}
	
	/* (non-Javadoc)
	 * @see imagej.data.roi.Overlay#getPosition(net.imglib2.img.Axis)
	 */
	@Override
	public Long getPosition(Axis axis) {
		if (axisPositions.containsKey(axis)) {
			return axisPositions.get(axis);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see imagej.data.roi.Overlay#getLineStartArrowStyle()
	 */
	@Override
	public ArrowStyle getLineStartArrowStyle() {
		return startArrowStyle;
	}

	/* (non-Javadoc)
	 * @see imagej.data.roi.Overlay#setLineStartArrowStyle(imagej.data.roi.Overlay.ArrowStyle)
	 */
	@Override
	public void setLineStartArrowStyle(ArrowStyle style) {
		startArrowStyle = style;
	}

	/* (non-Javadoc)
	 * @see imagej.data.roi.Overlay#getLineEndArrowStyle()
	 */
	@Override
	public ArrowStyle getLineEndArrowStyle() {
		return endArrowStyle;
	}

	/* (non-Javadoc)
	 * @see imagej.data.roi.Overlay#setLineEndArrowStyle(imagej.data.roi.Overlay.ArrowStyle)
	 */
	@Override
	public void setLineEndArrowStyle(ArrowStyle style) {
		endArrowStyle = style;
	}
}
