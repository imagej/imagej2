package imagej.data;

import static org.junit.Assert.*;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.ImgPlus;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.planar.PlanarImgFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;

import org.junit.Test;


public class DatasetTest {

	// -- private interface --
	private static final int CPLANES = 2;
	private static final int ZPLANES = 3;
	private static final int TPLANES = 4;
	private static final long[] DIMENSIONS = {4,4,CPLANES,ZPLANES,TPLANES};

	private Dataset createDataset(ImgFactory<IntType> factory) {
		Img<IntType> img = factory.create(DIMENSIONS, new IntType());
		ImgPlus<IntType> imgPlus = new ImgPlus<IntType>(img);
		return new Dataset(imgPlus);
	}
	
	private Dataset createPlanarDataset() {
		return createDataset(new PlanarImgFactory<IntType>());
	}
	
	private Dataset createNonplanarDataset() {
		return createDataset(new CellImgFactory<IntType>());
	}

	private int planeValue(int c, int z, int t) {
		return 100*t + 10*z + 1*c;
	}
	
	private void testPlanarCase() {
		// test planar container backed case : get by reference
		Dataset ds = createPlanarDataset();

		int planeSize = (int) (DIMENSIONS[0]*DIMENSIONS[1]);
		int[][][][] planes = new int[TPLANES][][][];
		for (int t = 0; t < TPLANES; t++) {
			planes[t] = new int[ZPLANES][][];
			for (int z = 0; z < ZPLANES; z++) {
				planes[t][z] = new int[CPLANES][];
				for (int c = 0; c < CPLANES; c++) {
					planes[t][z][c] = new int[planeSize];
					for (int i = 0; i < planeSize; i++)
						planes[t][z][c][i] = planeValue(c,z,t);
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
					int[] plane = (int[]) ds.getPlane(planeNum++,false);
					assertSame(plane, planes[t][z][c]);
					for (int i = 0; i < planeSize; i++)
						assertEquals(planeValue(c,z,t), plane[i]);
				}
			}
		}
	}
	
	private void testNonplanarCase() {
		// test non planar container backed case : get by copy
		Dataset ds = createNonplanarDataset();

		RandomAccess<? extends RealType<?>> accessor = ds.getImgPlus().getImg().randomAccess();
		long[] pos = new long[DIMENSIONS.length];
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
							accessor.get().setReal(planeValue(c,z,t));
							assertEquals(planeValue(c,z,t), accessor.get().getRealDouble(), 0);
						}
					}
				}
			}
		}
		int planeNum = 0;
		for (int t = 0; t < TPLANES; t++) {
			for (int z = 0; z < ZPLANES; z++) {
				for (int c = 0; c < CPLANES; c++) {
					int[] plane1 = (int[]) ds.getPlane(planeNum,true);
					int[] plane2 = (int[]) ds.getPlane(planeNum,true);
					assertNotSame(plane1, plane2);
					for (int i = 0; i < DIMENSIONS[0]*DIMENSIONS[1]; i++) {
						assertEquals(planeValue(c,z,t), plane1[i]);
						assertEquals(planeValue(c,z,t), plane2[i]);
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
