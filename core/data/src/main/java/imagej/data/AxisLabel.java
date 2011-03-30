//
// AxisLabel.java
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

import java.util.Hashtable;

// TODO - unify this enum with the axis labels used in
// Bio-Formats (loci.formats.FormatTools) and
// imglib-io (mpicbg.imglib.io.ImageOpener).

/**
 * TODO
 *
 * @author Curtis Rueden
 */
public enum AxisLabel {
	X("X"),
	Y("Y"),
	Z("Z"),
	TIME("Time"),
	CHANNEL("Channel"),
	LIFETIME("Lifetime"),
	OTHER("Other");

	private static Hashtable<String, AxisLabel> axisLabels =
		new Hashtable<String, AxisLabel>();

	static {
		for (final AxisLabel axisLabel : AxisLabel.values()) {
			axisLabels.put(axisLabel.getLabel(), axisLabel);
		}
	}

	public static AxisLabel getAxisLabel(String label) {
		return axisLabels.get(label);
	}

	public static boolean isXY(AxisLabel dimLabel) {
		return dimLabel == AxisLabel.X || dimLabel == AxisLabel.Y;
	}

	private String label;

	private AxisLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public String toString() {
		return label;
	}

}
