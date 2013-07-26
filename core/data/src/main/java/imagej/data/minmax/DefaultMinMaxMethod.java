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

package imagej.data.minmax;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.multithreading.Chunk;
import net.imglib2.multithreading.SimpleMultiThreading;
import net.imglib2.type.Type;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import org.scijava.app.StatusService;
import org.scijava.plugin.Plugin;
import org.scijava.thread.ThreadService;

/**
 * A {@link MinMaxMethod} implementation that forks
 * {@link net.imglib2.algorithm.stats.ComputeMinMax}.
 * 
 * @author Mark Hiner
 */
@Plugin(type = MinMaxMethod.class)
public class DefaultMinMaxMethod<T extends Type<T> & Comparable<T>> extends
	AbstractMinMaxMethod<T>
{

	// -- Constants --

	private final int MAX_UPDATES = 100;

	// -- Fields --

	// Number of positions processed
	private long progress = -1l;
	// Limit how many times to send status updates

	// Last reported percentage
	private int reported = -1;

	private long imageSize;
	private StatusService statusService;
	private ThreadService threadService;
	private IterableInterval<T> image;
	private T min;
	private T max;

	private String errorMessage = "";
	private int numThreads;
	private long processingTime;

	// -- ComputeMinMaxMethod API --

	@Override
	public void initialize(final Img<T> img, final T min, final T max) {
		initialize(img, min, max);
	}

	@Override
	public void initialize(final IterableInterval<T> interval, final T min,
		final T max)
	{
		image = interval;

		this.min = min;
		this.max = max;
		init();
	}

	@Override
	public void initialize(final RandomAccessibleInterval<T> interval,
		final T min, final T max)
	{
		initialize(interval, min, max);
	}

	@Override
	public void initialize(final Img<T> img) {
		initialize(img);
	}

	@Override
	public void initialize(final IterableInterval<T> interval) {
		image = interval;

		min = image.firstElement().createVariable();
		max = min.copy();
		init();
	}

	@Override
	public void initialize(final RandomAccessibleInterval<T> interval) {
		initialize(Views.iterable(interval));
	}

	@Override
	public T getMin() {
		return min;
	}

	@Override
	public T getMax() {
		return max;
	}

	@Override
	public boolean process() {
		final long startTime = System.currentTimeMillis();

		imageSize = image.size();

		report();

		final AtomicInteger ai = new AtomicInteger(0);
		final Thread[] threads = SimpleMultiThreading.newThreads(getNumThreads());

		final Vector<Chunk> threadChunks =
			SimpleMultiThreading.divideIntoChunks(imageSize, numThreads);
		final Vector<T> minValues = new Vector<T>();
		final Vector<T> maxValues = new Vector<T>();

		for (int ithread = 0; ithread < threads.length; ++ithread) {
			minValues.add(image.firstElement().createVariable());
			maxValues.add(image.firstElement().createVariable());

			threads[ithread] = threadService.newThread(new Runnable() {

				@Override
				public void run() {
					// Thread ID
					final int myNumber = ai.getAndIncrement();

					// get chunk of pixels to process
					final Chunk myChunk = threadChunks.get(myNumber);

					// compute min and max
					compute(myChunk.getStartPosition(), myChunk.getLoopSize(), minValues
						.get(myNumber), maxValues.get(myNumber));

				}
			});
		}

		SimpleMultiThreading.startAndJoin(threads);

		// compute overall min and max
		min.set(minValues.get(0));
		max.set(maxValues.get(0));

		for (int i = 0; i < threads.length; ++i) {
			T value = minValues.get(i);
			if (Util.min(min, value) == value) min.set(value);

			value = maxValues.get(i);
			if (Util.max(max, value) == value) max.set(value);
		}

		processingTime = System.currentTimeMillis() - startTime;

		statusService.showStatus("Computing min/max complete.");

		return true;
	}

	protected void compute(final long startPos, final long loopSize, final T min,
		final T max)
	{
		final Cursor<T> cursor = image.cursor();

		// init min and max
		cursor.fwd();

		min.set(cursor.get());
		max.set(cursor.get());

		cursor.reset();

		// move to the starting position of the current thread
		cursor.jumpFwd(startPos);

		// do as many pixels as wanted by this thread
		for (long j = 0; j < loopSize; ++j) {
			cursor.fwd();

			final T value = cursor.get();

			if (Util.min(min, value) == value) min.set(value);

			if (Util.max(max, value) == value) max.set(value);

			report();
		}
	}

	@Override
	public boolean checkInput() {
		if (errorMessage.length() > 0) {
			return false;
		}
		else if (image == null) {
			errorMessage = "ScaleSpace: [Image<A> img] is null.";
			return false;
		}
		else return true;
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

	// -- Helper Methods --

	private void init() {
		statusService = getContext().getService(StatusService.class);
		threadService = getContext().getService(ThreadService.class);
		setNumThreads();
		initialized = true;
	}

	// Reports the current progress
	private void report() {
		progress++;
		final double percentWork = ((double) progress / imageSize) * MAX_UPDATES;

		if (percentWork > reported) {
			synchronized (this) {
				// NB: check twice in case another thread has already reported the
				// status.
				// We do this to avoid executing a synchronized block for each pixel
				// analyzed.
				if (percentWork > reported) {
					reported++;
					statusService.showStatus(reported, MAX_UPDATES,
						"Computing min/max...");
				}
			}
		}
	}
}
