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
 * A wrapper class around {@link Histogram1d}.
 * 
 * This class provides inferred metadata: min and max.
 * 
 * @author Barry DeZonia
 */
public class HistogramBundle {

	private Histogram1d<?> histogram;
	private long min;
	private long max;
	private boolean hasChanges;

	public HistogramBundle(Histogram1d<?> hist, long min, long max) {
		this.histogram = hist;
		setMin(min);
		setMax(max);
		hasChanges = true;
	}

	public void setHistogram(Histogram1d<?> hist) {
		hasChanges |= hist != this.histogram;
		this.histogram = hist;
	}

	public void setMin(long min) {
		hasChanges |= min != this.min;
		this.min = min;
	}

	public void setMax(long max) {
		hasChanges |= max != this.max;
		this.max = max;
	}

	public Histogram1d<?> getHistogram() {
		return histogram;
	}

	public long getMin() {
		return min;
	}

	public long getMax() {
		return max;
	}

	public boolean hasChanges() {
		return hasChanges;
	}

	public void changesNoted() {
		hasChanges = false;
	}
}
