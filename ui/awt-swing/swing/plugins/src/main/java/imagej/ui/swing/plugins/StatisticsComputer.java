/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
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

// TODO - reconcile copyright

/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * @author Stephan Preibisch
 */

package imagej.ui.swing.plugins;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import net.imglib2.Cursor;
import net.imglib2.algorithm.Algorithm;
import net.imglib2.algorithm.Benchmark;
import net.imglib2.algorithm.MultiThreaded;
import net.imglib2.img.Img;
import net.imglib2.multithreading.Chunk;
import net.imglib2.multithreading.SimpleMultiThreading;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;

/*
 * A multithreaded statistics computer
 * Min/max, histogram, std, etc.
 * Based on ComputerMinMax
 * @author Grant Harris
 */
public class StatisticsComputer<T extends RealType<T> & Comparable<T>> implements Algorithm, MultiThreaded, Benchmark {

	final Img<T> image;
	String errorMessage = "";
	int numThreads;
	long processingTime;
	//
	final T min, max;
	private int[] histogram;
	private int bins;
	private final int defaultBins = 256;
	private double histoMin;
	private double histoMax;


	public StatisticsComputer(final Img<T> image) {
		setNumThreads();
		this.image = image;
		this.min = image.firstElement().createVariable();
		this.max = this.min.copy();
		this.bins = defaultBins;
		// + set default histoMin/Max
	}

	public void setHistogramBinsMinMax(int bins, double min, double max) {
		this.bins = bins;
		this.histoMin = min;
		this.histoMax = max;
	}

	public T getMin() {
		return min;
	}

	public T getMax() {
		return max;
	}

	public int[] getHistogram(){
		return histogram;
	}
			
	@Override
	public boolean process() {
		final long startTime = System.currentTimeMillis();

		final long imageSize = image.size();

		final AtomicInteger ai = new AtomicInteger(0);
		final Thread[] threads = SimpleMultiThreading.newThreads(getNumThreads());

		final Vector<Chunk> threadChunks = SimpleMultiThreading.divideIntoChunks(imageSize, numThreads);
		final Vector<T> minValues = new Vector<T>();
		final Vector<T> maxValues = new Vector<T>();
		final Vector<int[]> histograms = new Vector<int[]>();

		for (int ithread = 0; ithread < threads.length; ++ithread) {
			minValues.add(image.firstElement().createVariable());
			maxValues.add(image.firstElement().createVariable());
			histograms.add(new int[bins]);
			threads[ithread] = new Thread(new Runnable() {
				public void run() {
					// Thread ID
					final int myNumber = ai.getAndIncrement();
					// get chunk of pixels to process
					final Chunk myChunk = threadChunks.get(myNumber);
					// compute min and max
					compute(myChunk.getStartPosition(), myChunk.getLoopSize(),
							minValues.get(myNumber), maxValues.get(myNumber),
							histograms.get(myNumber));
				}
			});
		}

		SimpleMultiThreading.startAndJoin(threads);

		// Consolidate results from threads.... compute overall min and max, histogram...
		min.set(minValues.get(0));
		max.set(maxValues.get(0));
		histogram = new int[bins];
		for (int i = 0; i < threads.length; ++i) {
			T value = minValues.get(i);
			if (Util.min(min, value) == value) {
				min.set(value);
			}
			value = maxValues.get(i);
			if (Util.max(max, value) == value) {
				max.set(value);
			}
			int[] h = histograms.get(i);
			for (int j = 0; j < h.length; j++) {
				histogram[j] += h[j];
			}
		}
		processingTime = System.currentTimeMillis() - startTime;
		return true;
	}

	protected void compute(final long startPos, final long loopSize, final T min, final T max, final int[] histo) {
		final Cursor<T> cursor = image.cursor();

		// init min and max
		cursor.fwd();

		min.set(cursor.get());
		max.set(cursor.get());

		cursor.reset();

		// move to the starting position of the current thread
		cursor.jumpFwd(startPos);
		
		// calculate multiple for determining bin
		
		//double k = (BINS - 1)/ (max - min);
		double k = (bins - 1)/ (histoMax - histoMin);

		// do as many pixels as wanted by this thread
		for (long j = 0; j < loopSize; ++j) {
			cursor.fwd();
			final T value = cursor.get();
			
			// for min/max
			if (Util.min(min, value) == value) {
				min.set(value);
			}
			if (Util.max(max, value) == value) {
				max.set(value);
			}
			
			// for histogram
			double v = value.getRealDouble();
			final int bin = computeBin(v, k);
			histo[bin]++;
		}
	}

	private int computeBin(final double value, double k) {
		double v = value;
		if (v < histoMin) {
			v = histoMin;
		}
		if (v > histoMax) {
			v = histoMax;
		}
		final int bin = (int) (k * (v - histoMin));
		//final int bin = (int) ((BINS - 1) * (v - histMin) / (histMax - histMin));
		return bin;
	}
	
	@Override
	public boolean checkInput() {
		if (errorMessage.length() > 0) {
			return false;
		} else if (image == null) {
			errorMessage = "ScaleSpace: [Image<A> img] is null.";
			return false;
		} else {
			return true;
		}
	}

	@Override
	public long getProcessingTime() {
		return processingTime;
	}

	@Override
	public void setNumThreads() {
		this.numThreads = Runtime.getRuntime().availableProcessors();
	}

	@Override
	public void setNumThreads(final int numThreads) {
		this.numThreads = numThreads;
	}

	@Override
	public int getNumThreads() {
		return numThreads;
	}

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

}
