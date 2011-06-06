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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import imagej.data.AbstractDataObject;
import imagej.data.event.OverlayCreatedEvent;
import imagej.data.event.OverlayDeletedEvent;
import imagej.data.event.OverlayRestructuredEvent;
import imagej.data.event.OverlayUpdatedEvent;
import imagej.event.Events;
import imagej.util.ColorRGB;
import net.imglib2.roi.RegionOfInterest;

/**
 * Abstract superclass of {@link Overlay} implementations.
 * 
 * @author Curtis Rueden
 */
public class AbstractOverlay extends AbstractDataObject implements Overlay, Externalizable {

	private static final long serialVersionUID = 1L;
	protected ColorRGB fillColor = new ColorRGB(255, 255, 255);
	protected int alpha = 0;
	protected ColorRGB lineColor = new ColorRGB(0, 0, 0);
	protected double lineWidth = 1.0;
	protected Overlay.LineStyle lineStyle = Overlay.LineStyle.SOLID;

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
	}

}
