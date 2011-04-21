//
// Metadata.java
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

package imagej.data;

import net.imglib2.img.Img;

/**
 * TODO
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
public class Metadata {

	private String name;
	private AxisLabel[] axes;

	public Metadata() {
		this.name = "Untitled";
		this.axes = new AxisLabel[0];
	}

	/** Gets the name of dataset. */
	public String getName() {
		return name;
	}

	/** Sets the name of dataset. */
	public void setName(final String name) {
		this.name = name;
	}

	/** Returns the order of the axes. */
	public AxisLabel[] getAxes() {
		return axes;
	}

	/** Sets the order of the axes. */
	public void setAxes(final AxisLabel[] axes) {
		this.axes = axes;
	}

	/** Creates default metadata for the given image. */
	public static Metadata createMetadata(final Img<?> img) {
		final AxisLabel[] axes = createAxes(img);
		final Metadata md = new Metadata();
		md.setName(img.toString());
		md.setAxes(axes);
		return md;
	}

	/** Creates default axis labels for the given image. */
	public static AxisLabel[] createAxes(final Img<?> img) {
		// axes were not encoded in the name; return default axis order
		final AxisLabel[] axes = new AxisLabel[img.numDimensions()];
		for (int i = 0; i < axes.length; i++) {
			switch (i) {
				case 0:
					axes[i] = AxisLabel.X;
					break;
				case 1:
					axes[i] = AxisLabel.Y;
					break;
				case 2:
					axes[i] = AxisLabel.Z;
					break;
				case 3:
					axes[i] = AxisLabel.TIME;
					break;
				case 4:
					axes[i] = AxisLabel.CHANNEL;
					break;
				default:
					axes[i] = AxisLabel.OTHER;
			}
		}
		return axes;
	}

	/** Sets this Metadata's values from another Metadata object. */
	public void copyFrom(final Metadata other) {
		final String newName = other.getName();
		final AxisLabel[] newAxes = other.getAxes().clone();
		setName(newName);
		setAxes(newAxes);
	}

}
