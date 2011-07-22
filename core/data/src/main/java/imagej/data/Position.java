//
// Position.java
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

import net.imglib2.Iterator;
import net.imglib2.Localizable;
import net.imglib2.Positionable;

/**
 * A {@link Position} is used to move within a given reference
 * {@link Extents} object.
 * 
 * @author Barry DeZonia
 *
 */
public class Position implements Localizable, Positionable, Iterator {
	
	private final Extents parentSpace;
	private final long[] position;
	private boolean isInvalid;

	/**
	 * Constructor - takes an {@link Extents} object that represents the parent
	 * space to iterate within.
	 */
	public Position(Extents parentSpace) {
		this.parentSpace = parentSpace;
		this.position = new long[parentSpace.numDimensions()];
	  // Imglib convention - start out of bounds
		reset();
	}

	/** Constructs a position that is a copy of the given position. */
	public Position(Position pos) {
		this.parentSpace = pos.getExtents();
		this.position = new long[pos.numDimensions()];
		for (int i = 0; i < position.length; i++) {
			position[i] = pos.position[i];
		}
	}

	/**
	 * Returns the parent space {@link Extents} associated with this
	 * {@link Position}.
	 */
	public Extents getExtents() {
		return parentSpace;
	}

	/**
	 * Returns the number of dimensions within the {@link Position}.
	 */
	@Override
	public int numDimensions() {
		return parentSpace.numDimensions();
	}

	/**
	 * Returns the dimension of the {@link Position}'s parent {@link Extents} at
	 * a given i.
	 */
	public long dimension(int i) {
		return parentSpace.dimension(i);
	}

	/**
	 * Returns true if position can be moved forward (i.e. position is not in the
	 * last position).
	 */
	@Override
	public boolean hasNext() {
		if (this.isInvalid && position.length > 0)
			return true;
		for (int i = 0; i < position.length; i++)
			if (position[i] < parentSpace.max(i))
				return true;
		return false;
	}
	
	/**
	 * Returns true if position can be moved backward (i.e. position is not in
	 * the first position).
	 */
	public boolean hasPrev() {
		if (this.isInvalid && position.length > 0)
			return true;
		for (int i = 0; i < position.length; i++)
			if (position[i] > parentSpace.min(i))
				return true;
		return false;
	}

	/**
	 * Resets the {@link Position} for forward or backward traversal.
	 */
	@Override
	public void reset() {
		this.isInvalid = true;
		for (int i = 0; i < position.length; i++)
			position[i] = Long.MIN_VALUE;
	}

	/**
	 * Sets the {@link Position} to it's first state (all dimension positions
	 * to min values)
	 */
	public void first() {
		for (int i = 0; i < position.length; i++)
			position[i] = parentSpace.min(i);
		this.isInvalid = false;
	}
	
	/**
	 * Sets the {@link Position} to it's last state (all dimension positions
	 * to max-1)
	 */
	public void last() {
		for (int i = 0; i < position.length; i++)
			position[i] = parentSpace.max(i);
		this.isInvalid = false;
	}

	/**
	 * Moves the {@link Position} forward by one step. Increments the dimension
	 * positions from left to right. Throws an exception if fwd() called from
	 * last position.
	 */
	@Override
	public void fwd() {
		if (this.isInvalid) {
			first();
			return;
		}
		for (int i = 0; i < position.length; i++) {
			position[i]++;
			if (position[i] <= parentSpace.max(i))
				return;
			position[i] = parentSpace.min(i);
		}
		last();  // reset position to where it was
		throw new IllegalArgumentException("cannot move last position forward");
	}
	
	/**
	 * Moves the {@link Position} backward by one step. Decrements the dimension
	 * positions from left to right. Throws an exception if back() called from
	 * first position.
	 */
	public void bck() {
		if (this.isInvalid) {
			last();
			return;
		}
		for (int i = 0; i < position.length; i++) {
			position[i]--;
			if (position[i] >= parentSpace.min(i))
				return;
			position[i] = parentSpace.max(i);
		}
		first();  // reset position to where it was
		throw new IllegalArgumentException("cannot move first position backward");
	}

	/**
	 * Moves the {@link Position} forward one step in specified dimension.
	 * Throws an exception if specified move would take position outside
	 * parent {@link Extents}.
	 */
	@Override
	public void fwd(int d) {
		if (this.isInvalid)
			throw new IllegalArgumentException(
				"Cannot move position : it is uninitialized");
		long newValue = position[d] + 1;
		if (newValue > parentSpace.max(d))
			throw new IllegalArgumentException(
				"cannot move specified dimension forward");
		position[d]++;
	}

	/**
	 * Moves the {@link Position} backward one step in specified dimension.
	 * Throws an exception if specified move would take position outside
	 * parent {@link Extents}.
	 */
	@Override
	public void bck(int d) {
		if (this.isInvalid)
			throw new IllegalArgumentException(
				"Cannot move position : it is uninitialized");
		long newValue = position[d] - 1;
		if (newValue < parentSpace.min(d))
			throw new IllegalArgumentException(
				"cannot move specified dimension back");
		position[d]--;
	}

	/**
	 * Moves the {@link Position} forward the given number of steps.
	 */
	@Override
	public void jumpFwd(long steps) {
		
		// brain dead method
		
		//for (int i = 0; i < steps; i++)
		//	fwd();
		
		// method that could be faster if steps larger
		
		long stepsLeft = steps;
		if (this.isInvalid) {
			first();
			stepsLeft--;
		}
		final long currPos = getIndex();
		final long newPos = currPos + stepsLeft;
		setIndex(newPos);
	}

	/**
	 * Moves a given dimension of the {@link Position} by a given delta. Throws
	 * an exception if delta would move {@link Position} outside it's parent
	 * {@link Extents}.
	 */
	@Override
	public void move(long delta, int dim) {
		if (this.isInvalid)
			throw new IllegalArgumentException(
				"Cannot move position : it is uninitialized");
		final long newValue = position[dim] + delta;
		if ((newValue < parentSpace.min(dim)) || (newValue > parentSpace.max(dim)))
			throw new IllegalArgumentException(
				"specified move would take position outside defined extents");
		position[dim] = newValue;
	}

	/**
	 * Moves all dimensions of the {@link Position} by given deltas. Throws an
	 * exception if any delta would move {@link Position} outside it's parent
	 * {@link Extents}.
	 */
	@Override
	public void move(long[] deltas) {
		for (int i = 0; i < position.length; i++)
			move(deltas[i], i);
	}
	
	/**
	 * Moves a given dimension of the {@link Position} by a given delta. Throws
	 * an exception if delta would move {@link Position} outside it's parent
	 * {@link Extents}.
	 */
	@Override
	public void move(int distance, int d) {
		move((long)distance, d);
	}

	/**
	 * Moves all dimensions of the {@link Position} by given deltas. Throws an
	 * exception if any delta would move {@link Position} outside it's parent
	 * {@link Extents}.
	 */
	@Override
	public void move(int[] distance) {
		for (int i = 0; i < distance.length; i++)
			move((long)distance[i], i);
	}

	/**
	 * Moves all dimensions of the {@link Position} by given deltas. Throws an
	 * exception if any delta would move {@link Position} outside it's parent
	 * {@link Extents}. The delta is encoded as a relative Localizable vector.
	 */
	@Override
	public void move(Localizable localizable) {
		for (int i = 0; i < position.length; i++)
			move(localizable.getLongPosition(i), i);
	}

	/**
	 * Sets the value of the {@link Position} for a given dimension. Throws an
	 * exception if the given value is outside the bounds of the parent
	 * {@link Extents}.
	 */
	@Override
	public void setPosition(long value, int dim) {
		if ((value < parentSpace.min(dim)) || (value > parentSpace.max(dim))) {
			throw new IllegalArgumentException(
				"specified value would take position outside defined extents");
		}
		position[dim] = value;
		if (this.isInvalid)
			this.isInvalid = isInvalid();
	}

	/**
	 * Sets the values of the {@link Position} for all dimensions. Throws an
	 * exception if any given value is outside the bounds of the parent
	 * {@link Extents}.
	 */
	@Override
	public void setPosition(long[] value) {
		for (int i = 0; i < position.length; i++)
			setPosition(value[i], i);
	}

	/**
	 * Sets the values of the {@link Position} for all dimensions. Throws an
	 * exception if any given value is outside the bounds of the parent
	 * {@link Extents}.
	 */
	@Override
	public void setPosition(int[] position) {
		for (int i = 0; i < position.length; i++)
			setPosition((long)position[i], i);
	}

	/**
	 * Sets the value of the {@link Position} for a given dimension. Throws an
	 * exception if the given value is outside the bounds of the parent
	 * {@link Extents}.
	 */
	@Override
	public void setPosition(int position, int d) {
		setPosition((long)position, d);
	}
	
	/**
	 * Sets the values of the {@link Position} for all dimensions. Throws an
	 * exception if any given value is outside the bounds of the parent
	 * {@link Extents}. The position is encoded as an absolute Localizable
	 * vector.
	 */
	@Override
	public void setPosition(Localizable localizable) {
		for (int i = 0; i < position.length; i++)
			setPosition(localizable.getLongPosition(i), i);
	}

	/**
	 * Sets the {@link Position} from a given long index. The index ranges from
	 * 0 to extents.numElements()-1. Throws an exception if index out of range.
	 */
	public void setIndex(long index) {
		if ((index < 0) || (index >= parentSpace.numElements()))
			throw new IllegalArgumentException(
				"specified index value is outside bounds of extents");
		long offset = 1;
		long r = index;
		for (int i = 0; i < position.length; i++) {
			final long offset1 = offset * dimension(i);
			final long q = i < position.length - 1 ? r % offset1 : r;
			position[i] = (q / offset) + parentSpace.min(i);
			r -= q;
			offset = offset1;
		}
		this.isInvalid = false;
	}
	
	/**
	 * Gets the long index from the current {@link Position}. The index ranges
	 * from 0 to extents.numElements()-1.
	 */
	public long getIndex() {
		if (this.isInvalid && position.length > 0)
			throw new IllegalArgumentException(
				"Cannot get index value : position is uninitialized");
		long offset = 1;
		long index1D = 0;
		for (int i = 0; i < position.length; i++) {
			index1D += offset * (position[i]-parentSpace.min(i));
			offset *= dimension(i);
		}
		return index1D;
	}

	/**
	 * Populates a given int[] with the current {@link Position}'s coordinates 
	 */
	@Override
	public void localize(int[] pos) {
		if (this.isInvalid && position.length > 0)
			throw new IllegalArgumentException(
				"Cannot localize : position is uninitialized");
		for (int i = 0; i < position.length; i++)
			pos[i] = (int) position[i];
	}

	/**
	 * Populates a given long[] with the current {@link Position}'s coordinates 
	 */
	@Override
	public void localize(long[] pos) {
		if (this.isInvalid && position.length > 0)
			throw new IllegalArgumentException(
				"Cannot localize : position is uninitialized");
		for (int i = 0; i < position.length; i++)
			pos[i] = position[i];
	}

	/**
	 * Populates a given float[] with the current {@link Position}'s coordinates 
	 */
	@Override
	public void localize(float[] pos) {
		if (this.isInvalid && position.length > 0)
			throw new IllegalArgumentException(
				"Cannot localize : position is uninitialized");
		for (int i = 0; i < position.length; i++)
			pos[i] = position[i];
	}

	/**
	 * Populates a given double[] with the current {@link Position}'s coordinates 
	 */
	@Override
	public void localize(double[] pos) {
		if (this.isInvalid && position.length > 0)
			throw new IllegalArgumentException(
				"Cannot localize : position is uninitialized");
		for (int i = 0; i < position.length; i++)
			pos[i] = position[i];
	}

	/**
	 * Gets the current {@link Position}'s i'th coordinate as an int 
	 */
	@Override
	public int getIntPosition(int d) {
		if (this.isInvalid)
			throw new IllegalArgumentException(
				"Cannot get position : position is uninitialized");
		return (int) position[d];
	}

	/**
	 * Gets the current {@link Position}'s i'th coordinate as a long 
	 */
	@Override
	public long getLongPosition(int d) {
		if (this.isInvalid)
			throw new IllegalArgumentException(
				"Cannot get position : position is uninitialized");
		return position[d];
	}

	/**
	 * Gets the current {@link Position}'s i'th coordinate as a float 
	 */
	@Override
	public float getFloatPosition(int d) {
		if (this.isInvalid)
			throw new IllegalArgumentException(
				"Cannot get position : position is uninitialized");
		return position[d];
	}

	/**
	 * Gets the current {@link Position}'s i'th coordinate as a double 
	 */
	@Override
	public double getDoublePosition(int d) {
		if (this.isInvalid)
			throw new IllegalArgumentException(
				"Cannot get position : position is uninitialized");
		return position[d];
	}

	// -- private helpers --
	
	private boolean isInvalid() {
		for (int i = 0; i < position.length; i++)
			if ((position[i] < parentSpace.min(i)) || (position[i] > parentSpace.max(i)))
				return true;
		return false;
	}
}
