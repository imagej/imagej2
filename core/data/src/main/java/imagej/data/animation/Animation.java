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

package imagej.data.animation;

import imagej.data.display.ImageDisplay;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

/**
 * The Animation class takes care of running an animation along an axis.
 * Multiple animations can be running concurrently. Each animation runs in its
 * own thread.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
public class Animation implements Runnable {

	@Parameter
	private LogService log;

	private final ImageDisplay display;

	private boolean active;
	private Thread thread;

	private AxisType axis;
	private long first;
	private long last;
	private double fps = 8;
	private boolean backAndForth;

	private long increment = 1;
	private long delta = 1;
	private boolean isRelative = true;

	/** Creates an animation for the given {@link ImageDisplay}. */
	public Animation(final ImageDisplay display) {
		this.display = display;
		display.getContext().inject(this);

		// assign default animation options
		if (display.numDimensions() > 2) {
			if (display.dimensionIndex(Axes.TIME) >= 0) {
				// animation over time is preferred by default
				axis = Axes.TIME;
			}
			else if (display.dimensionIndex(Axes.Z) >= 0) {
				// failing that, animation over Z is OK
				axis = Axes.Z;
			}
			else {
				// no preferred animation axes; use first non-spatial axis
				axis = display.axis(2).type();
			}
			final int axisIndex = display.dimensionIndex(axis);
			last = display.getExtents().dimension(axisIndex) - 1;
		}
	}

	// -- Animation methods --

	/** Starts the animation. */
	public void start() {
		if (axis == null) return; // no axis over which to animate
		active = true;
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}

	/** Stops the animation. */
	public void stop() {
		active = false;
		if (thread != null) {
			try {
				thread.join();
			}
			catch (final InterruptedException exc) {
				log.error("Error stopping animation", exc);
			}
			thread = null;
		}
	}

	/** Returns true if the animation is currently running. */
	public boolean isActive() {
		return active;
	}

	/** Gets the display being animated. */
	public ImageDisplay getDisplay() {
		return display;
	}

	/** Gets the axis over which to animate. */
	public AxisType getAxis() {
		return axis;
	}

	public void setAxis(final AxisType axis) {
		this.axis = axis;
		clampPosition();
	}

	public double getFPS() {
		return fps;
	}

	public void setFPS(final double fps) {
		this.fps = fps;
	}

	public long getFirst() {
		return first;
	}

	public void setFirst(final long first) {
		this.first = first;
		clampPosition();
	}

	public long getLast() {
		return last;
	}

	public void setLast(final long last) {
		this.last = last;
		clampPosition();
	}

	public boolean isBackAndForth() {
		return backAndForth;
	}

	public void setBackAndForth(final boolean backAndForth) {
		this.backAndForth = backAndForth;
	}

	// -- Runnable methods --

	@Override
	public void run() {
		while (active) {
			updatePosition();
			try {
				Thread.sleep((long) (1000 / fps));
			}
			catch (final InterruptedException e) {
				// do nothing
			}
		}
	}

	// -- Helper methods --

	private synchronized void updatePosition() {
		long currPos = display.getLongPosition(axis);

		// reached right end
		if (increment > 0 && currPos == last) {
			if (!backAndForth) {
				isRelative = false;
				delta = first;
				currPos = first;
			}
			else {
				increment = -increment;
				isRelative = true;
				delta = -1;
				currPos--;
			}
		}
		// reached left end
		else if (increment < 0 && currPos == first) {
			if (!backAndForth) {
				isRelative = false;
				delta = last;
				currPos = last;
			}
			else {
				increment = -increment;
				isRelative = true;
				delta = +1;
				currPos++;
			}
		}
		else { // somewhere in the middle
			isRelative = true;
			if (increment > 0) {
				delta = +1;
				currPos++;
			}
			else { // increment < 0
				delta = -1;
				currPos--;
			}
		}

		final long pos =
			isRelative ? display.getLongPosition(axis) + delta : delta;
		display.setPosition(pos, axis);
	}

	/** Ensures the position of the relevant axis is within the legal range. */
	private void clampPosition() {
		if (axis == null) return;
		final long pos = display.getLongPosition(axis);
		if (pos < first || pos > last) {
			display.setPosition(first, axis);
		}
	}

}
