/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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

import java.util.ArrayList;
import java.util.List;

import net.imglib2.histogram.Histogram1d;

/**
 * HistogramBundles marry a list of {@link Histogram1d}s with addtional
 * rendering information.
 * 
 * @author Barry DeZonia
 */
public class HistogramBundle {

	// -- fields --

	private List<Histogram1d<?>> histograms;
	private long binMin = -1;
	private long binMax = -1;
	private double theoryMin = Double.NaN;
	private double theoryMax = Double.NaN;
	private double dataMin = Double.NaN;
	private double dataMax = Double.NaN;
	private int preferredSizeX = 300;
	private int preferredSizeY = 150;
	private double slope = Double.NaN;
	private double intercept = Double.NaN;
	private boolean hasChanges;

	// -- constructors --

	public HistogramBundle(Histogram1d<?> hist) {
		histograms = new ArrayList<Histogram1d<?>>();
		histograms.add(hist);
		hasChanges = true;
	}

	public HistogramBundle(List<Histogram1d<?>> histList) {
		histograms = histList;
		hasChanges = true;
	}

	// -- changes api --

	/**
	 * Returns true if the bundle has been changed via use of some of the field
	 * setting api. This information can be used by renderers to update their UI
	 * as appropriate. Note one can also force the change flag via
	 * setHasChanges().
	 */
	public boolean hasChanges() {
		return hasChanges;
	}

	/**
	 * Sets the changes flag. Called by renderers after they have updated their UI
	 * to reflect previous changes.
	 */
	public void setHasChanges(boolean val) {
		hasChanges = val;
	}

	// -- accessors --

	/**
	 * Returns the total number of histograms contained in this bundle.
	 */
	public int getHistogramCount() {
		return histograms.size();
	}

	/**
	 * Sets the histogram of interest for this HistogramBundle.
	 */
	public void setHistogram(int index, Histogram1d<?> hist) {
		if (index < 0) {
			throw new IllegalArgumentException("index number less than 0");
		}
		else if (index > histograms.size()) {
			throw new IllegalArgumentException("index number more than 1 beyond end");
		}
		else if (index == histograms.size()) {
			if (hist == null) return;
			hasChanges = true;
			histograms.add(hist);
		}
		else { // 0 <= index <= size()-1
			if (hist == null) {
				hasChanges = true;
				histograms.remove(index);
			}
			else {
				hasChanges |= hist != getHistogram(index);
				histograms.set(index, hist);
			}
		}
	}

	/**
	 * Gets the histogram of interest for this HistogramBundle.
	 */
	public Histogram1d<?> getHistogram(int index) {
		if (index < 0) {
			throw new IllegalArgumentException("index number less than 0");
		}
		else if (index > histograms.size()) {
			throw new IllegalArgumentException("index number beyond end");
		}
		return histograms.get(index);
	}

	/**
	 * Sets the bin number of the minimum value. Can be used to by renderers to
	 * display the minimum line on the histogram for example. A value of -1 notes
	 * that the minimum value is not of interest.
	 */
	public void setMinBin(long min) {
		hasChanges |= min != this.binMin;
		this.binMin = min;
	}

	/**
	 * Gets the bin number of the minimum value. A value of -1 notes the minimum
	 * value is not of interest.
	 */
	public long getMinBin() {
		return binMin;
	}

	/**
	 * Sets the bin number of the maximum value. Can be used to by renderers to
	 * display the maximum line on the histogram for example. A value of -1 notes
	 * that the maximum value is not of interest.
	 */
	public void setMaxBin(long max) {
		hasChanges |= max != this.binMax;
		this.binMax = max;
	}

	/**
	 * Gets the bin number of the maximum value. A value of -1 notes the maximum
	 * value is not of interest.
	 */
	public long getMaxBin() {
		return binMax;
	}

	/**
	 * Sets the preferred size for the rendering of this histogram as a widget.
	 */
	public void setPreferredSize(int x, int y) {
		hasChanges |= preferredSizeX != x;
		hasChanges |= preferredSizeY != y;
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

	// TODO - eliminate one of slope/intercept approach or data range approach

	/**
	 * Sets the line equation for the slope line of a histogram.
	 */
	public void setLineSlopeIntercept(double slope, double intercept) {
		hasChanges |= different(slope, this.slope);
		hasChanges |= different(intercept, this.intercept);
		this.slope = slope;
		this.intercept = intercept;
	}

	/**
	 * Gets the line equation slope.
	 */
	public double getLineSlope() {
		return slope;
	}

	/**
	 * Gets the line equation intercept. The scale is 1.0 for whole height of the
	 * plot. This does NOT mean that value must be between 0 and 1. Intercepts can
	 * go outside range of table height.
	 */
	public double getLineIntercept() {
		return intercept;
	}

	// TODO - eliminate one of slope/intercept approach or data range approach

	/**
	 * Sets the min/max values of the actual range.
	 */
	public void setDataMinMax(double min, double max) {
		hasChanges |= different(min, dataMin);
		hasChanges |= different(max, dataMax);
		dataMin = min;
		dataMax = max;
	}

	/**
	 * Gets the minimum value of the actual data range.
	 */
	public double getDataMin() {
		return dataMin;
	}

	/**
	 * Gets the maximum value of the actual data range.
	 */
	public double getDataMax() {
		return dataMax;
	}

	/**
	 * Sets the min/max values of the desired range.
	 */
	public void setTheoreticalMinMax(double min, double max) {
		hasChanges |= different(min, theoryMin);
		hasChanges |= different(max, theoryMax);
		theoryMin = min;
		theoryMax = max;
	}

	/**
	 * Gets the minimum value of the desired range.
	 */
	public double getTheoreticalMin() {
		return theoryMin;
	}

	/**
	 * Gets the maximum value of the desired range.
	 */
	public double getTheoreticalMax() {
		return theoryMax;
	}

	// -- helpers --

	private boolean different(double v1, double v2) {
		if (Double.isNaN(v1) && Double.isNaN(v2)) return false;
		return v1 != v2;
	}
}
