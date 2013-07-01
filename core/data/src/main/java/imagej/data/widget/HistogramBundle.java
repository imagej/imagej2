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

package imagej.data.widget;

import net.imglib2.histogram.Histogram1d;

/**
 * HistogramBundles marry a {@link Histogram1d} with rendering information.
 * 
 * @author Barry DeZonia
 */
public class HistogramBundle {

	// -- fields --

	private Histogram1d<?> histogram;
	private long min = -1;
	private long max = -1;
	private int preferredSizeX = 300;
	private int preferredSizeY = 150;
	private boolean hasChanges;

	// -- constructors --

	public HistogramBundle(Histogram1d<?> hist, long min, long max) {
		this.histogram = hist;
		setMin(min);
		setMax(max);
		hasChanges = true;
	}

	// -- accessors --

	/**
	 * Sets the histogram of interest for this HistogramBundle.
	 */
	public void setHistogram(Histogram1d<?> hist) {
		hasChanges |= hist != this.histogram;
		this.histogram = hist;
	}

	/**
	 * Gets the histogram of interest for this HistogramBundle.
	 */
	public Histogram1d<?> getHistogram() {
		return histogram;
	}

	/**
	 * Sets the bin number of the minimum value. Can be used to by renderers to
	 * display the minimum line on the histogram for example. A value of -1 notes
	 * that the minimum value is not of interest.
	 */
	public void setMin(long min) {
		hasChanges |= min != this.min;
		this.min = min;
	}

	/**
	 * Gets the bin number of the minimum value. A value of -1 notes the minimum
	 * value is not of interest.
	 */
	public long getMin() {
		return min;
	}

	/**
	 * Sets the bin number of the maximum value. Can be used to by renderers to
	 * display the maximum line on the histogram for example. A value of -1 notes
	 * that the maximum value is not of interest.
	 */
	public void setMax(long max) {
		hasChanges |= max != this.max;
		this.max = max;
	}

	/**
	 * Gets the bin number of the maximum value. A value of -1 notes the maximum
	 * value is not of interest.
	 */
	public long getMax() {
		return max;
	}

	/**
	 * Sets the preferred size for the rendering of this histogram as a widget.
	 */
	public void setPreferredSize(int x, int y) {
		preferredSizeX = x;
		preferredSizeY = y;
	}

	/**
	 * Gets the preferred X size of the widget for the rendering of this
	 * histogram.
	 */
	public int getPreferredSizeX() {
		return preferredSizeX;
	}

	/**
	 * Gets the preferred Y size of the widget for the rendering of this
	 * histogram.
	 */
	public int getPreferredSizeY() {
		return preferredSizeY;
	}

	// -- changes api --

	/**
	 * Returns true if the bundle has been changed via use of some of the field
	 * setting api. This information can be used by renderers to update their UI
	 * as appropriate.
	 */
	public boolean hasChanges() {
		return hasChanges;
	}

	/**
	 * Clears the changes flag. Called by renderers after they have updated their
	 * UI to reflect previous changes.
	 */
	public void changesNoted() {
		hasChanges = false;
	}
}
