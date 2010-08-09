package ij.process;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;

import ij.Assert;
import ij.ImagePlus;
import ij.ImageStack;

import mpicbg.imglib.container.ContainerFactory;
import mpicbg.imglib.container.array.ArrayContainerFactory;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImageFactory;
import mpicbg.imglib.io.LOCI;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;

import org.junit.Test;

// TODO - Right now doing all comparisons versus a ByteProcessor. Add comparison code vs. FloatProcessor and ShortProcessor also

public class ImgLibProcessorTest {

	// ************* Instance variables ***********************************************
	
	static int width;
	static int height;
	static ImgLibProcessor<UnsignedByteType> origIProc;
	static ByteProcessor origBProc;
	
	ImgLibProcessor<UnsignedByteType> iProc;
	ByteProcessor bProc;
	FloatProcessor fProc;
	ShortProcessor sProc;
	ColorProcessor cProc;  // may not need to test this for comparison

	// ************* Helper methods ***********************************************

	
	// ************* Helper tests ***********************************************

	private static void compareData(ImageProcessor baselineProc, ImageProcessor testedProc)
	{
		int w = baselineProc.getWidth();
		int h = baselineProc.getHeight();
	
		assertEquals(w,testedProc.getWidth());
		assertEquals(h,testedProc.getHeight());
	
		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++)
				if (Math.abs(baselineProc.getf(x,y)-testedProc.getf(x,y)) > Assert.FLOAT_TOL)
					fail("processor data differs at ("+x+","+y+") : ij(" + baselineProc.getf(x,y) +") imglib("+testedProc.getf(x,y)+")");
	}

	// this initialization code runs once - load the test image
	@BeforeClass
	public static void setup()
	{
		String filename = "data/head8bit.tif";
		
		ImagePlus imp = new ImagePlus(filename);
		origBProc = (ByteProcessor) imp.getProcessor();
		width = origBProc.getWidth();
		height = origBProc.getHeight();
		
		final ContainerFactory containerFactory = new ArrayContainerFactory();
		Image<UnsignedByteType> image = LOCI.openLOCIUnsignedByteType(filename, containerFactory);
		origIProc = new ImgLibProcessor<UnsignedByteType>(image, new UnsignedByteType());

		compareData(origBProc,origIProc);
	}
	
	// the following initialization code runs before every test
	@Before
	public void initialize()
	{
		bProc = (ByteProcessor)origBProc.duplicate();
		iProc = (ImgLibProcessor)origIProc.duplicate();
		compareData(bProc,iProc);
	}

	// ************* Tests ***********************************************

	@Test
	public void testApplyTable() {

		// make an inverted lut
		int[] newLut = new int[256];
		for (int i = 0; i < 256; i++)
			newLut[i] = 255 - i;
		
		// apply to both
		bProc.applyTable(newLut);
		iProc.applyTable(newLut);

		compareData(bProc,iProc);
	}

	@Test
	public void testConvolve() {
		
		/* TODO - enable
		float[][] kernel2d = new float[][] {{-1.2f,-1.2f,-1.2f,-1.2f,-1.2f},
											{-1.2f,-2.4f,-2.4f,-2.4f,-1.2f},
											{-1.2f,-2.4f,38.4f,-2.4f,-1.2f},
											{-1.2f,-2.4f,-2.4f,-2.4f,-1.2f},
											{-1.2f,-1.2f,-1.2f,-1.2f,-1.2f}};

		int kh = kernel2d.length;
		int kw = kernel2d[0].length;
		
		float[] kernel = new float[kw * kh];
		int i = 0;
		for (int x=0; x < kw; x++)
			for (int y=0; y < kh; y++)
				kernel[i++] = kernel2d[x][y];
		
		bProc.convolve(kernel, kw, kh);
		iProc.convolve(kernel, kw, kh);
		compareData(bProc,iProc);
		*/
	}

	@Test
	public void testConvolve3x3() {
		
		/* TODO - enable
		int[][] kernel2d = new int[][] {{1,3,1},{3,-16,3},{1,3,1}};

		int kh = kernel2d.length;
		int kw = kernel2d[0].length;
		
		int[] kernel = new int[kw * kh];
		int i = 0;
		for (int x=0; x < kw; x++)
			for (int y=0; y < kh; y++)
				kernel[i++] = kernel2d[x][y];
		
		bProc.convolve3x3(kernel);
		iProc.convolve3x3(kernel);
		compareData(bProc,iProc);
		*/
	}

	@Test
	public void testCopyBits() {
		
		/* TODO - enable
		ImageProcessor data = new ByteProcessor(5,5,new byte[]{-1,1,-2,2,-3,3,1,2,3,4,5,6,7,8,11,10,9,8,7,6,5,4,3,2,1},null);
		
		for (int mode = ImageProcessor.INVERT; mode <= ImageProcessor.ABS; mode++)
		{
			bProc.copyBits(data, 23, 19, mode);
			iProc.copyBits(data, 23, 19, mode);
			compareData(bProc,iProc);
		}
		*/
	}

	@Test
	public void testCreateImage() {
		java.awt.Image bImage = bProc.createImage(); 
		java.awt.Image iImage = iProc.createImage(); 

		assertNotNull(bImage);
		assertNotNull(iImage);
		
		// TODO - do some other kind of comparisons?
	}

	@Test
	public void testCreateImagePlus() {
		
		// TODO - enable
		//   note it crashes right now. its possible the extraDimenions need to be passed in to ImgLibProcessor so that it knows which
		//       slice of the image the processor is tied with.
		/*
		ContainerFactory contFact = new ArrayContainerFactory();
		ImageFactory<UnsignedShortType> factory = new ImageFactory<UnsignedShortType>(new UnsignedShortType(), contFact);
		Image<UnsignedShortType> image = factory.createImage(new int[] {3,4,5,6,7});
		ImagePlus imp = ImgLibProcessor.createImagePlus(image);
		
		int slices   = image.getDimension(2);
		int channels = image.getDimension(3);
		int frames   = image.getDimension(4);
		
		int totalPlanes = slices * channels * frames;
		
		assertEquals(totalPlanes,imp.getNDimensions());
		assertEquals(frames,imp.getNFrames());
		assertEquals(channels,imp.getNChannels());
		assertEquals(slices,imp.getNSlices());
		
		ImageStack stack = imp.getStack();
		for (int i = 0; i < totalPlanes; i++)
		{
			ImageProcessor proc = stack.getProcessor(i+1); 
			assertTrue(proc instanceof ImgLibProcessor<?>);
			assertEquals(image.getDimension(0),proc.getWidth());
			assertEquals(image.getDimension(1),proc.getHeight());
		}
		*/
	}

	@Test
	public void testCreateProcessor() {
		
		int width = 73;
		int height = 22;
		
		ImageProcessor newProc = iProc.createProcessor(width,height);
		
		assertEquals(width,newProc.getWidth());
		assertEquals(height,newProc.getHeight());
		
		assertEquals(iProc.getMin(),newProc.getMin(),Assert.DOUBLE_TOL);
		assertEquals(iProc.getMax(),newProc.getMax(),Assert.DOUBLE_TOL);
		assertEquals(iProc.getColorModel(),newProc.getColorModel());
		assertEquals(iProc.getInterpolate(),newProc.getInterpolate());
	}

	@Test
	public void testCrop() {
	
		int ox = 22;
		int oy = 53;
		int w = 107;
		int h = 214;
		
		bProc.setRoi(ox,oy,w,h);
		iProc.setRoi(ox,oy,w,h);
		
		ImageProcessor baseline = bProc.crop();
		ImageProcessor result = iProc.crop();
		
		compareData(bProc,iProc);
	}

	@Test
	public void testDilate() {
		
		/* TODO - enable
		iProc.dilate();
		bProc.dilate();
		compareData(bProc,iProc);

		iProc.dilate();
		bProc.dilate();
		compareData(bProc,iProc);

		iProc.dilate();
		bProc.dilate();
		compareData(bProc,iProc);
		*/
	}

	@Test
	public void testDrawPixel() {
		
		/* TODO - enable

		bProc.setColor(14);
		iProc.setColor(14);
		
		for (int x = 3; x < 22; x++)
			for (int y = 10; y < 79; y++)
			{
				bProc.drawPixel(x, y);
				iProc.drawPixel(x, y);
			}
		
		compareData(bProc,iProc);
		*/
	}

	@Test
	public void testDuplicate() {
		
		/* TODO - enable
		ImageProcessor newProc = iProc.duplicate();
		assertTrue(newProc instanceof ImgLibProcessor<?>);
		compareData(iProc,newProc);
		*/
	}

	@Test
	public void testErode() {

		/* TODO - enable
		iProc.erode();
		bProc.erode();
		compareData(bProc,iProc);

		iProc.erode();
		bProc.erode();
		compareData(bProc,iProc);

		iProc.erode();
		bProc.erode();
		compareData(bProc,iProc);
		*/
	}

	@Test
	public void testFillImageProcessor() {
		
		/* TODO - enable
		ByteProcessor byteMask = new ByteProcessor(width,height);
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				if ((x+y)%2 == 0)
					byteMask.set(x,y,1);
		
		bProc.setColor(7);
		iProc.setColor(7);
		
		bProc.fill(byteMask);
		iProc.fill(byteMask);

		compareData(bProc,iProc);
		*/
	}

	@Test
	public void testFilter() {
		
		/* TODO - enable
		for (int filterType = ImageProcessor.BLUR_MORE; filterType <= ImageProcessor.CONVOLVE; filterType++)
		{
			initialize();
			bProc.filter(filterType);
			iProc.filter(filterType);
			compareData(bProc,iProc);
		}
		*/
	}

	@Test
	public void testFlipVertical() {
		iProc.flipVertical();
		bProc.flipVertical();
		compareData(bProc,iProc);
	}

	@Test
	public void testGetBackgroundValue() {
		assertEquals(bProc.getBackgroundValue(),iProc.getBackgroundValue(),0.0);
		for (int i = 0; i < 25; i++)
		{
			bProc.setBackgroundValue(i+0.5);
			iProc.setBackgroundValue(i+0.5);
			assertEquals(bProc.getBackgroundValue(),iProc.getBackgroundValue(),0.0);
		}
	}

	@Test
	public void testGetfInt() {

		int maxPixels = width*height;
		
		assertEquals(bProc.getf(0*maxPixels/5), iProc.getf(0*maxPixels/5), Assert.FLOAT_TOL);
		assertEquals(bProc.getf(1*maxPixels/5), iProc.getf(1*maxPixels/5), Assert.FLOAT_TOL);
		assertEquals(bProc.getf(2*maxPixels/5), iProc.getf(2*maxPixels/5), Assert.FLOAT_TOL);
		assertEquals(bProc.getf(3*maxPixels/5), iProc.getf(3*maxPixels/5), Assert.FLOAT_TOL);
		assertEquals(bProc.getf(4*maxPixels/5), iProc.getf(4*maxPixels/5), Assert.FLOAT_TOL);
	}

	@Test
	public void testGetfIntInt() {
		
		assertEquals(bProc.getf(0,0), iProc.getf(0,0), Assert.FLOAT_TOL);
		assertEquals(bProc.getf(width-1,0), iProc.getf(width-1,0), Assert.FLOAT_TOL);
		assertEquals(bProc.getf(0,height-1), iProc.getf(0,height-1), Assert.FLOAT_TOL);
		assertEquals(bProc.getf(width-1,height-1), iProc.getf(width-1,height-1), Assert.FLOAT_TOL);
		assertEquals(bProc.getf(width/2,height/2), iProc.getf(width/2,height/2), Assert.FLOAT_TOL);
	}

	@Test
	public void testGetHistogram() {
		/* TODO - enable
		int[] bHist = bProc.getHistogram();
		int[] iHist = bProc.getHistogram();
		assertArrayEquals(bHist,iHist);
		*/
	}

	@Test
	public void testGetInt() {

		int maxPixels = width*height;
		
		assertEquals(bProc.get(0*maxPixels/5), iProc.get(0*maxPixels/5), Assert.FLOAT_TOL);
		assertEquals(bProc.get(1*maxPixels/5), iProc.get(1*maxPixels/5), Assert.FLOAT_TOL);
		assertEquals(bProc.get(2*maxPixels/5), iProc.get(2*maxPixels/5), Assert.FLOAT_TOL);
		assertEquals(bProc.get(3*maxPixels/5), iProc.get(3*maxPixels/5), Assert.FLOAT_TOL);
		assertEquals(bProc.get(4*maxPixels/5), iProc.get(4*maxPixels/5), Assert.FLOAT_TOL);
	}

	@Test
	public void testGetIntInt() {
		assertEquals(bProc.get(0,0), iProc.get(0,0), Assert.FLOAT_TOL);
		assertEquals(bProc.get(width-1,0), iProc.get(width-1,0), Assert.FLOAT_TOL);
		assertEquals(bProc.get(0,height-1), iProc.get(0,height-1), Assert.FLOAT_TOL);
		assertEquals(bProc.get(width-1,height-1), iProc.get(width-1,height-1), Assert.FLOAT_TOL);
		assertEquals(bProc.get(width/2,height/2), iProc.get(width/2,height/2), Assert.FLOAT_TOL);
	}

	@Test
	public void testGetInterpolatedPixel() {
		
		/* TODO - enable
		for (int interpMethod : new int[]{ImageProcessor.NONE,ImageProcessor.BILINEAR,ImageProcessor.BICUBIC})
		{
			bProc.setInterpolationMethod(interpMethod);
			iProc.setInterpolationMethod(interpMethod);
		
			double[][] points = new double[][] {
					new double[] {0,0},
					new double[] {width-1,0},
					new double[] {0,height-1},
					new double[] {width-1,height-1},
					new double[] {-1,-1},
					new double[] {5000,5000},
					new double[] {1,1},
					new double[] {4.7,3.2},
					new double[] {9.1,18.9},
					new double[] {25.75,96.35}
			};
			
			for (double[] point : points)
			{
				double x = point[0];
				double y = point[1];
				assertEquals(bProc.getInterpolatedPixel(x,y), iProc.getInterpolatedPixel(x,y), Assert.DOUBLE_TOL);
			}
		}
		*/
	}

	@Test
	public void testGetMax() {
		assertEquals(bProc.getMax(), iProc.getMax(), Assert.DOUBLE_TOL);
	}

	@Test
	public void testGetMin() {
		assertEquals(bProc.getMin(), iProc.getMin(), Assert.DOUBLE_TOL);
	}

	@Test
	public void testGetPixelInterpolated() {
		/* TODO - enable
		for (int interpMethod : new int[]{ImageProcessor.NONE,ImageProcessor.BILINEAR,ImageProcessor.BICUBIC})
		{
			bProc.setInterpolationMethod(interpMethod);
			iProc.setInterpolationMethod(interpMethod);
		
			double[][] points = new double[][] {
					new double[] {0,0},
					new double[] {width-1,0},
					new double[] {0,height-1},
					new double[] {width-1,height-1},
					new double[] {-1,-1},
					new double[] {5000,5000},
					new double[] {1,1},
					new double[] {4.7,3.2},
					new double[] {9.1,18.9},
					new double[] {25.75,96.35}
			};
			
			for (double[] point : points)
			{
				double x = point[0];
				double y = point[1];
				assertEquals(bProc.getPixelInterpolated(x,y), iProc.getPixelInterpolated(x,y), Assert.DOUBLE_TOL);
			}
		}
		*/
	}

	@Test
	public void testGetPixelIntInt() {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				assertEquals(bProc.get(x,y),iProc.get(x,y));
			}
		}
	}

	@Test
	public void testGetPixels() {
		byte[] bPix = (byte[])bProc.getPixels();
		byte[] iPix = (byte[])iProc.getPixels();
		
		assertArrayEquals(bPix,iPix);
	}

	@Test
	public void testGetPixelsCopy() {
		byte[] bPix = (byte[])bProc.getPixelsCopy();
		byte[] iPix = (byte[])iProc.getPixelsCopy();
		
		assertArrayEquals(bPix,iPix);
	}

	@Test
	public void testGetPixelValue() {

		/* TODO - enable
		
		// a few cases in ByteProc & ShortProc
		//   out of bounds
		//   in bounds with no ctable
		//   in bounds with ctable
		// FloatProc - just basically a get(x,y)
		
		
		// TODO - do something that alters whether cTable is created or not and try all cases below
		
		int[][] badPoints = new int[][] {
				new int[] {0,-1},
				new int[] {-1,0},
				new int[] {0,height},
				new int[] {width,0},
				new int[] {width,height},
				new int[] {-1,-1},
				new int[] {5000,5000}
		};
		
		for (int[] point : badPoints)
		{
			int x = point[0];
			int y = point[1];
			assertEquals(bProc.getPixelValue(x,y), iProc.getPixelValue(x,y), Assert.DOUBLE_TOL);
		}

		// all the good points
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				assertEquals(bProc.getPixelValue(x,y), iProc.getPixelValue(x,y), Assert.DOUBLE_TOL);
		*/
	}

	@Test
	public void testGetPlaneData() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneDataImageOfTIntIntIntArray() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneBytes() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneUnsignedBytes() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneShorts() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneUnsignedShorts() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneInts() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneUnsignedInts() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneLongs() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneFloats() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneDoubles() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetSnapshotPixels() {
		//fail("Not yet implemented");
	}

	@Test
	public void testImgLibProcessor() {
		assertNotNull(iProc);
	}

	@Test
	public void testMedianFilter() {
		//fail("Not yet implemented");
	}

	@Test
	public void testNoise() {
		//fail("Not yet implemented");
	}

	@Test
	public void testPutPixelIntIntInt() {
		//fail("Not yet implemented");
	}

	@Test
	public void testPutPixelValue() {
		//fail("Not yet implemented");
	}

	@Test
	public void testResetImageProcessor() {

		ByteProcessor mask = new ByteProcessor(7, 7);
		for (int x = 0; x < 7; x++)
			for (int y = 0; y < 7; y++)
				mask.set(x,y,(x+y)%2);
		
		bProc.setRoi(1, 2, 7, 7);
		iProc.setRoi(1, 2, 7, 7);
		
		bProc.snapshot();
		iProc.snapshot();
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				bProc.set(x,y,(x+y)%256);
				iProc.set(x,y,(x+y)%256);
			}
		}
		
		compareData(bProc,iProc);

		bProc.reset(mask);
		iProc.reset(mask);
		
		compareData(bProc,iProc);
	}

	@Test
	public void testResizeIntInt() {
		//fail("Not yet implemented");
	}

	@Test
	public void testRotate() {
		//fail("Not yet implemented");
	}

	@Test
	public void testScale() {
		//fail("Not yet implemented");
	}

	@Test
	public void testSetBackgroundValue() {
		//fail("Not yet implemented");
	}

	@Test
	public void testSetColorColor() {

		//fail("Not yet implemented");
	}

	@Test
	public void testSetfIntFloat() {
	
		int maxPixels = width*height;
		
		int numChanges = 5;
		for (int changeNum = 0; changeNum < numChanges; changeNum++)
		{
			int changeIndex = changeNum * maxPixels / numChanges;
			float changeValue = 10.0f + (1.1f * numChanges);
			bProc.setf(changeIndex, changeValue);
			iProc.setf(changeIndex, changeValue);
		}
		
		compareData(bProc,iProc);
	}

	@Test
	public void testSetfIntIntFloat() {
		
		// set the ByteProcessor
		bProc.setf(0, 0, 11.1f);
		bProc.setf(0, height-1, 22.3f);
		bProc.setf(width-1, 0, 33.5f);
		bProc.setf(width-1, height-1, 44.7f);
		bProc.setf(width/2,height/2, 55.9f);

		// set the ImgLibProcessor
		iProc.setf(0, 0, 11.1f);
		iProc.setf(0, height-1, 22.3f);
		iProc.setf(width-1, 0, 33.5f);
		iProc.setf(width-1, height-1, 44.7f);
		iProc.setf(width/2,height/2, 55.9f);
		
		compareData(bProc,iProc);
	}

	@Test
	public void testSetIntInt() {

		int maxPixels = width*height;
		
		int numChanges = 8;
		for (int changeNum = 0; changeNum < numChanges; changeNum++)
		{
			int changeIndex = changeNum * maxPixels / numChanges;
			int changeValue = 20 + (10 * numChanges);
			bProc.set(changeIndex, changeValue);
			iProc.set(changeIndex, changeValue);
		}
		
		compareData(bProc,iProc);
	}

	@Test
	public void testSetIntIntInt() {
		
		// set the ByteProcessor
		bProc.set(0, 0, 50);
		bProc.set(0, height-1, 60);
		bProc.set(width-1, 0, 70);
		bProc.set(width-1, height-1, 80);
		bProc.set(width/2,height/2,90);

		// set the ImgLibProcessor
		iProc.set(0, 0, 50);
		iProc.set(0, height-1, 60);
		iProc.set(width-1, 0, 70);
		iProc.set(width-1, height-1, 80);
		iProc.set(width/2,height/2,90);
		
		compareData(bProc,iProc);
	}

	@Test
	public void testSetMinAndMax() {
		
		double min = 22.0;
		double max = 96.0;
		
		bProc.setMinAndMax(min, max);
		iProc.setMinAndMax(min, max);
		
		assertEquals(bProc.getMin(),iProc.getMin(),0);
		assertEquals(bProc.getMax(),iProc.getMax(),0);
	}

	@Test
	public void testSetPixelsIntFloatProcessor() {
		//fail("Not yet implemented");
	}

	@Test
	public void testSetPixelsObject() {
		//fail("Not yet implemented");
	}

	@Test
	public void testSetSnapshotPixels() {
		
		// this one will act differently between the processors  since iProc should make data copies and bProc shouldn't
		// just make sure contents of the image snapshot match
		
		byte[] origPixels = (byte[])bProc.getPixelsCopy();
		
		byte[] newPixels = new byte [origPixels.length];
		
		for (int i = 0; i < newPixels.length; i++)
			newPixels[i] = (byte) (i % 50);
		
		bProc.setSnapshotPixels(newPixels);
		bProc.reset();

		iProc.setSnapshotPixels(newPixels);
		iProc.reset();
		
		assertArrayEquals((byte[])bProc.getPixels(),(byte[])iProc.getPixels());
	}

	@Test
	public void testSetValue() {
		
		bProc.setValue(0);
		iProc.setValue(0);
		assertEquals(bProc.fgColor,iProc.fgColor);

		bProc.setValue(-1);
		iProc.setValue(-1);
		assertEquals(bProc.fgColor,iProc.fgColor);

		bProc.setValue(1);
		iProc.setValue(1);
		assertEquals(bProc.fgColor,iProc.fgColor);

		bProc.setValue(14.2);
		iProc.setValue(14.2);
		assertEquals(bProc.fgColor,iProc.fgColor);
	}

	@Test
	public void testSnapshotAndReset() {
		
		bProc.snapshot();
		iProc.snapshot();
	
		for (int i = 0; i < width*height; i++)
		{
			bProc.set(i, i%256);
			iProc.set(i, i%256);
		}
		
		compareData(bProc,iProc);
		
		bProc.reset();
		iProc.reset();
		
		compareData(bProc,iProc);
	}

	@Test
	public void testThreshold() {
		int numTests = 5;
		for (int i = 0; i < 255; i+= 256/numTests)
		{
			initialize();
			bProc.threshold(i);
			iProc.threshold(i);
			compareData(bProc,iProc);
		}
	}

	@Test
	public void testToFloat() {
		
		FloatProcessor bFloat, iFloat;
		
		bFloat = bProc.toFloat(0, null);
		iFloat = iProc.toFloat(0, null);
		
		compareData(bProc,iProc);

		bFloat = bProc.toFloat(1, null);
		iFloat = iProc.toFloat(1, null);
		
		compareData(bProc,iProc);
	}
}
