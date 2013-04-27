/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.imglib2.Cursor;
import net.imglib2.type.Type;

/**
 *
 * @author Aivar Grislis
 */
public class CompoundCursor {
	private final int dimensions;
	private final long[] dimension;
	private final long[] position;
	private final Map<String, ComponentCursor> map;
	private final PositionIterator positionIterator;
	
	private CompoundCursor(long[] dimension) {
		this.dimensions = dimension.length;
		this.dimension = dimension;
		position = new long[dimension.length];
		map = new HashMap<String, ComponentCursor>();
		positionIterator = new PositionIterator(dimension);
	}
	
	public void addElement(String name, ComponentCursor element) { //TODO ARG nomenclature
		// check for consistency
		if (Arrays.equals(dimension, null /* element.getDimension() */)) {
			map.put(name, element);
		}
		else {
			throw new RuntimeException("Inconsistent dimensions");
		}
	}
	
	public boolean hasNext() {
		return positionIterator.hasNext();
	}
	
	public void fwd() {
		long[] position = positionIterator.fwd();
		
		// move all cursors forward to same position
		for (ComponentCursor element : map.values()) {
			// move to new position and update
			element.localize(position);
		}
	}
	
	public ComponentCursor getElement(String name) {
		return map.get(name);
	}
	
	/**
	 * Gets current value at cursor.
	 * 
	 * @param name
	 * @return Object
	 */
	public Object getValue(String name) {
		Object returnValue = null;
		ComponentCursor element = getElement(name);
		if (null != element) {
			returnValue = element.getValue();
		}
		return returnValue;
	}
	
	private interface ComponentCursor {
		
		/**
		 * Gets value at cursor.
		 * 
		 * @return 
		 */
		public Object getValue();
		
		/**
		 * Gets associated cursor.
		 * 
		 * @return 
		 */
		public Cursor getCursor();
		
		/**
		 * Move to given position.
		 * 
		 * @param position 
		 */
		public void localize(long[] position);
		
		/**
		 * Update value at current cursor position.
		 */
		public void update();
	}
	
	private class TypeElement<T extends Type<T>> implements ComponentCursor {
		private final String name;
		private Type type;
		private final Cursor<Type> cursor;
		
		public TypeElement(String name, Type type, Cursor<Type> cursor) {
			this.name = name;
			this.type = type;
			this.cursor = cursor;
		}

		@Override
		public Object getValue() {
			return type;
		}

		@Override
		public Cursor getCursor() {
			return cursor;
		}

		@Override
		public void localize(long[] position) {
			cursor.localize(position);
			update();
		}

		@Override
		public void update() {
			type = cursor.get();
		}
	}
	

	/**
	 * This element's value consists of a Type array.
	 */
	private class TypeArrayElement<T extends Type<T>> implements ComponentCursor {
		private final String name;
		private int dimIndex;
		private boolean input;
		private boolean output;
		private T[] value;
		private final Cursor<T> cursor;
		private long[] expandedPosition;
		private long[] previousPosition;
		
		public TypeArrayElement(String name, int dimIndex, int dimSize, boolean input, boolean output, Cursor<T> cursor) {
			this.name = name;
			this.dimIndex = dimIndex;
			this.input = input;
			this.output = output;
			this.cursor = cursor;
			value = (T[]) new Object[dimSize];
		}
		
		@Override
		public T[] getValue() {
			return value;
		}

		@Override
		public Cursor getCursor() {
			return cursor;
		}

		@Override
		public void localize(long[] position) {
			// save current value array if needed
			if (output && null != previousPosition) {
				for (int i = 0; i < value.length; ++i) {
					previousPosition[dimIndex] = i;
					cursor.localize(previousPosition);
					cursor.get().set(value[i]);
				}
			}
			
			// lazy instantiation
			if (null == expandedPosition) {
				int size = position.length + 1;
				expandedPosition = new long[size];
				previousPosition = new long[size];
			}
			
			// make room for added dimension
			expandPosition(expandedPosition, position, dimIndex);
			
			// keep a copy of this expanded position for fwd time
			if (output) {
				System.arraycopy(expandedPosition, 0, previousPosition, 0,
						previousPosition.length);
			}
			
			// get new value array if needed
			if (input) {
				// get new value
				for (int i = 0; i < value.length; ++i) {
					expandedPosition[dimIndex] = i;
					cursor.localize(expandedPosition);
					value[i] = cursor.get().createVariable();
				}
			}
		}
		
		@Override
		public void update() {
			//TODO ARG HACK
			// update is only used for the master, non-array element
			throw new RuntimeException("Method not available");
		}

		/**
		 * Expands the cursor position array at the dimension index.
		 * 
		 * @param expandedPosition
		 * @param position
		 * @param dimIndex
		 * @return 
		 */
		private void expandPosition(long[] expandedPosition, long[] position,
				int dimIndex)
		{
			for (int i = 0; i < dimIndex; ++i) {
				expandedPosition[i] = position[i];
			}
			expandedPosition[dimIndex] = 0;
			for (int i = dimIndex; i < position.length; ++i) {
				expandedPosition[i + 1] = position[i];
			}
		}
	}

	/**
	 * Class used to iterate through all possible positions.
	 */
	private class PositionIterator {
		final long[] dimension;
		long[] position;

		/**
		 * Creates iterator for given dimensions.
		 * 
		 * @param dimension 
		 */
		public PositionIterator(long[] dimension) {
			this.dimension = dimension;
			
			// initialize position before first increment
			position = new long[dimension.length];
			position[0] = -1;
			for (int i = 1; i < position.length; ++i) {
				position[i] = 0;
			}
			
		}

		/**
		 * Is there a next position?
		 * 
		 * @return 
		 */
		public boolean hasNext() {
			boolean returnValue = false;
			if (null != position) {
				for (int i = 0; i < position.length; ++i) {
					if (position[i] != dimension[i] - 1) {
						returnValue = true;
						break;
					}
				}
			}
			return returnValue;
		}

		/**
		 * Get next position.
		 * 
		 * @return new position or null at end
		 */
		public long[] fwd() {
			if (null != position) {
				int i = 0;
				while (i < position.length) {
					// is incremented value within dimension limit?
					if (++position[i] < dimension[i]) {
						// yes, got new position
						break;
					}
					// no, set back to zero and increment fwd dimension
					position[i++] = 0;
				}
				// done with iteration
				position = null;
			}
			return position;
		}
	}
}
