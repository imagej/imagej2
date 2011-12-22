//
// TestCursorSpeeds.java
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

import static org.junit.Assert.assertTrue;
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
		final long[] dims = new long[] { X, Y, Z };
		final Dataset ds1 =
			DatasetFactory.create(new UnsignedByteType(), dims, "junk1",
				new AxisType[] { Axes.X, Axes.Y, Axes.Z });
		final Dataset ds2 =
			DatasetFactory.create(new UnsignedByteType(), dims, "junk2",
				new AxisType[] { Axes.X, Axes.Y, Axes.Z });
		final Dataset ds3 =
			DatasetFactory.create(new UnsignedByteType(), dims, "junk3",
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
