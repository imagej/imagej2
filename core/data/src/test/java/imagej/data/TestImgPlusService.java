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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import net.imglib2.img.ImgPlus;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scijava.Context;

/**
 * @author Barry DeZonia
 */
public class TestImgPlusService {

	private static Context context;
	private static ImgPlusService service;

	@BeforeClass
	public static void setupOnce() {
		context = new Context(ImgPlusService.class);
		service = context.getService(ImgPlusService.class);
	}

	@AfterClass
	public static void teardownOnce() {
		context.dispose();
	}

	@Test
	public void testByte() {
		ImgPlus<? extends RealType<?>> imgPlus;
		final ArrayImg<?, ?> imgByte = ArrayImgs.bytes(5, 5);
		imgPlus = new ImgPlus(imgByte);

		assertNotNull(service.typed(imgPlus));
		assertNotNull(service.complex(imgPlus));
		assertNotNull(service.numeric(imgPlus));
		assertNotNull(service.real(imgPlus));
		assertNotNull(service.integer(imgPlus));
		assertNotNull(service.asType(imgPlus, new ByteType()));
		assertNull(service.asType(imgPlus, new FloatType()));
	}

	@Test
	public void testFloat() {
		ImgPlus<? extends RealType<?>> imgPlus;
		final ArrayImg<?, ?> imgFloat = ArrayImgs.floats(5, 5);
		imgPlus = new ImgPlus(imgFloat);

		assertNotNull(service.typed(imgPlus));
		assertNotNull(service.complex(imgPlus));
		assertNotNull(service.numeric(imgPlus));
		assertNotNull(service.real(imgPlus));
		assertNull(service.integer(imgPlus));
		assertNull(service.asType(imgPlus, new ByteType()));
		assertNotNull(service.asType(imgPlus, new FloatType()));
	}

	/* TODO - a non-numeric type
	@Test
	public void testBoolean() {
		ImgPlus<? extends RealType<?>> imgPlus;
		final Img<BooleanType> imgBoolean = null;
		imgPlus = new ImgPlus(imgBoolean);

		assertNull(TypedData.typed(imgPlus));
		assertNull(TypedData.complex(imgPlus));
		assertNull(TypedData.numeric(imgPlus));
		assertNull(TypedData.real(imgPlus));
		assertNull(TypedData.integer(imgPlus));
		assertNull(TypedData.asType(imgPlus, new ByteType()));
		assertNull(TypedData.asType(imgPlus, new FloatType()));
		assertNotNull(TypedData.asType(imgPlus, new BooleanType()));
	}
	*/

	// How useful is it to have typed ImgPluses using service code? Can we do the
	// math ops we might want to do?

	private void note() {

		ImgPlus<? extends RealType<?>> imgPlus;
		final ArrayImg<?, ?> imgLong = ArrayImgs.longs(5, 5);
		imgPlus = new ImgPlus(imgLong);
		ImgPlus<IntegerType<?>> typed = service.integer(imgPlus);
		IntegerType<?> var1 = typed.firstElement();
		IntegerType<?> var2 = typed.firstElement();

		// won't compile
		// var1.add(var2);

		// compiles
		var1.setInteger(1000);
		var2.setInteger(var1.getIntegerLong() + 4000);
	}
}
