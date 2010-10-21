package imagej.process;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileInfo;
import ij.process.ImageProcessor;
import imagej.process.ImageUtils;
import mpicbg.imglib.container.ContainerFactory;
import mpicbg.imglib.container.array.ArrayContainerFactory;
import mpicbg.imglib.container.basictypecontainer.array.PlanarAccess;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImageFactory;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.ByteType;
import mpicbg.imglib.type.numeric.integer.IntType;
import mpicbg.imglib.type.numeric.integer.LongType;
import mpicbg.imglib.type.numeric.integer.ShortType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import mpicbg.imglib.type.numeric.integer.UnsignedIntType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;
import mpicbg.imglib.type.numeric.real.DoubleType;
import mpicbg.imglib.type.numeric.real.FloatType;

import org.junit.Test;

public class ImageUtilsTest {
	
	// *************  instance vars ********************************************
	
	int width = 224, height = 403;

	// *************  private helpers ********************************************

	private Image<? extends RealType<?>> makeImage(RealType<?> type, int[] dimensions)
	{
		ArrayContainerFactory cFact = new ArrayContainerFactory();

		cFact.setPlanar(true);
		
		return ImageUtils.createImage(type, cFact, dimensions);
	}
	
	private void getDimsBeyondXYShouldFail(int[] dims)
	{
		try {
			ImageUtils.getDimsBeyondXY(dims);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}
	
	private void verifyDims(boolean shouldFail, int[] dims, int[] origin, int[] span)
	{
		try {
			ImageUtils.verifyDimensions(dims, origin, span);
			if (shouldFail)
				fail();
			else
				assertTrue(true);
		} catch (IllegalArgumentException e) {
			if (shouldFail)
				assertTrue(true);
			else
				fail();
		}
	}

	// *************  public tests ********************************************
	
	@Test
	public void testGetDimsBeyondXY() {
		getDimsBeyondXYShouldFail(new int[]{});
		getDimsBeyondXYShouldFail(new int[]{1});
		assertArrayEquals(new int[]{}, ImageUtils.getDimsBeyondXY(new int[]{1,2}));
		assertArrayEquals(new int[]{3}, ImageUtils.getDimsBeyondXY(new int[]{1,2,3}));
		assertArrayEquals(new int[]{3,4}, ImageUtils.getDimsBeyondXY(new int[]{1,2,3,4}));
		assertArrayEquals(new int[]{3,4,5}, ImageUtils.getDimsBeyondXY(new int[]{1,2,3,4,5}));
	}
	
	@Test
	public void testGetTotalSamples() {
		assertEquals(0,ImageUtils.getTotalSamples(new int[]{}));
		assertEquals(0,ImageUtils.getTotalSamples(new int[]{0}));
		assertEquals(1,ImageUtils.getTotalSamples(new int[]{1}));
		assertEquals(8,ImageUtils.getTotalSamples(new int[]{8}));
		assertEquals(1,ImageUtils.getTotalSamples(new int[]{1,1}));
		assertEquals(10,ImageUtils.getTotalSamples(new int[]{2,5}));
		assertEquals(24,ImageUtils.getTotalSamples(new int[]{2,3,4}));
		assertEquals(720,ImageUtils.getTotalSamples(new int[]{1,2,3,4,5,6}));
	}

	@Test
	public void testGetTotalPlanes() {
		assertEquals(0,ImageUtils.getTotalPlanes(new int[]{}));
		assertEquals(0,ImageUtils.getTotalPlanes(new int[]{0}));
		assertEquals(0,ImageUtils.getTotalPlanes(new int[]{1}));
		assertEquals(0,ImageUtils.getTotalPlanes(new int[]{8}));
		assertEquals(1,ImageUtils.getTotalPlanes(new int[]{1,1}));
		assertEquals(1,ImageUtils.getTotalPlanes(new int[]{2,5}));
		assertEquals(4,ImageUtils.getTotalPlanes(new int[]{2,3,4}));
		assertEquals(360,ImageUtils.getTotalPlanes(new int[]{1,2,3,4,5,6}));
	}

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
			
			assertEquals(sampleCounts[i], ImageUtils.getTotalSamples(image));
		}
	}
	
	@Test
	public void testGetType()
	{
		int[] dimensions = new int[]{3,5};
		
		Image<?> image;
		
		image = makeImage(new UnsignedByteType(), dimensions);
		assertTrue(ImageUtils.getType(image) instanceof UnsignedByteType);
		
		image = makeImage(new ShortType(), dimensions);
		assertTrue(ImageUtils.getType(image) instanceof ShortType);
		
		image = makeImage(new UnsignedIntType(), dimensions);
		assertTrue(ImageUtils.getType(image) instanceof UnsignedIntType);
		
		image = makeImage(new DoubleType(), dimensions);
		assertTrue(ImageUtils.getType(image) instanceof DoubleType);
		
		image = makeImage(new LongType(), dimensions);
		assertTrue(ImageUtils.getType(image) instanceof LongType);
	}
	
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
		
		double[] data = ImageUtils.getPlaneData(image, 2, 3, new int[]{});
		
		assertEquals(1,data[0],0);
		assertEquals(2,data[1],0);
		assertEquals(3,data[2],0);
		assertEquals(4,data[3],0);
		assertEquals(5,data[4],0);
		assertEquals(6,data[5],0);
	}
	
	@Test
	public void testGetPlanarAccess()
	{
		ArrayContainerFactory factory = new ArrayContainerFactory();
		
		Image<?> testImage;
		PlanarAccess<?> access;
		
		testImage = ImageUtils.createImage(new UnsignedByteType(), factory, new int[]{1,2});
		access = ImageUtils.getPlanarAccess(testImage);
		assertTrue(access == null);
		
		factory.setPlanar(true);
		
		testImage = ImageUtils.createImage(new UnsignedByteType(), factory, new int[]{1,2});
		access = ImageUtils.getPlanarAccess(testImage);
		assertTrue(access != null);
	}
	
	@Test
	public void testSetAndGetPlane()
	{
		Image<ShortType> image = (Image<ShortType>) makeImage(new ShortType(), new int[]{2,3,4});
		
		short[] ones = new short[]{1,1,1,1,1,1};
		short[] twos =  new short[]{2,2,2,2,2,2};
		short[] threes = new short[]{3,3,3,3,3,3};
		short[] fours = new short[]{4,4,4,4,4,4};
		
		ImageUtils.setPlane(image, new int[]{0}, ones);
		ImageUtils.setPlane(image, new int[]{1}, twos);
		ImageUtils.setPlane(image, new int[]{2}, threes);
		ImageUtils.setPlane(image, new int[]{3}, fours);

		assertArrayEquals(ones, (short[])ImageUtils.getPlane(image, new int[]{0}));
		assertArrayEquals(twos, (short[])ImageUtils.getPlane(image, new int[]{1}));
		assertArrayEquals(threes, (short[])ImageUtils.getPlane(image, new int[]{2}));
		assertArrayEquals(fours, (short[])ImageUtils.getPlane(image, new int[]{3}));
	}
	
	@Test
	public void testVerifyDimensions()
	{
		final boolean FAIL = true;
		
		// origin len != span len
		verifyDims(FAIL, new int[]{1}, new int[]{0}, new int[]{1,1});

		// origin len != dim len
		verifyDims(FAIL, new int[]{1}, new int[]{0,0}, new int[]{1,1});
		
		// dim len != span len
		verifyDims(FAIL, new int[]{1,2}, new int[]{0,0}, new int[]{1});

		// origin outside image in some dim
		verifyDims(FAIL, new int[]{1,2,3}, new int[]{0,0,-1}, new int[]{1,1,1});
		verifyDims(FAIL, new int[]{1,2,3}, new int[]{0,0,3}, new int[]{1,1,1});
		verifyDims(FAIL, new int[]{1,2,3}, new int[]{0,-1,0}, new int[]{1,1,1});
		verifyDims(FAIL, new int[]{1,2,3}, new int[]{0,2,0}, new int[]{1,1,1});
		verifyDims(FAIL, new int[]{1,2,3}, new int[]{-1,0,0}, new int[]{1,1,1});
		verifyDims(FAIL, new int[]{1,2,3}, new int[]{1,0,0}, new int[]{1,1,1});
		
		// span <= 0 in some dim
		verifyDims(FAIL, new int[]{1,2,3}, new int[]{0,0,-1}, new int[]{0,1,1});
		verifyDims(FAIL, new int[]{1,2,3}, new int[]{0,0,3}, new int[]{1,0,1});
		verifyDims(FAIL, new int[]{1,2,3}, new int[]{0,-1,0}, new int[]{1,1,0});
		
		// origin + span outside image in some dim
		verifyDims(FAIL, new int[]{1}, new int[]{0}, new int[]{2});
		verifyDims(FAIL, new int[]{2,2}, new int[]{0,1}, new int[]{2,2});
		verifyDims(FAIL, new int[]{2,2}, new int[]{1,0}, new int[]{2,2});
		
		// all other cases should succeed
		
		final boolean SUCCEED = false;
		
		verifyDims(SUCCEED, new int[]{2,2}, new int[]{0,0}, new int[]{2,2});
		verifyDims(SUCCEED, new int[]{2,2}, new int[]{0,0}, new int[]{1,1});
		verifyDims(SUCCEED, new int[]{2,2}, new int[]{0,0}, new int[]{1,2});
		verifyDims(SUCCEED, new int[]{2,2}, new int[]{0,0}, new int[]{2,1});
		verifyDims(SUCCEED, new int[]{2,2}, new int[]{1,0}, new int[]{1,1});
		verifyDims(SUCCEED, new int[]{2,2}, new int[]{1,0}, new int[]{1,2});
		verifyDims(SUCCEED, new int[]{2,2}, new int[]{0,1}, new int[]{1,1});
		verifyDims(SUCCEED, new int[]{2,2}, new int[]{0,1}, new int[]{2,1});
		verifyDims(SUCCEED, new int[]{2,2}, new int[]{1,1}, new int[]{1,1});
	}
	
	private void print(String name, int[] values)
	{
		System.out.print(name+" = ");
		for (int value : values)
			System.out.print(","+value);
		System.out.println();
	}
	
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
		
		ImageUtils.setPlane(srcImage, new int[]{0}, sevens);
		ImageUtils.setPlane(srcImage, new int[]{1}, eights);
		
		assertTrue(sevens == ImageUtils.getPlane(srcImage, new int[]{0}));
		assertTrue(eights == ImageUtils.getPlane(srcImage, new int[]{1}));

		Image<IntType> dstImage = (Image<IntType>) makeImage(new IntType(), new int[]{2,3,4});
		
		ImageUtils.setPlane(dstImage, new int[]{0}, ones);
		ImageUtils.setPlane(dstImage, new int[]{1}, twos);
		ImageUtils.setPlane(dstImage, new int[]{2}, threes);
		ImageUtils.setPlane(dstImage, new int[]{3}, fours);

		assertTrue(ones == ImageUtils.getPlane(dstImage, new int[]{0}));
		assertTrue(twos == ImageUtils.getPlane(dstImage, new int[]{1}));
		assertTrue(threes == ImageUtils.getPlane(dstImage, new int[]{2}));
		assertTrue(fours == ImageUtils.getPlane(dstImage, new int[]{3}));

		ImageUtils.copyFromImageToImage(srcImage, new int[]{0,0,0}, new int[]{2,3,2},
										dstImage, new int[]{0,0,2}, new int[]{2,3,2});
		
		assertTrue(ones == ImageUtils.getPlane(dstImage, new int[]{0}));
		assertTrue(twos == ImageUtils.getPlane(dstImage, new int[]{1}));
		assertTrue(threes == ImageUtils.getPlane(dstImage, new int[]{2}));
		assertTrue(fours == ImageUtils.getPlane(dstImage, new int[]{3}));

		Object srcPlane, dstPlane;

		srcPlane = ImageUtils.getPlane(srcImage, new int[]{0});
		dstPlane = ImageUtils.getPlane(dstImage, new int[]{2});
		assertTrue(dstPlane != srcPlane);
		assertArrayEquals(sevens, (int[])dstPlane);

		srcPlane = ImageUtils.getPlane(srcImage, new int[]{1});
		dstPlane = ImageUtils.getPlane(dstImage, new int[]{3});
		assertTrue(dstPlane != srcPlane);
		assertArrayEquals(eights, (int[])dstPlane);
	}

	@Test
	public void testCreateProcessor()
	{
		int width= 3, height = 5;
		
		byte[] bytes = new byte[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
		
		ImgLibProcessor<?> proc = ImageUtils.createProcessor(width, height, bytes, true);
		
		assertNotNull(proc);
		assertEquals(width, proc.getWidth());
		assertEquals(height, proc.getHeight());
		assertArrayEquals(bytes,(byte[])proc.getPixels());
	}

	@Test
	public void testCreateImagePlus()
	{
		int[] dimensions = new int[]{3,4,5,6,7};
		
		Image<UnsignedShortType> image = (Image<UnsignedShortType>) makeImage(new UnsignedShortType(), dimensions);

		// TODO : set pixel data to something
		
		ImagePlus imp = ImageUtils.createImagePlus(image);
		
		int channels = image.getDimension(2);
		int slices   = image.getDimension(3);
		int frames   = image.getDimension(4);
		
		assertEquals(frames, imp.getNFrames());
		assertEquals(channels, imp.getNChannels());
		assertEquals(slices, imp.getNSlices());

		ImageStack stack = imp.getStack();
		int totalPlanes = slices * channels * frames;
		for (int i = 0; i < totalPlanes; i++)
		{
			ImageProcessor proc = stack.getProcessor(i+1); 
			assertTrue(proc instanceof ImgLibProcessor);
			assertEquals(image.getDimension(0), proc.getWidth());
			assertEquals(image.getDimension(1), proc.getHeight());
		}
	}

	@Test
	public void testCreateImagePlusWithString()
	{
		int[] dimensions = new int[]{3,4,5,6,7};
		
		Image<UnsignedShortType> image = (Image<UnsignedShortType>) makeImage(new UnsignedShortType(), dimensions);

		// TODO : set pixel data to something
		
		ImagePlus imp = ImageUtils.createImagePlus(image, "gadzooks");
		
		int channels = image.getDimension(2);
		int slices   = image.getDimension(3);
		int frames   = image.getDimension(4);
		
		assertEquals(frames, imp.getNFrames());
		assertEquals(channels, imp.getNChannels());
		assertEquals(slices, imp.getNSlices());

		ImageStack stack = imp.getStack();
		int totalPlanes = slices * channels * frames;
		for (int i = 0; i < totalPlanes; i++)
		{
			ImageProcessor proc = stack.getProcessor(i+1); 
			assertTrue(proc instanceof ImgLibProcessor);
			assertEquals(image.getDimension(0), proc.getWidth());
			assertEquals(image.getDimension(1), proc.getHeight());
		}
		
		FileInfo fi = imp.getOriginalFileInfo();
		assertEquals("gadzooks",fi.url);
	}
	
	@Test
	public void testCreateImage()
	{
		Image<?> image;
		
		image = makeImage(new UnsignedIntType(), new int[]{1});
		assertTrue(ImageUtils.getType(image) instanceof UnsignedIntType);
		assertEquals(1,image.getDimension(0));
		
		image = makeImage(new FloatType(), new int[]{6,4});
		assertTrue(ImageUtils.getType(image) instanceof FloatType);
		assertEquals(6,image.getDimension(0));
		assertEquals(4,image.getDimension(1));
		
		image = makeImage(new LongType(), new int[]{6,4,2});
		assertTrue(ImageUtils.getType(image) instanceof LongType);
		assertEquals(6,image.getDimension(0));
		assertEquals(4,image.getDimension(1));
		assertEquals(2,image.getDimension(2));
	}
	
	@Test
	public void testGetVariousDims()
	{
		Image<?> image;
		
		image = makeImage(new UnsignedIntType(), new int[]{1,2,3,4,5,6,7});
		
		// we'll just test the default order
		assertEquals(1,ImageUtils.getWidth(image));
		assertEquals(2,ImageUtils.getHeight(image));
		assertEquals(3,ImageUtils.getNChannels(image));
		assertEquals(4,ImageUtils.getNSlices(image));
		assertEquals(5,ImageUtils.getNFrames(image));
		
		// could create an image with different dim ordering and then test them but not sure how to do this
		//   outside of loading data via BioFormats/FileOpener
	}
}
