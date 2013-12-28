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

import net.imglib2.meta.ImgPlus;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;

import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * This class allows one to obtain a more specifically typed {@link ImgPlus}
 * based on the actual backing type of the data.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Service.class)
public class DefaultImgPlusService extends AbstractService implements
	ImgPlusService
{

	// TODO
	// Using this class we should relax Dataset's ImgPlus' base type to Type
	// from RealType. Then those who need more specific access can use the
	// methods below. Thus we can break ImageJ2's reliance on RealType. And we
	// can support LongType with no data loss by using integer() below.
	//
	// One limitation of this approach: imagine we have a type (like the proposed
	// FloatingType) that does not derive directly from something in the numeric
	// hierarchy (i.e. FloatingType<T> rather than FloatingType<T extends
	// ComplexType<T>>). Then these casts only expose FloatingType's methods. So
	// we might be shut out of using basic things like add(), mul(), etc. In other
	// words we can't return types that implement Numeric<T> & Floating<T> unless
	// we make specific interfaces containing both and add another routine like
	// numericFloat() that checks both types internally. So we might have a
	// workaround (if the type implements the cobined interface). But I also think
	// this argues for FloatingType to be derived within the numeric hierarchy.

	// -- public static methods --

	// see Javadoc
	public <T extends Type<T>> ImgPlus<T> asType(ImgPlus<?> ip, T type) {
		if (isBackedAs(ip, type.getClass())) {
			return (ImgPlus) ip;
		}
		return null;
	}

	// see Javadoc
	public ImgPlus<Type<?>> typed(ImgPlus<?> ip) {
		if (isBackedAs(ip, Type.class)) {
			return (ImgPlus) ip;
		}
		return null;
	}

	// see Javadoc
	public ImgPlus<NumericType<?>> numeric(ImgPlus<?> ip) {
		if (isBackedAs(ip, NumericType.class)) {
			return (ImgPlus) ip;
		}
		return null;
	}

	// see Javadoc
	public ImgPlus<ComplexType<?>> complex(ImgPlus<?> ip) {
		if (isBackedAs(ip, ComplexType.class)) {
			return (ImgPlus) ip;
		}
		return null;
	}

	// see Javadoc
	public ImgPlus<RealType<?>> real(ImgPlus<?> ip) {
		if (isBackedAs(ip, RealType.class)) {
			return (ImgPlus) ip;
		}
		return null;
	}

	// see Javadoc
	public ImgPlus<IntegerType<?>> integer(ImgPlus<?> ip) {
		if (isBackedAs(ip, IntegerType.class)) {
			return (ImgPlus) ip;
		}
		return null;
	}

// TODO - once FloatingType is an Imglib type

	// see Javadoc
//	public ImgPlus<FloatingType<?>> floating(ImgPlus<?> ip) {
//		if (isBackedAs(ip, FloatingType.class)) {
//			return (ImgPlus<FloatingType<?>>) (ImgPlus) ip;
//		}
//		return null;
//	}

	// -- helpers --

	private boolean isBackedAs(ImgPlus<?> ip, Class<?> clazz) {
		Object type = ip.firstElement();
		return clazz.isAssignableFrom(type.getClass());
	}
}
