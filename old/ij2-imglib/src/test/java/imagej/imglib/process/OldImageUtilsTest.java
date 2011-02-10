package imagej.imglib.process;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import imagej.imglib.ImageUtils;
import mpicbg.imglib.container.planar.PlanarContainerFactory;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.IntType;
import mpicbg.imglib.type.numeric.integer.ShortType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import mpicbg.imglib.type.numeric.integer.UnsignedIntType;

import org.junit.Test;

public class OldImageUtilsTest {

	// *************  instance vars ********************************************

	int width = 224, height = 403;

	// *************  private helpers ********************************************

	private Image<? extends RealType<?>> makeImage(RealType<?> type, int[] dimensions)
	{
		PlanarContainerFactory cFact = new PlanarContainerFactory();

		return ImageUtils.createImage(type, cFact, dimensions);
	}

	// *************  public tests ********************************************

	@Test
	public void testGetTotalSamplesImage()
	{
		int[][] dimensions =
			new int[][]{
				new int[]{1},
				new int[]{2,3},
				new int[]{3,4,5},
				new int[]{4,5,6,7},
				new int[]{5,6,7,8,9}
				// TODO - causes imglib exception because size > ArrayContainer max size. Can't find a bigger container.
				//,new int[]{Short.MAX_VALUE,Short.MAX_VALUE,5}
				};

		long[] sampleCounts =
			new long[]{
				1,
				6,
				60,
				840,
				15120
				// TODO - see above TODO
				//,5368381445L
				};

		for (int i = 0; i < dimensions.length; i++)
		{
			Image<?> image = makeImage(new UnsignedByteType(), dimensions[i]);

			assertEquals(sampleCounts[i], OldImageUtils.getTotalSamples(image));
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetPlaneData()
	{
		int[] dimensions = new int[]{2,3};
		Image<? extends RealType<?>> image = makeImage(new UnsignedByteType(), dimensions);
		LocalizableByDimCursor<UnsignedByteType> cursor = ((Image<UnsignedByteType>)image).createLocalizableByDimCursor();
		cursor.setPosition(new int[]{0,0});
		cursor.getType().set(1);
		cursor.setPosition(new int[]{1,0});
		cursor.getType().set(2);
		cursor.setPosition(new int[]{0,1});
		cursor.getType().set(3);
		cursor.setPosition(new int[]{1,1});
		cursor.getType().set(4);
		cursor.setPosition(new int[]{0,2});
		cursor.getType().set(5);
		cursor.setPosition(new int[]{1,2});
		cursor.getType().set(6);

		double[] data = OldImageUtils.getPlaneData(image, 2, 3, new int[]{});

		assertEquals(1,data[0],0);
		assertEquals(2,data[1],0);
		assertEquals(3,data[2],0);
		assertEquals(4,data[3],0);
		assertEquals(5,data[4],0);
		assertEquals(6,data[5],0);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSetAndGetPlane()
	{
		Image<ShortType> image = (Image<ShortType>) makeImage(new ShortType(), new int[]{2,3,4});

		short[] ones = new short[]{1,1,1,1,1,1};
		short[] twos =  new short[]{2,2,2,2,2,2};
		short[] threes = new short[]{3,3,3,3,3,3};
		short[] fours = new short[]{4,4,4,4,4,4};

		OldImageUtils.setPlane(image, new int[]{0}, ones);
		OldImageUtils.setPlane(image, new int[]{1}, twos);
		OldImageUtils.setPlane(image, new int[]{2}, threes);
		OldImageUtils.setPlane(image, new int[]{3}, fours);

		assertArrayEquals(ones, (short[])OldImageUtils.getPlane(image, new int[]{0}));
		assertArrayEquals(twos, (short[])OldImageUtils.getPlane(image, new int[]{1}));
		assertArrayEquals(threes, (short[])OldImageUtils.getPlane(image, new int[]{2}));
		assertArrayEquals(fours, (short[])OldImageUtils.getPlane(image, new int[]{3}));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCopyFromImageToImage()
	{
		int[] ones = new int[]{1,1,1,1,1,1};
		int[] twos = new int[]{2,2,2,2,2,2};
		int[] threes = new int[]{3,3,3,3,3,3};
		int[] fours = new int[]{4,4,4,4,4,4};

		int[] sevens = new int[]{7,7,7,7,7,7};
		int[] eights = new int[]{8,8,8,8,8,8};

		Image<IntType> srcImage = (Image<IntType>) makeImage(new IntType(), new int[]{2,3,2});

		OldImageUtils.setPlane(srcImage, new int[]{0}, sevens);
		OldImageUtils.setPlane(srcImage, new int[]{1}, eights);

		assertTrue(sevens == OldImageUtils.getPlane(srcImage, new int[]{0}));
		assertTrue(eights == OldImageUtils.getPlane(srcImage, new int[]{1}));

		Image<IntType> dstImage = (Image<IntType>) makeImage(new IntType(), new int[]{2,3,4});

		OldImageUtils.setPlane(dstImage, new int[]{0}, ones);
		OldImageUtils.setPlane(dstImage, new int[]{1}, twos);
		OldImageUtils.setPlane(dstImage, new int[]{2}, threes);
		OldImageUtils.setPlane(dstImage, new int[]{3}, fours);

		assertTrue(ones == OldImageUtils.getPlane(dstImage, new int[]{0}));
		assertTrue(twos == OldImageUtils.getPlane(dstImage, new int[]{1}));
		assertTrue(threes == OldImageUtils.getPlane(dstImage, new int[]{2}));
		assertTrue(fours == OldImageUtils.getPlane(dstImage, new int[]{3}));

		OldImageUtils.copyFromImageToImage(srcImage, new int[]{0,0,0}, new int[]{2,3,2},
										dstImage, new int[]{0,0,2}, new int[]{2,3,2});

		assertTrue(ones == OldImageUtils.getPlane(dstImage, new int[]{0}));
		assertTrue(twos == OldImageUtils.getPlane(dstImage, new int[]{1}));
		assertTrue(threes == OldImageUtils.getPlane(dstImage, new int[]{2}));
		assertTrue(fours == OldImageUtils.getPlane(dstImage, new int[]{3}));

		Object srcPlane, dstPlane;

		srcPlane = OldImageUtils.getPlane(srcImage, new int[]{0});
		dstPlane = OldImageUtils.getPlane(dstImage, new int[]{2});
		assertTrue(dstPlane != srcPlane);
		assertArrayEquals(sevens, (int[])dstPlane);

		srcPlane = OldImageUtils.getPlane(srcImage, new int[]{1});
		dstPlane = OldImageUtils.getPlane(dstImage, new int[]{3});
		assertTrue(dstPlane != srcPlane);
		assertArrayEquals(eights, (int[])dstPlane);
	}

	@Test
	public void testGetVariousDims()
	{
		Image<?> image;

		image = makeImage(new UnsignedIntType(), new int[]{1,2,3,4,5,6,7});

		// we'll just test the default order
		assertEquals(1,OldImageUtils.getWidth(image));
		assertEquals(2,OldImageUtils.getHeight(image));
		assertEquals(3,OldImageUtils.getNChannels(image));
		assertEquals(4,OldImageUtils.getNSlices(image));
		assertEquals(5,OldImageUtils.getNFrames(image));

		// could create an image with different dim ordering and then test them but not sure how to do this
		//   outside of loading data via BioFormats/FileOpener
	}
}
