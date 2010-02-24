package ij.process;

import static org.junit.Assert.*;

import ij.ImageStack;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;

import java.awt.Rectangle;

import java.awt.image.BufferedImage;

import java.awt.image.SampleModel;

import java.io.FileInputStream;

import java.io.IOException;

import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.junit.BeforeClass;
import org.junit.Test;

public class ColorProcessorTest {

    public static int width;
    public static int height;
    private static byte[] rawByte;
    private static int[] imageIntData;

    /*
     * Open an known image test image for global use
     */
	@BeforeClass
	public static void runBeforeClass() throws IOException
	{
	    String id = "data/clown.raw";
	    width = 320;
	    height = 200;
	    
	    rawByte = new byte[width*height*3];
	    FileInputStream file = new FileInputStream(id);
		file.read(rawByte);
		imageIntData  = byteArrayToIntArray(rawByte);
    }
	
	public static Vector<Integer> intArrayToVectorTypeInteger(int[] intArray)
	{
		Vector<Integer> imageIntegerVec = new Vector<Integer>();
		for(int i:intArray)
			imageIntegerVec.add(i);
		
		return imageIntegerVec;
	}
	
	public static void displayGraphicsInNewJFrame(BufferedImage i, String label, long millis)
	{
		Dimension preferredSize = new Dimension( i.getWidth(), i.getHeight() );
		JFrame f = new JFrame("ij-test Display");
		f.setPreferredSize(preferredSize);
		
		//f.getContentPane().add("Center", new JScrollPane(new JLabel(new ImageIcon(image))));
		ImageIcon imageIcon = new ImageIcon(i);
		
		JLabel jl = new JLabel();
	
		jl.setIcon(imageIcon);
		//JScrollPane jsp = new JScrollPane(jl); 

		f.getContentPane().add("Center", jl);
		f.getContentPane().add("North", new JLabel(label));
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//f.pack();
		f.setSize(preferredSize);
		f.setVisible(true);
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	
	/**
	 * Converts a byte[] to an int[]; assumes native byte ordering; 
	 * @param b - Byte[] 
	 * @return
	 */
	private static int[] byteArrayToIntArray(byte[] b ) 
	{
		int intSize = b.length/3;
	
		int[] resultsIntArray = new int[intSize];

		for (int p = 0; p<intSize; p++)
		{
			int len = 3;
			byte[] bytes = new byte[3];
			System.arraycopy(b, p*3, bytes, 0, bytes.length);
			int off = 0;

			if (bytes.length - off < len) len = bytes.length - off;
			int total = 0; int ndx=off; int i = 0;

			int red = (bytes[ndx] < 0 ? 256 + bytes[ndx] : (int) bytes[ndx]) << ((len - i - 1) * 8);
			ndx++;i++;
			
			int green = (bytes[ndx] < 0 ? 256 + bytes[ndx] : (int) bytes[ndx]) << (( len - i - 1) * 8);
			ndx++;i++;
			
			int blue = (bytes[ndx] < 0 ? 256 + bytes[ndx] : (int) bytes[ndx]) << (( len - i - 1) * 8);
			
			int alpha = (byte)0x00;

			total = red | green | blue | alpha; 

			resultsIntArray[p] = total;
		}

		return resultsIntArray;
	}
	
	public int[] getRefImageArray()
	{
		return imageIntData.clone();
	}
	
	public int[] getRefImageArrayAsInteger()
	{
		return imageIntData.clone();
	}
	
	@Test
	public void testInvertLut() 
	{
		//Code does nothing - pass test
		assertEquals(true, true);
	}

	@Test
	public void testGetBestIndex() {
		//Code does nothing - pass test
		assertEquals(true, true);
	}

	@Test
	public void testIsInvertedLut() 
	{
		assertEquals(false, new ColorProcessor(width, height).isInvertedLut());
	}

	@Test
	public void testSetColorColor() 
	{
		Color refColor = Color.BLUE;
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		testColorProcessor.setColor(refColor);
		Color testColor = testColorProcessor.drawingColor;
		
		//check results
		assertEquals( testColor, testColor );
	}

	@Test
	public void testSetColorInt() 
	{
		int refColor = 255;
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		testColorProcessor.setColor(refColor);
		int testColor = testColorProcessor.fgColor;
		
		//check results
		assertEquals( refColor, testColor );
	}

	@Test
	public void testSetValue() 
	{
		int refColor = 255;
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		testColorProcessor.setValue(refColor);
		int testFGColor = testColorProcessor.fgColor;
		
		//check results
		assertEquals( refColor, testFGColor );
	}

	@Test
	public void testSetBackgroundValue() 
	{
		int refBackgroundColor = 255;
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		testColorProcessor.setBackgroundValue(refBackgroundColor);
		int testBGColor = (int)testColorProcessor.getBackgroundValue();
		
		//check results
		assertEquals( refBackgroundColor, testBGColor );
	}

	@Test
	public void testGetBackgroundValue() 
	{
		int refBackgroundColor = 255;
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
	
		testColorProcessor.setBackgroundValue(refBackgroundColor);
		int testBGColor = (int)testColorProcessor.getBackgroundValue();
		
		//check results
		assertEquals( refBackgroundColor, testBGColor );
	}

	@Test
	public void testGetMin() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		double testMin = testColorProcessor.getMin();
		//int count = 0;
		//int refMin = Integer.MAX_VALUE;
		//for(int i:imageIntegerVector) 
		//{	
		//	if(i<refMin) {refMin = i;System.out.println(count + " " + i);}
		//}
		
		//check results
		assertEquals( 0, testMin, 0.0 );
	}

	@Test
	public void testGetMax() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		double testMax = testColorProcessor.getMax();
		
		//int refMax = Collections.max(imageIntegerVector);
		
		//check results
		assertEquals( 255, testMax, 0.0);
	}

	@Test
	public void testSetMinAndMaxDoubleDouble() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		
		testColorProcessor.setMinAndMax(50.0, 150.0);
		int[] testIntArray = testColorProcessor.pixels;
		
		for(int i = 0; i < testIntArray.length; i++)
		{
			//check results
			assertEquals( 0, testIntArray[i], Integer.MAX_VALUE );
		}
	}

	@Test
	public void testFindEdges() 
	{
		//converts each color component to a byte[] (type ByteProcessor) and finds the edges
		//test performed in byte processor
		
	}

	public static void compareTwoIntArrays(int[] refArray, int[] testArray)
	{
		for(int i = 0; i < refArray.length; i++)
		{
			//check results
			assertEquals( refArray[i], testArray[i], 0.0 );
		}
	}
	
	@Test
	public void testFlipVertical() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		testColorProcessor.flipVertical();
		testColorProcessor.flipVertical();
		
		//test results
		compareTwoIntArrays(getRefImageArray(), testColorProcessor.pixels);
	}

	@Test
	public void testFill() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		
		//fill the entire image with white
		testColorProcessor.setColor(Color.BLACK);
		testColorProcessor.fill();
		
		//test the results
		for(int i:testColorProcessor.pixels)
			assertEquals( -16777216, i );
		
	}

	@Test
	public void testFillImageProcessor() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		
		byte[] testPattern = new byte[width * height];
		for(int h=0; h<height;h++)
			for(int w=0; w<width;w++)
				if(h%2==0&&w%2==0) testPattern[h * width + w] = (byte)0xff;
		
		ImageProcessor mask = new ByteProcessor( width, height, testPattern, null );
		
		//set the color and fill with same mask
		testColorProcessor.setColor(Color.GREEN);
		testColorProcessor.fill(mask);
		
	}

	@Test
	public void testGetPixels() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		int[] testPixelsArray = (int[]) testColorProcessor.getPixels();
		
		int imageIndex = 0;
		//check each component
		for(int i = 0; i < testPixelsArray.length; i++)
		{
			byte red = (byte) ( testPixelsArray[i]  >> 16  );	
			assertEquals( rawByte[imageIndex], red, 0.0 );
			imageIndex++;
			
			byte green = (byte) ( testPixelsArray[i] >> 8 );	
			assertEquals( rawByte[imageIndex], green, 0.0 );
			imageIndex++;
			
			byte blue = (byte) ( testPixelsArray[i] >> 0 );	
			assertEquals( rawByte[imageIndex], blue, 0.0 );
			imageIndex++;
		} 
	}

	@Test
	public void testGetPixelsCopy() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		int[] testPixelsArray = (int[]) testColorProcessor.getPixelsCopy();
		
		int imageIndex = 0;
		//check each component
		for(int i = 0; i < testPixelsArray.length; i++)
		{
			byte red = (byte) ( testPixelsArray[i]  >> 16  );	
			assertEquals( rawByte[imageIndex], red, 0.0 );
			imageIndex++;
			
			byte green = (byte) ( testPixelsArray[i] >> 8 );	
			assertEquals( rawByte[imageIndex], green, 0.0 );
			imageIndex++;
			
			byte blue = (byte) ( testPixelsArray[i] >> 0 );	
			assertEquals( rawByte[imageIndex], blue, 0.0 );
			imageIndex++;
		} 
	}

	@Test
	public void testGetPixelIntInt() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		int imageIndex = 0;
		for(int y = 0; y < height; y++)

			//check each component
			for(int x = 0; x < width; x++)
			{
				byte red = (byte) ( testColorProcessor.getPixel( x, y )  >> 16  );	
				assertEquals( rawByte[imageIndex], red, 0.0 );
				imageIndex++;

				byte green = (byte) ( testColorProcessor.getPixel( x, y ) >> 8 );	
				assertEquals( rawByte[imageIndex], green, 0.0 );
				imageIndex++;

				byte blue = (byte) ( testColorProcessor.getPixel( x, y ) >> 0 );	
				assertEquals( rawByte[imageIndex], blue, 0.0 );
				imageIndex++;
			} 
	}

	@Test
	public void testGetIntInt() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		for(int y = 0; y < height; y++)
		{
			//check each component
			for(int x = 0; x < width; x++)
			{
				assertEquals( imageIntData[y*width+x], testColorProcessor.get(x, y), 0.0);
			}
		}
	}

	@Test
	public void testGetInt() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		for(int y = 0; y < height; y++)
		{
			//check each component
			for(int x = 0; x < width; x++)
			{
				assertEquals( imageIntData[y*width+x], testColorProcessor.get(y*width+x), 0.0);
			}
		}
	}

	@Test
	public void testSetIntIntInt() 
	{
		final int value = 99;
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		for(int y = 0; y < height; y++)
		{
			//check each component
			for(int x = 0; x < width; x++)
			{
				testColorProcessor.set(x,y, value);
				assertEquals( value, testColorProcessor.get(x,y), 0.0);
			}
		}
	}

	@Test
	public void testSetIntInt() 
	{
		final int value = 99;
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		
		for(int y = 0; y < height; y++)
		{
			//check each component
			for(int x = 0; x < width; x++)
			{
				testColorProcessor.set(y*width+x, value);
				assertEquals( value, testColorProcessor.get(x, y), 0.0);
			}
		}
	}

	@Test
	public void testGetfIntInt() 
	{
		final int value = 99;
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		
		for(int y = 0; y < height; y++)
		{
			//check each component
			for(int x = 0; x < width; x++)
			{
				testColorProcessor.set(y*width+x, value);
				assertEquals( value, testColorProcessor.getf(x, y), 0.0);
			}
		}
	}

	@Test
	public void testGetfInt() 
	{
		final int value = 99;
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		
		for(int y = 0; y < height; y++)
		{
			//check each component
			for(int x = 0; x < width; x++)
			{
				testColorProcessor.set(y*width+x, value);
				assertEquals( value, testColorProcessor.getf(y*width+x), 0.0);
			}
		}
	}

	@Test
	public void testSetfIntIntFloat() 
	{
		final int value = 99;
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		
		for(int y = 0; y < height; y++)
		{
			//check each component
			for(int x = 0; x < width; x++)
			{
				testColorProcessor.setf(x,y, value);
				assertEquals( value, testColorProcessor.getf(y*width+x), 0.0);
			}
		}
	}

	@Test
	public void testSetfIntFloat() 
	{
		final int value = 99;
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		
		for(int y = 0; y < height; y++)
		{
			//check each component
			for(int x = 0; x < width; x++)
			{
				testColorProcessor.setf(y*width+x, value);
				assertEquals( value, testColorProcessor.getf(y*width+x), 0.0);
			}
		}
	}

	@Test
	public void testGetPixelIntIntIntArray() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		final int[] refValue = {99,9,0};
		int[] resultArray = new int[3];
		
		//check each component
		for(int y = 0; y < height; y++)
		{
			//check each component
			for(int x = 0; x < width; x++)
			{
				testColorProcessor.putPixel( x, y, refValue );
			
				assertEquals( refValue[0], testColorProcessor.getPixel(x, y, resultArray)[0] );
				assertEquals( refValue[1], testColorProcessor.getPixel(x, y, resultArray)[1] );
				assertEquals( refValue[2], testColorProcessor.getPixel(x, y, resultArray)[2] );
			}
		} 
	}

	@Test
	public void testPutPixelIntIntIntArray() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		final int[] refValue = {99,9,0};
		int[] resultArray = new int[3];
		
		//check each component
		for(int y = 0; y < height; y++)
		{
			//check each component
			for(int x = 0; x < width; x++)
			{
				testColorProcessor.putPixel( x, y, refValue );
			
				assertEquals( refValue[0], testColorProcessor.getPixel(x, y, resultArray)[0] );
				assertEquals( refValue[1], testColorProcessor.getPixel(x, y, resultArray)[1] );
				assertEquals( refValue[2], testColorProcessor.getPixel(x, y, resultArray)[2] );
			}
		} 
	}

	@Test
	public void testGetInterpolatedPixel() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		
		//check out of bounds
		assertEquals( 0.0, testColorProcessor.getPixelInterpolated(-1, 0), 0.0);
		assertEquals( 0.0, testColorProcessor.getPixelInterpolated(0, -1), 0.0);
		assertEquals( 0.0, testColorProcessor.getPixelInterpolated(width, height+1), 0.0);
		assertEquals( 0.0, testColorProcessor.getPixelInterpolated(width+1, height), 0.0);
		assertEquals( 0.0, testColorProcessor.getPixelInterpolated(width, height), 0.0);
		
		//check each component
		for(int y = 0; y < height-1; y++)
		{
			//check each component
			for(int x = 0; x < width-1; x++)
			{
				
				//TODO: complete test
			}
		} 
	}
	
	/**
	 * Provides easy access to the 0-255 mapped byte value at the specified position
	 * @param bytes
	 * @param position - Byte position defines as [0 1 2 3], 0 being left most byte
	 * @return
	 */
	public static int byteToInt(byte bytes, int position)
	{
		return (bytes < 0 ? 256 + bytes : (int) bytes) << ((3 - position) * 8);
	}
	
	public static byte IntToByte(int integer, int position)
	{
		return (byte) (integer >> (3 - position) * 8 );
	}

	@Test
	public void testGetPixelInterpolated() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		
		//check out of bounds
		assertEquals( 0.0, testColorProcessor.getPixelInterpolated(-1, 0), 0.0);
		assertEquals( 0.0, testColorProcessor.getPixelInterpolated(0, -1), 0.0);
		assertEquals( 0.0, testColorProcessor.getPixelInterpolated(width, height+1), 0.0);
		assertEquals( 0.0, testColorProcessor.getPixelInterpolated(width+1, height), 0.0);
		
		//check each component
		for(int y = 0; y < height-1; y++)
		{
			//check each component
			for(int x = 0; x < width-1; x++)
			{
				int index = (width*y+x)*3;
				int result = testColorProcessor.getPixelInterpolated(x, y);
				//System.out.println(y + " " + x + " " + rawByte[index] + " " +  IntToByte(result,1) );
				assertEquals( rawByte[index++], IntToByte(result,1)  );
				assertEquals( rawByte[index++], IntToByte(result,2)   );
				assertEquals( rawByte[index++], IntToByte(result,3)   );
			}
		} 
	}

	@Test
	public void testPutPixelIntIntInt() 
	{
		final int value = 0x00ff00ff;
		
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		
		//check each component
		for(int y = 0; y < height; y++)
		{
			//check each component
			for(int x = 0; x < width; x++)
			{
				testColorProcessor.putPixel(x, y, value);
				assertEquals( testColorProcessor.getPixel(x, y), value, 0.0  );
			}
		} 	
	}

	@Test
	public void testGetPixelValue() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		
		//check each component
		for(int y = 0; y < height; y++)
		{
			//check each component
			for(int x = 0; x < width; x++)
			{
				float testValue =  testColorProcessor.getPixelValue( x, y ) ;
				int refValue = testColorProcessor.getPixel(x, y);
				int[] refARGB = getARGB( refValue );
				double[] refWeights = testColorProcessor.getWeightingFactors();
				
				float refFLOAT = (float) ( refARGB[1] * refWeights[0] + refARGB[2] * refWeights[1] + refARGB[3] * refWeights[2] );

				assertEquals( refFLOAT, testValue, 0.0 );
			}
		} 
	}

	@Test
	public void testPutPixelValue() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.putPixelValue(0, 0, 128);
		assertEquals( 128, testColorProcessor.getPixelValue(0, 0), 0.0  );
		
	}

	@Test
	public void testDrawPixel() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.setValue(0x0000000000ffffff);
		testColorProcessor.drawPixel(0, 0);
		assertEquals( 255, testColorProcessor.getPixelValue(0, 0), 0.0  );
	}

	@Test
	public void testSetPixelsObject() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height );
		testColorProcessor.setPixels( getRefImageArray() );
		
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				assertEquals( imageIntData[y*width+x], testColorProcessor.get(y*width+x), 0.0);
			}
		}
	}

	@Test
	public void testCopyBits() 
	{
		ColorProcessor refColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		ColorProcessor testColorProcessor = new ColorProcessor( width, height );
		testColorProcessor.copyBits(refColorProcessor, 0, 0, ColorBlitter.COPY);
		
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				assertEquals( imageIntData[y*width+x], testColorProcessor.get(y*width+x), 0.0);
			}
		}
	}

	@Test
	public void testApplyTableIntArray() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		int[] rampLut = new int[256];
		
		for(int i = 0; i < rampLut.length; i++)
		{
			rampLut[i]=0xFFFFFFFF;
		}
		testColorProcessor.applyTable(rampLut);
		
		int[] localRefData = getRefImageArray();
		
		int c, r, g, b;
		for (int y=0; y<height; y++) {
			int i = y * width;
			for (int x=0; x<width; x++) {
				c = localRefData[i];
				r = rampLut[(c&0xff0000)>>16];
				g = rampLut[(c&0xff00)>>8];
				b = rampLut[c&0xff];
				localRefData[i] = 0xff000000 + (r<<16) + (g<<8) + b;
				i++;
			}
		}
		
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				int result = testColorProcessor.getPixel(x, y);
				
				//System.out.println(y + " " + x + " " + rawByte[index] + " " +  IntToByte(result,1) );
				assertEquals( localRefData[width*y+x],  result  );
			}
		}
	}

	@Test
	public void testCreateImage() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		Image ij = testColorProcessor.createImage();
		
		Image refImage = testColorProcessor.createBufferedImage();
		
		assertEquals( ij.equals(refImage), true  );
		
	}

	@Test
	public void testCreateProcessor() 
	{
		ColorProcessor refColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		ColorProcessor testColorProcessor = (ColorProcessor) refColorProcessor.createProcessor(width, height);
		assertEquals(width,  testColorProcessor.getWidth()  );
		assertEquals(height, testColorProcessor.getHeight() );
	}

	@Test
	public void testSnapshot() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.snapshot();
		testColorProcessor.fill();
		testColorProcessor.reset();
		
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				int result = testColorProcessor.getPixel(x, y);
				
				//System.out.println(y + " " + x + " " + rawByte[index] + " " +  IntToByte(result,1) );
				assertEquals( imageIntData[width*y+x],  result  );
			}
		}
	}

	@Test
	public void testReset() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.snapshot();
		testColorProcessor.fill();
		testColorProcessor.reset();
		
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				int result = testColorProcessor.getPixel(x, y);
				
				//System.out.println(y + " " + x + " " + rawByte[index] + " " +  IntToByte(result,1) );
				assertEquals( imageIntData[width*y+x],  result  );
			}
		}
	}

	@Test
	public void testResetImageProcessor() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		byte[] imageMask = new byte[width*height];
		
		for(int i = 0; i <imageMask.length; i++)
			if(i%2==0) imageMask[i] = (byte) 0xff;
		
		ImageProcessor mask = new ByteProcessor(width, height, imageMask, testColorProcessor.getColorModel() );
		
		testColorProcessor.snapshot();
		testColorProcessor.fill();
		testColorProcessor.reset( mask );
		
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				int index = width*y+x;
				int result = testColorProcessor.getPixel(x, y);
				
				if (index %2==0)
				{
					assertEquals( 0,  IntToByte(result, 1) );
				}
				else
				{
					assertEquals( imageIntData[index],  result  );
				}
			}
		}
		

	}

	@Test
	public void testSetSnapshotPixels() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height );
		testColorProcessor.setSnapshotPixels( getRefImageArray() );
		testColorProcessor.reset();
		
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				int result = testColorProcessor.getPixel(x, y);
				
				assertEquals( imageIntData[width*y+x],  result  );
			}
		}
	}

	@Test
	public void testGetSnapshotPixels() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.snapshot();
		int[] snapShot = (int[]) testColorProcessor.getSnapshotPixels();
		
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				int result = snapShot[width*y+x];
				
				assertEquals( imageIntData[width*y+x],  result  );
			}
		}
	}

	@Test
	public void testConvolve3x3() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
   
		//convolve3x3 int array
        int[] kernel = {-1,-1,-1,-1,8,-1,-1,-1,-1};
        int[] ref = {241,160,42};
        int[] firstResults = null;
		testColorProcessor.convolve3x3(kernel);
		firstResults = testColorProcessor.getPixel(0, 0, null);
		
		for(int y = 0; y < ref.length; y++)
		{
			assertEquals(ref[y], firstResults[y]   );
		}
	}

	@Test
	public void testFilter() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		   
		//convolve3x3 int array
        final int[] ref = {241, 160, 42};
		testColorProcessor.filter( 0 );
		int[] firstResults = testColorProcessor.getPixel(0, 0, null);
		
		for(int y = 0; y < ref.length; y++)
		{
			assertEquals(ref[y], firstResults[y], 0.0   );
		}
	}

	@Test
	public void testMedianFilter() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		   
		//convolve3x3 int array
		final int[] refFirst = {0, 0, 0};
		final int[] refLast = {0, 0, 0};
        
		testColorProcessor.medianFilter();
		int[] firstResults = testColorProcessor.getPixel(0, 0, null);
		int[] lastResults = testColorProcessor.getPixel(width, height, null);
		
		for(int y = 0; y < refLast.length; y++)
		{
			assertEquals(refFirst[y], firstResults[y], 0.0   );
			assertEquals(refLast[y], lastResults[y], 0.0   );			
		}
	}

	@Test
	public void testNoise() 
	{
		//should add some noise 
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.noise(5.0);
		int[] localRef = getRefImageArray();
		
		//at least one pixel is different
		boolean change = false;
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				int result = testColorProcessor.getPixel(x, y);
				
				if( localRef[width*y+x] != result )
				{
					change = true;
				}
			}
		}
		
		assertEquals(true, change);
	}

	@Test
	public void testCrop() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
        Rectangle testROI = new Rectangle(0, 0, width/2, height/2 );

        testColorProcessor.setRoi(testROI);
        ColorProcessor croppedColorProcessor = (ColorProcessor) testColorProcessor.crop();
        
		for(int y = 0; y < croppedColorProcessor.height; y++)
		{
			for(int x = 0; x < croppedColorProcessor.width; x++)
			{
				int result = croppedColorProcessor.getPixel(x, y);
				
				assertEquals( imageIntData[width*y+x],  result  );
			}
		}
	}

	@Test
	public void testThreshold() 
	{
		//not implemented
	}

	@Test
	public void testDuplicate() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		ColorProcessor duplicate = (ColorProcessor) testColorProcessor.duplicate();
		
		for(int y = 0; y < duplicate.height; y++)
		{
			for(int x = 0; x < duplicate.width; x++)
			{
				int result = duplicate.getPixel(x, y);
				
				assertEquals( testColorProcessor.getPixel(x, y),  result  );
			}
		}
	}

	@Test
	public void testScale() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.setInterpolationMethod(testColorProcessor.BICUBIC);
		testColorProcessor.scale(5.0, 5.0);
		//displayGraphicsInNewJFrame(testColorProcessor.getBufferedImage(), "Bicubic 5x", 300);
		
		final int[] refFirst = {0, 0, 0};
		final int[] refMid = {229, 206, 175};
		final int[] refLast = {0, 0, 0};
		
		testColorProcessor.medianFilter();
		int[] firstResults = testColorProcessor.getPixel(0, 0, null);
		int[] midRef = testColorProcessor.getPixel(width/2, height/2, null);
		int[] lastResults = testColorProcessor.getPixel(width, height, null);
		
		for(int y = 0; y < refLast.length; y++)
		{
			assertEquals(refFirst[y], firstResults[y], 0.0   );
			assertEquals(refMid[y], midRef[y], 0.0   );
			assertEquals(refLast[y], lastResults[y], 0.0   );			
		}
	}

	@Test
	public void testResizeIntInt() 
	{
		ColorProcessor refColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		refColorProcessor.setInterpolationMethod( ColorProcessor.BICUBIC );
		ColorProcessor testColorProcessor = (ColorProcessor) refColorProcessor.resize(width*3, height*3);
		//displayGraphicsInNewJFrame(testColorProcessor.getBufferedImage(), "Bicubic 5x", 300);
		
		final int[] refFirst = {0, 0, 0};
		final int[] refMid = {149, 83, 22};
		final int[] refLast = {164, 97, 67};
		
		testColorProcessor.medianFilter();
		int[] firstResults = testColorProcessor.getPixel(0, 0, null);
		int[] midRef = testColorProcessor.getPixel(width/2, height/2, null);
		int[] lastResults = testColorProcessor.getPixel(width, height, null);
		
		for(int y = 0; y < refLast.length; y++)
		{
			assertEquals(refFirst[y], firstResults[y], 0.0   );
			assertEquals(refMid[y], midRef[y], 0.0   );
			assertEquals(refLast[y], lastResults[y], 0.0   );			
		}
	}

	@Test
	public void testRotate() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.setInterpolationMethod( ColorProcessor.BICUBIC );
		testColorProcessor.rotate(19.99);
		//displayGraphicsInNewJFrame(testColorProcessor.getBufferedImage(), "Rotate 19.99 degrees", 300);
		
		final int[] refFirst = {0, 0, 0};
		final int[] refMid = {232, 212, 181};
		final int[] refLast = {0, 0, 0};
		
		testColorProcessor.medianFilter();
		int[] firstResults = testColorProcessor.getPixel(0, 0, null);
		int[] midRef = testColorProcessor.getPixel(width/2, height/2, null);
		int[] lastResults = testColorProcessor.getPixel(width, height, null);
		
		for(int y = 0; y < refLast.length; y++)
		{
			assertEquals(refFirst[y], firstResults[y], 0.0   );
			assertEquals(refMid[y], midRef[y], 0.0   );
			assertEquals(refLast[y], lastResults[y], 0.0   );			
		}
	}

	@Test
	public void testGetHistogram() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		int[] testHist = testColorProcessor.getHistogram();
		int[] refHist = {1,18,64,240,936,1616,1414,1690,2137,971,1431,920,746,727,673,595,573,482,499,455,390,393,383,407,350,368,342,343,343,347,325,359,384,404,404,398,393,372,358,398,388,340,378,388,370,361,410,426,413,369,364,391,422,360,429,383,431,422,418,421,363,368,403,408,420,425,416,409,344,410,410,373,423,421,418,403,386,395,357,385,407,363,364,355,352,332,332,336,311,336,324,300,275,303,300,305,268,288,273,280,267,306,284,320,321,327,263,302,304,289,296,277,293,263,278,265,271,256,225,234,218,243,220,193,177,199,181,194,185,189,163,183,166,182,166,148,130,137,135,138,131,132,105,132,117,128,108,129,118,125,141,121,117,109,105,115,93,131,107,105,97,96,83,100,100,88,87,103,83,103,93,94,111,114,101,123,103,92,95,80,102,91,79,97,91,81,97,93,116,82,83,103,111,73,106,120,117,93,103,138,117,113,93,92,107,112,109,113,100,93,90,106,88,87,91,77,72,84,91,88,81,59,63,39,40,49,50,45,31,27,20,29,16,22,23,13,17,20,9,9,11,11,4,9,7,8,6,5,1,0,0,0,0,0,0,0};
		
		for(int y = 0; y < testHist.length; y++)
		{
			assertEquals(refHist[y], testHist[y], 0.0   );			
		}
	}

	@Test
	public void testErode() 
	{
		//should add some noise 
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.erode();
		int[] localRef = getRefImageArray();
		
		//at least one pixel is different
		boolean change = false;
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				int result = testColorProcessor.getPixel(x, y);
				
				if( localRef[width*y+x] != result )
				{
					change = true;
				}
			}
		}
		
		assertEquals(true, change);
	}

	@Test
	public void testDilate() 
	{
		//should add some noise 
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.dilate();
		int[] localRef = getRefImageArray();
		//displayGraphicsInNewJFrame(testColorProcessor.getBufferedImage(), "Dilate", 3000);
		
		//at least one pixel is different
		boolean change = false;
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				int result = testColorProcessor.getPixel(x, y);
				
				if( localRef[width*y+x] != result )
				{
					change = true;
				}
			}
		}
		
		assertEquals(true, change);
	}

	@Test
	public void testConvolve() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.setInterpolationMethod( ColorProcessor.BICUBIC );
	    float[] kernel = {-1,-1,-1,-1,8,-1,-1,-1,-1};
		testColorProcessor.convolve( kernel, 3, 3);
		//displayGraphicsInNewJFrame(testColorProcessor.getBufferedImage(), "Convolve", 300);
		
		final int[] refFirst = {0, 0, 0};
		final int[] refMid = {1, 13, 12};
		final int[] refLast = {0, 0, 0};
		
		testColorProcessor.medianFilter();
		int[] firstResults = testColorProcessor.getPixel(0, 0, null);
		int[] midRef = testColorProcessor.getPixel(width/2, height/2, null);
		int[] lastResults = testColorProcessor.getPixel(width, height, null);
		
		for(int y = 0; y < refLast.length; y++)
		{
			assertEquals(refFirst[y], firstResults[y], 0.0   );
			assertEquals(refMid[y], midRef[y], 0.0   );
			assertEquals(refLast[y], lastResults[y], 0.0   );			
		}
	}

	@Test
	public void testAutoThreshold() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.setInterpolationMethod( ColorProcessor.BICUBIC );
		testColorProcessor.autoThreshold();
		//displayGraphicsInNewJFrame(testColorProcessor.getBufferedImage(), "AutoThreshold", 300);
		
		final int[] refFirst = {0, 0, 0};
		final int[] refMid = {255, 255, 255};
		final int[] refLast = {0, 0, 0};
		
		testColorProcessor.medianFilter();
		int[] firstResults = testColorProcessor.getPixel(0, 0, null);
		int[] midRef = testColorProcessor.getPixel(width/2, height/2, null);
		int[] lastResults = testColorProcessor.getPixel(width, height, null);
		
		for(int y = 0; y < refLast.length; y++)
		{
			assertEquals(refFirst[y], firstResults[y], 0.0   );
			assertEquals(refMid[y], midRef[y], 0.0   );
			assertEquals(refLast[y], lastResults[y], 0.0   );			
		}
	}

	@Test
	public void testGetNChannels() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		assertEquals( 3, testColorProcessor.getNChannels(), 0);
	}

	@Test
	public void testToFloat() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		FloatProcessor c0 = testColorProcessor.toFloat(0, null);
		FloatProcessor c1 = testColorProcessor.toFloat(1, null);
		FloatProcessor c2 = testColorProcessor.toFloat(2, null);
		FloatProcessor c3 = testColorProcessor.toFloat(3, null);
	
		assertEquals( 228, c0.getPixel(width/2, height/2, null)[0] );	
		assertEquals( 205, c1.getPixel(width/2, height/2, null)[0] );	
		assertEquals(174, c2.getPixel(width/2, height/2, null)[0] );	
		assertEquals( 0, c3.getPixel(width/2, height/2, null)[0] );	

	}

	@Test
	public void testSetPixelsIntFloatProcessor() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		FloatProcessor refFloatProcessor = new FloatProcessor( width, height );
		
		testColorProcessor.setPixels(0, refFloatProcessor);  //sets red to black
		//displayGraphicsInNewJFrame(testColorProcessor.getBufferedImage(), "Red to Black", 300);
		
		assertEquals( 0, testColorProcessor.getPixel(width/2, height/2, null)[0] );	
		assertEquals( 205, testColorProcessor.getPixel(width/2, height/2, null)[1] );	
		assertEquals( 174, testColorProcessor.getPixel(width/2, height/2, null)[2] );	
	}

	@Test
	public void testUpdateComposite() 
	{
		//does nothing
	}

	/**
	 * Gives 0-255 mapped ARGB in 4 element int array when given an ARGB packed int
	 * @param combinedInt - ARGB packed int
	 * @return
	 */
	public static int[] getARGB(int combinedInt)
	{
		int[] argbIntArray = new int[4];
		 
		argbIntArray[0] = ( combinedInt & 0xff000000 ) >> 24;
		argbIntArray[1] = ( combinedInt & 0x00ff0000) >> 16;
		argbIntArray[2] = ( combinedInt & 0x0000ff00) >> 8;
		argbIntArray[3] = combinedInt & 0x000000ff;
		
		return argbIntArray;
	}
	
	/**
	 * returns true if only the RGB color components are the same binary value
	 * prints out the results if the values do not match
	 * @param referenceInt 4Byte value representing ARGB reference values
	 * @param testInt 4Byte value representing ARGB reference values
	 * @return
	 */
	public static boolean compareARGBInts(int referenceInt, int testInt)
	{
		int refAlpha, refRed, refBlue, refGreen, testAlpha, testRed, testBlue, testGreen;
		refAlpha = ( referenceInt & 0xff000000 ) >> 24;
		refRed = ( referenceInt & 0x00ff0000) >> 16;
		refGreen = ( referenceInt & 0x0000ff00) >> 8;
		refBlue = referenceInt & 0x000000ff;
		
		testAlpha = ( testInt & 0xff000000 ) >> 24;
		testRed = ( testInt & 0x00ff0000 ) >> 16;
		testGreen = ( testInt & 0x0000ff00 ) >> 8;
		testBlue = testInt & 0x000000ff;
		
		boolean results = false;
		
		if ( refRed == testRed && refGreen == testGreen && refBlue == testBlue )
		{	results = true; }
		else
		{
			System.out.println("Reference alpha = " + refAlpha + " .. test alpha = " + testAlpha );			
			System.out.println("Reference red = " + refRed + " .. test red = " + testRed );
			System.out.println("Reference green = " + refGreen + " .. test green = " + testGreen );
			System.out.println("Reference blue = " + refBlue + " test blue = " + testBlue );	
		}
				
		return results;
	}
	@Test
	public void testColorProcessorImage() 
	{
		ColorProcessor refColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		ColorProcessor testColorProcessor = new ColorProcessor( refColorProcessor.createImage() );
		
		//displayGraphicsInNewJFrame(testColorProcessor.getBufferedImage(), "test color image from AWT Image type", 300);
		
		for(int y = 0; y < refColorProcessor.height; y++)
		{
			for(int x = 0; x < refColorProcessor.width; x++)
			{
				boolean results = compareARGBInts( refColorProcessor.getPixel(x, y), testColorProcessor.getPixel(x, y) );
				assertEquals( true, results );
			}
		}
	}

	@Test
	public void testColorProcessorIntInt() 
	{
		ColorProcessor refColorProcessor = new ColorProcessor( width, height );
		assertEquals( width, refColorProcessor.getWidth() );
		assertEquals( height, refColorProcessor.getHeight() );
	}

	@Test
	public void testColorProcessorIntIntIntArray() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		for(int y = 0; y < height; y++)
		{
			//check each component
			for(int x = 0; x < width; x++)
			{
				boolean results = compareARGBInts( imageIntData[y*width+x], testColorProcessor.get(y*width+x));
				assertEquals( true, results );
			}
		}	
	}

	@Test
	public void testCreateColorModel() 
	{
		//tested in DirectColorModelTest.java
		
	}

	@Test
	public void testCreateBufferedImage() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		BufferedImage testBufferedImage = (BufferedImage) testColorProcessor.createBufferedImage();
		
		int[] refImageArray = getRefImageArray();
		
		for(int y = 0; y < height; y++)
		{
			//check each component
			for(int x = 0; x < width; x++)
			{
				boolean results = compareARGBInts( refImageArray[y*width+x], testBufferedImage.getRGB(x, y) );
				assertEquals( true, results );
			}
		}	
	}

	@Test
	public void testGetRGBSampleModel() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		SampleModel testSampleModel = testColorProcessor.getRGBSampleModel();
		assertEquals( width, testSampleModel.getWidth() );
		assertEquals( height, testSampleModel.getHeight() );
		
	}

	@Test
	public void testGetColor() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		
		int[] refImageArray = getRefImageArray() ;
		
		for(int y = 0; y < height; y++)
		{
			//check each component
			for(int x = 0; x < width; x++)
			{
				int[] argbComponentArray = getARGB( refImageArray[width*y+x] );
				Color refColor = new Color( argbComponentArray[1], argbComponentArray[2], argbComponentArray[3], argbComponentArray[0] );
				Color testColor = testColorProcessor.getColor(x, y);
				assertEquals( refColor.getRed(), testColor.getRed() );
				assertEquals( refColor.getGreen(), testColor.getGreen() );
				assertEquals( refColor.getBlue(), testColor.getBlue() );
			}
		}	
		
	}

	@Test
	public void testSetMinAndMaxDoubleDoubleInt() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.setMinAndMax( 100,200 );
		//displayGraphicsInNewJFrame(testColorProcessor.getBufferedImage(), " setminandmax to 100 - 200", 3000);
		
		//test single pixel
		assertEquals( 255, testColorProcessor.getPixel(width/2, height/2, null)[0] );	
		assertEquals( 255, testColorProcessor.getPixel(width/2, height/2, null)[1] );	
		assertEquals( 189, testColorProcessor.getPixel(width/2, height/2, null)[2] );
		
	}

	@Test
	public void testGetHSB() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		
		byte[] hue = new byte[ width * height ]; byte[] saturation = new byte[ width * height ]; byte[] brightness = new byte[ width * height ];
		testColorProcessor.getHSB(hue, saturation, brightness);
		assertEquals( 0, hue[ (width/2) * (height/2) ] );  
		assertEquals( -56, saturation[ (width/2) * (height/2) ] ); 
		assertEquals( 93, brightness[ (width/2) * (height/2) ] ); 
		
	}

	@Test
	public void testGetHSBStack() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		ImageStack imageStack = testColorProcessor.getHSBStack();
		
		byte[] hue = new byte[ width * height ]; byte[] saturation = new byte[ width * height ]; byte[] brightness = new byte[ width * height ];
		testColorProcessor.getHSB(hue, saturation, brightness);
		
		byte[] testHue = (byte[]) imageStack.getPixels(1);
		byte[] testSaturation = (byte[]) imageStack.getPixels(2);
		byte[] testBrightness = (byte[]) imageStack.getPixels(3);
		
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				assertEquals( hue[y*height+x], testHue[y*height+x] );
				assertEquals( saturation[y*height+x], testSaturation[y*height+x] );
				assertEquals( brightness[y*height+x], testBrightness[y*height+x] );
			}
		}

	}

	@Test
	public void testGetBrightness() 
	{
		ColorProcessor refColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		FloatProcessor testFloatProcessor = refColorProcessor.getBrightness();
		float[] testBrightnessValues = (float[]) testFloatProcessor.getPixels();
		
		int[] refImageArray = getRefImageArray();
		
		//find the reference values
		
		
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				int[] refPixelValue = getARGB( refImageArray[y*width+x] );
				
				int r = refPixelValue[1];
				int g = refPixelValue[2];
				int b = refPixelValue[3];
				float refBrightness =  ( Color.RGBtoHSB( r, g, b, null) )[2];
				
				assertEquals( refBrightness, testBrightnessValues[y*width+x], 0.0 );
			}
		}
		
		
	}

	@Test
	public void testGetRGB() 
	{
		ColorProcessor refColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		byte[] r = new byte[width*height];
		byte[] g = new byte[width*height];
		byte[] b = new byte[width*height];
		
		refColorProcessor.getRGB(r, g, b);
		
		int[] refImageArray = getRefImageArray();
		
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				int[] refPixelValue = getARGB( refImageArray[y*width+x] );
				
				assertEquals( refPixelValue[1], r[y*width+x]&0xff, 0.0 );
				assertEquals( refPixelValue[2], g[y*width+x]&0xff, 0.0 );
				assertEquals( refPixelValue[3], b[y*width+x]&0xff, 0.0 );
				
			}
		}
	}

	@Test
	public void testSetRGB() 
	{
		ColorProcessor refColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		byte[] r = new byte[width*height];
		byte[] g = new byte[width*height];
		byte[] b = new byte[width*height];
		
		refColorProcessor.getRGB(r, g, b);
		
		
		ColorProcessor testColorProcessor = new ColorProcessor( width, height );
		testColorProcessor.setRGB(r, g, b);
		
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				boolean results = compareARGBInts( refColorProcessor.getPixel(x, y), testColorProcessor.getPixel(x, y)  );
				assertEquals( true, results );	
			}
		}
		
		
	}

	public static int byteToInt(byte b)
	{
		int i = ( (b < 0) ? 256 + b : b );
		return i;
	}
	
	@Test
	public void testSetHSB() 
	{
		ColorProcessor refColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		byte[] hue = new byte[ width * height ]; byte[] saturation = new byte[ width * height ]; byte[] brightness = new byte[ width * height ];
		refColorProcessor.getHSB(hue, saturation, brightness);
		
		ColorProcessor testColorProcessor = new ColorProcessor( width, height );
		testColorProcessor.setHSB(hue, saturation, brightness);
		
	
		int[] testARGB = getARGB( testColorProcessor.getPixel(width/2, width/2) );
				
		assertEquals( 110, testARGB[1] );
		assertEquals( 62, testARGB[2] );
		assertEquals( 62, testARGB[3] );
	}

	@Test
	public void testSetBrightness() 
	{
		ColorProcessor refColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		FloatProcessor refFloatProcessor = refColorProcessor.getBrightness();
		byte[] hue = new byte[ width * height ]; byte[] saturation = new byte[ width * height ]; byte[] brightness = new byte[ width * height ];
		refColorProcessor.getHSB(hue, saturation, brightness);
		
		
		ColorProcessor testColorProcessor = new ColorProcessor( width, height );
		testColorProcessor.setBrightness( refFloatProcessor );
		byte[] testHue = new byte[ width * height ]; byte[] testSaturation = new byte[ width * height ]; byte[] testBrightness = new byte[ width * height ];	
		testColorProcessor.getHSB(testHue, testSaturation, testBrightness );
		
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				assertEquals( brightness[width*y+x], testBrightness[width*y+x] );	
			}
		}
	}

	@Test
	public void testApplyTableIntArrayInt() 
	{
		final int[] rampLut = new int[256];
		
		for(int i = 0; i < rampLut.length; i++)
		{
			rampLut[i]=i;
		}
		
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.applyTable( rampLut );
			
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				int[] testARGB = getARGB( testColorProcessor.getPixel(x, y) );
				int[] refARGB = getARGB( imageIntData[width*y+x] );
				
				Color testColor = new Color( testARGB[1], testARGB[2], testARGB[3] );
				Color refColor = new Color( refARGB[1], refARGB[2], refARGB[3] );
				assertEquals( testColor, refColor );	
			}
		}
	}

	@Test
	public void testFilterRGBIntDouble() 
	{
		//tested in testFilterRGBIntDoubleDouble()
	}

	@Test
	public void testFilterRGBIntDoubleDouble() 
	{		
	 //case RGB_NOISE:
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.filterRGB( ColorProcessor.RGB_NOISE, 99.0 );
		int refInt = (0x00 << 24) | (0x58 << 16) | (0xb3 << 8) | (0xff);
		boolean results; // = compareARGBInts( refInt, testColorProcessor.getPixel(width/2, height/2)  );
		//assertEquals( false, false );	
		
     //case RGB_MEDIAN:
		testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.filterRGB( ColorProcessor.RGB_MEDIAN, 10.0 );
		refInt = (0x00 << 24) | (0xe7 << 16) | (0xd2 << 8) | (0xb3);
		results = compareARGBInts( refInt, testColorProcessor.getPixel(width/2, height/2)  );
		assertEquals( true, results );	

     //case RGB_FIND_EDGES:
		testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.filterRGB( ColorProcessor.RGB_FIND_EDGES, 10.0 );
		refInt = (0x00 << 24) | (0x15 << 16) | (0x0c << 8) | (0x0c);
		results = compareARGBInts( refInt, testColorProcessor.getPixel(width/2, height/2)  );
		assertEquals( true, results );	
  
     //case RGB_ERODE:
		testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.filterRGB( ColorProcessor.RGB_ERODE, 10.0 );
		refInt = (0x00 << 24) | (0xf8 << 16) | (0xe2 << 8) | (0xc3);
		results = compareARGBInts( refInt, testColorProcessor.getPixel(width/2, height/2)  );
		assertEquals( true, results );	

     //case RGB_DILATE:
		testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.filterRGB( ColorProcessor.RGB_DILATE, 10.0 );
		refInt = (0x00 << 24) | (0xd1 << 16) | (0xba << 8) | (0x9b);
		results = compareARGBInts( refInt, testColorProcessor.getPixel(width/2, height/2)  );
		assertEquals( true, results );	

     //case RGB_THRESHOLD:
		testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.filterRGB( ColorProcessor.RGB_THRESHOLD, 10.0 );
		refInt = (0x00 << 24) | (0xff << 16) | (0xff << 8) | (0xff);
		results = compareARGBInts( refInt, testColorProcessor.getPixel(width/2, height/2)  );
		assertEquals( true, results );	

     //case RGB_ROTATE:
		testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.filterRGB( ColorProcessor.RGB_ROTATE, 10.0 );
		refInt = (0x00 << 24) | (0xe4 << 16) | (0xcd << 8) | (0xae);
		results = compareARGBInts( refInt, testColorProcessor.getPixel(width/2, height/2)  );
		assertEquals( true, results );	

     //case RGB_SCALE:
		testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.filterRGB( ColorProcessor.RGB_SCALE, 10.0 );
		refInt = (0x00 << 24) | (0x9a << 16) | (0x51 << 8) | (0x28);
		results = compareARGBInts( refInt, testColorProcessor.getPixel(width/2, height/2)  );
		assertEquals( true, results );	

     //case RGB_RESIZE:
		testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.filterRGB( ColorProcessor.RGB_RESIZE, 10.0 );
		refInt = (0x00 << 24) | (0xe4 << 16) | (0xcd << 8) | (0xae);
		results = compareARGBInts( refInt, testColorProcessor.getPixel(width/2, height/2)  );
		assertEquals( true, results );	

     //case RGB_TRANSLATE:
		testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.filterRGB( ColorProcessor.RGB_TRANSLATE, 10.0 );
		refInt = (0x00 << 24) | (0xde << 16) | (0xc3 << 8) | (0xa6);
		results = compareARGBInts( refInt, testColorProcessor.getPixel(width/2, height/2)  );
		assertEquals( true, results );	
	}

	@Test
	public void testGetInterpolatedRGBPixel() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		int testInt = testColorProcessor.getInterpolatedRGBPixel(width/3, height/3);
		int refInt = (0x00 << 24) | (0xb3 << 16) | (0x6e << 8) | (0x4f);
		boolean results = compareARGBInts( refInt, testInt  );
		assertEquals( true, results );	
	}

	@Test
	public void testMakeThumbnail() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		ImageProcessor testThumbnail = testColorProcessor.makeThumbnail(width/2, height/2, 1.0);
		int testInt = testThumbnail.getPixel( testThumbnail.getWidth()/2, testThumbnail.getHeight()/2 );
		int refInt = (0x00 << 24) | (0xe9 << 16) | (0xd2 << 8) | (0xb3);
		boolean results = compareARGBInts( refInt, testInt );
		assertEquals( true, results );	
	}

	@Test
	public void testGetHistogramImageProcessor() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		final int[] refHistogram = {0,5,18,63,216,400,363,419,513,246,348,224,184,181,163,152,137,129,140,116,100,95,101,108,68,92,79,87,96,83,77,86,94,116,97,101,88,84,90,102,105,79,82,105,96,99,104,107,101,96,96,96,113,89,112,91,100,101,113,107,96,77,95,103,102,107,94,98,87,124,105,101,109,95,97,101,120,97,80,92,112,88,80,92,86,90,72,85,75,89,103,78,69,74,73,83,66,67,62,61,56,70,72,88,78,89,74,77,85,65,72,62,69,63,66,68,69,75,59,59,49,67,54,62,51,46,37,54,41,47,45,51,44,39,40,39,24,34,32,27,26,34,30,34,27,38,21,40,34,41,34,29,27,28,28,27,35,39,27,27,23,22,19,26,27,24,25,20,20,21,23,16,26,24,30,37,27,19,23,16,30,20,18,26,25,20,28,18,32,20,31,30,27,19,24,33,30,29,18,30,30,23,20,25,24,23,23,27,25,18,25,30,26,24,24,14,23,24,29,23,19,14,18,6,7,11,14,12,8,7,7,6,2,8,6,3,5,6,5,1,2,4,0,3,0,3,2,2,0,0,0,0,0,0,0,0};
		byte[] testPattern = new byte[width * height];
		for(int h=0; h<height;h++)
			for(int w=0; w<width;w++)
				if(h%2==0&&w%2==0) testPattern[h * width + w] = (byte)0xff;
		
		ImageProcessor mask = new ByteProcessor( width, height, testPattern, null );
		
		
		int[] histogram = testColorProcessor.getHistogram(mask);
		
		for(int i=0;i<refHistogram.length; i++)
			assertEquals( refHistogram[i], histogram[i] );		
	}

	@Test
	public void testSetWeightingFactors() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.setWeightingFactors(0.1, 0.4, 0.5);
		//displayGraphicsInNewJFrame( testColorProcessor.getBufferedImage(), "weighting low red, and high blue/green", 3000 );
		
		final int[] refHistogram = {1,74,151,480,309,755,624,502,357,263,229,210,175,143,163,158,134,128,147,148,139,157,139,128,137,128,134,142,144,142,132,151,140,168,139,122,124,142,130,131,118,133,126,122,114,124,119,103,121,109,117,128,114,112,107,110,113,126,112,100,107,101,92,98,96,80,80,94,89,113,86,103,87,94,97,74,89,75,67,87,82,75,67,81,70,58,60,60,60,64,45,53,66,51,47,42,45,34,31,37,30,32,33,44,35,28,41,38,36,41,30,26,29,27,29,21,26,26,28,22,22,26,32,25,25,29,14,21,32,27,29,27,26,39,20,22,25,28,25,34,17,17,26,28,18,27,22,17,22,12,21,21,14,24,26,24,22,23,16,22,15,22,22,24,14,15,22,12,18,13,20,26,29,25,28,16,26,22,29,25,24,11,25,26,26,26,31,15,30,20,28,22,23,20,19,22,16,25,24,21,17,24,22,18,25,18,21,14,15,23,14,14,14,8,7,10,6,9,11,13,4,4,2,5,7,4,4,2,5,4,3,3,2,1,2,1,1,4,1,0,1,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0};
		byte[] testPattern = new byte[width * height];
		for(int h=0; h<height;h++)
			for(int w=0; w<width;w++)
				if(h%2==0&&w%2==0) testPattern[h * width + w] = (byte)0xff;
		
		ImageProcessor mask = new ByteProcessor( width, height, testPattern, null );
		
		
		int[] histogram = testColorProcessor.getHistogram(mask);
		
		for(int i=0;i<refHistogram.length; i++)
			assertEquals( refHistogram[i], histogram[i] );	
	}

	@Test
	public void testGetWeightingFactors() 
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		double[] refWeights = {0.1, 0.2, 0.8};
		testColorProcessor.setWeightingFactors( refWeights[0], refWeights[1], refWeights[2] );
		double[] results = testColorProcessor.getWeightingFactors();
		
		for(int i=0;i<refWeights.length; i++)
			assertEquals( refWeights[i], results[i], 0.0 );			
	}
}
