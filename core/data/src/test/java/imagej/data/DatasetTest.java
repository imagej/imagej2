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
	private static final long[] DIMENSIONS = {1,1,CPLANES,ZPLANES,TPLANES};

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

		int[][][][] planes = new int[TPLANES][][][];
		for (int t = 0; t < TPLANES; t++) {
			planes[t] = new int[ZPLANES][][];
			for (int z = 0; z < ZPLANES; z++) {
				planes[t][z] = new int[CPLANES][];
				for (int c = 0; c < CPLANES; c++) {
					planes[t][z][c] = new int[1];
					planes[t][z][c][0] = planeValue(c,z,t);
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
					int[] plane = (int[]) ds.getPlane(planeNum++);
					assertSame(plane, planes[t][z][c]);
					assertEquals(planeValue(c,z,t), plane[0]);
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
					accessor.setPosition(pos);
					accessor.get().setReal(planeValue(c,z,t));
					assertEquals(planeValue(c,z,t), accessor.get().getRealDouble(), 0);
				}
			}
		}
		int planeNum = 0;
		for (int t = 0; t < TPLANES; t++) {
			for (int z = 0; z < ZPLANES; z++) {
				for (int c = 0; c < CPLANES; c++) {
					int[] plane1 = (int[]) ds.getPlane(planeNum);
					int[] plane2 = (int[]) ds.getPlane(planeNum);
					assertNotSame(plane1, plane2);
					assertEquals(planeValue(c,z,t), plane1[0]);
					assertEquals(planeValue(c,z,t), plane2[0]);
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
