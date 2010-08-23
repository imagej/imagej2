package ij.process;

import static org.junit.Assert.*;

import java.awt.Color;
import java.util.Random;

import org.junit.Before;
import org.junit.BeforeClass;

import ij.Assert;

import mpicbg.imglib.container.array.ArrayContainerFactory;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImageFactory;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;

import org.junit.Test;

// TODO - Right now doing all comparisons versus a ByteProcessor. Add comparison code vs. FloatProcessor and ShortProcessor also

public class ImgLibProcessorTest {

	// ************* Instance variables ***********************************************

	static boolean SKIP_UNFINISHED = true;  // some ImgLibProcessor methods are unimplemented. 
	
	static int width;
	static int height;
	static ImgLibProcessor<UnsignedByteType> origIProc;
	static ByteProcessor origBProc;
	
	ImgLibProcessor<UnsignedByteType> iProc;
	ByteProcessor bProc;
	//FloatProcessor fProc;
	//ShortProcessor sProc;
	//ColorProcessor cProc;  // may not need to test this for comparison

	// ************* Helper methods ***********************************************
	
	// this initialization code runs once - load the test image
	@BeforeClass
	public static void setup()
	{
		/*
		String filename = "data/head8bit.tif";
		
		ImagePlus imp = new ImagePlus(filename);
		origBProc = (ByteProcessor) imp.getProcessor();
		width = origBProc.getWidth();
		height = origBProc.getHeight();
		
		final ContainerFactory containerFactory = new ArrayContainerFactory();
		Image<UnsignedByteType> image = LOCI.openLOCIUnsignedByteType(filename, containerFactory);
		origIProc = new ImgLibProcessor<UnsignedByteType>(image, new UnsignedByteType(), 0);
		 */
		
		/* TODO - without a cachedCursor 300 x 400 kills this puppy with over 4 gig of ram use and system basically halts during tests */
        //   with cachedCursor tests to 10Kx900 and worked though somewhat slow
		width = 343;
		height = 769;
		
		origBProc = new ByteProcessor(width, height);
		
		ImageFactory<UnsignedByteType> factory = new ImageFactory<UnsignedByteType>(new UnsignedByteType(), new ArrayContainerFactory());
		
		Image<UnsignedByteType> image = factory.createImage(new int[]{width, height});

		origIProc = new ImgLibProcessor<UnsignedByteType>(image, new UnsignedByteType(), 0);
			
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int value = (19 + (x+y)) % 256;
				origBProc.set(x, y, value);
				origIProc.set(x, y, value);
			}
		}

		origIProc.setMinAndMax(0, 255);  // TODO - if not here then getMax() test fails. Might point out some ImgLibProc code needed. Although
											//     it might just be that IJ calls setMinAndMax() and we aren't using IJ infrastructure yet.
		compareData(origBProc, origIProc);
	}
	
	// the following initialization code runs before every test
	@Before
	public void initialize()
	{
		bProc = (ByteProcessor)origBProc.duplicate();
		iProc = (ImgLibProcessor)origIProc.duplicate();
		compareData(bProc, iProc);
	}

	// ************* Helper tests ***********************************************

	private static void compareData(ImageProcessor baselineProc, ImageProcessor testedProc)
	{
		int w = baselineProc.getWidth();
		int h = baselineProc.getHeight();
	
		assertEquals(w, testedProc.getWidth());
		assertEquals(h, testedProc.getHeight());
	
		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++)
				if (Math.abs(baselineProc.getf(x, y) - testedProc.getf(x, y)) > Assert.FLOAT_TOL)
					fail("processor data differs at ("+x+","+y+") : ij(" + baselineProc.getf(x, y) +") imglib("+testedProc.getf(x, y)+")");
	}

	// ************* Constructor tests ***********************************************

	// constructor 1
	@Test
	public void testImgLibProcessorPosition()
	{
		int width = 3, height = 5;
		
		ImageFactory<UnsignedByteType> factory = new ImageFactory<UnsignedByteType>(new UnsignedByteType(), new ArrayContainerFactory());
		
		Image<UnsignedByteType> image = factory.createImage(new int[]{width, height, 1});

		ImgLibProcessor<?> proc = new ImgLibProcessor<UnsignedByteType>(image, new UnsignedByteType(), new int[]{0});
		
		assertNotNull(proc);
		assertEquals(width, proc.getWidth());
		assertEquals(height, proc.getHeight());
	}

	// constructor 2
	@Test
	public void testImgLibProcessorPlane()
	{
		int width = 3, height = 5;
		
		ImageFactory<UnsignedByteType> factory = new ImageFactory<UnsignedByteType>(new UnsignedByteType(), new ArrayContainerFactory());
		
		Image<UnsignedByteType> image = factory.createImage(new int[]{width, height});

		ImgLibProcessor<?> proc = new ImgLibProcessor<UnsignedByteType>(image, new UnsignedByteType(), 0);
		
		assertNotNull(proc);
		assertEquals(width, proc.getWidth());
		assertEquals(height, proc.getHeight());
	}

	// ************* All other tests ***********************************************

	@Test
	public void testAbs()
	{
		//if (SKIP_UNFINISHED) return;
		
		for (int i = 0; i < 12; i++)
		{
			bProc.abs();
			iProc.abs();
			compareData(bProc, iProc);
		}
	}

	@Test
	public void testAddDouble()
	{
		//if (SKIP_UNFINISHED) return;
		
		for (int i = 0; i < 12; i++)
		{
			double value = i*Math.PI;
			bProc.add(value);
			iProc.add(value);
			compareData(bProc, iProc);
		}
	}
	
	@Test
	public void testAddInt()
	{
		//if (SKIP_UNFINISHED) return;
		
		for (int i = 0; i < 12; i++)
		{
			bProc.add(i);
			iProc.add(i);
			compareData(bProc, iProc);
		}
	}
	
	@Test
	public void testAndInt()
	{
		//if (SKIP_UNFINISHED) return;
		
		for (int i = 0; i < 12; i++)
		{
			bProc.and(i);
			iProc.and(i);
			compareData(bProc, iProc);
		}
	}
	
	@Test
	public void testApplyTable()
	{
		// make an inverted lut
		int[] newLut = new int[256];
		for (int i = 0; i < 256; i++)
			newLut[i] = 255 - i;
		
		// apply to both
		bProc.applyTable(newLut);
		iProc.applyTable(newLut);

		compareData(bProc, iProc);
	}

	@Test
	public void testConvolve()
	{
		if (SKIP_UNFINISHED) return;
		
		float[][] kernel2d = new float[][] {{-1.2f, -1.2f, -1.2f, -1.2f, -1.2f},
											{-1.2f, -2.4f, -2.4f, -2.4f, -1.2f},
											{-1.2f, -2.4f, 38.4f, -2.4f, -1.2f},
											{-1.2f, -2.4f, -2.4f, -2.4f, -1.2f},
											{-1.2f, -1.2f, -1.2f, -1.2f, -1.2f}};

		int kh = kernel2d.length;
		int kw = kernel2d[0].length;
		
		float[] kernel = new float[kw * kh];
		int i = 0;
		for (int x=0; x < kw; x++)
			for (int y=0; y < kh; y++)
				kernel[i++] = kernel2d[x][y];
		
		bProc.convolve(kernel, kw, kh);
		iProc.convolve(kernel, kw, kh);
		compareData(bProc, iProc);
	}

	@Test
	public void testConvolve3x3()
	{
		if (SKIP_UNFINISHED) return;
		
		int[][] kernel2d = new int[][] {{1,3,1}, {3,-16,3}, {1,3,1}};

		int kh = kernel2d.length;
		int kw = kernel2d[0].length;
		
		int[] kernel = new int[kw * kh];
		int i = 0;
		for (int x=0; x < kw; x++)
			for (int y=0; y < kh; y++)
				kernel[i++] = kernel2d[x][y];
		
		bProc.convolve3x3(kernel);
		iProc.convolve3x3(kernel);
		compareData(bProc, iProc);
	}

	@Test
	public void testCopyBits()
	{
		byte[] bytes = new byte[256];
		for (int b = Byte.MIN_VALUE; b <= Byte.MAX_VALUE; b++)
			bytes[b-Byte.MIN_VALUE] = (byte) b;
		
		ImageProcessor data = new ByteProcessor(16, 16, bytes, null);
		
		for (int mode = Blitter.COPY; mode <= Blitter.COPY_ZERO_TRANSPARENT; mode++)
		{
			bProc.copyBits(data, 23, 19, mode);
			iProc.copyBits(data, 23, 19, mode);
			//System.out.println("blitter mode: "+mode);
			compareData(bProc, iProc);
		}
	}

	@Test
	public void testCreateImage()
	{
		java.awt.Image bImage = bProc.createImage(); 
		java.awt.Image iImage = iProc.createImage(); 

		assertNotNull(bImage);
		assertNotNull(iImage);
		
		// TODO - do some other kind of comparisons?
	}

	@Test
	public void testCreateProcessor()
	{
		int width = 73;
		int height = 22;
		
		ImageProcessor newProc = iProc.createProcessor(width, height);
		
		assertEquals(width, newProc.getWidth());
		assertEquals(height, newProc.getHeight());
		
		assertEquals(iProc.getMin(), newProc.getMin(), Assert.DOUBLE_TOL);
		assertEquals(iProc.getMax(), newProc.getMax(), Assert.DOUBLE_TOL);
		assertEquals(iProc.getColorModel(), newProc.getColorModel());
		assertEquals(iProc.getInterpolate(), newProc.getInterpolate());
	}

	@Test
	public void testCrop()
	{
		int ox = 3, oy = 10, w = width/2, h = height/2;
		
		assertTrue((ox+w) <= width);
		assertTrue((oy+h) <= height);
		
		bProc.setRoi(ox, oy, w, h);
		iProc.setRoi(ox, oy, w, h);
		
		ImageProcessor baseline = bProc.crop();
		ImageProcessor result = iProc.crop();
		
		compareData(baseline, result);
	}

	@Test
	public void testDilate()
	{
		if (SKIP_UNFINISHED) return;
		
		iProc.dilate();
		bProc.dilate();
		compareData(bProc, iProc);

		iProc.dilate();
		bProc.dilate();
		compareData(bProc, iProc);

		iProc.dilate();
		bProc.dilate();
		compareData(bProc, iProc);
	}

	@Test
	public void testDrawPixel()
	{
		int ox = 3, oy = 10, w = width/2, h = height/2;
		
		assertTrue((ox+w) <= width);
		assertTrue((oy+h) <= height);
		
		bProc.setColor(14);
		iProc.setColor(14);
		
		for (int x = ox; x < ox+w; x++)
			for (int y = oy; y < oy+h; y++)
			{
				bProc.drawPixel(x, y);
				iProc.drawPixel(x, y);
			}
		
		compareData(bProc, iProc);
	}

	@Test
	public void testDuplicate()
	{
		ImageProcessor newProc = iProc.duplicate();
		assertTrue(newProc instanceof ImgLibProcessor<?>);
		compareData(iProc, newProc);
	}

	@Test
	public void testErode() 
	{
		if (SKIP_UNFINISHED) return;
		
		iProc.erode();
		bProc.erode();
		compareData(bProc, iProc);

		iProc.erode();
		bProc.erode();
		compareData(bProc, iProc);

		iProc.erode();
		bProc.erode();
		compareData(bProc, iProc);
	}
	
	@Test
	public void testExp()
	{
		//if (SKIP_UNFINISHED) return;
		
		for (int i = 0; i < 12; i++)
		{
			bProc.exp();
			iProc.exp();
			compareData(bProc, iProc);
		}
	}

	@Test
	public void testFill()
	{
		//if (SKIP_UNFINISHED) return;
		
		bProc.setColor(7);
		iProc.setColor(7);
		
		bProc.fill();
		iProc.fill();

		compareData(bProc, iProc);
	}

	@Test
	public void testFillImageProcessor()
	{
		ByteProcessor byteMask = new ByteProcessor(width, height);
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				if ((x+y)%2 == 0)
					byteMask.set(x, y, 1);
		
		bProc.setColor(19);
		iProc.setColor(19);
		
		bProc.fill(byteMask);
		iProc.fill(byteMask);

		compareData(bProc, iProc);
	}

	@Test
	public void testFilter()
	{
		if (SKIP_UNFINISHED) return;
		
		for (int filterType = ImageProcessor.BLUR_MORE; filterType <= ImageProcessor.CONVOLVE; filterType++)
		{
			initialize();
			bProc.filter(filterType);
			iProc.filter(filterType);
			compareData(bProc, iProc);
		}
	}

	@Test
	public void testFlipVertical()
	{
		iProc.flipVertical();
		bProc.flipVertical();
		compareData(bProc, iProc);
	}

	@Test
	public void testGammaDouble()
	{
		//if (SKIP_UNFINISHED) return;
		
		for (int i = 0; i < 12; i++)
		{
			double value = i*0.68;
			bProc.gamma(value);
			iProc.gamma(value);
			compareData(bProc, iProc);
		}
	}

	@Test
	public void testGetBackgroundValue()
	{
		assertEquals(bProc.getBackgroundValue(), iProc.getBackgroundValue(), 0.0);
		for (int i = 0; i < 25; i++)
		{
			bProc.setBackgroundValue(i+0.5);
			iProc.setBackgroundValue(i+0.5);
			assertEquals(bProc.getBackgroundValue(), iProc.getBackgroundValue(), 0.0);
		}
	}

	@Test
	public void testGetfInt()
	{
		int maxPixels = width*height;
		
		assertEquals(bProc.getf(0*maxPixels/5), iProc.getf(0*maxPixels/5), Assert.FLOAT_TOL);
		assertEquals(bProc.getf(1*maxPixels/5), iProc.getf(1*maxPixels/5), Assert.FLOAT_TOL);
		assertEquals(bProc.getf(2*maxPixels/5), iProc.getf(2*maxPixels/5), Assert.FLOAT_TOL);
		assertEquals(bProc.getf(3*maxPixels/5), iProc.getf(3*maxPixels/5), Assert.FLOAT_TOL);
		assertEquals(bProc.getf(4*maxPixels/5), iProc.getf(4*maxPixels/5), Assert.FLOAT_TOL);
	}

	@Test
	public void testGetfIntInt()
	{
		assertEquals(bProc.getf(0,0), iProc.getf(0,0), Assert.FLOAT_TOL);
		assertEquals(bProc.getf(width-1,0), iProc.getf(width-1,0), Assert.FLOAT_TOL);
		assertEquals(bProc.getf(0,height-1), iProc.getf(0,height-1), Assert.FLOAT_TOL);
		assertEquals(bProc.getf(width-1,height-1), iProc.getf(width-1,height-1), Assert.FLOAT_TOL);
		assertEquals(bProc.getf(width/2,height/2), iProc.getf(width/2,height/2), Assert.FLOAT_TOL);
	}

	@Test
	public void testGetHistogram()
	{
		// regular case
		int[] bHist = bProc.getHistogram();
		int[] iHist = iProc.getHistogram();
		assertArrayEquals(bHist, iHist);
		
		// masked case
		ImageProcessor mask = new ByteProcessor(width, height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				bProc.set(x, y, (x+y) % 2);
			}
		}
		bProc.setMask(mask);
		iProc.setMask(mask);
		bHist = bProc.getHistogram();
		iHist = iProc.getHistogram();
		assertArrayEquals(bHist, iHist);
	}

	@Test
	public void testGetInt()
	{
		int maxPixels = width*height;
		
		assertEquals(bProc.get(0*maxPixels/5), iProc.get(0*maxPixels/5), Assert.FLOAT_TOL);
		assertEquals(bProc.get(1*maxPixels/5), iProc.get(1*maxPixels/5), Assert.FLOAT_TOL);
		assertEquals(bProc.get(2*maxPixels/5), iProc.get(2*maxPixels/5), Assert.FLOAT_TOL);
		assertEquals(bProc.get(3*maxPixels/5), iProc.get(3*maxPixels/5), Assert.FLOAT_TOL);
		assertEquals(bProc.get(4*maxPixels/5), iProc.get(4*maxPixels/5), Assert.FLOAT_TOL);
	}

	@Test
	public void testGetIntInt()
	{
		assertEquals(bProc.get(0,0), iProc.get(0,0), Assert.FLOAT_TOL);
		assertEquals(bProc.get(width-1,0), iProc.get(width-1,0), Assert.FLOAT_TOL);
		assertEquals(bProc.get(0,height-1), iProc.get(0,height-1), Assert.FLOAT_TOL);
		assertEquals(bProc.get(width-1,height-1), iProc.get(width-1,height-1), Assert.FLOAT_TOL);
		assertEquals(bProc.get(width/2,height/2), iProc.get(width/2,height/2), Assert.FLOAT_TOL);
	}

	@Test
	public void testGetInterpolatedPixel()
	{
		for (int interpMethod : new int[]{ImageProcessor.NONE, ImageProcessor.BILINEAR, ImageProcessor.BICUBIC})
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
				assertEquals(bProc.getInterpolatedPixel(x, y), iProc.getInterpolatedPixel(x, y), Assert.DOUBLE_TOL);
			}
		}
	}

	@Test
	public void testGetMax()
	{
		assertEquals(bProc.getMax(), iProc.getMax(), Assert.DOUBLE_TOL);
		bProc.setMinAndMax(42.4,107.6);
		iProc.setMinAndMax(42.4,107.6);
		assertEquals(bProc.getMax(), iProc.getMax(), Assert.DOUBLE_TOL);
	}

	@Test
	public void testGetMin()
	{
		assertEquals(bProc.getMin(), iProc.getMin(), Assert.DOUBLE_TOL);
		bProc.setMinAndMax(42.4,107.6);
		iProc.setMinAndMax(42.4,107.6);
		assertEquals(bProc.getMin(), iProc.getMin(), Assert.DOUBLE_TOL);
	}

	@Test
	public void testGetPixelInterpolated()
	{
		for (int interpMethod : new int[]{ImageProcessor.NONE, ImageProcessor.BILINEAR, ImageProcessor.BICUBIC})
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
				//System.out.println("method("+interpMethod+") : attempting at point ("+x+","+y+")");
				assertEquals(bProc.getPixelInterpolated(x, y), iProc.getPixelInterpolated(x, y), Assert.DOUBLE_TOL);
			}
		}
	}

	@Test
	public void testGetPixelIntInt()
	{
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				assertEquals(bProc.get(x, y), iProc.get(x, y));
			}
		}
	}

	@Test
	public void testGetPixels()
	{
		byte[] bPix = (byte[])bProc.getPixels();
		byte[] iPix = (byte[])iProc.getPixels();
		
		assertArrayEquals(bPix, iPix);
	}

	@Test
	public void testGetPixelsCopy()
	{
		byte[] bPix = (byte[])bProc.getPixelsCopy();
		byte[] iPix = (byte[])iProc.getPixelsCopy();
		
		assertArrayEquals(bPix, iPix);
	}

	@Test
	public void testGetPixelValue()
	{
		// a few cases in ByteProc & ShortProc
		//   out of bounds
		//   in bounds with no ctable
		//   in bounds with ctable
		// FloatProc - just basically a get(x, y)
		
		
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
			assertEquals(bProc.getPixelValue(x, y), iProc.getPixelValue(x, y), Assert.DOUBLE_TOL);
		}

		// all the good points
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				assertEquals(bProc.getPixelValue(x, y), iProc.getPixelValue(x, y), Assert.DOUBLE_TOL);
	}

	@Test
	public void testGetPlaneData()
	{
		double[] pixels = iProc.getPlaneData();
		
		for (int i = 0; i < width*height; i++)
			assertEquals((double)iProc.get(i),  pixels[i], Assert.DOUBLE_TOL);
	}

	@Test
	public void testGetSnapshotPixels() 
	{
		assertNull(bProc.getSnapshotPixels());
		assertNull(iProc.getSnapshotPixels());
		
		bProc.snapshot();
		iProc.snapshot();

		assertArrayEquals((byte[])bProc.getSnapshotPixels(), (byte[])iProc.getSnapshotPixels());
	}

	@Test
	public void testInvert()
	{
		//if (SKIP_UNFINISHED) return;
		
		bProc.invert();
		iProc.invert();
		compareData(bProc, iProc);
	}

	@Test
	public void testLog()
	{
		//if (SKIP_UNFINISHED) return;
		
		for (int i = 0; i < 12; i++)
		{
			bProc.log();
			iProc.log();
			compareData(bProc, iProc);
		}
	}

	@Test
	public void testMaxDouble()
	{
		//if (SKIP_UNFINISHED) return;
		
		for (int i = 0; i < 12; i++)
		{
			double value = 2.0 * i;
			bProc.max(value);
			iProc.max(value);
			compareData(bProc, iProc);
		}
	}

	@Test
	public void testMedianFilter() 
	{
		if (SKIP_UNFINISHED) return;
		
		bProc.medianFilter();
		iProc.medianFilter();
		compareData(bProc, iProc);
	}

	@Test
	public void testMinDouble()
	{
		//if (SKIP_UNFINISHED) return;
		
		for (int i = 0; i < 12; i++)
		{
			double value = 2.0 * i;
			bProc.min(value);
			iProc.min(value);
			compareData(bProc, iProc);
		}
	}
	
	@Test
	public void testMultiplyDouble()
	{
		//if (SKIP_UNFINISHED) return;
		
		for (int i = 0; i < 12; i++)
		{
			double value = i*Math.PI;
			bProc.multiply(value);
			iProc.multiply(value);
			compareData(bProc, iProc);
		}
	}
	
	@Test
	public void testNoise()
	{
		if (SKIP_UNFINISHED) return;
		
		double[] noises = new double[]{0,1,2,3,4,0.5,1.2};
		
		for (double noiseVal : noises) {
			
			initialize();
			bProc.noise(noiseVal);
			iProc.noise(noiseVal);
			compareData(bProc, iProc);
		}
	}

	@Test
	public void testOrInt()
	{
		//if (SKIP_UNFINISHED) return;
		
		for (int i = 0; i < 12; i++)
		{
			bProc.or(i);
			iProc.or(i);
			compareData(bProc, iProc);
		}
	}

	@Test
	public void testPutPixelIntIntInt()
	{
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int newValue = Math.abs(x-y) % 256;
				bProc.putPixel(x, y, newValue);
				iProc.putPixel(x, y, newValue);
			}
		}
		compareData(bProc, iProc);
	}

	@Test
	public void testPutPixelValue()
	{
		//if (SKIP_UNFINISHED) return;
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				double newValue = (Math.abs(x-y) % 512) / 2.7;
				bProc.putPixelValue(x, y, newValue);
				iProc.putPixelValue(x, y, newValue);
			}
		}
		compareData(bProc, iProc);
	}

	@Test
	public void testResetImageProcessor()
	{
		ByteProcessor mask = new ByteProcessor(7, 7);
		for (int x = 0; x < 7; x++)
			for (int y = 0; y < 7; y++)
				mask.set(x, y, (x+y)%2);
		
		bProc.setRoi(1, 2, 7, 7);
		iProc.setRoi(1, 2, 7, 7);
		
		bProc.snapshot();
		iProc.snapshot();
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				bProc.set(x, y, (x+y)%256);
				iProc.set(x, y, (x+y)%256);
			}
		}
		
		compareData(bProc, iProc);

		bProc.reset(mask);
		iProc.reset(mask);
		
		compareData(bProc, iProc);
	}

	@Test
	public void testResizeIntInt()
	{
		if (SKIP_UNFINISHED) return;
		
		for (int interpMethod : new int[]{ImageProcessor.NONE, ImageProcessor.BILINEAR, ImageProcessor.BICUBIC}) {
			int[][] points = new int[][] {
				new int[]{0,0},
				new int[]{width+5,height+5},
				new int[]{width,1},
				new int[]{1,height},
				new int[]{width,height},
				new int[]{bProc.roiWidth,bProc.roiHeight},
				new int[]{10,20},
				new int[]{41,36}
			};
			
			for (int[] point : points) {
				
				ImageProcessor newBProc, newIProc;
				
				bProc.setInterpolationMethod(interpMethod);
				iProc.setInterpolationMethod(interpMethod);
				
				newBProc = bProc.resize(point[0], point[1]);
				newIProc = iProc.resize(point[0], point[1]);
				
				compareData(newBProc, newIProc);
			}
		}
	}

	@Test
	public void testRotate() 
	{
		for (int interpMethod : new int[]{ImageProcessor.NONE, ImageProcessor.BILINEAR, ImageProcessor.BICUBIC}) {
			
			double[] rotations = new double[] {0,15,30,45,90,135,224,271,360,-36,-180,-212,-284,-360};
			
			for (double rotation : rotations) {
				
				initialize();
				
				bProc.setInterpolationMethod(interpMethod);
				iProc.setInterpolationMethod(interpMethod);
				
				bProc.rotate(rotation);
				iProc.rotate(rotation);
				
				compareData(bProc, iProc);
			}
		}
	}

	@Test
	public void testScale()
	{
		for (int interpMethod : new int[]{ImageProcessor.NONE, ImageProcessor.BILINEAR, ImageProcessor.BICUBIC}) {
			
			double[][] scales = new double[][] {
					//TODO - enable this point: new double[]{0,0},
					new double[]{1,1},
					new double[]{1,2},
					new double[]{2,1},
					new double[]{1.4,1.5},
					new double[]{2.6,2.7},
					new double[]{3.8,3.9},
					new double[]{7.4,5.9},
					new double[]{0.2,0.3}
			};
			
			for (double[] scale : scales) {
				
				initialize();
				
				bProc.setInterpolationMethod(interpMethod);
				iProc.setInterpolationMethod(interpMethod);
				
				bProc.scale(scale[0], scale[1]);
				iProc.scale(scale[0], scale[1]);
				
				compareData(bProc, iProc);
			}
		}
	}

	@Test
	public void testSetBackgroundValue()
	{
		double[] bgVals = new double[] {-1,0,1,44,55.8,66.1,254,255,256,1000};
		
		for (double bg : bgVals) {
			bProc.setBackgroundValue(bg);
			iProc.setBackgroundValue(bg);
			assertEquals(bProc.getBackgroundValue(), iProc.getBackgroundValue(), 0);
		}
	}

	@Test
	public void testSetColorColor()
	{
		Color[] colors = new Color[]{Color.white, Color.black, Color.blue, Color.red, Color.green, Color.gray, Color.magenta};
		
		for (Color color : colors)
		{
			bProc.setColor(color);
			iProc.setColor(color);
			
			assertEquals(bProc.drawingColor, iProc.drawingColor);
			assertEquals(bProc.fgColor, iProc.fgColor);
			// TODO : for float types what about .fillColor??? since private don't worry about it? Or find some test that teases it out.
		}

	}

	@Test
	public void testSetfIntFloat()
	{
		int maxPixels = width*height;
		
		int numChanges = 5;
		for (int changeNum = 0; changeNum < numChanges; changeNum++)
		{
			int changeIndex = changeNum * maxPixels / numChanges;
			float changeValue = 10.0f + (1.1f * numChanges);
			bProc.setf(changeIndex, changeValue);
			iProc.setf(changeIndex, changeValue);
		}
		
		compareData(bProc, iProc);
	}

	@Test
	public void testSetfIntIntFloat()
	{
		// set the ByteProcessor
		bProc.setf(0, 0, 11.1f);
		bProc.setf(0, height-1, 22.3f);
		bProc.setf(width-1, 0, 33.5f);
		bProc.setf(width-1, height-1, 44.7f);
		bProc.setf(width/2, height/2, 55.9f);

		// set the ImgLibProcessor
		iProc.setf(0, 0, 11.1f);
		iProc.setf(0, height-1, 22.3f);
		iProc.setf(width-1, 0, 33.5f);
		iProc.setf(width-1, height-1, 44.7f);
		iProc.setf(width/2, height/2, 55.9f);
		
		compareData(bProc, iProc);
	}

	@Test
	public void testSetIntInt()
	{
		int maxPixels = width*height;
		
		int numChanges = 8;
		for (int changeNum = 0; changeNum < numChanges; changeNum++)
		{
			int changeIndex = changeNum * maxPixels / numChanges;
			int changeValue = 20 + (10 * numChanges);
			bProc.set(changeIndex, changeValue);
			iProc.set(changeIndex, changeValue);
		}
		
		compareData(bProc, iProc);
	}

	@Test
	public void testSetIntIntInt()
	{
		// set the ByteProcessor
		bProc.set(0, 0, 50);
		bProc.set(0, height-1, 60);
		bProc.set(width-1, 0, 70);
		bProc.set(width-1, height-1, 80);
		bProc.set(width/2, height/2, 90);

		// set the ImgLibProcessor
		iProc.set(0, 0, 50);
		iProc.set(0, height-1, 60);
		iProc.set(width-1, 0, 70);
		iProc.set(width-1, height-1, 80);
		iProc.set(width/2, height/2, 90);
		
		compareData(bProc, iProc);
	}

	@Test
	public void testSetMinAndMax()
	{
		double min = 22.0;
		double max = 96.0;
		
		bProc.setMinAndMax(min, max);
		iProc.setMinAndMax(min, max);
		
		assertEquals(bProc.getMin(), iProc.getMin(), 0);
		assertEquals(bProc.getMax(), iProc.getMax(), 0);
	}

	@Test
	public void testSetPixelsIntFloatProcessor()
	{
		FloatProcessor fProc = new FloatProcessor(width, height);
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				fProc.setf(x, y, 0.6f*(x+y));

		for (int channel = 0; channel < 3; channel++) {
			bProc.setPixels(channel, fProc);
			iProc.setPixels(channel, fProc);
			compareData(bProc, iProc);
		}
	}

	@Test
	public void testSetPixelsObject()
	{
		byte[] newPixels = new byte[width*height];
		
		for (int i = 0; i < width*height; i++)
			newPixels[i] = (byte) ((123 + i) % 256);
		
		bProc.setPixels(newPixels);
		iProc.setPixels(newPixels);
		
		compareData(bProc, iProc);
	}

	@Test
	public void testSetSnapshotPixels()
	{
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
		
		assertArrayEquals((byte[])bProc.getPixels(), (byte[])iProc.getPixels());
	}

	@Test
	public void testSetValue()
	{
		bProc.setValue(0);
		iProc.setValue(0);
		assertEquals(bProc.fgColor, iProc.fgColor);

		bProc.setValue(-1);
		iProc.setValue(-1);
		assertEquals(bProc.fgColor, iProc.fgColor);

		bProc.setValue(1);
		iProc.setValue(1);
		assertEquals(bProc.fgColor, iProc.fgColor);

		bProc.setValue(14.2);
		iProc.setValue(14.2);
		assertEquals(bProc.fgColor, iProc.fgColor);
	}

	@Test
	public void testSnapshotAndReset()
	{
		bProc.snapshot();
		iProc.snapshot();
	
		for (int i = 0; i < width*height; i++)
		{
			bProc.set(i, i%256);
			iProc.set(i, i%256);
		}
		
		compareData(bProc, iProc);
		
		bProc.reset();
		iProc.reset();
		
		compareData(bProc, iProc);
	}
	
	@Test
	public void testSqr()
	{
		//if (SKIP_UNFINISHED) return;
		
		for (int i = 0; i < 12; i++)
		{
			bProc.sqr();
			iProc.sqr();
			compareData(bProc, iProc);
		}
	}
	
	@Test
	public void testSqrt()
	{
		//if (SKIP_UNFINISHED) return;
		
		for (int i = 0; i < 12; i++)
		{
			bProc.sqrt();
			iProc.sqrt();
			compareData(bProc, iProc);
		}
	}

	@Test
	public void testThreshold()
	{
		int numTests = 5;
		for (int i = 0; i < numTests; i++)
		{
			initialize();
			int thresh = 256 / (i+1);
			bProc.threshold(thresh);
			iProc.threshold(thresh);
			compareData(bProc, iProc);
		}
	}

	@Test
	public void testToFloat()
	{
		FloatProcessor bFloat, iFloat;
		
		bFloat = bProc.toFloat(0, null);
		iFloat = iProc.toFloat(0, null);
		
		compareData(bFloat, iFloat);

		bFloat = bProc.toFloat(1, null);
		iFloat = iProc.toFloat(1, null);
		
		compareData(bFloat, iFloat);
	}

	@Test
	public void testXorInt()
	{
		//if (SKIP_UNFINISHED) return;
		
		for (int i = 0; i < 12; i++)
		{
			bProc.xor(i);
			iProc.xor(i);
			compareData(bProc, iProc);
		}
	}
	
	@Test
	public void testCachedCursorClose()
	{
		// have tested that the cached cursors eventually close via print statements in ImgLibProcessor using the below code
		
		// hatch a mess of threads that will randomly change the images and compare that the shared cursors do not get out of synch 
		for (int i = 0; i < 20000; i++) {
			new Thread() {
				@Override
				public void run() {
					
					Random rand = new Random();
					
					rand.setSeed(System.nanoTime());
					
					double value = rand.nextDouble();
					
					int x = (int) (width * rand.nextDouble());
					int y = (int) (height * rand.nextDouble());
					
					if (value < 0.2)
					{
						assertEquals(bProc.get(x, y), iProc.get(x, y));
					}
					else if (value < 0.4)
					{
						assertEquals(bProc.getHistogram(), iProc.getHistogram());
					}
					else if (value < 0.5)
					{
						iProc.snapshot();
						bProc.snapshot();
						compareData(bProc, iProc);
					}
					else if (value < 0.6)
					{
						bProc.reset();
						iProc.reset();
						compareData(bProc, iProc);
					}
					else
					{
						for (int i = 0; i < 10; i++ )
						{
							int x1 = (int) (width * rand.nextDouble());
							int y1 = (int) (height * rand.nextDouble());
							bProc.putPixelValue(x1, y1, value*255);
							iProc.putPixelValue(x1, y1, value*255);
						}
						compareData(bProc,iProc);
					}
				}
			};
		}
	}
}
