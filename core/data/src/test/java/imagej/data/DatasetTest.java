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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import imagej.ImageJ;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.ImgPlus;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.planar.PlanarImgFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;

import org.junit.Test;

/**
 * Unit tests for {@link Dataset}.
 * 
 * @author Barry DeZonia
 */
public class DatasetTest {

	// -- private interface --

	private static final int CPLANES = 2;
	private static final int ZPLANES = 3;
	private static final int TPLANES = 4;
	private static final long[] DIMENSIONS = { 4, 4, CPLANES, ZPLANES, TPLANES };

	private Dataset createDataset(final ImgFactory<IntType> factory) {
		@SuppressWarnings("unchecked")
		final ImageJ context = ImageJ.createContext(DatasetService.class);
		final DatasetService datasetService =
			context.getService(DatasetService.class);

		final Img<IntType> img = factory.create(DIMENSIONS, new IntType());
		final ImgPlus<IntType> imgPlus = new ImgPlus<IntType>(img);
		return datasetService.create(imgPlus);
	}

	private Dataset createPlanarDataset() {
		return createDataset(new PlanarImgFactory<IntType>());
	}

	private Dataset createNonplanarDataset() {
		return createDataset(new CellImgFactory<IntType>());
	}

	private int planeValue(final int c, final int z, final int t) {
		return 100 * t + 10 * z + 1 * c;
	}

	private void testPlanarCase() {
		// test planar container backed case : get by reference
		final Dataset ds = createPlanarDataset();

		final int planeSize = (int) (DIMENSIONS[0] * DIMENSIONS[1]);
		final int[][][][] planes = new int[TPLANES][][][];
		for (int t = 0; t < TPLANES; t++) {
			planes[t] = new int[ZPLANES][][];
			for (int z = 0; z < ZPLANES; z++) {
				planes[t][z] = new int[CPLANES][];
				for (int c = 0; c < CPLANES; c++) {
					planes[t][z][c] = new int[planeSize];
					for (int i = 0; i < planeSize; i++)
						planes[t][z][c][i] = planeValue(c, z, t);
				}
			}
		}

		int planeNum = 0;
		for (int t = 0; t < TPLANES; t++) {
			for (int z = 0; z < ZPLANES; z++) {
				for (int c = 0; c < CPLANES; c++) {
					ds.setPlane(planeNum++, planes[t][z][c]);
				}
			}
		}

		planeNum = 0;
		for (int t = 0; t < TPLANES; t++) {
			for (int z = 0; z < ZPLANES; z++) {
				for (int c = 0; c < CPLANES; c++) {
					final int[] plane = (int[]) ds.getPlane(planeNum++, false);
					assertSame(plane, planes[t][z][c]);
					for (int i = 0; i < planeSize; i++)
						assertEquals(planeValue(c, z, t), plane[i]);
				}
			}
		}
	}

	private void testNonplanarCase() {
		// test non planar container backed case : get by copy
		final Dataset ds = createNonplanarDataset();

		final RandomAccess<? extends RealType<?>> accessor =
			ds.getImgPlus().randomAccess();
		final long[] pos = new long[DIMENSIONS.length];
		for (int t = 0; t < TPLANES; t++) {
			pos[4] = t;
			for (int z = 0; z < ZPLANES; z++) {
				pos[3] = z;
				for (int c = 0; c < CPLANES; c++) {
					pos[2] = c;
					for (int y = 0; y < DIMENSIONS[1]; y++) {
						pos[1] = y;
						for (int x = 0; x < DIMENSIONS[0]; x++) {
							pos[0] = x;
							accessor.setPosition(pos);
							accessor.get().setReal(planeValue(c, z, t));
							assertEquals(planeValue(c, z, t),
								accessor.get().getRealDouble(), 0);
						}
					}
				}
			}
		}
		int planeNum = 0;
		for (int t = 0; t < TPLANES; t++) {
			for (int z = 0; z < ZPLANES; z++) {
				for (int c = 0; c < CPLANES; c++) {
					final int[] plane1 = (int[]) ds.getPlane(planeNum, true);
					final int[] plane2 = (int[]) ds.getPlane(planeNum, true);
					assertNotSame(plane1, plane2);
					for (int i = 0; i < DIMENSIONS[0] * DIMENSIONS[1]; i++) {
						assertEquals(planeValue(c, z, t), plane1[i]);
						assertEquals(planeValue(c, z, t), plane2[i]);
					}
					planeNum++;
				}
			}
		}
	}

	// -- public tests --

	@Test
	public void testGetPlane() {
		testPlanarCase();
		testNonplanarCase();
	}
}
