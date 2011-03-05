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

package imagej.model;

import mpicbg.imglib.image.Image;

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
	public String getName() { return name; }

	/** Sets the name of dataset. */
	public void setName(final String name) { this.name = name; }

	/** Returns the order of the axes. */
	public AxisLabel[] getAxes() { return axes; }

	/** Sets the order of the axes. */
	public void setAxes(final AxisLabel[] axes) { this.axes = axes; }

	/**
	 * Extracts metadata, including axis types,
	 * from the given encoded image name.
	 */
	public static Metadata createMetadata(final Image<?> img) {
		final String name = decodeName(img);
		final AxisLabel[] axes = decodeTypes(img);
		final Metadata md = new Metadata();
		md.setName(name);
		md.setAxes(axes);
		return md;
	}

	// CTR TODO - Code below is partially duplicated from imglib-io ImageOpener.
	// This functionality should live in a common utility place somewhere instead.

	/** Converts the given image's encoded name back to just the name. */
	public static String decodeName(final Image<?> img) {
		final String name = img.getName();
		final int lBracket = name.lastIndexOf(" [");
		if (lBracket < 0) return name;
		return name.substring(0, lBracket);
	}

	/**
	 * Converts the given image's encoded name back to a list of
	 * dimensional axis types.
	 *
	 * If the name is not encoded, returns some default type assignments.
	 */
	public static AxisLabel[] decodeTypes(final Image<?> img) {
		final String name = img.getName();

		// extract axis labels from encoded name
		final int lBracket = name.lastIndexOf(" [");
		if (lBracket >= 0) {
			final int rBracket = name.lastIndexOf("]");
			if (rBracket >= lBracket) {
				final String[] tokens = name.substring(lBracket + 2, rBracket).split(" ");
				final AxisLabel[] axes = new AxisLabel[tokens.length];
				for (int i=0; i<tokens.length; i++) {
					axes[i] = AxisLabel.getAxisLabel(tokens[i]);
				}
				return axes;
			}
		}

		// axes were not encoded in the name; return default axis order
		final AxisLabel[] axes = new AxisLabel[img.getNumDimensions()];
		for (int i=0; i<axes.length; i++) {
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

}
