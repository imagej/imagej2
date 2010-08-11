package ij.process;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import mpicbg.imglib.container.array.ArrayContainerFactory;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImageFactory;
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
	
	int width = 224, height = 403;
	
	@Test
	public void testGetDimsBeyondXY() {
		// TODO
	}
	
	@Test
	public void testGetTotalSamples() {
		// TODO
	}

	@Test
	public void testGetTotalPlanes() {
		// TODO
	}

	private void getPositionShouldFail(int[] dimensions, int index)
	{
		try {
			ImageUtils.getPosition(dimensions, index);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}
	
	@Test
	public void testGetPosition() {
	
		int[] dimensions;
		
		dimensions = new int[]{};
		getPositionShouldFail(dimensions, -1);
		getPositionShouldFail(dimensions, 0);
		getPositionShouldFail(dimensions, 1);
		
		dimensions = new int[]{4};
		getPositionShouldFail(dimensions, -1);
		assertArrayEquals(new int[]{0}, ImageUtils.getPosition(dimensions, 0));
		assertArrayEquals(new int[]{1}, ImageUtils.getPosition(dimensions, 1));
		assertArrayEquals(new int[]{2}, ImageUtils.getPosition(dimensions, 2));
		assertArrayEquals(new int[]{3}, ImageUtils.getPosition(dimensions, 3));
		getPositionShouldFail(dimensions, 4);

		dimensions = new int[]{2,3};
		getPositionShouldFail(dimensions, -1);
		assertArrayEquals(new int[]{0,0}, ImageUtils.getPosition(dimensions, 0));
		assertArrayEquals(new int[]{0,1}, ImageUtils.getPosition(dimensions, 1));
		assertArrayEquals(new int[]{0,2}, ImageUtils.getPosition(dimensions, 2));
		assertArrayEquals(new int[]{1,0}, ImageUtils.getPosition(dimensions, 3));
		assertArrayEquals(new int[]{1,1}, ImageUtils.getPosition(dimensions, 4));
		assertArrayEquals(new int[]{1,2}, ImageUtils.getPosition(dimensions, 5));
		getPositionShouldFail(dimensions, 6);

		dimensions = new int[]{2,2,2};
		getPositionShouldFail(dimensions, -1);
		assertArrayEquals(new int[]{0,0,0}, ImageUtils.getPosition(dimensions, 0));
		assertArrayEquals(new int[]{0,0,1}, ImageUtils.getPosition(dimensions, 1));
		assertArrayEquals(new int[]{0,1,0}, ImageUtils.getPosition(dimensions, 2));
		assertArrayEquals(new int[]{0,1,1}, ImageUtils.getPosition(dimensions, 3));
		assertArrayEquals(new int[]{1,0,0}, ImageUtils.getPosition(dimensions, 4));
		assertArrayEquals(new int[]{1,0,1}, ImageUtils.getPosition(dimensions, 5));
		assertArrayEquals(new int[]{1,1,0}, ImageUtils.getPosition(dimensions, 6));
		assertArrayEquals(new int[]{1,1,1}, ImageUtils.getPosition(dimensions, 7));
		getPositionShouldFail(dimensions, 8);
	}
	
	private void getPlanePositionShouldFail(int[] dimensions, int index)
	{
		try {
			ImageUtils.getPlanePosition(dimensions, index);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}
	
	@Test
	public void testGetPlanePosition() {
		
		int[] dimensions;
		
		dimensions = new int[]{};
		getPlanePositionShouldFail(dimensions, -1);
		getPlanePositionShouldFail(dimensions, 0);
		getPlanePositionShouldFail(dimensions, 1);
		
		dimensions = new int[]{50};
		getPlanePositionShouldFail(dimensions, -1);
		getPlanePositionShouldFail(dimensions, 0);
		getPlanePositionShouldFail(dimensions, 1);

		// TODO - the middle case is unintuitive. Its up to the user to specify a MxNx1 image for a single plane
		dimensions = new int[]{50,60};
		getPlanePositionShouldFail(dimensions, -1);
		assertArrayEquals(new int[]{}, ImageUtils.getPlanePosition(dimensions, 0));
		getPlanePositionShouldFail(dimensions, 1);

		dimensions = new int[]{50,60,3};
		getPlanePositionShouldFail(dimensions, -1);
		assertArrayEquals(new int[]{0}, ImageUtils.getPlanePosition(dimensions, 0));
		assertArrayEquals(new int[]{1}, ImageUtils.getPlanePosition(dimensions, 1));
		assertArrayEquals(new int[]{2}, ImageUtils.getPlanePosition(dimensions, 2));
		getPlanePositionShouldFail(dimensions, 3);

		dimensions = new int[]{50,60,2,2};
		getPlanePositionShouldFail(dimensions, -1);
		assertArrayEquals(new int[]{0,0}, ImageUtils.getPlanePosition(dimensions, 0));
		assertArrayEquals(new int[]{0,1}, ImageUtils.getPlanePosition(dimensions, 1));
		assertArrayEquals(new int[]{1,0}, ImageUtils.getPlanePosition(dimensions, 2));
		assertArrayEquals(new int[]{1,1}, ImageUtils.getPlanePosition(dimensions, 3));
		getPlanePositionShouldFail(dimensions, 4);

		dimensions = new int[]{50,60,2,2,2};
		getPlanePositionShouldFail(dimensions, -1);
		assertArrayEquals(new int[]{0,0,0}, ImageUtils.getPlanePosition(dimensions, 0));
		assertArrayEquals(new int[]{0,0,1}, ImageUtils.getPlanePosition(dimensions, 1));
		assertArrayEquals(new int[]{0,1,0}, ImageUtils.getPlanePosition(dimensions, 2));
		assertArrayEquals(new int[]{0,1,1}, ImageUtils.getPlanePosition(dimensions, 3));
		assertArrayEquals(new int[]{1,0,0}, ImageUtils.getPlanePosition(dimensions, 4));
		assertArrayEquals(new int[]{1,0,1}, ImageUtils.getPlanePosition(dimensions, 5));
		assertArrayEquals(new int[]{1,1,0}, ImageUtils.getPlanePosition(dimensions, 6));
		assertArrayEquals(new int[]{1,1,1}, ImageUtils.getPlanePosition(dimensions, 7));
		getPlanePositionShouldFail(dimensions, 8);
	}
	
	@Test
	public void testGetPlaneBytes() {
		
		ImageFactory<ByteType> factory = new ImageFactory<ByteType>( new ByteType(), new ArrayContainerFactory() );

		Image<ByteType> image = factory.createImage(new int[]{width,height});
		
		Cursor<ByteType> genCursor = image.createCursor();
		
		int i = 0;
		for (ByteType value : genCursor) {
			value.set( (byte) (255 - (i%256)) );
			i++;
		}
		
		byte[] pixels = ImageUtils.getPlaneBytes(image, width, height, new int[0]);

		LocalizableByDimCursor<ByteType> cursor = image.createLocalizableByDimCursor();
		int pNum = 0;
		int[] position = new int[2];
	    for (int y = 0; y < height; y++) {
	    	for (int x = 0; x < width; x++) {
	    		position[0] = x;
	    		position[1] = y;
	    		cursor.setPosition(position);
	    		assertEquals(cursor.getType().get(),pixels[pNum]);
	    		pNum++;
	    	}
	    }
	}

	@Test
	public void testGetPlaneUnsignedBytes() {
		
		ImageFactory<UnsignedByteType> factory = new ImageFactory<UnsignedByteType>( new UnsignedByteType(), new ArrayContainerFactory() );

		Image<UnsignedByteType> image = factory.createImage(new int[]{width,height});
		
		Cursor<UnsignedByteType> genCursor = image.createCursor();
		
		int i = 0;
		for (UnsignedByteType value : genCursor) {
			value.set( (byte) (255 - (i%256)) );
			i++;
		}
		
		byte[] pixels = ImageUtils.getPlaneUnsignedBytes(image, width, height, new int[0]);

		LocalizableByDimCursor<UnsignedByteType> cursor = image.createLocalizableByDimCursor();
		int pNum = 0;
		int[] position = new int[2];
	    for (int y = 0; y < height; y++) {
	    	for (int x = 0; x < width; x++) {
	    		position[0] = x;
	    		position[1] = y;
	    		cursor.setPosition(position);
	    		assertEquals((byte)cursor.getType().get(),pixels[pNum]);
	    		pNum++;
	    	}
	    }
	}

	@Test
	public void testGetPlaneShorts() {
		
		ImageFactory<ShortType> factory = new ImageFactory<ShortType>( new ShortType(), new ArrayContainerFactory() );

		Image<ShortType> image = factory.createImage(new int[]{width,height});
		
		Cursor<ShortType> genCursor = image.createCursor();
		
		int i = 0;
		for (ShortType value : genCursor) {
			value.set( (short) (65535 - (i%65536)) );
			i++;
		}
		
		short[] pixels = ImageUtils.getPlaneShorts(image, width, height, new int[0]);

		LocalizableByDimCursor<ShortType> cursor = image.createLocalizableByDimCursor();
		int pNum = 0;
		int[] position = new int[2];
	    for (int y = 0; y < height; y++) {
	    	for (int x = 0; x < width; x++) {
	    		position[0] = x;
	    		position[1] = y;
	    		cursor.setPosition(position);
	    		assertEquals(cursor.getType().get(),pixels[pNum]);
	    		pNum++;
	    	}
	    }
	}

	@Test
	public void testGetPlaneUnsignedShorts() {
		
		ImageFactory<UnsignedShortType> factory = new ImageFactory<UnsignedShortType>( new UnsignedShortType(), new ArrayContainerFactory() );

		Image<UnsignedShortType> image = factory.createImage(new int[]{width,height});
		
		Cursor<UnsignedShortType> genCursor = image.createCursor();
		
		int i = 0;
		for (UnsignedShortType value : genCursor) {
			value.set( (short) (65535 - (i%65536)) );
			i++;
		}
		
		short[] pixels = ImageUtils.getPlaneUnsignedShorts(image, width, height, new int[0]);

		LocalizableByDimCursor<UnsignedShortType> cursor = image.createLocalizableByDimCursor();
		int pNum = 0;
		int[] position = new int[2];
	    for (int y = 0; y < height; y++) {
	    	for (int x = 0; x < width; x++) {
	    		position[0] = x;
	    		position[1] = y;
	    		cursor.setPosition(position);
	    		assertEquals((short)cursor.getType().get(),pixels[pNum]);
	    		pNum++;
	    	}
	    }
	}

	@Test
	public void testGetPlaneInts() {
		
		ImageFactory<IntType> factory = new ImageFactory<IntType>( new IntType(), new ArrayContainerFactory() );

		Image<IntType> image = factory.createImage(new int[]{width,height});
		
		Cursor<IntType> genCursor = image.createCursor();
		
		int i = 0;
		for (IntType value : genCursor) {
			value.set( (int) (1234567 - (i%1234568)) );
			i++;
		}
		
		int[] pixels = ImageUtils.getPlaneInts(image, width, height, new int[0]);

		LocalizableByDimCursor<IntType> cursor = image.createLocalizableByDimCursor();
		int pNum = 0;
		int[] position = new int[2];
	    for (int y = 0; y < height; y++) {
	    	for (int x = 0; x < width; x++) {
	    		position[0] = x;
	    		position[1] = y;
	    		cursor.setPosition(position);
	    		assertEquals(cursor.getType().get(),pixels[pNum]);
	    		pNum++;
	    	}
	    }
	}

	@Test
	public void testGetPlaneUnsignedInts() {
		
		ImageFactory<UnsignedIntType> factory = new ImageFactory<UnsignedIntType>( new UnsignedIntType(), new ArrayContainerFactory() );

		Image<UnsignedIntType> image = factory.createImage(new int[]{width,height});
		
		Cursor<UnsignedIntType> genCursor = image.createCursor();
		
		int i = 0;
		for (UnsignedIntType value : genCursor) {
			value.set( (int) (1234567 - (i%1234568)) );
			i++;
		}
		
		int[] pixels = ImageUtils.getPlaneUnsignedInts(image, width, height, new int[0]);

		LocalizableByDimCursor<UnsignedIntType> cursor = image.createLocalizableByDimCursor();
		int pNum = 0;
		int[] position = new int[2];
	    for (int y = 0; y < height; y++) {
	    	for (int x = 0; x < width; x++) {
	    		position[0] = x;
	    		position[1] = y;
	    		cursor.setPosition(position);
	    		assertEquals(cursor.getType().get(),pixels[pNum]);
	    		pNum++;
	    	}
	    }
	}

	@Test
	public void testGetPlaneLongs() {
		
		ImageFactory<LongType> factory = new ImageFactory<LongType>( new LongType(), new ArrayContainerFactory() );

		Image<LongType> image = factory.createImage(new int[]{width,height});
		
		Cursor<LongType> genCursor = image.createCursor();
		
		int i = 0;
		for (LongType value : genCursor) {
			value.set( (long) (1234567 - (i%1234568)) );
			i++;
		}
		
		long[] pixels = ImageUtils.getPlaneLongs(image, width, height, new int[0]);

		LocalizableByDimCursor<LongType> cursor = image.createLocalizableByDimCursor();
		int pNum = 0;
		int[] position = new int[2];
	    for (int y = 0; y < height; y++) {
	    	for (int x = 0; x < width; x++) {
	    		position[0] = x;
	    		position[1] = y;
	    		cursor.setPosition(position);
	    		assertEquals(cursor.getType().get(),pixels[pNum]);
	    		pNum++;
	    	}
	    }
	}

	@Test
	public void testGetPlaneFloats() {
		
		ImageFactory<FloatType> factory = new ImageFactory<FloatType>( new FloatType(), new ArrayContainerFactory() );

		Image<FloatType> image = factory.createImage(new int[]{width,height});
		
		Cursor<FloatType> genCursor = image.createCursor();
		
		int i = 0;
		for (FloatType value : genCursor) {
			value.set( i+103030.689f );
			i++;
		}
		
		float[] pixels = ImageUtils.getPlaneFloats(image, width, height, new int[0]);

		LocalizableByDimCursor<FloatType> cursor = image.createLocalizableByDimCursor();
		int pNum = 0;
		int[] position = new int[2];
	    for (int y = 0; y < height; y++) {
	    	for (int x = 0; x < width; x++) {
	    		position[0] = x;
	    		position[1] = y;
	    		cursor.setPosition(position);
	    		assertEquals(cursor.getType().get(),pixels[pNum],0);
	    		pNum++;
	    	}
	    }
	}

	@Test
	public void testGetPlaneDoubles() {

		ImageFactory<DoubleType> factory = new ImageFactory<DoubleType>( new DoubleType(), new ArrayContainerFactory() );

		Image<DoubleType> image = factory.createImage(new int[]{width,height});
		
		Cursor<DoubleType> genCursor = image.createCursor();
		
		int i = 0;
		for (DoubleType value : genCursor) {
			value.set( i+103030.689 );
			i++;
		}
		
		double[] pixels = ImageUtils.getPlaneDoubles(image, width, height, new int[0]);

		LocalizableByDimCursor<DoubleType> cursor = image.createLocalizableByDimCursor();
		int pNum = 0;
		int[] position = new int[2];
	    for (int y = 0; y < height; y++) {
	    	for (int x = 0; x < width; x++) {
	    		position[0] = x;
	    		position[1] = y;
	    		cursor.setPosition(position);
	    		assertEquals(cursor.getType().get(),pixels[pNum],0);
	    		pNum++;
	    	}
	    }
	}

	@Test
	public void testGetPlaneData() {

		ImageFactory<IntType> factory = new ImageFactory<IntType>( new IntType(), new ArrayContainerFactory() );

		Image<IntType> image = factory.createImage(new int[]{width,height});
		
		Cursor<IntType> genCursor = image.createCursor();
		
		int i = 0;
		for (IntType value : genCursor) {
			value.set( i );
			i++;
		}
		
		double[] pixels = ImageUtils.getPlaneData(image, width, height, new int[0]);

		LocalizableByDimCursor<IntType> cursor = image.createLocalizableByDimCursor();
		int pNum = 0;
		int[] position = new int[2];
	    for (int y = 0; y < height; y++) {
	    	for (int x = 0; x < width; x++) {
	    		position[0] = x;
	    		position[1] = y;
	    		cursor.setPosition(position);
	    		assertEquals(cursor.getType().get(),pixels[pNum],0);
	    		pNum++;
	    	}
	    }
	}

	@Test
	public void testCopyFromImageToImage()
	{
		// TODO
	}
}
