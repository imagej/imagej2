package ij.process;

import loci.formats.FormatException;
import loci.plugins.util.ImagePlusReader;
import org.junit.BeforeClass;
import org.junit.Test;

import java.awt.*;
import java.awt.image.*;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ByteProcessorTest {


    private static int width;
    private static int height;
    private static byte[] imageByteData;
    private static ColorModel cm;

    /*
     * Open an known image for internal testing...
     */
	@BeforeClass
	public static void runBeforeClass()
	{
	    String id = "/Volumes/data/khoros/samples/head8bit.tif";

		ImagePlusReader imagePlusReader = new ImagePlusReader();
		ImageProcessor imageProcessor = null;

        try {
            imagePlusReader.setId(id);
        } catch (FormatException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            imageProcessor = imagePlusReader.openProcessors(0)[0];
        } catch (FormatException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        width = imageProcessor.getWidth();
        height = imageProcessor.getHeight();
        imageByteData = new byte[width*height];
        try {
            imagePlusReader.openBytes( 0, imageByteData );
        } catch (FormatException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        //assign the color model
        cm = imageProcessor.getColorModel();
    }

	@Test
	public void testSetColorColor()
    {
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height,imageByteData, cm);

		//set the default fill value
		int setcolorvalue = 2;

		//set the value
		byteProcessor.setColor(setcolorvalue);

		//see if the test passes
		assertEquals(setcolorvalue, byteProcessor.fgColor, 0.0);
	}

	@Test
	public void testSetValue()
    {
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height,imageByteData, cm);

        //set the default fill value
		float setcolorvalue = 1;

		byteProcessor.setValue(setcolorvalue);

		//overwrite the pixel value with the new default fill value
		byteProcessor.drawPixel(1, 1);

		//see if the value was over-writen with the SetColor value
		float postDrawPixelValue = byteProcessor.getf(1, 1);

		assertEquals(setcolorvalue, postDrawPixelValue, 0.0);
	}

	@Test
	public void testSetBackgroundValue()
    {
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height,imageByteData, cm);

        //reference
        double refBackground = 0x00000000;

        //set the background
		byteProcessor.setBackgroundValue(refBackground);


        //get the value
        double returnedReferenceValue = byteProcessor.getBackgroundValue();

		//see if the test passes assertEquals(float expected, float actual, float delta)
		assertEquals(refBackground, returnedReferenceValue, 0.0);
	}

	@Test
	public void testGetMin()
    {
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height,imageByteData, cm);

        int min = Integer.MAX_VALUE;

        for (byte b : imageByteData)
               if(b < min) min = b;

        //correct for +-128 bias
        min+=127;

		//see if the test passes assertEquals(float expected, float actual, float delta)
		assertEquals(min, byteProcessor.getMin(), 0.0);
	}

	@Test
	public void testGetMax()
    {
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height,imageByteData, cm);

        int max = Integer.MIN_VALUE;

        for (byte b : imageByteData)
               if(b > max) max = b;

        //correct for +-128 bias
        max+=128;

		//see if the test passes assertEquals(float expected, float actual, float delta)
		assertEquals(max, byteProcessor.getMax(), 0.0);
	}

	@Test
	public void testSetMinAndMax()
    {
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height,imageByteData, cm);

        //set the values for max and min used in the test
	    double max = 2.0;
		double min = 1.0;

        byteProcessor.setMinAndMax(min, max);

		//see if the test passes assertEquals(float expected, float actual, float delta)
		assertEquals(max, byteProcessor.getMax(), 1.0);
        assertEquals(min, byteProcessor.getMin(), 1.0);

	}

	@Test
	public void testResetMinAndMax()
    {
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height,imageByteData, cm);

        double refMax = byteProcessor.getMax();
        double refMin = byteProcessor.getMin();

        //set the values for max and min used in the test
		double max = 2.0;
		double min = 1.0;

        byteProcessor.setMinAndMax(min, max);

        //reset should yield the initial values
		byteProcessor.resetMinAndMax();

		//see if the test passes assertEquals(float expected, float actual, float delta)
		assertEquals(refMax, byteProcessor.getMax(), 0.0);

	}

	@Test
	public void testFlipVertical()
    {
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height,imageByteData, cm);

		byteProcessor.flipVertical();
		byteProcessor.flipVertical();

		for(int y = 0; y<height; y++)
        {
			for(int x = 0; x<width; x++)
			{
				int reference = y*width + x;

				//get the set value (converted back to an int)
				int result =  byteProcessor.get( reference );
                int refValue;
                if ( imageByteData[reference] < 0)
                {    refValue = imageByteData[reference] + 256; }
                else
                {   refValue = imageByteData[reference]; }

                assertEquals( refValue, result, 0.0);
            }
        }
	}

	@Test
	public void testFillImageProcessor()
    {
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height,imageByteData, cm);

        //reference
        double refBackground = 0x00000000;

        //set the background
		byteProcessor.setBackgroundValue(refBackground);

        //fill the image
        byteProcessor.fill(byteProcessor);

		for(int y = 0; y<height; y++)
        {
			for(int x = 0; x<width; x++)
			{
				int reference = y*width + x;

				//get the set value (converted back to an int)
				double result =  byteProcessor.get( reference );

                assertEquals( refBackground, result, 0.0);
            }
        }
	}

	@Test
	public void testGetPixels()
    {
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, imageByteData, cm);

        byte[] testRef = (byte[]) byteProcessor.getPixels();

        for(int i=0; i<imageByteData.length; i++)
        {
        	assertEquals( imageByteData[i], testRef[i], 0.0 );
        }
	}

	@Test
	public void testGetPixelsCopy()
    {
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, imageByteData, cm);

        byte[] testRef = (byte[]) byteProcessor.getPixelsCopy();

        for(int i=0; i<imageByteData.length; i++)
        {
        	assertEquals( imageByteData[i], testRef[i], 0.0 );
        }

        //test snapshot mode
        byteProcessor.setSnapshotCopyMode(true);
        Rectangle roi = new Rectangle(0, 0, width/2, height/2);

        byteProcessor.setRoi(roi);
        byteProcessor.snapshot();

        testRef = (byte[]) byteProcessor.getPixelsCopy();

        for(int i=0; i<width/2 + height/2; i++)
        {
        	assertEquals( imageByteData[i], testRef[i], 0.0 );
        }
	}

	@Test
	public void testGetPixelIntInt()
    {
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, imageByteData, cm);

		for(int y = 0; y<height; y++)
        {
			for(int x = 0; x<width; x++)
			{
				int reference = y*width + x;

				//get the set value (converted back to an int)
				int result =  byteProcessor.getPixel( x , y );
                int refValue;
                if ( imageByteData[reference] < 0)
                {    refValue = imageByteData[reference] + 256; }
                else
                {   refValue = imageByteData[reference]; }

                assertEquals( refValue, result, 0.0);
            }
        }
    }

	@Test
	public void testGetIntInt()
    {
        //Create a new ByteProcessor object for testing
        ByteProcessor byteProcessor =  new ByteProcessor(width, height, imageByteData, cm);

		for(int y = 0; y<height; y++)
        {
			for(int x = 0; x<width; x++)
			{
				int reference = y*width + x;

				//get the set value (converted back to an int)
				int result =  byteProcessor.get( x , y );
                int refValue;
                if ( imageByteData[reference] < 0)
                {    refValue = imageByteData[reference] + 256; }
                else
                {   refValue = imageByteData[reference]; }

                assertEquals( refValue, result, 0.0);
            }
        }
	}

	@Test
	public void testGetInt()
    {
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, imageByteData, cm);

		for(int y = 0; y<height; y++)
        {
			for(int x = 0; x<width; x++)
			{
				int reference = y*width + x;

				//get the set value (converted back to an int)
				int result =  byteProcessor.get( reference );
                int refValue;
                if ( imageByteData[reference] < 0)
                {    refValue = imageByteData[reference] + 256; }
                else
                {   refValue = imageByteData[reference]; }

                assertEquals( refValue, result, 0.0);
            }
        }
	}

	@Test
	public void testSetIntIntInt()
    {
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, imageByteData, cm);

        //set a reference value
        int refValue = 1;

		for(int y = 0; y<height; y++)
        {
			for(int x = 0; x<width; x++)
			{
				int reference = y*width + x;
                byteProcessor.set(x, y, refValue);

				//get the set value (converted back to an int)
				int result =  byteProcessor.get( reference );

                assertEquals( refValue, result, 0.0);
            }
        }
	}

	@Test
	public void testSetIntInt()
    {
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, imageByteData, cm);

        //set a reference value
        int refValue = 1;

        //set the reference value
        byteProcessor.setValue(refValue);

		for(int y = 0; y<height; y++)
        {
			for(int x = 0; x<width; x++)
			{
				int reference = y*width + x;
                byteProcessor.set(reference, refValue);

				//get the set value (converted back to an int)
				int result =  byteProcessor.get( reference );

                assertEquals( refValue, result, 0.0);
            }
        }
	}

	@Test
	public void testGetfIntInt()
    {
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, imageByteData, cm);

		for(int y = 0; y<height; y++)
        {
			for(int x = 0; x<width; x++)
			{
				int reference = y*width + x;

				//get the set value (converted back to an int)
				float result =  byteProcessor.getf( x, y );
                int refValue;

                if ( imageByteData[reference] < 0)
                {    refValue = imageByteData[reference] + 256; }
                else
                {   refValue = imageByteData[reference]; }

                assertEquals( refValue, result, 0.0);
            }
        }
	}

	@Test
	public void testGetfInt()
    {
        //Create a new ByteProcessor object for testing
        ByteProcessor byteProcessor =  new ByteProcessor(width, height, imageByteData, cm);

        for(int y = 0; y<height; y++)
        {
            for(int x = 0; x<width; x++)
            {
                int reference = y*width + x;

                //get the set value (converted back to an int)
                float result =  byteProcessor.getf( reference );
                int refValue;

                if ( imageByteData[reference] < 0)
                {    refValue = imageByteData[reference] + 256; }
                else
                {   refValue = imageByteData[reference]; }

                assertEquals( refValue, result, 0.0);
            }
        }
	}

	@Test
	public void testSetfIntIntFloat()
    {
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, imageByteData, cm);

        //set a reference value
        float refValue = 2.0f;

		for(int y = 0; y<height; y++)
        {
			for(int x = 0; x<width; x++)
			{
				int reference = y*width + x;
                byteProcessor.setf( x, y, refValue);

				//get the set value (converted back to an int)
				float result =  byteProcessor.getf( reference );

                assertEquals( refValue, result, 0.0);
            }
        }
	}

	@Test
	public void testSetfIntFloat()
    {
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, imageByteData, cm);

        //set a reference value
        float refValue = 2.0f;

		for(int y = 0; y < height; y++)
        {
			for(int x = 0; x < width; x++)
			{
				int reference = y * width + x;
                byteProcessor.setf( reference, refValue);

				//get the set value (converted back to an int)
				float result =  byteProcessor.getf( reference );

                assertEquals( refValue, result, 0.0);
            }
        }
	}

	@Test
	public void testGetInterpolatedPixel()
    {
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, imageByteData, cm);

        byteProcessor.setInterpolationMethod(ImageProcessor.BILINEAR);
		for(int y = 0; y < height-1; y++)
        {
			for(int x = 0; x < width-1; x++)
			{
                //Bilinear interpolation
		        int xbase = x;
		        int ybase = y;

		        double xFraction = x - xbase;
		        double yFraction = y - ybase;

		        int offset = ybase * width + xbase;

		        double lowerLeft = imageByteData[offset];
		        double lowerRight = imageByteData[offset + 1];
		        double upperRight = imageByteData[offset + width + 1];
		        double upperLeft = imageByteData[offset + width];
                double upperAverage = upperLeft + xFraction * (upperRight - upperLeft);
		        double lowerAverage = lowerLeft + xFraction * (lowerRight - lowerLeft);

		        double referenceResult = lowerAverage + yFraction * (upperAverage - lowerAverage);

                //get the pixel value that was set
                double result = byteProcessor.getInterpolatedPixel((double) x, (double) y);

                //check the result
                assertEquals( referenceResult, result, Float.MAX_VALUE );
            }
        }
    }

	@Test
	public void testGetPixelInterpolated()
    {
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, imageByteData, cm);

        byteProcessor.setInterpolationMethod(ImageProcessor.BILINEAR);
		for(int y = 0; y < height-1; y++)
        {
			for(int x = 0; x < width-1; x++)
			{
                //Bilinear interpolation
		        int xbase = x;
		        int ybase = y;

		        double xFraction = x - xbase;
		        double yFraction = y - ybase;

		        int offset = ybase * width + xbase;

		        double lowerLeft = imageByteData[offset];
		        double lowerRight = imageByteData[offset + 1];
		        double upperRight = imageByteData[offset + width + 1];
		        double upperLeft = imageByteData[offset + width];
                double upperAverage = upperLeft + xFraction * (upperRight - upperLeft);
		        double lowerAverage = lowerLeft + xFraction * (lowerRight - lowerLeft);

		        double referenceResult = lowerAverage + yFraction * (upperAverage - lowerAverage);

                //get the pixel value that was set
                double result = byteProcessor.getPixelInterpolated( (double) x, (double) y);

                //check the result
                assertEquals( referenceResult, result, Float.MAX_VALUE );
            }
        }
    }

	@Test
	public void testPutPixelIntIntInt()
    {
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, imageByteData, cm);

        //set a reference value
        int refValue = 2;

		for(int y = 0; y<height; y++)
        {
			for(int x = 0; x<width; x++)
			{
				int reference = y*width + x;
                byteProcessor.putPixel( x, y, refValue);

				//get the set value (converted back to an int)
				int result =  byteProcessor.get( reference );

                assertEquals( refValue, result, 0.0);
            }
        }
	}

	@Test
	public void testGetPixelValue()
    {
	    //Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, imageByteData, cm);

		for(int y = 0; y<height; y++)
        {
			for(int x = 0; x<width; x++)
			{
				int reference = y*width + x;

				//get the set value (converted back to an int)
				float result =  byteProcessor.getPixelValue( x, y );
                int refValue;

                if ( imageByteData[reference] < 0)
                {    refValue = imageByteData[reference] + 256; }
                else
                {   refValue = imageByteData[reference]; }

                assertEquals( refValue, result, 0.0);
            }
        }
    }

	@Test
	public void testPutPixelValue()
    {
	    //Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, imageByteData, cm);

        double refValue = 3.0;

		for(int y = 0; y<height; y++)
        {
			for(int x = 0; x<width; x++)
			{
				int reference = y*width + x;

				//get the set value (converted back to an int)
				byteProcessor.putPixelValue( x, y, refValue );

                float result =  byteProcessor.getPixelValue( x, y );

                assertEquals( refValue, result, 0.0);
            }
        }
	}

	@Test
	public void testDrawPixel()
    {
	    //Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height,imageByteData, cm);

        //set the default fill value
		float setColorValue = 1;

		byteProcessor.setValue(setColorValue);

		//overwrite the pixel value with the new default fill value
		byteProcessor.drawPixel(1, 1);

		//see if the value was over-writen with the SetColor value
		float postDrawPixelValue = byteProcessor.getf(1, 1);

		assertEquals(setColorValue, postDrawPixelValue, 0.0);
    }

	@Test
	public void testSetPixelsObject()
    {
	    //Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height);

        byteProcessor.setPixels( imageByteData );


		for(int y = 0; y<height; y++)
        {
			for(int x = 0; x<width; x++)
			{
				int reference = y*width + x;

                float result =  byteProcessor.getf( reference );

                int refValue;
                if ( imageByteData[reference] < 0)
                {    refValue = imageByteData[reference] + 256; }
                else
                {   refValue = imageByteData[reference]; }
                assertEquals( refValue, result, 0.0);
            }
        }
    }

	@Test
	public void testCopyBits()
    {
	    //Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, imageByteData, cm);
 		ByteProcessor testByteProcessor =  new ByteProcessor(width, height);

        testByteProcessor.copyBits( byteProcessor, 0, 0, Blitter.COPY  );

		for(int y = 0; y<height; y++)
        {
			for(int x = 0; x<width; x++)
			{
				int reference = y*width + x;
                
                assertEquals( byteProcessor.getf( reference ), testByteProcessor.getf( reference ), 0.0);
            }
        }
	}

	@Test
	public void testApplyTable()
    {
	    //Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, imageByteData, cm);
        int[] sine_table = {99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99};

        byteProcessor.applyTable(sine_table);
        
		for(int y = 0; y<height; y++)
        {
			for(int x = 0; x<width; x++)
			{
				int reference = y*width + x;

                float result =  byteProcessor.getf( reference );

                int refValue;
                if ( imageByteData[reference] < 0)
                {    refValue = imageByteData[reference] + 256; }
                else
                {   refValue = imageByteData[reference]; }

                int lutValue = sine_table[refValue];

                assertEquals( lutValue, result, 0.0);
            }
        }
	}

	@Test
	public void testCreateImage()
    {
	    //Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, imageByteData, cm);

        //get the image
        Image testImage = byteProcessor.createImage();

        assertEquals( testImage.getWidth(null), width);
        assertEquals( testImage.getHeight(null), height);

        //TODO: add testing for actual image objects
	}

	@Test
	public void testGetBufferedImage()
    {
	    //Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, imageByteData, cm);

        //get the image
        BufferedImage testImage = byteProcessor.getBufferedImage();

        assertEquals( testImage.getWidth(null), width);
        assertEquals( testImage.getHeight(null), height);

        //create a reference image
        //TODO: Compare the images
	}

	@Test
	public void testCreateProcessor()
    {
	    //Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, imageByteData, cm);

        //get the image
        ByteProcessor testByteProcessor = (ByteProcessor) byteProcessor.createProcessor(width, height);

        //test empty image
        assertEquals( testByteProcessor.getWidth(), width);
        assertEquals( testByteProcessor.getHeight(), height);      
	}

	@Test
	public void testSnapshot()
    {
	    //Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, imageByteData, cm);

        //take a snapshot
        byteProcessor.snapshot();

        //change the entire image
        byteProcessor.flipVertical();

        //revert from snapshot
        byteProcessor.reset();

		for(int y = 0; y<height; y++)
        {
			for(int x = 0; x<width; x++)
			{
				int reference = y*width + x;

                float result =  byteProcessor.getf( reference );

                int refValue;
                if ( imageByteData[reference] < 0)
                {    refValue = imageByteData[reference] + 256; }
                else
                {   refValue = imageByteData[reference]; }
                assertEquals( refValue, result, 0.0);
            }
        }
	}

	@Test
	public void testReset()
    {
	    //Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, imageByteData, cm);

        //take a snapshot
        byteProcessor.snapshot();

        //change the entire image
        byteProcessor.flipVertical();

        //revert from snapshot
        byteProcessor.reset();

		for(int y = 0; y<height; y++)
        {
			for(int x = 0; x<width; x++)
			{
				int reference = y*width + x;

                float result =  byteProcessor.getf( reference );

                int refValue;
                if ( imageByteData[reference] < 0)
                {    refValue = imageByteData[reference] + 256; }
                else
                {   refValue = imageByteData[reference]; }
                assertEquals( refValue, result, 0.0);
            }
        }
	}

	@Test
	public void testResetImageProcessor()
    {
	    //Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, imageByteData, cm);

        //change the entire image
        byteProcessor.flipVertical();

        //reset from new imageprocessor
        byteProcessor.reset( new ByteProcessor(width, height, imageByteData, cm));

		for(int y = 0; y<height; y++)
        {
			for(int x = 0; x<width; x++)
			{
				int reference = y*width + x;

                float result =  byteProcessor.getf( reference );

                int refValue;
                if ( imageByteData[reference] < 0)
                {    refValue = imageByteData[reference] + 256; }
                else
                {   refValue = imageByteData[reference]; }
                assertEquals( refValue, result, 0.0);
            }
        }
	}

	@Test
	public void testSetSnapshotPixels()
    {
	    //Create a new ByteProcessor object for testing
		ByteProcessor refByteProcessor =  new ByteProcessor(width, height, imageByteData, cm);
		ByteProcessor testByteProcessor =  new ByteProcessor(width, height);

        //change the entire image
        testByteProcessor.setSnapshotPixels( imageByteData );

        //reset from new imageprocessor
        testByteProcessor.reset();

		for(int y = 0; y<height; y++)
        {
			for(int x = 0; x<width; x++)
			{
				int reference = y*width + x;
                assertEquals( refByteProcessor.getf( reference ), testByteProcessor.getf( reference ), 0.0);
            }
        }
	}

	@Test
	public void testGetSnapshotPixels()
    {
	    //Create a new ByteProcessor object for testing
		ByteProcessor testByteProcessor =  new ByteProcessor(width, height);

        //change the entire image
        testByteProcessor.setSnapshotPixels( imageByteData );

        byte[] snapShotPixels = ( byte[] )testByteProcessor.getSnapshotPixels();

		for(int y = 0; y<height; y++)
        {
			for(int x = 0; x<width; x++)
			{
				int reference = y*width + x;
                assertEquals( snapShotPixels[ reference ], imageByteData[ reference ], 0.0);
            }
        }
	}

	@Test    //TODO: fix this test
	public void testConvolve3x3()
    {
	    //Create a new ByteProcessor object for testing
		ByteProcessor refByteProcessor =  new ByteProcessor(width, height, imageByteData, cm);
 		ByteProcessor testByteProcessor =  new ByteProcessor(width, height, imageByteData, cm);
        byte[] refPixels = new byte[width*height];

        int[] kernel = {-1, -1, -1, -1, 8, -1, -1, -1, -1};
        testByteProcessor.convolve3x3(kernel);
        byte[] testPixels = (byte[]) testByteProcessor.getPixels();


        int p1, p2, p3,
            p4, p5, p6,
            p7, p8, p9;
        int k1=kernel[0], k2=kernel[1], k3=kernel[2],
            k4=kernel[3], k5=kernel[4], k6=kernel[5],
            k7=kernel[6], k8=kernel[7], k9=kernel[8];

        int scale = 0;
        for (int i=0; i<kernel.length; i++)
            scale += kernel[i];
        if (scale==0) scale = 1;
        int inc = refByteProcessor.roiHeight/25;
        if (inc<1) inc = 1;

        byte[] pixels2 = (byte[]) refByteProcessor.getPixelsCopy();
        int offset, sum;
        int rowOffset = refByteProcessor.width;
        for (int y=refByteProcessor.yMin; y<=refByteProcessor.yMax; y++) {
            offset = refByteProcessor.xMin + y * refByteProcessor.width;
            p1 = 0;
            p2 = pixels2[offset-rowOffset-1]&0xff;
            p3 = pixels2[offset-rowOffset]&0xff;
            p4 = 0;
            p5 = pixels2[offset-1]&0xff;
            p6 = pixels2[offset]&0xff;
            p7 = 0;
            p8 = pixels2[offset+rowOffset-1]&0xff;
            p9 = pixels2[offset+rowOffset]&0xff;

            for (int x=refByteProcessor.xMin; x<=refByteProcessor.xMax; x++) {
                p1 = p2; p2 = p3;
                p3 = pixels2[offset-rowOffset+1]&0xff;
                p4 = p5; p5 = p6;
                p6 = pixels2[offset+1]&0xff;
                p7 = p8; p8 = p9;
                p9 = pixels2[offset+rowOffset+1]&0xff;

                sum = k1*p1 + k2*p2 + k3*p3
                    + k4*p4 + k5*p5 + k6*p6
                    + k7*p7 + k8*p8 + k9*p9;
                sum /= scale;

                if(sum>255) sum= 255;
                if(sum<0) sum= 0;

                refPixels[offset++] = (byte)sum;
            }
        }

        //test
        for(int y = 0; y<refByteProcessor.height; y++)
        {
			for(int x = 0; x<refByteProcessor.width; x++)
			{
				int reference = y*refByteProcessor.width + x;
                assertEquals( refPixels[ reference ], testPixels[ reference ], 0.0);
            }
        }
	}

	@Test
	public void testFilter()
    {
    //Test Filter:BLUR_MORE
        //Create a new ByteProcessor object for testing
		ByteProcessor refByteProcessor =  new ByteProcessor(width, height, imageByteData, cm);
        ByteProcessor testByteProcessor =  new ByteProcessor(width, height, imageByteData, cm);
        byte[] refPixelArray = (byte[]) refByteProcessor.getPixelsCopy();
        
        int p1, p2, p3, p4, p5, p6, p7, p8, p9;
           int inc = refByteProcessor.roiHeight/25;
           if (inc<1) inc = 1;

           byte[] pixels2 = (byte[])refByteProcessor.getPixelsCopy();
           if (width==1) {
               refByteProcessor.filterEdge(refByteProcessor.BLUR_MORE, pixels2, refByteProcessor.roiHeight, refByteProcessor.roiX, refByteProcessor.roiY, 0, 1);
               return;
           }

           int offset, sum=0;
           int rowOffset = width;

           for (int y=refByteProcessor.yMin; y<=refByteProcessor.yMax; y++) {
               offset = refByteProcessor.xMin + y * width;
               p2 = pixels2[offset-rowOffset-1]&0xff;
               p3 = pixels2[offset-rowOffset]&0xff;
               p5 = pixels2[offset-1]&0xff;
               p6 = pixels2[offset]&0xff;
               p8 = pixels2[offset+rowOffset-1]&0xff;
               p9 = pixels2[offset+rowOffset]&0xff;

               for (int x=refByteProcessor.xMin; x<=refByteProcessor.xMax; x++) {
                   p1 = p2; p2 = p3;
                   p3 = pixels2[offset-rowOffset+1]&0xff;
                   p4 = p5; p5 = p6;
                   p6 = pixels2[offset+1]&0xff;
                   p7 = p8; p8 = p9;
                   p9 = pixels2[offset+rowOffset+1]&0xff;

                  sum = (p1+p2+p3+p4+p5+p6+p7+p8+p9)/9;

                  refPixelArray[offset++] = (byte)sum;
               }
               //if (y%inc==0)
               //  showProgress((double)(y-roiY)/roiHeight);
           }
           if (refByteProcessor.xMin==1) refByteProcessor.filterEdge(refByteProcessor.BLUR_MORE, pixels2, refByteProcessor.roiHeight, refByteProcessor.roiX, refByteProcessor.roiY, 0, 1);
           if (refByteProcessor.yMin==1) refByteProcessor.filterEdge(refByteProcessor.BLUR_MORE, pixels2, refByteProcessor.roiWidth, refByteProcessor.roiX, refByteProcessor.roiY, 1, 0);
           if (refByteProcessor.xMax==width-2) refByteProcessor.filterEdge(refByteProcessor.BLUR_MORE, pixels2, refByteProcessor.roiHeight, width-1, refByteProcessor.roiY, 0, 1);
           if (refByteProcessor.yMax==height-2) refByteProcessor.filterEdge(refByteProcessor.BLUR_MORE, pixels2, refByteProcessor.roiWidth, refByteProcessor.roiX, height-1, 1, 0);

        //assert testFilter:BLUR_MORE
        testByteProcessor.filter(ImageProcessor.BLUR_MORE);
        byte[] testPixels = (byte[]) testByteProcessor.getPixelsCopy();

        for(int y = 0; y<height; y++)
        {
			for(int x = 0; x<width; x++)
			{
				int reference = y*width + x;
                assertEquals( refPixelArray[ reference ], testPixels[ reference ], 0.0);
            }
        }
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
	public void testRotate() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetHistogram() {
		fail("Not yet implemented");
	}

	@Test
	public void testErode() {
		fail("Not yet implemented");
	}

	@Test
	public void testDilate() {
		fail("Not yet implemented");
	}

	@Test
	public void testConvolve() {
		fail("Not yet implemented");
	}

	@Test
	public void testToFloat() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetPixelsIntFloatProcessor() {
		fail("Not yet implemented");
	}

	@Test
	public void testCreate8BitImage() {
		fail("Not yet implemented");
	}

	@Test
	public void testByteProcessorImage() {
		fail("Not yet implemented");
	}

	@Test
	public void testByteProcessorIntInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testByteProcessorIntIntByteArrayColorModel() {
		fail("Not yet implemented");
	}

	@Test
	public void testByteProcessorBufferedImage() {
		fail("Not yet implemented");
	}

	@Test
	public void testCreateBufferedImage() {
		fail("Not yet implemented");
	}

	@Test
	public void testFilterEdge() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetEdgePixel() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetEdgePixel0() {
		fail("Not yet implemented");
	}

	@Test
	public void testErodeIntInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testDilateIntInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testOutline() {
		fail("Not yet implemented");
	}

	@Test
	public void testSkeletonize() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetHistogramImageProcessor() {
		fail("Not yet implemented");
	}

	@Test
	public void testApplyLut() {
		fail("Not yet implemented");
	}

	@Test
	public void testToFloatProcessors() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetFromFloatProcessors() {
		fail("Not yet implemented");
	}

	@Test
	public void testToFloatArrays() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetFromFloatArrays() {
		fail("Not yet implemented");
	}

}
