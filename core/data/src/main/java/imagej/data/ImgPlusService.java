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

package imagej.data;

import net.imglib2.img.ImgPlus;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;

import org.scijava.service.Service;

/**
 * @author Barry DeZonia
 */
public interface ImgPlusService extends Service {

	/**
	 * Returns a ImgPlus of a given type from a Dataset. Returns null if the given
	 * Dataset does not have data of the given type.
	 */
	public <T extends Type<T>> ImgPlus<T> asType(ImgPlus<?> ip, T type);

	/**
	 * Returns a ImgPlus of type Type given a Dataset. Returns null if the given
	 * Dataset does not have data of Type.
	 */
	public ImgPlus<Type<?>> typed(ImgPlus<?> ip);

	/**
	 * Returns a ImgPlus of type NumericType given a Dataset. Returns null if the
	 * given Dataset does not have data of NumericType.
	 */
	public ImgPlus<NumericType<?>> numeric(ImgPlus<?> ip);

	/**
	 * Returns a ImgPlus of type ComplexType given a Dataset. Returns null if the
	 * given Dataset does not have data of ComplexType.
	 */
	public ImgPlus<ComplexType<?>> complex(ImgPlus<?> ip);

	/**
	 * Returns a ImgPlus of type RealType given a Dataset. Returns null if the
	 * given Dataset does not have data of RealType.
	 */
	public ImgPlus<RealType<?>> real(ImgPlus<?> ip);

	/**
	 * Returns a ImgPlus of type IntegerType given a Dataset. Returns null if the
	 * given Dataset does not have data of IntegerType.
	 */
	public ImgPlus<IntegerType<?>> integer(ImgPlus<?> ip);

	// TODO - once FloatingType is an Imglib type

	// /**
	// * Returns a ImgPlus of type FloatingType given a Dataset. Returns null if
	// the
	// * given Dataset does not have data of FloatingType.
	// */
	// public ImgPlus<FloatingType<?>> floating(ImgPlus<?> ip);

}
