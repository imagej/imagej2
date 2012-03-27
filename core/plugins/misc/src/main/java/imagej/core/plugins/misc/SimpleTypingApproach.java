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

package imagej.core.plugins.misc;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.complex.ComplexDoubleType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

/**
 * This class shows a possible refactoring that could be made to handle
 * stronger typing as needed. It also shows support for complex and real images.
 * Finally it implements an inversion method that shows how a user might access
 * the API in a simple fashion.
 * 
 * @author Barry DeZonia
 *
 */
public class SimpleTypingApproach {

	/**
	 * A container of complex typed image data.
	 */
	private static class ComplexImage<T extends ComplexType<T>>
	{
		final Img<T> img;
		
		public ComplexImage(Img<T> img) {
			this.img = img;
		}

		public Cursor<T> cursor() {
			return img.cursor();
		}

		// in general one should not assume that a Img<SubClassOfA> is a Img<A>.
		//   But I think we can safely cast here since ComplexType is only an
		//   interface and not an actual class. all implementations of complex
		//   numbers implement the interface and should have the correct signatures.
		// note this method is useful if an external user has a ComplexImage<?>
		//   rather than a ComplexImage<T>.
		@SuppressWarnings({"unchecked"})
		public Cursor<ComplexType<?>> complexCursor() {
			return (Cursor<ComplexType<?>>) ((Cursor)cursor());
		}
	}
	

	/**
	 * A container of real typed image data.
	 */
	private static class RealImage<T extends RealType<T>>
		extends ComplexImage<T>
	{
		public RealImage(Img<T> img) {
			super(img);
		}

		// in general one should not assume that a Img<SubClassOfA> is a Img<A>.
		//   But I think we can safely cast here since RealType is only an
		//   interface and not an actual class. all implementations of real
		//   numbers implement the interface and should have the correct signatures.
		// note this method is useful if an external user has a RealImage<?>
		//   rather than a RealImage<T>.
		@SuppressWarnings("unchecked")
		public Cursor<RealType<?>> realCursor() {
			return (Cursor<RealType<?>>) ((Cursor)cursor());
		}
	}

	// NB - it is worth noting we can write algorithms that work on ComplexImages
	// or RealImages. Due to inheritance a RealImage can be passed to any
	// algorithm that works on ComplexImages while vice versa is not the case.
	
	/**
	 * API examples
	 */
	@SuppressWarnings("unused")
	private static void apiExample() {
	
		Img<UnsignedByteType> bytes = null;
		
		RealImage<UnsignedByteType> rImage = new RealImage<UnsignedByteType>(bytes);

		Cursor<UnsignedByteType> c1 = rImage.cursor();
		while (c1.hasNext()) {
			UnsignedByteType b = c1.next();
			UnsignedByteType x = new UnsignedByteType();
			b.set(x);
			b.set(1);
			b.setInteger(100);
			b.setOne();
			b.setReal(10);
			b.setZero();
			b.get();
			b.getInteger();
			b.getRealDouble();
		}
		
		Cursor<RealType<?>> c2 = rImage.realCursor();
		while (c2.hasNext()) {
			RealType<?> r = c2.next();
			r.setOne();
			r.setReal(10);
			r.setZero();
			r.getRealDouble();
		}
		
		Cursor<ComplexType<?>> c3 = rImage.complexCursor();
		while (c3.hasNext()) {
			ComplexType<?> c = c3.next();
			c.setComplexNumber(5.3, 17.8);
			c.setImaginary(4.3);
			c.setOne();
			c.setReal(6.7);
			c.setZero();
			c.getImaginaryDouble();
			c.getRealDouble();
			c.getPhaseDouble();
			c.getPowerDouble();
		}

		Img<ComplexDoubleType> complexes = null;
		
		ComplexImage<ComplexDoubleType> cImage =
				new ComplexImage<ComplexDoubleType>(complexes);

		Cursor<ComplexDoubleType> c4 = cImage.cursor();
		while (c4.hasNext()) {
			ComplexDoubleType c = c4.next();
			ComplexDoubleType x = new ComplexDoubleType();
			c.set(x);
			c.set(1.4, 8.3);
			c.setComplexNumber(5.3, 17.8);
			c.setImaginary(4.3);
			c.setOne();
			c.setReal(6.7);
			c.setZero();
			c.getImaginaryDouble();
			c.getRealDouble();
			c.getPhaseDouble();
			c.getPowerDouble();
		}
		
		Cursor<ComplexType<?>> c5 = cImage.complexCursor();
		while (c5.hasNext()) {
			ComplexType<?> c = c5.next();
			c.setComplexNumber(5.3, 17.8);
			c.setImaginary(4.3);
			c.setOne();
			c.setReal(6.7);
			c.setZero();
			c.getImaginaryDouble();
			c.getRealDouble();
			c.getPhaseDouble();
			c.getPowerDouble();
		}

	}

	/**
	 * A realistic example on how to invert an image
	 */
	@SuppressWarnings("unused")
	private static void inversionExample() {
		Img<UnsignedByteType> img = null;
		RealImage<UnsignedByteType> rImage = new RealImage<UnsignedByteType>(img);

		// strongly typed version
		Cursor<UnsignedByteType> cursor = rImage.cursor();
		int imin = (int) cursor.get().getMinValue();
		int imax = (int) cursor.get().getMaxValue();
		UnsignedByteType b;
		while (cursor.hasNext()) {
			b = cursor.next();
			int invertedVal = imax - (b.get() - imin);
			b.set(invertedVal);
		}
		
		// weakly typed version
		Cursor<RealType<?>> rCursor = rImage.realCursor();
		double rmin = rCursor.get().getMinValue();
		double rmax = rCursor.get().getMaxValue();
		RealType<?> r;
		while (rCursor.hasNext()) {
			r = rCursor.next();
			double invertedVal = rmax - (r.getRealDouble() - rmin);
			r.setReal(invertedVal);
		}
	}
	
}
