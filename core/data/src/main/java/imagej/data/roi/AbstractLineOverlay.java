//
// AbstractLineOverlay.java
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

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;


/**
 * @author leek
 *
 * The AbstractLineROI represents an overlay that has line properties
 * such as line color and line width.
 */
public abstract class AbstractLineOverlay extends AbstractOverlay implements Serializable {

	private static final long serialVersionUID = 1L;
	
	protected Color lineColor = new Color(0, 0, 0);
	
	protected double lineWidth = 1.0;
	
	/**
	 * @return the color to be used to paint lines or shape borders
	 */
	public Color getLineColor() {
		return lineColor;
	}

	/**
	 * @param lineColor the color to be used to paint lines and shape borders
	 */
	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
	}

	/**
	 * @return the width to be used when painting lines and shape borders, in pixels.
	 */
	public double getLineWidth() {
		return lineWidth;
	}

	/**
	 * @param lineWidth the width to be used when painting lines and shape borders, in pixels.
	 */
	public void setLineWidth(double lineWidth) {
		this.lineWidth = lineWidth;
	}
	
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(lineColor);
		out.writeDouble(lineWidth);
	}
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		lineColor = (Color)in.readObject();
		lineWidth = in.readDouble();
	}

}
