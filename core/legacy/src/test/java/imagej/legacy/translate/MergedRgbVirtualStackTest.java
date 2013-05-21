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
		
		ds.setPlane(0, new byte[] { 5, 5, 5, 5 });
		ds.setPlane(1, new byte[] { 15, 15, 15, 15 });
		ds.setPlane(2, new byte[] { 25, 25, 25, 25 });

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

		ds.setPlane(0, new byte[] { 5, 5, 5, 5 });
		ds.setPlane(1, new byte[] { 15, 15, 15, 15 });
		ds.setPlane(2, new byte[] { 25, 25, 25, 25 });
		ds.setPlane(3, new byte[] { 35, 35, 35, 35 });
		ds.setPlane(4, new byte[] { 45, 45, 45, 45 });
		ds.setPlane(5, new byte[] { 55, 55, 55, 55 });

		vstack = new MergedRgbVirtualStack(ds);

		rgb = (0xff << 24) | (5 << 16) | (15 << 8) | (25 << 0);

		proc = vstack.getProcessor(1);
		assertEquals(rgb, proc.get(0));

		rgb = (0xff << 24) | (35 << 16) | (45 << 8) | (55 << 0);

		proc = vstack.getProcessor(2);
		assertEquals(rgb, proc.get(0));

		proc.set(0, 77);
		assertEquals(77, proc.get(0));
		proc = vstack.getProcessor(1);
		proc = vstack.getProcessor(2);
		assertEquals(rgb, proc.get(0));
	}
}
