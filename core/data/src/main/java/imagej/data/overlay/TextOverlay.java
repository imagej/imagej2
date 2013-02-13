/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

import imagej.Context;
import net.imglib2.roi.RectangleRegionOfInterest;

// TODO - this code is a place holder for when we really support text overlays.
// This version allows IJ1 TextRois to survive translations back and forth
// across the legacy layer somewhat intact.

/**
 * @author Barry DeZonia
 */
public class TextOverlay extends AbstractROIOverlay<RectangleRegionOfInterest> {

	public enum Justification {
		LEFT, CENTER, RIGHT
	}

	private String text;
	private Justification just;

	public TextOverlay(Context context, double x, double y, String text,
		Justification j)
	{
		super(context, new RectangleRegionOfInterest(new double[] { x, y },
			new double[] { 0, 0 }));
		this.text = text;
		this.just = j;
	}

	public TextOverlay(Context context, double x, double y, String text) {
		this(context, x, y, text, Justification.LEFT);
	}

	public TextOverlay(Context context, double x, double y) {
		this(context, x, y, "Default Text");
	}

	public TextOverlay(Context context) {
		this(context, 0, 0);
	}

	@Override
	public void move(double[] deltas) {
		getRegionOfInterest().move(deltas);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Justification getJustification() {
		return just;
	}

	public void setJustification(Justification j) {
		just = j;
	}
}
