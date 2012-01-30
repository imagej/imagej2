
package imagej.updater.util;

import java.util.Iterator;

public class OneItemIterable<T> implements Iterable<T> {

	T justOne;

	public OneItemIterable(final T justOne) {
		this.justOne = justOne;
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {

			boolean isFirst = true;

			@Override
			public boolean hasNext() {
				return isFirst;
			}

			@Override
			public T next() {
				isFirst = false;
				return justOne;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
