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


/**
 * A {@link Position} is used to iterate within a given reference
 * {@link Extents} object.
 * 
 * @author Barry DeZonia
 *
 */
public class Position {
	
	private final Extents parentSpace;
	private final long[] position;

	/**
	 * Constructor - takes an {@link Extents} object that represents the parent
	 * space to iterate within.
	 */
	public Position(Extents parentSpace) {
		this.parentSpace = parentSpace;
		this.position = new long[parentSpace.numDimensions()];
	}

	/**
	 * Returns the parent space {@link Extents} associated with this
	 * {@link Position}.
	 */
	public Extents getExtents() {
		return parentSpace;
	}

	/**
	 * Sets the {@link Position} to it's first state (all dimension positions
	 * to 0)
	 */
	public void first() {
		for (int i = 0; i < position.length; i++)
			position[i] = 0;
	}
	
	/**
	 * Sets the {@link Position} to it's last state (all dimension positions
	 * to max-1)
	 */
	public void last() {
		for (int i = 0; i < position.length; i++)
			position[i] = dimension(i)-1;
	}

	/**
	 * Moves the {@link Position} forward by one step. Increments the dimension
	 * positions from left to right. Throws an exception if fwd() called from
	 * last position.
	 */
	public void fwd() {
		for (int i = 0; i < position.length; i++) {
			position[i]++;
			if (position[i] < dimension(i))
				return;
			position[i] = 0;
		}
		throw new IllegalArgumentException("cannot move last position forward");
	}
	
	/**
	 * Moves the {@link Position} backward by one step. Decrements the dimension
	 * positions from left to right. Throws an exception if back() called from
	 * first position.
	 */
	public void back() {
		for (int i = 0; i < position.length; i++) {
			position[i]--;
			if (position[i] >= 0)
				return;
			position[i] = parentSpace.dimension(i)-1;
		}
		throw new IllegalArgumentException("cannot move first position backward");
	}

	/**
	 * Moves a given dimension of the {@link Position} by a given delta. Throws
	 * an exception if delta would move {@link Position} outside it's parent
	 * {@link Extents}.
	 */
	public void move(long delta, int dim) {
		final long newValue = position[dim] + delta;
		if ((newValue < 0) || (newValue >= dimension(dim)))
			throw new IllegalArgumentException(
				"specified move would take position outside defined extents");
		position[dim] = newValue;
	}

	/**
	 * Moves all dimensions of the {@link Position} by given deltas. Throws an
	 * exception if any delta would move {@link Position} outside it's parent
	 * {@link Extents}.
	 */
	public void move(long[] deltas) {
		for (int i = 0; i < position.length; i++)
			move(deltas[i], i);
	}
	
	/**
	 * Gets the value of the {@link Position} for a given dimension.
	 */
	public long get(int dim) {
		return position[dim];
	}

	/**
	 * Gets the values of the {@link Position} and places them in a given long[].
	 */
	public void get(long[] outputPos) {
		for (int i = 0; i < position.length; i++)
			outputPos[i] = get(i);
	}

	/**
	 * Sets the value of the {@link Position} for a given dimension. Throws an
	 * exception if the given value is outside the bounds of the parent
	 * {@link Extents}.
	 */
	public void set(long value, int dim) {
		if ((value < 0) || (value >= dimension(dim)))
			throw new IllegalArgumentException(
				"specified value would take position outside defined extents");
		position[dim] = value;
	}

	/**
	 * Sets the values of the {@link Position} for all dimensions. Throws an
	 * exception if any given value is outside the bounds of the parent
	 * {@link Extents}.
	 */
	public void set(long[] value) {
		for (int i = 0; i < position.length; i++)
			set(value[i], i);
	}

	/**
	 * Returns the number of dimensions within the {@link Position}.
	 */
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
			position[i] = q / offset;
			r -= q;
			offset = offset1;
		}
	}
	
	/**
	 * Gets the long index from the current {@link Position}. The index ranges
	 * from 0 to extents.numElements()-1.
	 */
	public long getIndex() {
		long offset = 1;
		long index1D = 0;
		for (int i = 0; i < position.length; i++) {
			index1D += offset * position[i];
			offset *= dimension(i);
		}
		return index1D;
	}
}
