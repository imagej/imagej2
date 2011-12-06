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

package imagej.data.overlay;

import imagej.data.AbstractData;
import imagej.data.Extents;
import imagej.data.event.OverlayCreatedEvent;
import imagej.data.event.OverlayDeletedEvent;
import imagej.data.event.OverlayRestructuredEvent;
import imagej.data.event.OverlayUpdatedEvent;
import imagej.util.ColorRGB;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

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

	private final Map<AxisType, Long> positions =
		new HashMap<AxisType, Long>();

	private int alpha;
	private ColorRGB fillColor;
	private ColorRGB lineColor;
	private double lineWidth;
	private Overlay.LineStyle lineStyle;
	private ArrowStyle startArrowStyle;
	private ArrowStyle endArrowStyle;

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

	// CTR: TODO: Eliminate getPosition and setPosition from Overlay API.
	//
	// These methods are redundant with DataView, and actually misplaced.
	// An Overlay should not contain any dimensions other than its actual ones
	// (so all normal 2D overlays have only XY for now; in future we could have
	// overlays of 3D+, but not now).
	//
	// The JHotDraw figures need to be linked to an OverlayView, not an Overlay
	// directly.
	//
	// Further, DataView needs to support localizing the position across any
	// dimensional axis, not just those present in the linked Data object.
	// So a 2D overlay can be localized in the Display's space with no fuss.
	//
	// We still need the concept of a composite data object, and/or composite data
	// view -- further thought is needed. But for beta1, we may not need it.
	// On the other hand, the axis compositing logic done in AbstractImageDisplay
	// would fit very well in a CompositeData or CompositeDataView object. If we
	// go that route sooner, the Display can be simplified to allow only one
	// object at a time, rather than a list. What would the consequences be?

	@Override
	public Long getPosition(final AxisType axis) {
		return positions.get(axis);
	}

	@Override
	public void setPosition(final AxisType axis, final long position) {
		positions.put(axis, position);
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
		return new Extents(new long[0]);
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
