package imagej.process;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import imagej.process.ImageUtils;
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

// TODO - write test for copyFromImageToImage()

public class ImageUtilsTest {
	
	int width = 224, height = 403;
	
	private void getDimsBeyondXYShouldFail(int[] dims)
	{
		try {
			ImageUtils.getDimsBeyondXY(dims);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}
	
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
		//ImageUtils.copyFromImageToImage(sourceImage, destinationImage, sourceDimensionOrigins, destinationDimensionOrigins, dimensionSpans);
	}

	// constructor 3
	@Test
	public void testCreate()
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
		
		ArrayContainerFactory contFact = new ArrayContainerFactory();
		// TODO CTR see me - this causes this test to fail:
		//contFact.setPlanar(true);
		ImageFactory<UnsignedShortType> factory = new ImageFactory<UnsignedShortType>(new UnsignedShortType(), contFact);
		Image<UnsignedShortType> image = factory.createImage(dimensions);
		// TODO : set pixel data to something
		ImagePlus imp = ImageUtils.createImagePlus(image);
		
		int slices   = image.getDimension(2);
		int channels = image.getDimension(3);
		int frames   = image.getDimension(4);
		
		assertEquals(frames, imp.getNFrames());
		assertEquals(channels, imp.getNChannels());
		assertEquals(slices, imp.getNSlices());

		ImageStack stack = imp.getStack();
		int totalPlanes = slices * channels * frames;
		for (int i = 0; i < totalPlanes; i++)
		{
			ImageProcessor proc = stack.getProcessor(i+1); 
			//TODO : enable this when IJ does not screw up the processors
			//  it turns out that ImageStack.addSlice(processor) just copies the pixels of the processor. Later getProcessor() calls to
			//  the ImagePlus creates a processor on the pixel data and since its a short[] here we get back a ShortProcessor.
			//assertTrue(proc instanceof ImgLibProcessor);
			assertEquals(image.getDimension(0), proc.getWidth());
			assertEquals(image.getDimension(1), proc.getHeight());
		}
	}

}
