package ij.process;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

public class ShortProcessorTest {

	 public static int width;
	 public static int height;
	 private static byte[] rawByte;
	 private static short[] imageShortData;

	    /*
	     * Open an known image test image for global use
	     */
		@BeforeClass
		public static void runBeforeClass() throws IOException
		{
		    String id = DataConstants.DATA_DIR + "CardioShort.raw";
		    width = 1000;
		    height = 1000;

		    rawByte = new byte[width*height*2];
		    FileInputStream file = new FileInputStream(id);
			file.read(rawByte);
			imageShortData  = byteArrayToIntArray(rawByte);
	    }


		/**
		 * Converts a byte[] to an int[]; assumes native byte ordering; assumes short
		 * @param b - Byte[]
		 * @return
		 */
		private static short[] byteArrayToIntArray(byte[] b )
		{
			int shortSize = b.length/2;

			short[] resultsIntArray = new short[width*height];

			for (int p = 0; p<shortSize; p++)
			{
				int len = 2;
				byte[] bytes = new byte[len];
				System.arraycopy(b, p*len, bytes, 0, bytes.length);
				int off = 0;

				if (bytes.length - off < len) len = bytes.length - off;
				short total = 0; int ndx=off; int i = 0;

				int major = (bytes[ndx] < 0 ? 256 + bytes[ndx] : (int) bytes[ndx]) << (( len - i - 1) * 8);
				ndx++;i++;

				int minor = (short) ((bytes[ndx] < 0 ? 256 + bytes[ndx] : (int) bytes[ndx]) << (( len - i - 1) * 8));

				total = (short) (major | minor);

				resultsIntArray[p] = total;
			}

			return resultsIntArray;
		}

		public short[] getRefImageArray()
		{
			return imageShortData.clone();
		}

		/**
		 * Displays an image using the getBufferedImage method of the respective image Processor object
		 * @param imageProcessor - the image processor to display
		 * @param title - Optional - Title to display
		 * @param displayTime - Optional - the number of seconds to display
		 */
	private void showImageProcessor( ImageProcessor imageProcessor, String title, int displayTime )
	{
		if (title == null) title = "";
		if (displayTime <= 0 ) displayTime = 3000;
		{
			ColorProcessorTest.displayGraphicsInNewJFrame( imageProcessor.getBufferedImage(), title, displayTime);
		}

	}

	/**
	 * @param ip imageProcessor
	 * @param expected String of expected results unique to the reference image
	 */
	private void testImageStats( ImageProcessor ip, String expected)
	{
		ImageStatistics imageStatistics = ImageStatistics.getStatistics( ip, 0xFFFFFFFF, null );
		imageStatistics.getCentroid( ip );
		String testResults = imageStatistics + " " + imageStatistics.xCenterOfMass + " " + imageStatistics.yCenterOfMass + " " + imageStatistics.xCentroid + " " + imageStatistics.yCentroid;

		//{ System.out.println(imageStatistics + " " + imageStatistics.xCenterOfMass + " " + imageStatistics.yCenterOfMass + " " + imageStatistics.xCentroid + " " + imageStatistics.yCentroid); }

		assertEquals( expected, testResults );

	}


	@Test
	public void testSetColorColor()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );

		Color refColor = Color.BLUE;

		testShortProcessor.setColor(refColor);
		Color testColor = testShortProcessor.drawingColor;

		//Display image
		//showImageProcessor( testShortProcessor, "SetColor", 3000 );

		//check results
		assertEquals( testColor, testColor );
	}

	@Test
	public void testSetValue()
	{
		int refColor = 255;
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.setValue(refColor);
		int testFGColor = testShortProcessor.fgColor;

		//check results
		assertEquals( refColor, testFGColor );
	}

	@Test
	public void testSetBackgroundValue()
	{
		int refBackgroundColor = 0;
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.setBackgroundValue(refBackgroundColor);
		int testBGColor = (int)testShortProcessor.getBackgroundValue();

		//check results
		assertEquals( refBackgroundColor, testBGColor );
	}

	@Test
	public void testGetBackgroundValue()
	{
		int refBackgroundColor = 0;
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );

		testShortProcessor.setBackgroundValue(refBackgroundColor);
		int testBGColor = (int)testShortProcessor.getBackgroundValue();

		//check results
		assertEquals( refBackgroundColor, testBGColor );
	}

	@Test
	public void testGetMin()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		double testMin = testShortProcessor.getMin();
		assertEquals( 0, testMin, 0.0 );
	}

	@Test
	public void testGetMax()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		double testMax = testShortProcessor.getMax();
		assertEquals( 255.0, testMax, 0.0 );
	}

	@Test
	public void testSetMinAndMax()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.setMinAndMax(50.0, 150.0);
		short[] testShortArray = (short[]) testShortProcessor.getPixels();

		for(int i = 0; i < testShortArray.length; i++)
		{
			assertEquals( 0, testShortArray[i], Short.MAX_VALUE );
		}
	}

	@Test
	public void testResetMinAndMax()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.resetMinAndMax();
		assertEquals( 255.0, testShortProcessor.getMax(), 0.0 );
		assertEquals( 0.0, testShortProcessor.getMin(), 0.0 );
	}

	@Test
	public void testSetThreshold()
	{
		//RED_LUT, BLACK_AND_WHITE_LUT, OVER_UNDER_LUT or NO_LUT_UPDATE
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );

		testShortProcessor.setThreshold( 12, 200, ImageProcessor.RED_LUT );
		testImageStats( testShortProcessor, "stats[count=226379, mean=73.13617429178501, min=12.0, max=200.0] 493.700509661043 426.8716212502478 500.0 500.0");

		testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.setThreshold( 12, 200, ImageProcessor.BLACK_AND_WHITE_LUT );
		testImageStats( testShortProcessor, "stats[count=226379, mean=73.13617429178501, min=12.0, max=200.0] 493.700509661043 426.8716212502478 500.0 500.0");

		testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.setThreshold( 128, 130, ImageProcessor.OVER_UNDER_LUT );
		testImageStats( testShortProcessor, "stats[count=1499, mean=128.98132088058705, min=128.0, max=130.0] 500.5312501616298 330.78860108718703 500.0 500.0");

		testShortProcessor= new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.setThreshold( 12, 200, ImageProcessor.NO_LUT_UPDATE );
		testImageStats( testShortProcessor, "stats[count=226379, mean=73.13617429178501, min=12.0, max=200.0] 493.700509661043 426.8716212502478 500.0 500.0");
	}

	@Test
	public void testFlipVertical()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.flipVertical();
		testImageStats( testShortProcessor, "stats[count=1000000, mean=16.850448, min=0.0, max=255.0] 498.3031433941697 577.2848704081933 500.0 500.0");
	}

	@Test
	public void testFill()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.flipVertical();
		testImageStats( testShortProcessor, "stats[count=1000000, mean=16.850448, min=0.0, max=255.0] 498.3031433941697 577.2848704081933 500.0 500.0");
	}

	@Test
	public void testFillImageProcessor()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.flipVertical();
		testImageStats( testShortProcessor, "stats[count=1000000, mean=16.850448, min=0.0, max=255.0] 498.3031433941697 577.2848704081933 500.0 500.0");
	}

	@Test
	public void testGetPixels()
	{
		ShortProcessor refShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		short[] actual = (short[]) refShortProcessor.getPixels();

		assertArrayEquals( getRefImageArray(), actual);

	}

	@Test
	public void testGetPixelsCopy()
	{
		ShortProcessor refShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		short[] actual = (short[]) refShortProcessor.getPixelsCopy();
		assertArrayEquals( getRefImageArray(), actual);
	}

	@Test
	public void testGetPixelIntInt()
	{
		ShortProcessor refShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		short[] actual = new short[ width*height ];
		short[] reference = getRefImageArray();

		for( int i = 0; i < height; i++ )
		{
			for( int j = 0; j < width; j++ )
			{
				actual[i*width+j] = (short) refShortProcessor.getPixel( j, i );
			}
		}

		assertArrayEquals( reference, actual );
	}

	@Test
	public void testGetIntInt()
	{
		ShortProcessor refShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		short[] actual = new short[ width*height ];

		for( int i = 0; i < height; i++ )
		{
			for( int j = 0; j < width; j++ )
			{
				actual[i*width+j] = (short) refShortProcessor.get( j, i );
			}
		}
		assertArrayEquals( getRefImageArray(), actual );
	}

	@Test
	public void testGetInt()
	{
		ShortProcessor refShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		short[] actual = new short[ width*height ];

		for( int j = 0; j < height*width; j++ )
		{
			actual[j] = (short) refShortProcessor.get( j );
		}

		assertArrayEquals( getRefImageArray(), actual );
	}

	@Test
	public void testSetIntIntInt()
	{
		ShortProcessor refShortProcessor = new ShortProcessor( width, height );

		short[] reference = getRefImageArray();

		for( int i = 0; i < height; i++ )
		{
			for( int j = 0; j < width; j++ )
			{
				refShortProcessor.set( j, i, reference[ i*width+j ] );
			}
		}

		short[] actual = (short[]) refShortProcessor.getPixels();
		assertArrayEquals( reference, actual );
	}

	@Test
	public void testSetIntInt()
	{
		ShortProcessor refShortProcessor = new ShortProcessor( width, height );
		short[] reference = getRefImageArray();

		for( int j = 0; j < height*width; j++ )
		{
			refShortProcessor.set( j, reference[j] );
		}

		short[] actual = (short[]) refShortProcessor.getPixels();
		assertArrayEquals( reference, actual );
	}

	@Test
	public void testGetfIntInt()
	{
		ShortProcessor refShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		short[] reference = getRefImageArray();

		for( int i = 0; i < height; i++ )
		{
			for( int j = 0; j < width; j++ )
			{
				float actual = refShortProcessor.getf( j, i );
				assertEquals(  reference[ i*width+j ], actual, 0.0 );
			}
		}
	}

	@Test
	public void testGetfInt()
	{
		ShortProcessor refShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		short[] reference = getRefImageArray();

		for( int j = 0; j < height*width; j++ )
		{
			float actual = refShortProcessor.getf( j );
			assertEquals(  reference[ j ], actual, 0.0 );
		}
	}

	@Test
	public void testSetfIntIntFloat()
	{
		ShortProcessor refShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		short[] reference = getRefImageArray();

		for( int i = 0; i < height; i++ )
		{
			for( int j = 0; j < width; j++ )
			{
				refShortProcessor.setf( j, i, reference[ i*width+j ] );
			}
		}

		short[] actual = (short[]) refShortProcessor.getPixels();
		assertArrayEquals( reference, actual );
	}

	@Test
	public void testSetfIntFloat()
	{
		ShortProcessor refShortProcessor = new ShortProcessor( width, height );
		short[] reference = getRefImageArray();

		for( int j = 0; j < height*width; j++ )
		{
			refShortProcessor.setf( j, reference[ j ] );
		}

		short[] actual = (short[]) refShortProcessor.getPixels();
		assertArrayEquals( reference, actual );
	}

	@Test
	public void testGetInterpolatedPixel()
	{
		ShortProcessor refShortProcessor = new ShortProcessor( width, height );
		short[] reference = getRefImageArray();
		short[] actual = new short[ width*height ];

		for( int i = 0; i < height; i++ )
		{
			for( int j = 0; j < width; j++ )
			{
				actual[i*width+j] = (short) refShortProcessor.getInterpolatedPixel(j,i);
			}
		}
		testImageStats( refShortProcessor, "stats[count=1000000, mean=0.0, min=0.0, max=0.0] NaN NaN 500.0 500.0");
	}

	@Test
	public void testGetPixelInterpolated()
	{
		ShortProcessor refShortProcessor = new ShortProcessor( width, height );
		short[] reference = getRefImageArray();
		short[] actual = new short[ width*height ];

		for( int i = 0; i < height; i++ )
		{
			for( int j = 0; j < width; j++ )
			{
				actual[i*width+j] = (short) refShortProcessor.getPixelInterpolated(j,i);
			}
		}
		testImageStats( refShortProcessor, "stats[count=1000000, mean=0.0, min=0.0, max=0.0] NaN NaN 500.0 500.0");

	}

	@Test
	public void testPutPixelIntIntInt()
	{
		ShortProcessor refShortProcessor = new ShortProcessor( width, height );
		short[] reference = getRefImageArray();

		for( int i = 0; i < height; i++ )
		{
			for( int j = 0; j < width; j++ )
			{
				refShortProcessor.putPixel( j, i, reference[ i*width+j ] );
			}
		}

		short[] actual = (short[]) refShortProcessor.getPixels();
		assertArrayEquals( reference, actual );
	}

	@Test
	public void testGetPixelValue()
	{
		ShortProcessor refShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		short[] reference = getRefImageArray();
		short[] actual = new short[ width*height ];

		for( int i = 0; i < height; i++ )
		{
			for( int j = 0; j < width; j++ )
			{
				actual[ i*width+j ] = (short) refShortProcessor.getPixelValue( j, i );
			}
		}

		assertArrayEquals( reference, actual );
	}

	@Test
	public void testPutPixelValue()
	{
		ShortProcessor refShortProcessor = new ShortProcessor( width, height );
		short[] reference = getRefImageArray();

		for( int i = 0; i < height; i++ )
		{
			for( int j = 0; j < width; j++ )
			{
				refShortProcessor.putPixelValue( j, i, reference[ i*width+j ] );
			}
		}

		short[] actual = (short[]) refShortProcessor.getPixels();
		assertArrayEquals( reference, actual );
	}

	@Test
	public void testDrawPixel()
	{
		ShortProcessor refShortProcessor = new ShortProcessor( width, height );
		short[] reference = getRefImageArray();

		for( int i = 0; i < height; i++ )
		{
			for( int j = 0; j < width; j++ )
			{
				refShortProcessor.setValue( reference[i*width+j ]);
				refShortProcessor.drawPixel( j, i );
			}
		}

		short[] actual = (short[]) refShortProcessor.getPixels();
		assertArrayEquals( reference, actual );
	}

	@Test
	public void testSetPixelsObject()
	{
		ShortProcessor refShortProcessor = new ShortProcessor( width, height );
		short[] reference = getRefImageArray();
		refShortProcessor.setPixels( reference );

		short[] actual = (short[]) refShortProcessor.getPixels();
		assertArrayEquals( reference, actual );
	}

	@Test
	public void testCopyBits()
	{
		ShortProcessor refShortProcessor = new ShortProcessor( width, height );
		short[] reference = getRefImageArray();
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		refShortProcessor.copyBits( testShortProcessor, 0, 0, Blitter.COPY );

		short[] actual = (short[]) refShortProcessor.getPixels();
		assertArrayEquals( reference, actual );
	}

	@Test
	public void testApplyTable()
	{
		ShortProcessor refShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );

		int[] rampLut = new int[65536];
		for(int i = 0; i < rampLut.length; i++)
		{
			rampLut[i]=0xFFFFFFFF;
		}

		refShortProcessor.applyTable( rampLut );
		testImageStats( refShortProcessor, "stats[count=1000000, mean=65535.0, min=65535.0, max=65535.0] 500.0 500.0 500.0 500.0");
	}

	@Test
	public void testInvert()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.invert();
		testImageStats( testShortProcessor, "stats[count=1000000, mean=238.149552, min=0.0, max=255.0] 500.12006234636965 505.46834826714263 500.0 500.0");
	}

	@Test
	public void testAddInt()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.add( 19 );
		testImageStats( testShortProcessor, "stats[count=1000000, mean=35.850448, min=19.0, max=274.0] 499.2024424910952 463.6745211663743 500.0 500.0");

	}

	@Test
	public void testAddDouble()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.add( 19.99 );
		testImageStats( testShortProcessor, "stats[count=1000000, mean=35.850448, min=19.0, max=274.0] 499.2024424910952 463.6745211663743 500.0 500.0");

	}

	@Test
	public void testMultiply()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.multiply( 19.99 );
		testImageStats( testShortProcessor, "stats[count=1000000, mean=336.817067, min=0.0, max=5097.0] 498.30604993214314 422.7105549330729 500.0 500.0");

	}

	@Test
	public void testAnd()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.and( 19 );
		testImageStats( testShortProcessor, "stats[count=1000000, mean=2.297324, min=0.0, max=19.0] 492.2125882113276 442.2925995636663 500.0 500.0");

	}

	@Test
	public void testOr()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.or( 19 );
		testImageStats( testShortProcessor, "stats[count=1000000, mean=33.553124, min=19.0, max=255.0] 499.68102564756714 465.13850412259677 500.0 500.0");

	}

	@Test
	public void testXor()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.xor( 19 );
		testImageStats( testShortProcessor, "stats[count=1000000, mean=31.2558, min=0.0, max=255.0] 500.229961223197 466.8176946998637 500.0 500.0");

	}

	@Test
	public void testGamma()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.gamma(19.99);
		testImageStats( testShortProcessor, "stats[count=1000000, mean=0.167973, min=0.0, max=255.0] 801.4506587368207 123.33004411423265 500.0 500.0");

	}

	@Test
	public void testLog()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.log();
		testImageStats( testShortProcessor, "stats[count=1000000, mean=44.586755, min=0.0, max=255.0] 493.3669982374811 437.89728634658434 500.0 500.0");

	}

	@Test
	public void testExp()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.exp();
		testImageStats( testShortProcessor, "stats[count=1000000, mean=2.28473, min=1.0, max=254.0] 530.0959159287968 415.7959596976448 500.0 500.0");

	}

	@Test
	public void testSqr()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.sqr();
		testImageStats( testShortProcessor, "stats[count=1000000, mean=1467.13319, min=0.0, max=65025.0] 511.77232732428337 397.4907999409379 500.0 500.0");

	}

	@Test
	public void testSqrt()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.sqrt();
		testImageStats( testShortProcessor, "stats[count=1000000, mean=1.824248, min=0.0, max=15.0] 494.83297610851156 432.6406497362201 500.0 500.0");

	}

	@Test
	public void testAbs()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.abs();
		testImageStats( testShortProcessor, "stats[count=1000000, mean=16.850448, min=0.0, max=255.0] 498.3031433941697 422.7151295918067 500.0 500.0");

	}

	@Test
	public void testMin()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.min( 19.9 );

		testImageStats( testShortProcessor, "stats[count=1000000, mean=31.493966, min=19.0, max=255.0] 500.1173057086554 466.83324215184587 500.0 500.0");
	}

	@Test
	public void testMax()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.max( 199.9 );

		testImageStats( testShortProcessor, "stats[count=1000000, mean=16.806114, min=0.0, max=199.0] 497.49234641631017 423.52412455371893 500.0 500.0");
	}

	@Test
	public void testCreateImage()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		Image ij = testShortProcessor.createImage();

		Image refImage = testShortProcessor.createBufferedImage();

		assertEquals( ij.equals(refImage), true  );

	}

	@Test
	public void testGetBufferedImage()
	{
		//TODO: this throws exception....
	}

	@Test
	public void testCreateProcessor()
	{
		ShortProcessor refShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		ShortProcessor testShortProcessor = (ShortProcessor) refShortProcessor.createProcessor( width, height );

		assertEquals( refShortProcessor.width, testShortProcessor.width );
	}

	@Test
	public void testSnapshot()
	{
		ShortProcessor refShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		short[] reference = getRefImageArray();
		refShortProcessor.snapshot();
		refShortProcessor.fill();
		refShortProcessor.reset();
		assertArrayEquals( reference, (short[]) refShortProcessor.getPixels() );
	}

	@Test
	public void testReset()
	{
		ShortProcessor refShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		short[] reference = getRefImageArray();
		refShortProcessor.snapshot();
		refShortProcessor.fill();
		refShortProcessor.reset();
		assertArrayEquals( reference, (short[]) refShortProcessor.getPixels() );
	}

	@Test
	public void testResetImageProcessor()
	{
		ShortProcessor refShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		refShortProcessor.fill();
		byte[] imageMask = new byte[width*height];

		for(int i = 0; i <imageMask.length; i++)
			if(i%2==0) imageMask[i] = (byte) 0xff;
		ImageProcessor mask = new ByteProcessor(width, height, imageMask, refShortProcessor.getColorModel() );
		refShortProcessor.reset( mask );

	}

	@Test
	public void testSetSnapshotPixels()
	{
		ShortProcessor refShortProcessor = new ShortProcessor( width, height );
		short[] reference = getRefImageArray();
		refShortProcessor.setSnapshotPixels( reference );
		refShortProcessor.reset();
		assertArrayEquals( reference, (short[]) refShortProcessor.getPixels() );
	}

	@Test
	public void testGetSnapshotPixels()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		short[] reference = getRefImageArray();
		testShortProcessor.snapshot();
		short[] actual = (short[]) testShortProcessor.getSnapshotPixels();
		assertArrayEquals( reference, actual );
		testImageStats( testShortProcessor, "stats[count=1000000, mean=16.850448, min=0.0, max=255.0] 498.3031433941697 422.7151295918067 500.0 500.0");

	}

	@Test
	public void testConvolve3x3()
	{
		 final int[] kernel = {-1,-1,-1,-1,8,-1,-1,-1,-1};
		 ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		 testShortProcessor.convolve3x3( kernel );
		 testImageStats( testShortProcessor, "stats[count=1000000, mean=3.365756, min=0.0, max=1253.0] 607.6431886328064 372.68363155261403 500.0 500.0");
	}

	@Test
	public void testFilter()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.filter( ImageProcessor.BLUR_MORE );
		testImageStats( testShortProcessor, "stats[count=1000000, mean=16.849997, min=0.0, max=236.0] 498.299781982157 422.7096992658218 500.0 500.0");

		testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.filter( ImageProcessor.FIND_EDGES );
		testImageStats( testShortProcessor, "stats[count=1000000, mean=16.260558, min=0.0, max=1124.0] 566.2793151993923 374.4035295098729 500.0 500.0");

		//TODO throws exception
		//testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		//testShortProcessor.filter( ImageProcessor.CONVOLVE );
		//testImageStats( testShortProcessor, "stats[count=1000000, mean=13.042211, min=0.0, max=680.0] 558.3728342149963 376.2391702219815 500.0 500.0");
	}

	@Test
	public void testMedianFilter()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.medianFilter();
		testImageStats( testShortProcessor, "stats[count=1000000, mean=16.850448, min=0.0, max=255.0] 498.3031433941697 422.7151295918067 500.0 500.0");

	}

	@Test
	public void testNoise()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.noise( 19.99 );
	}

	@Test
	public void testCrop()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.setRoi( new Rectangle( 0, 0, width, height ) );
		testShortProcessor.crop();
		testImageStats( testShortProcessor, "stats[count=1000000, mean=16.850448, min=0.0, max=255.0] 498.3031433941697 422.7151295918067 500.0 500.0");
	}

	@Test
	public void testThreshold()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.threshold( 19 );
		testImageStats( testShortProcessor, "stats[count=1000000, mean=56.791305, min=0.0, max=255.0] 491.34025934956065 437.96728720179965 500.0 500.0");
	}

	@Test
	public void testDuplicate()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		ImageProcessor testIP = testShortProcessor.duplicate();
		testImageStats( testIP, "stats[count=1000000, mean=16.850448, min=0.0, max=255.0] 498.3031433941697 422.7151295918067 500.0 500.0");
	}

	@Test
	public void testScale()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.scale( 0.5, 0.5 );
		testImageStats( testShortProcessor, "stats[count=1000000, mean=4.211751, min=0.0, max=255.0] 499.30536812361413 461.64208935903383 500.0 500.0");
	}

	@Test
	public void testResizeIntInt()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.resize( 5, 5 );
		testImageStats( testShortProcessor, "stats[count=1000000, mean=16.850448, min=0.0, max=255.0] 498.3031433941697 422.7151295918067 500.0 500.0");

	}

	@Test
	public void testRotate() {
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.rotate( 19 );
		testImageStats( testShortProcessor, "stats[count=1000000, mean=16.634975, min=0.0, max=255.0] 518.0090180778751 425.672104797272 500.0 500.0");
	}

	@Test
	public void testGetHistogram()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
	    int[] histogram = testShortProcessor.getHistogram();

	    //convert the int array to bytes
	    ByteArrayOutputStream baos = new ByteArrayOutputStream( histogram.length * 4 );
	    for(int i : histogram ) baos.write(i);

	    //get sha1 of histogram array
	    byte[] results = FloatProcessorTest.getSHA1DigestFromByteArray( baos.toByteArray() );
			byte[] reference = {
				-0x80, -0x18, 0x2d, 0x19, -0x6d, -0x24, 0x45, -0xc, -0x62, -0x57,
				-0x29, 0x53, 0x78, -0x33, -0x3c, -0x17, 0x45, 0x5d, -0x68, 0x13
			};
			assertArrayEquals( reference, results );
	}

	@Test
	public void testErode()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.erode();
		testImageStats( testShortProcessor, "stats[count=1000000, mean=16.850448, min=0.0, max=255.0] 498.3031433941697 422.7151295918067 500.0 500.0");

	}

	@Test
	public void testDilate()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.dilate();
		testImageStats( testShortProcessor, "stats[count=1000000, mean=16.850448, min=0.0, max=255.0] 498.3031433941697 422.7151295918067 500.0 500.0");

	}

	@Test
	public void testConvolve()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
	    float[] kernel = {-1,-1,-1,-1,8,-1,-1,-1,-1};
		testShortProcessor.convolve( kernel, 3, 3 );
		testImageStats( testShortProcessor, "stats[count=1000000, mean=3.365756, min=0.0, max=1253.0] 607.6431886328064 372.68363155261403 500.0 500.0");
	}

	@Test
	public void testToFloat()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		FloatProcessor testFloatProcessor = testShortProcessor.toFloat( 0, null );
		testImageStats( testFloatProcessor, "stats[count=1000000, mean=16.850448, min=0.0, max=255.0] 498.3031433941697 422.7151295918067 500.0 500.0" );
	}

	@Test
	public void testSetPixelsIntFloatProcessor()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.flipVertical();
		FloatProcessor refFloatProcessor = new FloatProcessor ( testShortProcessor.getFloatArray()  );

		testShortProcessor.setPixels( 0, refFloatProcessor);

		testImageStats( testShortProcessor, "stats[count=1000000, mean=16.850448, min=0.0, max=255.0] 498.3031433941697 577.2848704081933 500.0 500.0" );
	}

	@Test
	public void testMaxValue()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		assertEquals( 65535.0, testShortProcessor.maxValue(), 0.0 );
	}

	@Test
	public void testCreate8BitImage()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.flipHorizontal();
		byte[] results = testShortProcessor.create8BitImage();
		byte[] sha1Results = FloatProcessorTest.getSHA1DigestFromByteArray( results );
		byte[] reference = {
			-0x6e, -0x74, 0x56, 0x1d, -0x37, -0x47, 0x1, 0x72, 0x1b, 0x5e,
			-0x29, 0x1d, 0x1a, 0x7e, 0x7d, -0x63, 0x28, -0x2d, 0x5, -0x20
		};
		assertArrayEquals( reference, sha1Results );
	}

	@Test
	public void testShortProcessorIntIntShortArrayColorModel()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testImageStats( testShortProcessor, "stats[count=1000000, mean=16.850448, min=0.0, max=255.0] 498.3031433941697 422.7151295918067 500.0 500.0");
	}

	@Test
	public void testShortProcessorIntInt()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testImageStats( testShortProcessor, "stats[count=1000000, mean=16.850448, min=0.0, max=255.0] 498.3031433941697 422.7151295918067 500.0 500.0");
	}

	@Test
	public void testShortProcessorBufferedImage()
	{
		BufferedImage bi = new BufferedImage( width, height, BufferedImage.TYPE_USHORT_GRAY );
		ShortProcessor testShortProcessor = new ShortProcessor( bi );
		testImageStats( testShortProcessor, "stats[count=1000000, mean=0.0, min=0.0, max=0.0] NaN NaN 500.0 500.0");
	}

	@Test
	public void testInit()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height );
		testShortProcessor.init(width, height, getRefImageArray(), null);
		testImageStats( testShortProcessor, "stats[count=1000000, mean=16.850448, min=0.0, max=255.0] 498.3031433941697 422.7151295918067 500.0 500.0");
	}

	@Test
	public void testShortProcessorIntIntShortArrayColorModelBoolean()
	{
		ShortProcessor refShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null, true);
		testImageStats( refShortProcessor, "stats[count=1000000, mean=16.850448, min=0.0, max=255.0] 498.3031433941697 422.7151295918067 500.0 500.0");
	}

	@Test
	public void testShortProcessorIntIntBoolean()
	{
		ShortProcessor refShortProcessor = new ShortProcessor( width, height, true );
		testImageStats( refShortProcessor, "stats[count=1000000, mean=0.0, min=0.0, max=0.0] NaN NaN 500.0 500.0");
	}

	@Test
	public void testFindMinAndMax()
	{
		ShortProcessor refShortProcessor = new ShortProcessor( width, height, true );
		refShortProcessor.findMinAndMax();
		assertEquals( 0.0, refShortProcessor.getMin() , 0.0 );
		assertEquals( 0.0, refShortProcessor.getMax() , 0.0 );
	}

	//TODO:throws Exception
	@Test
	public void testCreateBufferedImage()
	{
		ShortProcessor refShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		//Image bi = refShortProcessor.createBufferedImage();
		//assertEquals( true, bi.getHeight(null)==bi.getWidth(null) );
	}

	@Test
	public void testGet16BitBufferedImage() {
		ShortProcessor refShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		DataBuffer db = refShortProcessor.get16BitBufferedImage().getData().getDataBuffer();
		short[] results = new short[width*height];
		short[] reference = getRefImageArray();
		for(int i =0; i<width*height; i++) results[i] = (short) db.getElem(i);
		assertArrayEquals( reference, results );
	}

	@Test
	public void testGetRow2()
	{
		ShortProcessor refShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		int[] data = new int[width*height];
		refShortProcessor.getRow2( 0, 0, data, data.length );
		ColorProcessor fp = new ColorProcessor( width, height, data );
		testImageStats( fp, "stats[count=1000000, mean=5.616306, min=0.0, max=85.0] 498.30314335718765 422.7151296491211 500.0 500.0" );
	}

	//TODO: this contains an error
	@Test
	public void testPutColumn2()
	{
		//ShortProcessor refShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		//int[] data = new int[width*height];
		//refShortProcessor.putColumn2(0, 0, data, data.length);
		//ColorProcessor fp = new ColorProcessor( width, height, data );
		//testImageStats( fp, "stats[count=1000000, mean=5.616306, min=0.0, max=85.0] 498.30314335718765 422.7151296491211 500.0 500.0" );
	}

	@Test
	public void testFilter3x3()
	{
		int[] kernel = {-1,-1,-1,-1, 8,-1,-1,-1,-1};
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.filter3x3( ImageProcessor.BLUR_MORE, kernel );
		testImageStats( testShortProcessor, "stats[count=1000000, mean=16.849997, min=0.0, max=236.0] 498.299781982157 422.7096992658218 500.0 500.0");

		testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.filter3x3( ImageProcessor.FIND_EDGES, kernel  );
		testImageStats( testShortProcessor, "stats[count=1000000, mean=16.260558, min=0.0, max=1124.0] 566.2793151993923 374.4035295098729 500.0 500.0");


		testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		testShortProcessor.filter3x3( ImageProcessor.CONVOLVE, kernel  );
		testImageStats( testShortProcessor, "stats[count=1000000, mean=3.365756, min=0.0, max=1253.0] 607.6431886328064 372.68363155261403 500.0 500.0");

	}

	@Test
	public void testGetHistogramImageProcessor()
	{
		ShortProcessor testShortProcessor = new ShortProcessor( width, height, getRefImageArray(), null );
		byte[] imageMask = new byte[width*height];

		for(int i = 0; i <imageMask.length; i++)
			if(i%2==0) imageMask[i] = (byte) 0xff;
		ImageProcessor mask = new ByteProcessor(width, height, imageMask, testShortProcessor.getColorModel() );
		int[] histogram = testShortProcessor.getHistogram( mask );

	    //convert the int array to bytes
	    ByteArrayOutputStream baos = new ByteArrayOutputStream( histogram.length * 4 );
	    for(int i : histogram ) baos.write(i);

	    //get sha1 of histogram array
	    byte[] results = FloatProcessorTest.getSHA1DigestFromByteArray( baos.toByteArray() );
			byte[] reference = {
				0x57, -0x62, 0x1b, -0x12, 0x15, -0x1c, -0x36, 0x2a, 0x7e, -0x20,
				0x41, -0x2b, -0x11, -0x6d, 0x1a, -0x47, -0x19, 0x7b, -0x3b, -0x23
			};
			assertArrayEquals ( reference, results );
	}

}
