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

package imagej.legacy.translate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import ij.process.ImageProcessor;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import net.imglib2.RandomAccess;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.numeric.RealType;

import org.junit.Test;
import org.scijava.Context;


/**
 * Tests the MergedRgbVirtualStack
 * 
 * @author Barry DeZonia
 */
public class MergedRgbVirtualStackTest {

	private Context context = new Context(DatasetService.class);

	@Test
	public void test() {
		DatasetService service = context.getService(DatasetService.class);

		Dataset ds =
			service.create(new long[] { 2, 2, 3 }, "test", new AxisType[] {
				Axes.X, Axes.Y, Axes.CHANNEL}, 8, false, false);

		try {
			new MergedRgbVirtualStack(ds);
			fail();
		}
		catch (Exception e) {
			assertTrue(true);
		}

		ds.setRGBMerged(true);
		MergedRgbVirtualStack vstack = new MergedRgbVirtualStack(ds);
		
		setPlane(ds, 0, 5, 6, 7, 8);
		setPlane(ds, 1, 15, 16, 17, 18);
		setPlane(ds, 2, 25, 26, 27, 28);

		RandomAccess<? extends RealType<?>> accessor =
			ds.getImgPlus().randomAccess();

		accessor.setPosition(new long[] { 0, 0, 0 });
		assertEquals(5, accessor.get().getRealDouble(), 0);

		accessor.setPosition(new long[] { 0, 0, 1 });
		assertEquals(15, accessor.get().getRealDouble(), 0);

		accessor.setPosition(new long[] { 0, 0, 2 });
		assertEquals(25, accessor.get().getRealDouble(), 0);

		int rgb = (0xff << 24) | (5 << 16) | (15 << 8) | (25 << 0);

		ImageProcessor proc = vstack.getProcessor(1);
		assertEquals(rgb, proc.get(0));

		ds =
			service.create(new long[] { 2, 2, 3, 2 }, "test", new AxisType[] {
				Axes.X, Axes.Y, Axes.CHANNEL, Axes.Z }, 8, false, false);
		ds.setRGBMerged(true);

		setPlane(ds, 0, 5, 6, 7, 8);
		setPlane(ds, 1, 15, 16, 17, 18);
		setPlane(ds, 2, 25, 26, 27, 28);
		setPlane(ds, 3, 35, 36, 37, 38);
		setPlane(ds, 4, 45, 46, 47, 48);
		setPlane(ds, 5, 55, 56, 57, 58);

		vstack = new MergedRgbVirtualStack(ds);

		int rgb0 = (0xff << 24) | (5 << 16) | (15 << 8) | (25 << 0);
		int rgb1 = (0xff << 24) | (6 << 16) | (16 << 8) | (26 << 0);
		int rgb2 = (0xff << 24) | (7 << 16) | (17 << 8) | (27 << 0);
		int rgb3 = (0xff << 24) | (8 << 16) | (18 << 8) | (28 << 0);

		proc = vstack.getProcessor(1);
		assertEquals(rgb0, proc.get(0));
		assertEquals(rgb1, proc.get(1));
		assertEquals(rgb2, proc.get(2));
		assertEquals(rgb3, proc.get(3));

		rgb0 = (0xff << 24) | (35 << 16) | (45 << 8) | (55 << 0);
		rgb1 = (0xff << 24) | (36 << 16) | (46 << 8) | (56 << 0);
		rgb2 = (0xff << 24) | (37 << 16) | (47 << 8) | (57 << 0);
		rgb3 = (0xff << 24) | (38 << 16) | (48 << 8) | (58 << 0);

		proc = vstack.getProcessor(2);
		assertEquals(rgb0, proc.get(0));

		for (int i = 0; i < 4; i++) {
			proc.set(i, 77);
			assertEquals(77, proc.get(i));
		}
		// proc = vstack.getProcessor(1);
		proc = vstack.getProcessor(2);
		assertEquals(rgb0, proc.get(0));
		assertEquals(rgb1, proc.get(1));
		assertEquals(rgb2, proc.get(2));
		assertEquals(rgb3, proc.get(3));
	}

	private void
		setPlane(Dataset ds, int channel, int v0, int v1, int v2, int v3)
	{
		byte[] plane = new byte[4];
		plane[0] = (byte) v0;
		plane[1] = (byte) v1;
		plane[2] = (byte) v2;
		plane[3] = (byte) v3;
		// when planar container is no longer default this code must change or
		// setPlane() must copy pixels values rather than plane refs.
		ds.setPlane(channel, plane);
	}
}
