package imagej.display;

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
 * 
 * @author Stephan Preibisch
 * @author Grant Harris
 */
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.multithreading.Chunk;
import net.imglib2.multithreading.SimpleMultiThreading;
import net.imglib2.type.Type;
import net.imglib2.util.Util;

/*
 * A temporary version of GetImgMinMax which encloses its own interfaces.
 */
public class GetImgMinMax<T extends Type<T> & Comparable<T>> implements Algorithm, MultiThreaded, Benchmark {

	final Img<T> image;
	final T min, max;
	String errorMessage = "";
	int numThreads;
	long processingTime;

	public GetImgMinMax(final Img<T> image) {
		setNumThreads();
		this.image = image;
		this.min = image.firstElement().createVariable();
		this.max = this.min.copy();
	}

	public T getMin() {
		return min;
	}

	public T getMax() {
		return max;
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

		for (int ithread = 0; ithread < threads.length; ++ithread) {
			minValues.add(image.firstElement().createVariable());
			maxValues.add(image.firstElement().createVariable());
			threads[ithread] = new Thread(new Runnable() {
				@Override
				public void run() {
					// Thread ID
					final int myNumber = ai.getAndIncrement();
					// get chunk of pixels to process
					final Chunk myChunk = threadChunks.get(myNumber);
					// compute min and max
					compute(myChunk.getStartPosition(), myChunk.getLoopSize(), minValues.get(myNumber), maxValues.get(myNumber));
				}
			});
		}

		SimpleMultiThreading.startAndJoin(threads);

		// compute overall min and max
		min.set(minValues.get(0));
		max.set(maxValues.get(0));
		for (int i = 0; i < threads.length; ++i) {
			T value = minValues.get(i);
			if (Util.min(min, value) == value) {
				min.set(value);
			}
			value = maxValues.get(i);
			if (Util.max(max, value) == value) {
				max.set(value);
			}
		}
		processingTime = System.currentTimeMillis() - startTime;
		return true;
	}

	protected void compute(final long startPos, final long loopSize, final T omin, final T omax) {
		final Cursor<T> cursor = image.cursor();
		// init min and max
		cursor.fwd();
		omin.set(cursor.get());
		omax.set(cursor.get());
		cursor.reset();
		// move to the starting position of the current thread
		cursor.jumpFwd(startPos);
		// do as many pixels as wanted by this thread
		for (long j = 0; j < loopSize; ++j) {
			cursor.fwd();
			final T value = cursor.get();
			if (Util.min(omin, value) == value) {
				omin.set(value);
			}
			if (Util.max(omax, value) == value) {
				omax.set(value);
			}
		}
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

interface Algorithm {

	public boolean checkInput();

	public boolean process();

	public String getErrorMessage();

}

interface Benchmark {

	public long getProcessingTime();

}

interface MultiThreaded {

	/**
	 * Sets the number of threads to the amount of processors available
	 */
	public void setNumThreads();

	/**
	 * Sets the number of threads 
	 * @param numThreads - number of threads to use
	 */
	public void setNumThreads(final int numThreads);

	/**
	 * The number of threads used by the algorithm
	 * @return - the number of threads
	 */
	public int getNumThreads();

}
