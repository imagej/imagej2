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

package imagej.util;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * A class that provides more thorough support for iteration. Any
 * {@link Enumeration}, {@link Iterator} or {@link Iterable} object can be
 * provided to the constructor, and the resultant {@code IteratorPlus} will
 * provide all three access mechanisms. In the case of {@link #iterator()} it
 * simply returns {@code this}, for more convenient usage with <a
 * href="http://docs.oracle.com/javase/1.5.0/docs/guide/language/foreach.html"
 * >for-each loops</a>. Note, however, that because of this fact, multiple calls
 * to {#iterator()} will produce the same {@link Iterator} every time (in fact,
 * the {@link IteratorPlus} object itself).
 * <p>
 * For example, let's say you have an {@code Enumeration<String>} and you want
 * to loop over it with the for-each syntax. You could write:
 * </p>
 * {@code
 * final Enumeration<String> en = ...;
 * for (final String s : new IteratorPlus(en))
 *   // do something with the string
 * }
 * <p>
 * The same technique works with {@link Iterator}.
 * </p>
 * 
 * @author Curtis Rueden
 */
public class IteratorPlus<E> implements Enumeration<E>, Iterator<E>,
	Iterable<E>
{

	/** The backing {@link Iterator}. */
	private final Iterator<E> iterator;

	// -- Constructors --

	public IteratorPlus(final Iterable<E> iterable) {
		this(iterable.iterator());
	}

	public IteratorPlus(final Enumeration<E> enumeration) {
		this(new EnumerationIterator<E>(enumeration));
	}

	public IteratorPlus(final Iterator<E> iterator) {
		this.iterator = iterator;
	}

	// -- Enumeration methods --

	@Override
	public boolean hasMoreElements() {
		return hasNext();
	}

	@Override
	public E nextElement() {
		return next();
	}

	// -- Iterator methods --

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public E next() {
		return iterator.next();
	}

	@Override
	public void remove() {
		iterator.remove();
	}

	// -- Iterable methods --

	@Override
	public Iterator<E> iterator() {
		return this;
	}

	// -- Helper classes --

	/**
	 * A helper class for translating an {@link Enumeration} into an
	 * {@link Iterator}.
	 */
	private static class EnumerationIterator<E> implements Iterator<E> {

		private final Enumeration<E> enumeration;

		private EnumerationIterator(final Enumeration<E> enumeration) {
			this.enumeration = enumeration;
		}

		@Override
		public boolean hasNext() {
			return enumeration.hasMoreElements();
		}

		@Override
		public E next() {
			return enumeration.nextElement();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

}
