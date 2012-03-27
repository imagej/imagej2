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

package imagej.data;

import static org.junit.Assert.assertTrue;
import imagej.ImageJ;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.Test;

/**
 * Unit tests for ImgLib2 cursor performance.
 * 
 * @author Lee Kamentsky
 */
public class TestCursorSpeeds {

	static final int X = 400;
	static final int Y = 400;
	static final int Z = 50;

	@Test
	public void testCursorSpeeds() {
		@SuppressWarnings("unchecked")
		final ImageJ context = ImageJ.createContext(DatasetService.class);
		final DatasetService datasetService =
			context.getService(DatasetService.class);

		final long[] dims = new long[] { X, Y, Z };
		final Dataset ds1 =
			datasetService.create(new UnsignedByteType(), dims, "junk1",
				new AxisType[] { Axes.X, Axes.Y, Axes.Z });
		final Dataset ds2 =
			datasetService.create(new UnsignedByteType(), dims, "junk2",
				new AxisType[] { Axes.X, Axes.Y, Axes.Z });
		final Dataset ds3 =
			datasetService.create(new UnsignedByteType(), dims, "junk3",
				new AxisType[] { Axes.X, Axes.Y, Axes.Z });

		fill(ds1);
		localizingCursorSpeedTest(ds1, ds2);
		cursorSpeedTest(ds1, ds3);

		assertTrue(true);
	}

	// -- helpers --

	private void fill(final Dataset ds) {
		final Img<? extends RealType<?>> image1 = ds.getImgPlus();
		final Cursor<? extends RealType<?>> cursor = image1.cursor();
		for (long i = 0; i < X * Y * Z; i++) {
			cursor.next();
			cursor.get().setReal(i);
		}
	}

	private void speedTest(final Img<? extends RealType<?>> img1,
		final Img<? extends RealType<?>> img2,
		final Cursor<? extends RealType<?>> inputCursor, final String testName)
	{
		final long start = System.currentTimeMillis();

		final RandomAccess<? extends RealType<?>> outputAccessor =
			img2.randomAccess();

		final long[] position = new long[img1.numDimensions()];

		while (inputCursor.hasNext()) {
			inputCursor.next();
			inputCursor.localize(position);
			outputAccessor.setPosition(position);
			final double value = inputCursor.get().getRealDouble();
			outputAccessor.get().setReal(value);
		}

		final long stop = System.currentTimeMillis();

		System.out.println(testName + " : elapsed time = " + (stop - start));
	}

	private void cursorSpeedTest(final Dataset ds1, final Dataset ds2) {
		for (int i = 0; i < 5; i++)
			speedTest(ds1.getImgPlus(), ds2.getImgPlus(), ds2
				.getImgPlus().cursor(), "ignore - regular cursor");
		speedTest(ds1.getImgPlus(), ds2.getImgPlus(), ds2
			.getImgPlus().cursor(), "regular cursor");
	}

	private void localizingCursorSpeedTest(final Dataset ds1, final Dataset ds2)
	{
		for (int i = 0; i < 5; i++)
			speedTest(ds1.getImgPlus(), ds2.getImgPlus(), ds2
				.getImgPlus().localizingCursor(),
				"ignore - localizing cursor");
		speedTest(ds1.getImgPlus(), ds2.getImgPlus(), ds2
			.getImgPlus().localizingCursor(), "localizing cursor");
	}
}
