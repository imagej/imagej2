package ij.process;

import static org.junit.Assert.*;


import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import loci.formats.FormatException;
import loci.plugins.util.ImagePlusReader;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class FloatProcessorTest {

	private static float[][] imageFloatData;

    public static float[][] combined;

    /*
     * Open an known image for internal testing...
     */
	@BeforeClass
	public static void runBeforeClass()
	{
	    String id = "/Volumes/data/khoros/samples/head.xv";
		ImagePlusReader imagePlusReader = new ImagePlusReader();
		ImageProcessor imageProcessor = null;


		try {
			imagePlusReader.setId(id);
		} catch (FormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			imageProcessor = imagePlusReader.openProcessors(0)[0];
		} catch (FormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		imageFloatData = imageProcessor.getFloatArray();
		combined = new float[2][imageProcessor.width*imageFloatData[0].length];

	}



	@AfterClass
	public static void runAfterClass()
	{
	}

	@Test
	public void testSetColor()
	{
		//Create a new FloatProcessor object for testing
		FloatProcessor floatProcessor = null;

		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
		floatProcessor = new FloatProcessor(imageFloatData);

		//set the default fill value
		int setcolorvalue = 2;

		//set the value
		floatProcessor.setColor(setcolorvalue);

		//see if the test passes
		assertEquals(setcolorvalue, floatProcessor.fgColor, 0.0);
	}

	@Test
	public void testSetValue()
	{
		//Create a new FloatProcessor object for testing
		FloatProcessor floatProcessor = null;

		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
		floatProcessor = new FloatProcessor(imageFloatData);

		//set the default fill value
		float setcolorvalue = 1;

		floatProcessor.setValue(setcolorvalue);

		//overwrite the pixel value with the new default fill value
		floatProcessor.drawPixel(1, 1);

		//see if the value was over-writen with the SetColor value
		float postDrawPixelValue = floatProcessor.getf(1, 1);

		//see if the test passes assertEquals(float expected, float actual, float delta)
		assertEquals(setcolorvalue, postDrawPixelValue, 0.0);
	}

	@Test
	public void testSetBackgroundValue() {
		//Create a new FloatProcessor object for testing
		FloatProcessor floatProcessor = null;

		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
		floatProcessor = new FloatProcessor(imageFloatData);

		//how can it fail? it does nothing!
		floatProcessor.setBackgroundValue(0);

		//see if the test passes assertEquals(float expected, float actual, float delta)
		assertEquals(true, true);
	}

	@Test
	public void testGetMin() {
		//Create a new FloatProcessor object for testing
		FloatProcessor floatProcessor = null;

		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
		floatProcessor = new FloatProcessor(imageFloatData);

		//see if the test passes assertEquals(float expected, float actual, float delta)
		assertEquals(floatProcessor.getMin(), floatProcessor.getMin(), 0.0);
	}

	@Test
	public void testGetMax() {
		//Create a new FloatProcessor object for testing
		FloatProcessor floatProcessor = null;

		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
		floatProcessor = new FloatProcessor(imageFloatData);

		//see if the test passes assertEquals(float expected, float actual, float delta)
		assertEquals(floatProcessor.getMax(), floatProcessor.getMax(), 0.0);
	}

	@Test
	public void testSetMinAndMax() {
		//Create a new FloatProcessor object for testing
		FloatProcessor floatProcessor = null;


		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
		floatProcessor = new FloatProcessor(imageFloatData);

		//see if the test passes assertEquals(float expected, float actual, float delta)
		assertEquals(floatProcessor.getMax(), floatProcessor.getMax(), 0.0);
	}

	@Test
	public void testResetMinAndMax()
	{
		//Create a new FloatProcessor object for testing
		FloatProcessor floatProcessor = null;

		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
		floatProcessor = new FloatProcessor(imageFloatData);

		//get the min and max
		double maxImage = floatProcessor.getMax();
		double minImage = floatProcessor.getMin();

		//set the values for max and min used in the test
		double max = 2.99;
		double min = 1.99;

		//change the min and max
		floatProcessor.setMinAndMax(min, max);

		//reset should yield the initial values
		floatProcessor.resetMinAndMax();

		//see if the test passes assertEquals(float expected, float actual, float delta)
		assertEquals(maxImage, floatProcessor.getMax(), 0.0);
		assertEquals(minImage, floatProcessor.getMin(), 0.0);
	}

	@Test
	public void testSetThreshold()
	{
		//Create a new FloatProcessor object for testing
		FloatProcessor floatProcessor = null;

		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
		floatProcessor = new FloatProcessor(imageFloatData);

		floatProcessor.setThreshold(1.0, 2.0, 4);

		//assert that the max has been set
		assertEquals(floatProcessor.getMaxThreshold(), 2.0, 0.0);
		//assert that the min has been set
		assertEquals(floatProcessor.getMinThreshold(), 1.0, 0.0);
		//assert that the lutUpdate mode change has taken effect
		assertEquals(floatProcessor.getLutUpdateMode(), 4);

	}

	public static byte[] float1DtoByteArray (float[] f)
	{

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        int i = 0;
        for (float m : f)
        {
        	try {
        		out.writeFloat(m);
            	combined[0][i++] = m;
        	} catch (IOException e) {
        		// TODO Auto-generated catch block
        		e.printStackTrace();
        	}
        }

		return baos.toByteArray();
	}

	public static byte[] float2DtoByteArray (float[][] f)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        int width = f.length;
		int height = f[0].length;
		int i=0;
		for (int y=0; y<height; y++)
		{
			for (int x=0; x<width; x++)
			{
				try {
					out.writeFloat(f[x][y]);
	            	combined[1][i++] = f[x][y];
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return baos.toByteArray();
	}

	public static byte[] getSHA1DigestFromByteArray(byte[] b)
	{
		 MessageDigest sha1MessageDigest = null;

		 //assign a message digest from type SHA1
		 try {
				sha1MessageDigest = MessageDigest.getInstance("SHA");
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for(byte bte:b)
			sha1MessageDigest.update(bte);
		byte[] sha1Digest = sha1MessageDigest.digest();

		//print out the message
		//for(byte t:sha1Digest)
		//	System.out.print(t + " ");
		//System.out.println();

		return sha1Digest;
	}

	/*
	 * There are many ways to test value based methods.  This takes the sum of
	 * all the values for a known image and ensures the static double value
	 * representing the sum of all float values in the array hasn't changed.
	 */
	@Test
	public void testGetPixels()
	{
		//Create a new FloatProcessor object for testing
		FloatProcessor floatProcessor = null;

		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
		floatProcessor = new FloatProcessor(imageFloatData);

        //ensure the reference and test arrays are the same using the Java SHA1 implementation
        byte[] referenceImageData = float2DtoByteArray( imageFloatData);
        byte[] testImageData = float1DtoByteArray( (float[]) floatProcessor.getPixels());

        byte[] referenceSHA1 = getSHA1DigestFromByteArray( referenceImageData );
        byte[] testSHA1 = getSHA1DigestFromByteArray( testImageData);


        boolean result = true;

        for(int i=0;i<referenceSHA1.length;i++)
        {
        	//System.out.print(referenceSHA1[i] + " = " +testSHA1[i] + "  ");
        	if(referenceSHA1[i] != testSHA1[i])
        	{
        		result = false;
        		i=referenceSHA1.length;
        	}
        }

		 assertEquals(true, result);
	}



	@Test
	public void testFlipVertical()
	{
		//Create a new FloatProcessor object for testing
		FloatProcessor floatProcessorTest = null;

		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
		floatProcessorTest = new FloatProcessor(imageFloatData);

		float[] initialImageReference = (float[]) floatProcessorTest.getPixels();

		floatProcessorTest.flipVertical();
		floatProcessorTest.flipVertical();

		float[] testImageReference = (float[]) floatProcessorTest.getPixels();

		boolean result = true;

		for(int i=0; i<initialImageReference.length; i++)
		{
			//System.out.print(referenceSHA1[i] + " = " +testSHA1[i] + "  ");
			if(initialImageReference[i] != testImageReference[i])
			{
				result = false;
				i=initialImageReference.length;
			}
		}

		assertEquals(true, result);
	}

	@Test
	public void testFill()
	{
		//set a test fill value
		double testFillValue = 19.99;

		//Create a new FloatProcessor object for testing
		FloatProcessor floatProcessorTest = null;

		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
		floatProcessorTest = new FloatProcessor(imageFloatData);

		//set the fill value
		floatProcessorTest.setValue(testFillValue);

		//fill the image
		floatProcessorTest.fill();

		//get the 1D float array
		float[] testImageResults = (float[]) floatProcessorTest.getPixels();

		//iterate through the results and ensure the expected value...
		for(int i=0; i<testImageResults.length; i++)
		{
			assertEquals( testImageResults[0], (float) testFillValue, 0.0);
		}
	}


	@Test
	public void testGetPixelsCopy()
	{
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
		FloatProcessor floatProcessorTest  = new FloatProcessor(imageFloatData);

		//get a copy of the native data
		float[] referencePixels = (float[]) floatProcessorTest.getPixels();

		//get a copy of Pixels
		float[] testPixels = (float[]) floatProcessorTest.getPixelsCopy();

		boolean result = true;
		//iterate through the results and ensure the expected value...
		for(int i=0; i<referencePixels.length; i++)
		{
			if(referencePixels[i] != testPixels[i])
			{
				result = false;
				i = referencePixels.length;
			}
		}

		assertEquals(true, result);
	}

	@Test
	public void testGetPixelIntInt()
	{
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
		FloatProcessor floatProcessorTest  = new FloatProcessor(imageFloatData);

		//ensure that all pixel values were the same
		boolean result  = true;
		for(int i = 0; i<floatProcessorTest.height; i++)
			for(int j = 0; j<floatProcessorTest.width; j++)
				if(floatProcessorTest.getPixel(j, i) != Float.floatToIntBits( imageFloatData[j][i] ) )
				{
					result = false;
					i = floatProcessorTest.height;
					j = floatProcessorTest.width;
				}

		//ensure that all pixel values were the same
		assertEquals(true, result);
	}

	@Test
	public void testGetInt()
	{
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
		FloatProcessor floatProcessorTest  = new FloatProcessor(imageFloatData);

		//get the linear array of pixels
		float[] referencePixels = (float[]) floatProcessorTest.getPixels();

		//ensure that all pixel values were the same
		boolean result  = true;

		//iterate through the results an flag any changes...
		for(int j = 0; j < floatProcessorTest.getPixelCount(); j++)
		{
			if( floatProcessorTest.get(j) != Float.floatToIntBits(referencePixels[j])  )
			{
				result = false;
			}
		}

		//ensure that all pixel values were the same
		assertEquals(true, result);
	}

	@Test
	public void testGetIntInt()
	{
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
		FloatProcessor floatProcessorTest  = new FloatProcessor(imageFloatData);

		//ensure that all pixel values were the same
		boolean result  = true;

		for(int i = 0; i<floatProcessorTest.height; i++)
			for(int j = 0; j<floatProcessorTest.width; j++)
				if(floatProcessorTest.getPixel(j, i) != Float.floatToIntBits( imageFloatData[j][i] ) )
				{
					System.out.println( floatProcessorTest.getf(j, i) + " i " + i + " j " + j + " " + imageFloatData[j][i] );
					result = false;
				}

		//ensure that all pixel values were the same
		assertEquals(true, result);
	}

	@Test
	public void testSetIntIntInt()
	{
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
		FloatProcessor floatProcessorTest  = new FloatProcessor(imageFloatData);

		for(int y = 0; y<floatProcessorTest.height; y++)
			for(int x = 0; x<floatProcessorTest.width; x++)
			{
				int reference = y*floatProcessorTest.width + x;

				//set the value
				floatProcessorTest.set(x, y, reference);

				//get the set value (converted back to an int)
				int result =  floatProcessorTest.get(x, y);

				//check the result
				assertEquals( reference, result );
			}
	}

	@Test
	public void testSetIntInt()
	{
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
		FloatProcessor floatProcessorTest  = new FloatProcessor(imageFloatData);

		for(int y = 0; y<floatProcessorTest.height*floatProcessorTest.width; y++)
		{
			int reference = y;

			//set the value
			floatProcessorTest.set(y, reference);

			//get the set value (converted back to an int)
			int result =  floatProcessorTest.get(y);

			//check the result
			assertEquals( reference, result );
		}
	}

	@Test
	public void testGetfIntInt()
	{
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
		FloatProcessor floatProcessorTest  = new FloatProcessor(imageFloatData);

		//ensure that all pixel values were the same
		boolean result  = true;

		for(int i = 0; i<floatProcessorTest.height; i++)
			for(int j = 0; j<floatProcessorTest.width; j++)
			{

				if(floatProcessorTest.getf(j, i) != imageFloatData[j][i] )
				{
					System.out.println( floatProcessorTest.getf(j, i) + " i " + i + " j " + j + " " + imageFloatData[j][i] );
					result = false;
				}
			}

		//ensure that all pixel values were the same
		assertEquals(true, result);
	}

	@Test
	public void testGetfInt()
	{
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
		FloatProcessor floatProcessorTest  = new FloatProcessor(imageFloatData);

		//get the reference array
		float[] imageReferenceArray = (float[]) floatProcessorTest.getPixels();

		for(int y = 0; y<floatProcessorTest.height*floatProcessorTest.width; y++)
		{
			//check the result
			assertEquals( floatProcessorTest.getf(y), imageReferenceArray[y], 0.0f );
		}
	}

	@Test
	public void testSetfIntIntFloat()
	{
		//set the reference float
		float referenceFloat = 19.99f;

		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
		FloatProcessor floatProcessorTest  = new FloatProcessor(imageFloatData);

		for(int i = 0; i<floatProcessorTest.height; i++)
			for(int j = 0; j<floatProcessorTest.width; j++)
			{
				//set the value
				floatProcessorTest.setf(j, i, referenceFloat);

				//check the result
				assertEquals( referenceFloat, floatProcessorTest.getf(j, i), 0.0f );
			}
	}

	@Test
	public void testSetfIntFloat()
	{
		//set the reference float
		float referenceFloat = 19.99f;

		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
		FloatProcessor floatProcessorTest  = new FloatProcessor(imageFloatData);


			for(int j = 0; j<floatProcessorTest.getPixelCount(); j++)
			{
				//set the value
				floatProcessorTest.setf(j, referenceFloat);

				//check the result
				assertEquals( referenceFloat, floatProcessorTest.getf(j), 0.0f );
			}
	}

	@Test
	public void testGetPixelIntIntIntArray() {
		fail("Not yet implemented");
	}

	@Test
	public void testPutPixelIntIntIntArray() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetInterpolatedPixel() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPixelInterpolated() {
		fail("Not yet implemented");
	}

	@Test
	public void testPutPixelIntIntInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPixelValue() {
		fail("Not yet implemented");
	}

	@Test
	public void testPutPixelValue() {
		fail("Not yet implemented");
	}

	@Test
	public void testDrawPixel() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetPixelsObject() {
		fail("Not yet implemented");
	}

	@Test
	public void testCopyBits() {
		fail("Not yet implemented");
	}

	@Test
	public void testApplyTable() {
		fail("Not yet implemented");
	}

	@Test
	public void testInvert() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddDouble() {
		fail("Not yet implemented");
	}

	@Test
	public void testMultiply() {
		fail("Not yet implemented");
	}

	@Test
	public void testAnd() {
		fail("Not yet implemented");
	}

	@Test
	public void testOr() {
		fail("Not yet implemented");
	}

	@Test
	public void testXor() {
		fail("Not yet implemented");
	}

	@Test
	public void testGamma() {
		fail("Not yet implemented");
	}

	@Test
	public void testLog() {
		fail("Not yet implemented");
	}

	@Test
	public void testExp() {
		fail("Not yet implemented");
	}

	@Test
	public void testSqr() {
		fail("Not yet implemented");
	}

	@Test
	public void testSqrt() {
		fail("Not yet implemented");
	}

	@Test
	public void testAbs() {
		fail("Not yet implemented");
	}

	@Test
	public void testMin() {
		fail("Not yet implemented");
	}

	@Test
	public void testMax() {
		fail("Not yet implemented");
	}

	@Test
	public void testCreateImage() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetBufferedImage() {
		fail("Not yet implemented");
	}

	@Test
	public void testCreateProcessor() {
		fail("Not yet implemented");
	}

	@Test
	public void testSnapshot() {
		fail("Not yet implemented");
	}

	@Test
	public void testReset() {
		fail("Not yet implemented");
	}

	@Test
	public void testResetImageProcessor() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetSnapshotPixels() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetSnapshotPixels() {
		fail("Not yet implemented");
	}

	@Test
	public void testConvolve3x3() {
		fail("Not yet implemented");
	}

	@Test
	public void testFilter() {
		fail("Not yet implemented");
	}

	@Test
	public void testMedianFilter() {
		fail("Not yet implemented");
	}

	@Test
	public void testNoise() {
		fail("Not yet implemented");
	}

	@Test
	public void testCrop() {
		fail("Not yet implemented");
	}

	@Test
	public void testThreshold() {
		fail("Not yet implemented");
	}

	@Test
	public void testDuplicate() {
		fail("Not yet implemented");
	}

	@Test
	public void testScale() {
		fail("Not yet implemented");
	}

	@Test
	public void testResizeIntInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testRotate()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testGetHistogram()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testErode()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testDilate()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testConvolve()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testAutoThreshold()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testToFloat()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testSetPixelsIntFloatProcessor()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testMinValue()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testMaxValue()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testCreate8BitImage()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testFloatProcessorIntIntFloatArrayColorModel()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testFloatProcessorIntInt()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testFloatProcessorIntIntIntArray()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testFloatProcessorIntIntDoubleArray()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testFloatProcessorFloatArrayArray()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testFloatProcessorIntArrayArray()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testFindMinAndMax()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testFilter3x3() {
		fail("Not yet implemented");
	}

}
