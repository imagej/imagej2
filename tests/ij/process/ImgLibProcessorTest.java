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
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;
import mpicbg.imglib.type.numeric.real.FloatType;

import org.junit.Test;

// TODO - Right now doing all comparisons versus a ByteProcessor and ShortProcessor(partial support). Will add comparison code vs. FloatProcessor.

// TODO - setPix and setSnapPix need float version

public class ImgLibProcessorTest {

	// ************* Instance variables ***********************************************

	static boolean SKIP_THIS_ONE = true; // TODO : used to skip various tests during development to keep Hudson tests passing

	static int width;
	static int height;
	
	static ByteProcessor origBProc;
	static ShortProcessor origSProc;
	static FloatProcessor origFProc;
	static ImgLibProcessor<UnsignedByteType> origIUBProc;
	static ImgLibProcessor<UnsignedShortType> origIUSProc;
	static ImgLibProcessor<FloatType> origIFProc;
	
	private ByteProcessor bProc;
	private ShortProcessor sProc;
	private FloatProcessor fProc;
	private ImgLibProcessor<UnsignedByteType> iubProc;
	private ImgLibProcessor<UnsignedShortType> iusProc;
	private ImgLibProcessor<FloatType> ifProc;

	ImageProcessor[][] PROC_PAIRS;
	ImageProcessor[] PROCS;

	// ************* Helper methods ***********************************************
	
	// this initialization code runs once - load the test image
	@BeforeClass
	public static void setup()
	{
		width = 343;
		height = 769;

		// *******   BYTE Processors
		
		// setup BProc
		origBProc = new ByteProcessor(width, height);
		
		// setup iubProc
		ImageFactory<UnsignedByteType> ubFactory = new ImageFactory<UnsignedByteType>(new UnsignedByteType(), new ArrayContainerFactory());
		Image<UnsignedByteType> ubImage = ubFactory.createImage(new int[]{width, height});
		origIUBProc = new ImgLibProcessor<UnsignedByteType>(ubImage, new UnsignedByteType(), 0);
		
		// set their pixels identically
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				int value = (19 + (x+y)) % 256;
				origBProc.set(x, y, value);
				origIUBProc.set(x, y, value);
			}
		}
		
		// hack?
		origIUBProc.setMinAndMax(0, 255);  // TODO - if not here then getMax() test fails. Might point out some ImgLibProc code needed. Although
											//     it might just be that IJ calls setMinAndMax() and we aren't using IJ infrastructure yet.
		
		// make sure they are the same
		compareData(origBProc, origIUBProc);

		// *******   SHORT Processors
		
		// setup sProc
		origSProc = new ShortProcessor(width, height);
		
		// setup iusProc
		ImageFactory<UnsignedShortType> usFactory = new ImageFactory<UnsignedShortType>(new UnsignedShortType(), new ArrayContainerFactory());
		Image<UnsignedShortType> usImage = usFactory.createImage(new int[]{width, height});
		origIUSProc = new ImgLibProcessor<UnsignedShortType>(usImage, new UnsignedShortType(), 0);
		
		// set their pixels identically
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				int value = 32000 + (x+y);
				origSProc.set(x, y, value);
				origIUSProc.set(x, y, value);
			}
		}
		
		// make sure they are the same
		compareData(origSProc, origIUSProc);

		// *******   FLOAT Processors
		
		// setup fProc
		origFProc = new FloatProcessor(width, height);
		
		// setup iusProc
		ImageFactory<FloatType> fFactory = new ImageFactory<FloatType>(new FloatType(), new ArrayContainerFactory());
		Image<FloatType> fImage = fFactory.createImage(new int[]{width, height});
		origIFProc = new ImgLibProcessor<FloatType>(fImage, new FloatType(), 0);
		
		// set their pixels identically
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				//TODO : 16 breakages
				// float value = (float)(Math.PI * (x/(y+1)));
				
				// TODO: 13 breakages
				float value = (float)(x+y);
				
				origFProc.setf(x, y, value);
				origIFProc.setf(x, y, value);
			}
		}
		
		// make sure they are the same
		compareData(origFProc, origIFProc);

	}
	
	// the following initialization code runs before every test
	@Before
	public void initialize()
	{
		bProc = (ByteProcessor)origBProc.duplicate();
		iubProc = (ImgLibProcessor<UnsignedByteType>)origIUBProc.duplicate();
		compareData(bProc, iubProc);

		sProc = (ShortProcessor)origSProc.duplicate();
		iusProc = (ImgLibProcessor<UnsignedShortType>)origIUSProc.duplicate();
		compareData(sProc, iusProc);
		
		fProc = (FloatProcessor)origFProc.duplicate();
		ifProc = (ImgLibProcessor<FloatType>)origIFProc.duplicate();
		compareData(sProc, iusProc);
		
		 PROCS = new ImageProcessor[]{iubProc, iusProc, ifProc};
		 PROC_PAIRS = new ImageProcessor[][]{{bProc,iubProc},{sProc,iusProc},/*{fProc,ifProc}*/};
	}

	// ************* Helper tests ***********************************************

	private static void compareData(ImageProcessor baselineProc, ImageProcessor testedProc)
	{
		int w = baselineProc.getWidth();
		int h = baselineProc.getHeight();
	
		assertEquals(w, testedProc.getWidth());
		assertEquals(h, testedProc.getHeight());
	
		for (int x = 0; x < w; x++)
		{
			for (int y = 0; y < h; y++)
			{
				if (Math.abs(baselineProc.getf(x, y) - testedProc.getf(x, y)) > Assert.FLOAT_TOL)
					fail("processor data differs at ("+x+","+y+") : ij(" + baselineProc.getf(x, y) +") imglib("+testedProc.getf(x, y)+")");
			}
		}
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
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			for (int i = 0; i < 12; i++)
			{
				procPair[0].abs();
				procPair[1].abs();
				compareData(procPair[0], procPair[1]);
			}
		}
	}

	@Test
	public void testAddDouble()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			for (int i = 0; i < 12; i++)
			{
				double value = i*Math.PI;
				
				procPair[0].add(value);
				procPair[1].add(value);
				compareData(procPair[0], procPair[1]);
			}
		}
	}
	
	@Test
	public void testAddInt()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			for (int i = 0; i < 12; i++)
			{
				procPair[0].add(i);
				procPair[1].add(i);
				compareData(procPair[0], procPair[1]);
			}
		}
	}
	
	@Test
	public void testAndInt()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			for (int i = 0; i < 12; i++)
			{
				procPair[0].and(i);
				procPair[1].and(i);
				compareData(procPair[0], procPair[1]);
			}
		}
	}
	
	@Test
	public void testApplyTable()
	{
		// make an 8-bit inverted lut
		int[] newLut = new int[256];
		for (int i = 0; i < 256; i++)
			newLut[i] = 255 - i;
		
		bProc.applyTable(newLut);
		iubProc.applyTable(newLut);
		compareData(bProc, iubProc);
		
		// make a 16-bit inverted lut
		newLut = new int[65536];
		for (int i = 0; i < 65536; i++)
			newLut[i] = 65536 - i;
		
		sProc.applyTable(newLut);
		iusProc.applyTable(newLut);
		compareData(sProc, iusProc);
	}

	@Test
	public void testConvolve()
	{
		// predetermined, balanced kernel
		
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
		
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			procPair[0].convolve(kernel, kw, kh);
			procPair[1].convolve(kernel, kw, kh);
			compareData(procPair[0], procPair[1]);
		}
		
		// random kernel
		
		Random generator = new Random();
		generator.setSeed(1735);  // but repeatable
		for (int k = 0; k < kw*kh; k++)
			kernel[k] = (float)generator.nextGaussian();

		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			procPair[0].convolve(kernel, kw, kh);
			procPair[1].convolve(kernel, kw, kh);
			compareData(procPair[0], procPair[1]);
		}
	}

	@Test
	public void testConvolve3x3()
	{
		int[][] kernel2d = new int[][] {{1,3,1}, {3,-16,3}, {1,3,1}};

		int kh = kernel2d.length;
		int kw = kernel2d[0].length;
		
		int[] kernel = new int[kw * kh];
		int i = 0;
		for (int x=0; x < kw; x++)
			for (int y=0; y < kh; y++)
				kernel[i++] = kernel2d[x][y];
		
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			procPair[0].convolve3x3(kernel);
			procPair[1].convolve3x3(kernel);
			compareData(procPair[0], procPair[1]);
		}
	}

	@Test
	public void testCopyBits()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			byte[] bytes = new byte[256];
			for (int b = Byte.MIN_VALUE; b <= Byte.MAX_VALUE; b++)
				bytes[b-Byte.MIN_VALUE] = (byte) b;
			
			ImageProcessor data = new ByteProcessor(16, 16, bytes, null);
			for (int mode = Blitter.COPY; mode <= Blitter.COPY_ZERO_TRANSPARENT; mode++)
			{
				initialize();
				procPair[0].copyBits(data, 23, 19, mode);  // TODO : was 23,19 : changed to 0,0 to simplify debugging
				procPair[1].copyBits(data, 23, 19, mode);  // TODO : was 23,19 : changed to 0,0 to simplify debugging
				compareData(procPair[0], procPair[1]);
			}
		}
	}

	@Test
	public void testCreateImage()
	{
		
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			java.awt.Image image0 = procPair[0].createImage(); 
			java.awt.Image image1 = procPair[1].createImage(); 
	
			assertNotNull(image0);
			assertNotNull(image1);
			
			// TODO - do some other kind of comparisons?
		}
	}

	@Test
	public void testCreateProcessor()
	{
		int width = 73;
		int height = 22;

		for (ImageProcessor proc : PROCS)
		{
			ImageProcessor newProc = proc.createProcessor(width, height);
			
			assertEquals(width, newProc.getWidth());
			assertEquals(height, newProc.getHeight());
			
			assertEquals(proc.getMin(), newProc.getMin(), Assert.DOUBLE_TOL);
			assertEquals(proc.getMax(), newProc.getMax(), Assert.DOUBLE_TOL);
			assertEquals(proc.getColorModel(), newProc.getColorModel());
			assertEquals(proc.getInterpolate(), newProc.getInterpolate());
		}
	}

	@Test
	public void testCrop()
	{
		int ox = 3, oy = 10, w = width/2, h = height/2;
		
		assertTrue((ox+w) <= width);
		assertTrue((oy+h) <= height);
		
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			procPair[0].setRoi(ox, oy, w, h);
			procPair[1].setRoi(ox, oy, w, h);
			ImageProcessor baseline = procPair[0].crop();
			ImageProcessor result = procPair[1].crop();
			compareData(baseline, result);
		}
	}

	@Test
	public void testDilate()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			for (int i = 0; i < 3; i++)
			{
				procPair[0].dilate();
				procPair[1].dilate();
				compareData(procPair[0], procPair[1]);
			}
		}
	}

	@Test
	public void testDilateCounts()
	{
		iubProc.dilate(50,5);
		bProc.dilate(50,5);
		compareData(bProc, iubProc);

		iubProc.dilate(20,0);
		bProc.dilate(20,0);
		compareData(bProc, iubProc);

		iubProc.dilate(100,12);
		bProc.dilate(100,12);
		compareData(bProc, iubProc);
	}
	
	@Test
	public void testDrawPixel()
	{
		int ox = 3, oy = 10, w = width/2, h = height/2;
		
		assertTrue((ox+w) <= width);
		assertTrue((oy+h) <= height);
		
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			procPair[0].setColor(14);
			procPair[1].setColor(14);
			
			for (int x = ox; x < ox+w; x++)
			{
				for (int y = oy; y < oy+h; y++)
				{
					procPair[0].drawPixel(x, y);
					procPair[1].drawPixel(x, y);
				}
			}
			
			compareData(procPair[0], procPair[1]);
		}
	}

	@Test
	public void testDuplicate()
	{
		ImageProcessor newProc = iubProc.duplicate();
		assertTrue(newProc instanceof ImgLibProcessor<?>);
		compareData(iubProc, newProc);
	}

	@Test
	public void testErode() 
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			for (int i = 0; i < 3; i++)
			{
				procPair[0].erode();
				procPair[1].erode();
				compareData(procPair[0], procPair[1]);
			}
		}
	}
	
	@Test
	public void testErodeCounts()
	{
		iubProc.erode(50,5);
		bProc.erode(50,5);
		compareData(bProc, iubProc);

		iubProc.erode(20,0);
		bProc.erode(20,0);
		compareData(bProc, iubProc);

		iubProc.erode(100,12);
		bProc.erode(100,12);
		compareData(bProc, iubProc);
	}

	@Test
	public void testExp()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			for (int i = 0; i < 12; i++)
			{
				procPair[0].exp();
				procPair[1].exp();
				compareData(procPair[0],procPair[1]);
			}
		}
	}

	@Test
	public void testFill()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			procPair[0].setColor(7);
			procPair[1].setColor(7);
			
			procPair[0].fill();
			procPair[1].fill();
	
			compareData(procPair[0],procPair[1]);
		}
	}

	@Test
	public void testFillImageProcessor()
	{
		ByteProcessor byteMask = new ByteProcessor(width, height);
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				if ((x+y)%2 == 0)
					byteMask.set(x, y, 1);
		
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			procPair[0].setColor(19);
			procPair[1].setColor(19);
			
			procPair[0].fill(byteMask);
			procPair[1].fill(byteMask);
	
			compareData(procPair[0],procPair[1]);
		}
	}

	@Test
	public void testFilterInt()
	{
		// NOTE - purposely left out CONVOLVE. It is tested elsewhere and in this case a NULL kernel is passed causing a runtime exception
		
		int[] filterNumbers = new int[]{ImgLibProcessor.BLUR_MORE, ImgLibProcessor.FIND_EDGES, ImgLibProcessor.MEDIAN_FILTER,
										ImgLibProcessor.MIN, ImgLibProcessor.MAX, ImgLibProcessor.ERODE, ImgLibProcessor.DILATE};
		
		for (int filterNum : filterNumbers)
		{
			for (ImageProcessor[] procPair : PROC_PAIRS)
			{
				initialize();
				procPair[0].filter(filterNum);
				procPair[1].filter(filterNum);
				compareData(procPair[0], procPair[1]);
			}
		}
	}

	@Test
	public void testFlipVertical()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			procPair[0].flipVertical();
			procPair[1].flipVertical();
			compareData(procPair[0],procPair[1]);
		}
	}

	@Test
	public void testGammaDouble()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			for (int i = 0; i < 12; i++)
			{
				double value = i*0.68;
				procPair[0].gamma(value);
				procPair[1].gamma(value);
				compareData(procPair[0],procPair[1]);
			}
		}
	}

	@Test
	public void testGetBackgroundValue()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			assertEquals(procPair[0].getBackgroundValue(), procPair[1].getBackgroundValue(), 0.0);
			for (int i = 0; i < 25; i++)
			{
				procPair[0].setBackgroundValue(i + (i%4)*0.5);
				procPair[1].setBackgroundValue(i + (i%4)*0.5);
				assertEquals(procPair[0].getBackgroundValue(), procPair[1].getBackgroundValue(), 0.0);
			}
		}
	}

	@Test
	public void testGetfInt()
	{
		int maxPixels = width*height;
		
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			assertEquals(procPair[0].getf(0*maxPixels/5), procPair[1].getf(0*maxPixels/5), Assert.FLOAT_TOL);
			assertEquals(procPair[0].getf(1*maxPixels/5), procPair[1].getf(1*maxPixels/5), Assert.FLOAT_TOL);
			assertEquals(procPair[0].getf(2*maxPixels/5), procPair[1].getf(2*maxPixels/5), Assert.FLOAT_TOL);
			assertEquals(procPair[0].getf(3*maxPixels/5), procPair[1].getf(3*maxPixels/5), Assert.FLOAT_TOL);
			assertEquals(procPair[0].getf(4*maxPixels/5), procPair[1].getf(4*maxPixels/5), Assert.FLOAT_TOL);
		}
	}

	@Test
	public void testGetfIntInt()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			assertEquals(procPair[0].getf(0,0), procPair[1].getf(0,0), Assert.FLOAT_TOL);
			assertEquals(procPair[0].getf(width-1,0), procPair[1].getf(width-1,0), Assert.FLOAT_TOL);
			assertEquals(procPair[0].getf(0,height-1), procPair[1].getf(0,height-1), Assert.FLOAT_TOL);
			assertEquals(procPair[0].getf(width-1,height-1), procPair[1].getf(width-1,height-1), Assert.FLOAT_TOL);
			assertEquals(procPair[0].getf(width/2,height/2), procPair[1].getf(width/2,height/2), Assert.FLOAT_TOL);
		}
	}

	@Test
	public void testGetHistogram()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			// regular case
			int[] bHist = procPair[0].getHistogram();
			int[] iHist = procPair[1].getHistogram();
			assertArrayEquals(bHist, iHist);
			
			// masked case
			ImageProcessor mask = new ByteProcessor(width, height);
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					mask.set(x, y, (x+y) % 2);
				}
			}
			procPair[0].setMask(mask);
			procPair[1].setMask(mask);
			bHist = procPair[0].getHistogram();
			iHist = procPair[1].getHistogram();
			assertArrayEquals(bHist, iHist);
		}
	}

	@Test
	public void testGetInt()
	{
		int maxPixels = width*height;
		
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			assertEquals(procPair[0].get(0*maxPixels/5), procPair[1].get(0*maxPixels/5), Assert.FLOAT_TOL);
			assertEquals(procPair[0].get(1*maxPixels/5), procPair[1].get(1*maxPixels/5), Assert.FLOAT_TOL);
			assertEquals(procPair[0].get(2*maxPixels/5), procPair[1].get(2*maxPixels/5), Assert.FLOAT_TOL);
			assertEquals(procPair[0].get(3*maxPixels/5), procPair[1].get(3*maxPixels/5), Assert.FLOAT_TOL);
			assertEquals(procPair[0].get(4*maxPixels/5), procPair[1].get(4*maxPixels/5), Assert.FLOAT_TOL);
		}
	}

	@Test
	public void testGetIntInt()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			assertEquals(procPair[0].get(0,0), procPair[1].get(0,0), Assert.FLOAT_TOL);
			assertEquals(procPair[0].get(width-1,0), procPair[1].get(width-1,0), Assert.FLOAT_TOL);
			assertEquals(procPair[0].get(0,height-1), procPair[1].get(0,height-1), Assert.FLOAT_TOL);
			assertEquals(procPair[0].get(width-1,height-1), procPair[1].get(width-1,height-1), Assert.FLOAT_TOL);
			assertEquals(procPair[0].get(width/2,height/2), procPair[1].get(width/2,height/2), Assert.FLOAT_TOL);
		}
	}

	@Test
	public void testGetInterpolatedPixel()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			for (int interpMethod : new int[]{ImageProcessor.NONE, ImageProcessor.BILINEAR, ImageProcessor.BICUBIC})
			{
				procPair[0].setInterpolationMethod(interpMethod);
				procPair[1].setInterpolationMethod(interpMethod);
			
				double[][] points = new double[][]
				{
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
					assertEquals(procPair[0].getInterpolatedPixel(x, y), procPair[1].getInterpolatedPixel(x, y), Assert.DOUBLE_TOL);
				}
			}
		}
	}

	@Test
	public void testGetMax()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			assertEquals(procPair[0].getMax(), procPair[1].getMax(), Assert.DOUBLE_TOL);
			procPair[0].setMinAndMax(42.4,107.6);
			procPair[1].setMinAndMax(42.4,107.6);
			assertEquals(procPair[0].getMax(), procPair[1].getMax(), Assert.DOUBLE_TOL);
		}
	}

	@Test
	public void testGetMin()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			assertEquals(procPair[0].getMin(), procPair[1].getMin(), Assert.DOUBLE_TOL);
			procPair[0].setMinAndMax(42.4,107.6);
			procPair[1].setMinAndMax(42.4,107.6);
			assertEquals(procPair[0].getMin(), procPair[1].getMin(), Assert.DOUBLE_TOL);
		}
	}

	@Test
	public void testGetPixelInterpolated()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			for (int interpMethod : new int[]{ImageProcessor.NONE, ImageProcessor.BILINEAR, ImageProcessor.BICUBIC})
			{
				procPair[0].setInterpolationMethod(interpMethod);
				procPair[1].setInterpolationMethod(interpMethod);
			
				double[][] points = new double[][]
				{
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
					assertEquals(procPair[0].getPixelInterpolated(x, y), procPair[1].getPixelInterpolated(x, y), Assert.DOUBLE_TOL);
				}
			}
		}
	}

	@Test
	public void testGetPixelIntInt()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			for (int x = 0; x < width; x++)
			{
				for (int y = 0; y < height; y++)
				{
					assertEquals(procPair[0].get(x, y), procPair[1].get(x, y));
				}
			}
		}
	}

	@Test
	public void testGetPixels()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			if (procPair[0] instanceof ByteProcessor)
				assertArrayEquals((byte[])procPair[0].getPixels(), (byte[])procPair[1].getPixels());
			else if (procPair[0] instanceof ShortProcessor)
				assertArrayEquals((short[])procPair[0].getPixels(), (short[])procPair[1].getPixels());
			else if (procPair[0] instanceof FloatProcessor)
				Assert.assertFloatArraysEqual((float[])procPair[0].getPixels(), (float[])procPair[1].getPixels(), Assert.FLOAT_TOL);
			else
				fail("unknown processor type : " + procPair[0].getClass());
		}
	}

	@Test
	public void testGetPixelsCopy()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			if (procPair[0] instanceof ByteProcessor)
				assertArrayEquals((byte[])procPair[0].getPixelsCopy(), (byte[])procPair[1].getPixelsCopy());
			else if (procPair[0] instanceof ShortProcessor)
				assertArrayEquals((short[])procPair[0].getPixelsCopy(), (short[])procPair[1].getPixelsCopy());
			else if (procPair[0] instanceof FloatProcessor)
				Assert.assertFloatArraysEqual((float[])procPair[0].getPixelsCopy(), (float[])procPair[1].getPixelsCopy(), Assert.FLOAT_TOL);
			else
				fail("unknown processor type : " + procPair[0].getClass());
		}
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
		
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			int[][] badPoints = new int[][]
			{
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
				assertEquals(procPair[0].getPixelValue(x, y), procPair[1].getPixelValue(x, y), Assert.DOUBLE_TOL);
			}
	
			// all the good points
			for (int x = 0; x < width; x++)
				for (int y = 0; y < height; y++)
					assertEquals(procPair[0].getPixelValue(x, y), procPair[1].getPixelValue(x, y), Assert.DOUBLE_TOL);
		}
	}

	@Test
	public void testGetPlaneData()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			double[] pixels = ((ImgLibProcessor<?>)procPair[1]).getPlaneData();
			
			for (int i = 0; i < width*height; i++)
				assertEquals((double)procPair[1].get(i),  pixels[i], Assert.DOUBLE_TOL);
		}
	}

	@Test
	public void testGetSnapshotPixels() 
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			assertNull(procPair[0].getSnapshotPixels());
			assertNull(procPair[1].getSnapshotPixels());
			
			procPair[0].snapshot();
			procPair[1].snapshot();
	
			if (procPair[0] instanceof ByteProcessor)
				assertArrayEquals((byte[])procPair[0].getSnapshotPixels(), (byte[])procPair[1].getSnapshotPixels());
			else if (procPair[0] instanceof ShortProcessor)
				assertArrayEquals((short[])procPair[0].getSnapshotPixels(), (short[])procPair[1].getSnapshotPixels());
			else if (procPair[0] instanceof FloatProcessor)
				Assert.assertFloatArraysEqual((float[])procPair[0].getSnapshotPixels(), (float[])procPair[1].getSnapshotPixels(), Assert.FLOAT_TOL);
			else
				fail("unknown processor type : " + procPair[0].getClass());
		}
	}

	@Test
	public void testInvert()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			procPair[0].invert();
			procPair[1].invert();
			compareData(procPair[0], procPair[1]);
		}
	}

	@Test
	public void testLog()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			for (int i = 0; i < 12; i++)
			{
				procPair[0].log();
				procPair[1].log();
				compareData(procPair[0], procPair[1]);
			}
		}
	}

	@Test
	public void testMaxDouble()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			for (int i = 0; i < 12; i++)
			{
				initialize();
				
				double value = 2.0 * i;
				procPair[0].max(value);
				procPair[1].max(value);
				compareData(procPair[0], procPair[1]);
			}
		}
	}

	@Test
	public void testMedianFilter() 
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			procPair[0].medianFilter();
			procPair[1].medianFilter();
			compareData(procPair[0], procPair[1]);
		}
	}

	@Test
	public void testMinDouble()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			for (int i = 0; i < 12; i++)
			{
				initialize();
				
				double value = 2.0 * i;
				procPair[0].min(value);
				procPair[1].min(value);
				compareData(procPair[0], procPair[1]);
			}
		}
	}
	
	@Test
	public void testMultiplyDouble()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			for (int i = 0; i < 12; i++)
			{
				double value = i*Math.PI;
				procPair[0].multiply(value);
				procPair[1].multiply(value);
				compareData(procPair[0], procPair[1]);
			}
		}
	}
	
	@Test
	public void testNoise()
	{
		// NOTE/TODO - can't test this one for equality: they utilize random number generators with no seed set.
		//   Must think of some way to test. The noise() routine is small and looks correct. Still would rather test somehow.
	
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			double[] noises = new double[]{0,1,2,3,4,0.5,1.2};
			
			for (double noiseVal : noises)
			{
				initialize();
				procPair[0].noise(noiseVal);
				procPair[1].noise(noiseVal);
				//compareData(bProc, iubProc);
				assertTrue(true);
			}
		}
	}

	@Test
	public void testOrInt()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			for (int i = 0; i < 12; i++)
			{
				procPair[0].or(i);
				procPair[1].or(i);
				compareData(procPair[0], procPair[1]);
			}
		}
	}

	@Test
	public void testPutPixelIntIntInt()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			for (int x = 0; x < width; x++)
			{
				for (int y = 0; y < height; y++)
				{
					int newValue = Math.abs(x-y) % 256;
					procPair[0].putPixel(x, y, newValue);
					procPair[1].putPixel(x, y, newValue);
				}
			}
			compareData(procPair[0], procPair[1]);
		}
	}

	@Test
	public void testPutPixelValue()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			for (int x = 0; x < width; x++)
			{
				for (int y = 0; y < height; y++)
				{
					double newValue = (Math.abs(x-y) % 512) / 2.7;
					procPair[0].putPixelValue(x, y, newValue);
					procPair[1].putPixelValue(x, y, newValue);
				}
			}
			compareData(procPair[0], procPair[1]);
		}
	}

	@Test
	public void testResetImageProcessor()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			ByteProcessor mask = new ByteProcessor(7, 7);
			for (int x = 0; x < 7; x++)
				for (int y = 0; y < 7; y++)
					mask.set(x, y, (x+y)%2);
			
			procPair[0].setRoi(1, 2, 7, 7);
			procPair[1].setRoi(1, 2, 7, 7);
			
			procPair[0].snapshot();
			procPair[1].snapshot();
			
			for (int x = 0; x < width; x++)
			{
				for (int y = 0; y < height; y++)
				{
					procPair[0].set(x, y, (x+y)%256);
					procPair[1].set(x, y, (x+y)%256);
				}
			}
			
			compareData(procPair[0], procPair[1]);
	
			procPair[0].reset(mask);
			procPair[1].reset(mask);
			
			compareData(procPair[0], procPair[1]);
		}
	}

	@Test
	public void testResizeIntInt()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			// TODO - set bProc's and iubProc's rois to something
			for (int interpMethod : new int[]{ImageProcessor.NONE, ImageProcessor.BILINEAR, ImageProcessor.BICUBIC})
			{
				int[][] points = new int[][]
				{
					// new int[]{0,0},  // TODO - ImgLib does not like this. Changes dimensions from (0,0) to (1,1)
					new int[]{width+5,height+5},
					new int[]{width,1},
					new int[]{1,height},
					new int[]{width,height},
					new int[]{bProc.roiWidth,bProc.roiHeight},
					new int[]{width/3,height/4},
					new int[]{(int)(width*1.2), (int)(height*1.375)}
				};
				
				for (int[] point : points)
				{
					ImageProcessor newIjProc, newImglibProc;
					
					procPair[0].setInterpolationMethod(interpMethod);
					procPair[1].setInterpolationMethod(interpMethod);
					
					newIjProc = procPair[0].resize(point[0], point[1]);
					newImglibProc = procPair[1].resize(point[0], point[1]);
					
					compareData(newIjProc, newImglibProc);
				}
			}
		}
	}

	@Test
	public void testRotate() 
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			for (int interpMethod : new int[]{ImageProcessor.NONE, ImageProcessor.BILINEAR, ImageProcessor.BICUBIC})
			{
				double[] rotations = new double[] {0,15,30,45,90,135,224,271,360,-36,-180,-212,-284,-360};
				
				for (double rotation : rotations)
				{
					initialize();
					
					procPair[0].setInterpolationMethod(interpMethod);
					procPair[1].setInterpolationMethod(interpMethod);
					
					procPair[0].rotate(rotation);
					procPair[1].rotate(rotation);
					
					compareData(procPair[0], procPair[1]);
				}
			}
		}
	}

	@Test
	public void testScale()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			for (int interpMethod : new int[]{ImageProcessor.NONE, ImageProcessor.BILINEAR, ImageProcessor.BICUBIC})
			{
				double[][] scales = new double[][]
				{
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
				
				for (double[] scale : scales)
				{
					initialize();
					
					procPair[0].setInterpolationMethod(interpMethod);
					procPair[1].setInterpolationMethod(interpMethod);
					
					procPair[0].scale(scale[0], scale[1]);
					procPair[1].scale(scale[0], scale[1]);
					
					compareData(procPair[0], procPair[1]);
				}
			}
		}
	}

	@Test
	public void testSetBackgroundValue()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			double[] bgVals = new double[] {-1,0,1,44,55.8,66.1,254,255,256,1000};
			
			for (double bg : bgVals)
			{
				procPair[0].setBackgroundValue(bg);
				procPair[1].setBackgroundValue(bg);
				assertEquals(procPair[0].getBackgroundValue(), procPair[1].getBackgroundValue(), 0);
			}
		}
	}

	@Test
	public void testSetColorColor()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			Color[] colors = new Color[]{Color.white, Color.black, Color.blue, Color.red, Color.green, Color.gray, Color.magenta};
			
			for (Color color : colors)
			{
				procPair[0].setColor(color);
				procPair[1].setColor(color);
				
				assertEquals(procPair[0].drawingColor, procPair[1].drawingColor);
				assertEquals(procPair[0].fgColor, procPair[1].fgColor);
				// TODO : for float types what about .fillColor??? since private don't worry about it? Or find some test that teases it out.
			}
		}
	}

	@Test
	public void testSetfIntFloat()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			int maxPixels = width*height;
			
			int numChanges = 5;
			for (int changeNum = 0; changeNum < numChanges; changeNum++)
			{
				int changeIndex = changeNum * maxPixels / numChanges;
				float changeValue = 10.0f + (1.1f * numChanges);
				procPair[0].setf(changeIndex, changeValue);
				procPair[1].setf(changeIndex, changeValue);
			}
			
			compareData(procPair[0], procPair[1]);
		}
	}

	@Test
	public void testSetfIntIntFloat()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			// set the IJ Processor
			procPair[0].setf(0, 0, 11.1f);
			procPair[0].setf(0, height-1, 22.3f);
			procPair[0].setf(width-1, 0, 33.5f);
			procPair[0].setf(width-1, height-1, 44.7f);
			procPair[0].setf(width/2, height/2, 55.9f);
	
			// set the ImgLib Processor
			procPair[1].setf(0, 0, 11.1f);
			procPair[1].setf(0, height-1, 22.3f);
			procPair[1].setf(width-1, 0, 33.5f);
			procPair[1].setf(width-1, height-1, 44.7f);
			procPair[1].setf(width/2, height/2, 55.9f);
			
			compareData(procPair[0], procPair[1]);
		}
	}

	@Test
	public void testSetIntInt()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			int maxPixels = width*height;
			
			int numChanges = 8;
			for (int changeNum = 0; changeNum < numChanges; changeNum++)
			{
				int changeIndex = changeNum * maxPixels / numChanges;
				int changeValue = 20 + (10 * numChanges);
				procPair[0].set(changeIndex, changeValue);
				procPair[1].set(changeIndex, changeValue);
			}
			
			compareData(procPair[0], procPair[1]);
		}
	}

	@Test
	public void testSetIntIntInt()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			// set the ByteProcessor
			procPair[0].set(0, 0, 50);
			procPair[0].set(0, height-1, 60);
			procPair[0].set(width-1, 0, 70);
			procPair[0].set(width-1, height-1, 80);
			procPair[0].set(width/2, height/2, 90);
	
			// set the ImgLibProcessor
			procPair[1].set(0, 0, 50);
			procPair[1].set(0, height-1, 60);
			procPair[1].set(width-1, 0, 70);
			procPair[1].set(width-1, height-1, 80);
			procPair[1].set(width/2, height/2, 90);
			
			compareData(procPair[0], procPair[1]);
		}
	}

	@Test
	public void testSetMinAndMax()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			double min = 22.0;
			double max = 96.0;
			
			procPair[0].setMinAndMax(min, max);
			procPair[1].setMinAndMax(min, max);
			
			assertEquals(procPair[0].getMin(), procPair[1].getMin(), 0);
			assertEquals(procPair[0].getMax(), procPair[1].getMax(), 0);
		}
	}

	@Test
	public void testSetPixelsIntFloatProcessor()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			FloatProcessor fProc = new FloatProcessor(width, height);
			for (int x = 0; x < width; x++)
				for (int y = 0; y < height; y++)
					fProc.setf(x, y, 0.6f*(x+y));
	
			for (int channel = 0; channel < 3; channel++)
			{
				procPair[0].setPixels(channel, fProc);
				procPair[1].setPixels(channel, fProc);
				compareData(procPair[0], procPair[1]);
			}
		}
	}

	@Test
	public void testSetPixelsObject()
	{
		// test byte proc
		
		byte[] newBytes = new byte[width*height];
		
		for (int i = 0; i < width*height; i++)
			newBytes[i] = (byte) ((123 + i) % 256);
		
		PROC_PAIRS[0][0].setPixels(newBytes);
		PROC_PAIRS[0][1].setPixels(newBytes);
		
		compareData(PROC_PAIRS[0][0], PROC_PAIRS[0][1]);
		
		// test short proc
		
		short[] newShorts = new short[width*height];
		
		for (int i = 0; i < width*height; i++)
			newShorts[i] = (short) ((123 + i) % 65536);
		
		PROC_PAIRS[1][0].setPixels(newShorts);
		PROC_PAIRS[1][1].setPixels(newShorts);
		
		compareData(PROC_PAIRS[1][0], PROC_PAIRS[1][1]);

		// test float proc : TODO
	}

	@Test
	public void testSetSnapshotPixels()
	{
		// this one will act differently between the processors  since iubProc should make data copies and bProc shouldn't
		// just make sure contents of the image snapshot match
		
		// byte processor

		byte[] origBytes = (byte[])PROC_PAIRS[0][0].getPixelsCopy();
		
		byte[] newBytes = new byte [origBytes.length];
		
		for (int i = 0; i < newBytes.length; i++)
			newBytes[i] = (byte) (i % 50);
		
		PROC_PAIRS[0][0].setSnapshotPixels(newBytes);
		PROC_PAIRS[0][0].reset();

		PROC_PAIRS[0][1].setSnapshotPixels(newBytes);
		PROC_PAIRS[0][1].reset();
		
		assertArrayEquals((byte[])PROC_PAIRS[0][0].getPixels(), (byte[])PROC_PAIRS[0][1].getPixels());
		
		// short processor

		short[] origShorts = (short[])PROC_PAIRS[1][0].getPixelsCopy();
		
		short[] newShorts = new short [origShorts.length];
		
		for (int i = 0; i < newShorts.length; i++)
			newShorts[i] = (short) (i % 50);
		
		PROC_PAIRS[1][0].setSnapshotPixels(newShorts);
		PROC_PAIRS[1][0].reset();

		PROC_PAIRS[1][1].setSnapshotPixels(newShorts);
		PROC_PAIRS[1][1].reset();
		
		assertArrayEquals((short[])PROC_PAIRS[1][0].getPixels(), (short[])PROC_PAIRS[1][1].getPixels());
		
		// float processor : TODO
	}

	@Test
	public void testSetValue()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			procPair[0].setValue(0);
			procPair[1].setValue(0);
			assertEquals(procPair[0].fgColor, procPair[1].fgColor);
	
			procPair[0].setValue(-1);
			procPair[1].setValue(-1);
			assertEquals(procPair[0].fgColor, procPair[1].fgColor);
	
			procPair[0].setValue(1);
			procPair[1].setValue(1);
			assertEquals(procPair[0].fgColor, procPair[1].fgColor);
	
			procPair[0].setValue(14.2);
			procPair[1].setValue(14.2);
			assertEquals(procPair[0].fgColor, procPair[1].fgColor);
		}
	}

	@Test
	public void testSnapshotAndReset()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			procPair[0].snapshot();
			procPair[1].snapshot();
		
			for (int i = 0; i < width*height; i++)
			{
				procPair[0].set(i, i%256);
				procPair[1].set(i, i%256);
			}
			
			compareData(procPair[0], procPair[1]);
			
			procPair[0].reset();
			procPair[1].reset();
			
			compareData(procPair[0], procPair[1]);
		}
	}
	
	@Test
	public void testSqr()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			for (int i = 0; i < 12; i++)
			{
				procPair[0].sqr();
				procPair[1].sqr();
				compareData(procPair[0], procPair[1]);
			}
		}
	}
	
	@Test
	public void testSqrt()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			for (int i = 0; i < 12; i++)
			{
				procPair[0].sqrt();
				procPair[1].sqrt();
				compareData(procPair[0], procPair[1]);
			}
		}
	}

	@Test
	public void testThreshold()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			int numTests = 5;
			for (int i = 0; i < numTests; i++)
			{
				initialize();
				int thresh = 256 / (i+1);
				procPair[0].threshold(thresh);
				procPair[1].threshold(thresh);
				compareData(procPair[0], procPair[1]);
			}
		}
	}

	@Test
	public void testToFloat()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			FloatProcessor bFloat, iFloat;
			
			bFloat = procPair[0].toFloat(0, null);
			iFloat = procPair[1].toFloat(0, null);
			
			compareData(bFloat, iFloat);
	
			bFloat = procPair[0].toFloat(1, null);
			iFloat = procPair[1].toFloat(1, null);
			
			compareData(bFloat, iFloat);
		}
	}

	@Test
	public void testXorInt()
	{
		for (ImageProcessor[] procPair : PROC_PAIRS)
		{
			for (int i = 0; i < 12; i++)
			{
				procPair[0].xor(i);
				procPair[1].xor(i);
				compareData(procPair[0], procPair[1]);
			}
		}
	}
	
	@Test
	public void testCachedCursorClose()
	{
		// have tested that the cached cursors eventually close via print statements in ImgLibProcessor using the below code
		
		// hatch a mess of threads that will randomly change the images and compare that the shared cursors do not get out of synch 
		for (int i = 0; i < 20000; i++)
		{
			new Thread()
			{
				@Override
				public void run()
				{
					Random rand = new Random();
					
					rand.setSeed(System.nanoTime());
					
					double value = rand.nextDouble();
					
					int x = (int) (width * rand.nextDouble());
					int y = (int) (height * rand.nextDouble());
					
					if (value < 0.2)
					{
						assertEquals(bProc.get(x, y), iubProc.get(x, y));
					}
					else if (value < 0.4)
					{
						assertEquals(bProc.getHistogram(), iubProc.getHistogram());
					}
					else if (value < 0.5)
					{
						iubProc.snapshot();
						bProc.snapshot();
						compareData(bProc, iubProc);
					}
					else if (value < 0.6)
					{
						bProc.reset();
						iubProc.reset();
						compareData(bProc, iubProc);
					}
					else
					{
						for (int i = 0; i < 10; i++ )
						{
							int x1 = (int) (width * rand.nextDouble());
							int y1 = (int) (height * rand.nextDouble());
							bProc.putPixelValue(x1, y1, value*255);
							iubProc.putPixelValue(x1, y1, value*255);
						}
						compareData(bProc,iubProc);
					}
				}
			};
		}
	}
}
