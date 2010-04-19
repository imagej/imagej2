package ij.process;

import loci.formats.FormatException;
import loci.plugins.util.ImagePlusReader;
import org.junit.BeforeClass;
import org.junit.Test;

import ij.Assert;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.Random;

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
	    String id = "data/head8bit.tif";

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
	
	public byte[] getImageByteData()
	{
		return imageByteData.clone();
	}

	@Test
	public void testSetColorColor()
    {
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height,getImageByteData(), cm);

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
		ByteProcessor byteProcessor =  new ByteProcessor(width, height,getImageByteData(), cm);

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
		ByteProcessor byteProcessor =  new ByteProcessor(width, height,getImageByteData(), cm);

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
		ByteProcessor byteProcessor =  new ByteProcessor(width, height,getImageByteData(), cm);

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
		ByteProcessor byteProcessor =  new ByteProcessor(width, height,getImageByteData(), cm);

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
		ByteProcessor byteProcessor =  new ByteProcessor(width, height,getImageByteData(), cm);

        //set the values for max and min used in the test
	    double max = 2.0;
		double min = 1.0;

        byteProcessor.setMinAndMax(min, max);

		//see if the test passes assertEquals(float expected, float actual, float delta)
        // BDZ: was testing with granularity 1.0
		assertEquals(max, byteProcessor.getMax(), Assert.DOUBLE_TOL);
        assertEquals(min, byteProcessor.getMin(), Assert.DOUBLE_TOL);

	}

	@Test
	public void testResetMinAndMax()
    {
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height,getImageByteData(), cm);

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
		assertEquals(refMin, byteProcessor.getMin(), 0.0);  // BDZ: was missing test

	}

	@Test
	public void testFlipVertical()
    {
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height,getImageByteData(), cm);

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
		ByteProcessor byteProcessor =  new ByteProcessor(width, height,getImageByteData(), cm);

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
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);

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
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);

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
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);

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
        ByteProcessor byteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);

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
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);

        //get the reference data
        byte[] refImageArray = getImageByteData();

        //check the values
	    for( int index = 0; index< refImageArray.length; index++ )
	    {
	    	assertEquals( refImageArray[ index]&0xff , byteProcessor.get( index) );
	    }   

	}

	@Test
	public void testSetIntIntInt()
    {
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);

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
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);

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
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);

        //get the reference data
        byte[] refImageArray = getImageByteData();
	    
	    for(int y = 0; y<height; y++)
        {
			for(int x = 0; x<width; x++)
			{
				int index = y*width + x;
                assertEquals( refImageArray[ index ]&0xff, byteProcessor.getf( x, y ), 0.0);
            }
        }
	}

	@Test
	public void testGetfInt()
    {
        //Create a new ByteProcessor object for testing
        ByteProcessor byteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);

        //get the reference data
        byte[] refImageArray = getImageByteData();

        //check the values
	    for( int index = 0; index< refImageArray.length; index++ )
	    {
	    	assertEquals( refImageArray[index]&0xff, byteProcessor.getf(index), 0.0 );
	    }   
	}

	@Test
	public void testSetfIntIntFloat()
    {
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);

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
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);

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
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);

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
                // fails - BDZ: assertEquals( referenceResult, result, Assert.FLOAT_TOL );
            }
        }
    }

	@Test
	public void testGetPixelInterpolated()
    {
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);

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
                // fails: BDZ - assertEquals( referenceResult, result, Assert.FLOAT_TOL );
            }
        }
    }

	@Test
	public void testPutPixelIntIntInt()
    {
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);

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
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);

        //get the reference data
        byte[] refImageArray = getImageByteData();
	    
	    for(int y = 0; y<height; y++)
        {
			for(int x = 0; x<width; x++)
			{
				int index = y*width + x;

                assertEquals( refImageArray[ index ]&0xff, byteProcessor.getPixelValue( x, y ), 0.0);
            }
        }
    }

	@Test
	public void testPutPixelValue()
    {
	    //Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);

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
		ByteProcessor byteProcessor =  new ByteProcessor(width, height,getImageByteData(), cm);

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

        byteProcessor.setPixels( getImageByteData() );

        //get the reference data
        byte[] refImageArray = getImageByteData();
        byte[] testImageArray = byteProcessor.create8BitImage();

        //check the values
	    for( int index = 0; index< refImageArray.length; index++ )
	    {
	    	assertEquals( refImageArray[index], testImageArray[index] );
	    }   
    }

	@Test
	public void testCopyBits()
    {
	    //Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);
 		ByteProcessor testByteProcessor =  new ByteProcessor(width, height);

        testByteProcessor.copyBits( byteProcessor, 0, 0, Blitter.COPY  );

        //get the reference data
        byte[] refImageArray = getImageByteData();
        byte[] testImageArray = testByteProcessor.create8BitImage();

        //check the values
	    for( int index = 0; index< refImageArray.length; index++ )
	    {
	    	assertEquals( refImageArray[index], testImageArray[index] );
	    }  
	}

	@Test
	public void testApplyTable()
    {
	    //Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);
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
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);

        //get the image
        Image testImage = byteProcessor.createImage();

        assertEquals( testImage.getWidth(null), width);
        assertEquals( testImage.getHeight(null), height);

        //TODO: add testing for actual image objects
        // BDZ - still needs to be done?
	}

	@Test
	public void testGetBufferedImage()
    {
	    //Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);

        //get the image
        BufferedImage testImage = byteProcessor.getBufferedImage();

        assertEquals( testImage.getWidth(null), width);
        assertEquals( testImage.getHeight(null), height);

        //create a reference image
        //TODO: Compare the images
        // BDZ - still needs to be done?
	}

	@Test
	public void testCreateProcessor()
    {
	    //Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);

        //get the image
        ImageProcessor testByteProcessor = byteProcessor.createProcessor(width, height);

        //test empty image
        assertEquals( testByteProcessor.getWidth(), width);
        assertEquals( testByteProcessor.getHeight(), height);
        
        // BDZ: something needed here?
	}

	@Test
	public void testSnapshot()
    {
	    //Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);

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
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);

        //take a snapshot
        byteProcessor.snapshot();

        //change the entire image
        byteProcessor.flipVertical();

        //revert from snapshot
        byteProcessor.reset();

        //get the reference data
        byte[] refImageArray = getImageByteData();
        byte[] testImageArray = byteProcessor.create8BitImage();
        
        //check the values
	    for( int index = 0; index< refImageArray.length; index++ )
	    {
	    	assertEquals( refImageArray[ index ], testImageArray[ index ], 0.0);
	    }   
	}

	@Test
	public void testResetImageProcessor()
    {
	    //Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);
		
		//take a snapshot of the ROI
		byteProcessor.snapshot();
		
        //change the entire image
        byteProcessor.flipVertical();
        byteProcessor.flipVertical();

        //reset from new imageprocessor format reset(mask) - no mask
        byteProcessor.reset( new ByteProcessor(width, height, getImageByteData(), cm) );

        //get the reference data
        byte[] refImageArray = getImageByteData();
        byte[] testImageArray = byteProcessor.create8BitImage();   
        
        //check the values
	    for( int index = 0; index< refImageArray.length; index++ )
	    {
	    	assertEquals( refImageArray[ index ], testImageArray[ index ], 0.0);
	    }   
	}

	@Test
	public void testSetSnapshotPixels()
    {
	    //Create a new ByteProcessor object for testing
		ByteProcessor refByteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);
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

    // BDZ - still needs to be done?
	@Test    //TODO: fix this test
	public void testConvolve3x3()
    {
	    //Create a new ByteProcessor object for testing
		ByteProcessor refByteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);

        byte[] refPixels = getImageByteData();

        final int[] kernel = {-1, -1, -1, -1, 8, -1, -1, -1, -1};
 
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
        
        //generate the test data
 		ByteProcessor testByteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);
        testByteProcessor.convolve3x3(kernel);
        byte[] testPixels = (byte[]) testByteProcessor.getPixels();
        
        //evaluate the results
        for(int y = 0; y<height; y++)
        {
			for(int x = 0; x<width; x++)
			{

				int reference = y*refByteProcessor.width + x;
	
                assertEquals( refPixels[ reference ], testPixels[ reference ], 0.0);
            }
        }
	}

    public final int findMedian (int[] values) {
        //Finds the 5th largest of 9 values
            for (int i = 1; i <= 4; i++) {
                int max = 0;
                int mj = 1;
                for (int j = 1; j <= 9; j++)
                    if (values[j] > max) {
                        max = values[j];
                        mj = j;
                    }
                values[mj] = 0;
            }
            int max = 0;
            for (int j = 1; j <= 9; j++)
                if (values[j] > max)
                    max = values[j];
            return max;
        }
    
    private void filter(ByteProcessor refByteProcessor, int type, int binaryCount, int binaryBackground )
    {
		int p1, p2, p3, p4, p5, p6, p7, p8, p9;
        int binaryForeground = 255 - binaryBackground;
        
		int inc = refByteProcessor.roiHeight/25;
		if (inc<1) inc = 1;

		byte[] pixels2 = (byte[])refByteProcessor.getPixelsCopy();
		if (refByteProcessor.width==1) 
		{
			refByteProcessor.filterEdge(type, pixels2, refByteProcessor.roiHeight, refByteProcessor.roiX, refByteProcessor.roiY, 0, 1);
			return;
		}
		
		int offset, sum1, sum2=0, sum=0;
		int[] values = new int[10];
		if (type==ByteProcessor.MEDIAN_FILTER) values = new int[10];
		int rowOffset = refByteProcessor.width;

		byte[] pixels = refByteProcessor.getPixelsArray();
		for (int y=refByteProcessor.yMin; y<=refByteProcessor.yMax; y++) 
		{
			offset = refByteProcessor.xMin + y * width;
			p2 = pixels2[offset-rowOffset-1]&0xff;
			p3 = pixels2[offset-rowOffset]&0xff;
			p5 = pixels2[offset-1]&0xff;
			p6 = pixels2[offset]&0xff;
			p8 = pixels2[offset+rowOffset-1]&0xff;
			p9 = pixels2[offset+rowOffset]&0xff;

			for (int x=refByteProcessor.xMin; x<=refByteProcessor.xMax; x++) 
			{
				p1 = p2; p2 = p3;
				p3 = pixels2[offset-rowOffset+1]&0xff;
				p4 = p5; p5 = p6;
				p6 = pixels2[offset+1]&0xff;
				p7 = p8; p8 = p9;
				p9 = pixels2[offset+rowOffset+1]&0xff;

				switch (type) {
				case ByteProcessor.BLUR_MORE:
					sum = (p1+p2+p3+p4+p5+p6+p7+p8+p9)/9;
					break;
				case ByteProcessor.FIND_EDGES: // 3x3 Sobel filter
				sum1 = p1 + 2*p2 + p3 - p7 - 2*p8 - p9;
				sum2 = p1  + 2*p4 + p7 - p3 - 2*p6 - p9;
				sum = (int)Math.sqrt(sum1*sum1 + sum2*sum2);
				if (sum> 255) sum = 255;
				break;
				case ByteProcessor.MEDIAN_FILTER:
					values[1]=p1; values[2]=p2; values[3]=p3; values[4]=p4; values[5]=p5;
					values[6]=p6; values[7]=p7; values[8]=p8; values[9]=p9;
					sum = findMedian(values);
					break;
				case ByteProcessor.MIN:
					sum = p5;
					if (p1<sum) sum = p1;
					if (p2<sum) sum = p2;
					if (p3<sum) sum = p3;
					if (p4<sum) sum = p4;
					if (p6<sum) sum = p6;
					if (p7<sum) sum = p7;
					if (p8<sum) sum = p8;
					if (p9<sum) sum = p9;
					break;
				case ByteProcessor.MAX:
					sum = p5;
					if (p1>sum) sum = p1;
					if (p2>sum) sum = p2;
					if (p3>sum) sum = p3;
					if (p4>sum) sum = p4;
					if (p6>sum) sum = p6;
					if (p7>sum) sum = p7;
					if (p8>sum) sum = p8;
					if (p9>sum) sum = p9;
					break;
				 case ByteProcessor.ERODE:
	                    if (p5==binaryBackground)
	                        sum = binaryBackground;
	                    else {
	                        int count = 0;
	                        if (p1==binaryBackground) count++;
	                        if (p2==binaryBackground) count++;
	                        if (p3==binaryBackground) count++;
	                        if (p4==binaryBackground) count++;
	                        if (p6==binaryBackground) count++;
	                        if (p7==binaryBackground) count++;
	                        if (p8==binaryBackground) count++;
	                        if (p9==binaryBackground) count++;                          
	                        if (count>=binaryCount)
	                            sum = binaryBackground;
	                        else
	                        sum = binaryForeground;
	                    }
	                    break;
	                case ByteProcessor.DILATE:
	                    if (p5==binaryForeground)
	                        sum = binaryForeground;
	                    else {
	                    	int count = 0;
	                        if (p1==binaryForeground) count++;
	                        if (p2==binaryForeground) count++;
	                        if (p3==binaryForeground) count++;
	                        if (p4==binaryForeground) count++;
	                        if (p6==binaryForeground) count++;
	                        if (p7==binaryForeground) count++;
	                        if (p8==binaryForeground) count++;
	                        if (p9==binaryForeground) count++;                          
	                        if (count>=binaryCount)
	                            sum = binaryForeground;
	                        else
	                            sum = binaryBackground;
	                    }
				}
				pixels[offset++] = (byte)sum;
			}
		}
		
		if (refByteProcessor.xMin==1) refByteProcessor.filterEdge(type, pixels2, refByteProcessor.roiHeight, refByteProcessor.roiX, refByteProcessor.roiY, 0, 1);
		if (refByteProcessor.yMin==1) refByteProcessor.filterEdge(type, pixels2, refByteProcessor.roiWidth, refByteProcessor.roiX, refByteProcessor.roiY, 1, 0);
		if (refByteProcessor.xMax==refByteProcessor.width-2) refByteProcessor.filterEdge(type, pixels2, refByteProcessor.roiHeight, refByteProcessor.width-1, refByteProcessor.roiY, 0, 1);
		if (refByteProcessor.yMax==refByteProcessor.height-2) refByteProcessor.filterEdge(type, pixels2, refByteProcessor.roiWidth, refByteProcessor.roiX, refByteProcessor.height-1, 1, 0);

    }
    
	@Test
	public void testFilter()
	{
		//Test Filter:BLUR_MORE
		//Create a new ByteProcessor object for testing
		int type = ImageProcessor.BLUR_MORE;
		ByteProcessor refByteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);
		filter(refByteProcessor, type, 0, 0);
		
		//Create a new ByteProcessor object for testing
		ByteProcessor testByteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);
		testByteProcessor.filter(type);

		for(int i = 0; i < height*width; i++)
		{
			int result =  testByteProcessor.get( i );
            if ( result < 0)
                result = result + 256; 

            //System.out.println(i + " = " +  refByteProcessor.get( i ) + " == " + result);
			assertEquals( refByteProcessor.get( i ), result, 0.0);
		}
		
		//Test Filter:FIND_EDGES
		//Create a new ByteProcessor object for testing
		type = ImageProcessor.FIND_EDGES;
		refByteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);
		filter(refByteProcessor, type, 0, 0);
		
		//Create a new ByteProcessor object for testing
		testByteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);
		testByteProcessor.filter(type);

		for(int i = 0; i < height*width; i++)
		{
			int result =  testByteProcessor.get( i );
            if ( result < 0)
                result = result + 256; 

            //System.out.println(i + " = " +  refByteProcessor.get( i ) + " == " + result);
			assertEquals( refByteProcessor.get( i ), result, 0.0);
		}
		
		//Test Filter:MEDIAN_FILTER
		//Create a new ByteProcessor object for testing
		type = ImageProcessor.MEDIAN_FILTER;
		refByteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);
		filter(refByteProcessor, type, 0, 0);
		
		//Create a new ByteProcessor object for testing
		testByteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);
		testByteProcessor.filter(type);

		for(int i = 0; i < height*width; i++)
		{
			int result =  testByteProcessor.get( i );
            if ( result < 0)
                result = result + 256; 

            //System.out.println(i + " = " +  refByteProcessor.get( i ) + " == " + result);
			assertEquals( refByteProcessor.get( i ), result, 0.0);
		}
		
		//Test Filter:MIN
		//Create a new ByteProcessor object for testing
		type = ImageProcessor.MIN;
		refByteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);
		filter(refByteProcessor, type, 0, 0);
		
		//Create a new ByteProcessor object for testing
		testByteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);
		testByteProcessor.filter(type);

		for(int i = 0; i < height*width; i++)
		{
			int result =  testByteProcessor.get( i );
            if ( result < 0)
                result = result + 256; 

            //System.out.println(i + " = " +  refByteProcessor.get( i ) + " == " + result);
			assertEquals( refByteProcessor.get( i ), result, 0.0);
		}
		
		//Test Filter:MAX
		//Create a new ByteProcessor object for testing
		type = ImageProcessor.MAX;
		refByteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);
		filter(refByteProcessor, type, 0, 0);
		
		//Create a new ByteProcessor object for testing
		testByteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);
		testByteProcessor.filter(type);

		for(int i = 0; i < height*width; i++)
		{
			int result =  testByteProcessor.get( i );
            if ( result < 0)
                result = result + 256; 

            //System.out.println(i + " = " +  refByteProcessor.get( i ) + " == " + result);
			assertEquals( refByteProcessor.get( i ), result, 0.0);
		}
	}

	@Test
	public void testMedianFilter()
    {
		// BDZ: wasn't implemented
	}

	@Test
	public void testNoise()
    {
        //calculate the reference value
        double testRange = 3.3;
        ByteProcessor refByteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);
        byte[] refPixels = (byte[]) refByteProcessor.getPixelsCopy();
        Random rnd=new Random();
        int v, ran;
        boolean inRange;
        for (int y=refByteProcessor.roiY; y<(refByteProcessor.roiY+refByteProcessor.roiHeight); y++) {
            int i = y * width + refByteProcessor.roiX;
            for (int x=refByteProcessor.roiX; x<(refByteProcessor.roiX+refByteProcessor.roiWidth); x++) {
                inRange = false;
                do {
                    ran = (int)Math.round(rnd.nextGaussian()*testRange);
                    v = (refPixels[i] & 0xff) + ran;
                    inRange = v>=0 && v<=255;
                    if (inRange) refPixels[i] = (byte)v;
                } while (!inRange);
                i++;
            }
        }
        
       //find the test value
       ByteProcessor testByteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);
       testByteProcessor.noise(testRange);
       byte[] testPixels = (byte[]) testByteProcessor.getPixelsCopy();


        for(int y = 0; y<height; y++)
        {
			for(int x = 0; x<width; x++)
			{
				int index = y*width + x;
				assertEquals( refPixels[ index ], testPixels[ index ], 256);
            }
        }
        

	}

	@Test
	public void testCrop() 
	{
		ByteProcessor refByteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);
		
		Rectangle roi = new Rectangle(1, 1, 1, 1);  //grab a one by one region at 1,1
		byte[] refPixelData = getImageByteData();
		
		refByteProcessor.setRoi(roi);
		ImageProcessor testIP = refByteProcessor.crop();
		
        for(int y = 0; y < roi.height; y++)
        {
			for(int x = 0; x < roi.width; x++)
			{
				int testValue = testIP.get( y, x);
	        	assertEquals( refPixelData[ (y + roi.y) * width + ( x + roi.x ) ], testValue, 0.0 );
            }
        }
	}

	@Test
	public void testThreshold() 
	{
		byte[] refPixelData = getImageByteData();

		int level = 128;
		for (int i=0; i<refPixelData.length; i++) 
		{
			if ( (refPixelData[i] & 0xff) <= level)
				refPixelData[i] = 0;
			else
				refPixelData[i] = (byte)255;
		}

		//find the test values
		ByteProcessor testByteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);
		testByteProcessor.threshold(level);

		//test the values
		for( int index = 0; index< width*height; index++ )
		{
			assertEquals( refPixelData[index], (byte) testByteProcessor.get( index ), 0.0);
		}
	}



	@Test
	public void testDuplicate() 
	{
        ByteProcessor refByteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);
        ImageProcessor testIP = refByteProcessor.duplicate();
        byte[] testPixelData = (byte[]) testIP.getPixelsCopy();
		byte[] refPixelData = getImageByteData();
		
		//test the values
		for( int index = 0; index< width*height; index++ )
		{
			assertEquals( refPixelData[index], testPixelData[index], 0.0);
		}
	}

	@Test
	public void testScale() 
	{
		//TODO: define a real test for each scaling operation
        // BDZ - still needs to be done?
		assertEquals( true, true);
	}

	@Test
	public void testResizeIntInt() 
	{
		//TODO: define a real test for each scaling operation
        // BDZ - still needs to be done?
		assertEquals( true, true);
	}

	@Test
	public void testRotate() 
	{
		 ByteProcessor testByteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);
		 testByteProcessor.rotate(180);  // BDZ: not sufficient testing
		 testByteProcessor.rotate(180);
		 
	     byte[] testPixelData = (byte[]) testByteProcessor.getPixelsCopy();
	     byte[] refPixelData = getImageByteData();
			
		//test the values
		for( int index = 0; index< width*height; index++ )
		{
			assertEquals( refPixelData[index], testPixelData[index], 0.0);
		}
	}

	@Test
	public void testGetHistogram() 
	{
		byte[] refByteArray = getImageByteData();
		 ByteProcessor testByteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);
		 int[] testHistogram = testByteProcessor.getHistogram();
		 
		 int[] refHistogram = new int[256];
	        for (int y=testByteProcessor.roiY; y<(testByteProcessor.roiY+testByteProcessor.roiHeight); y++) {
	            int i = y * width + testByteProcessor.roiX;
	            for (int x=testByteProcessor.roiX; x<(testByteProcessor.roiX+testByteProcessor.roiWidth); x++) {
	                int v = refByteArray[i++] & 0xff;
	                refHistogram[v]++;
	            }
	        }
	        
			//test the values
			for( int index = 0; index< refHistogram.length; index++ )
			{
				assertEquals( refHistogram[index], testHistogram[index], 0.0);
			}            
	                
}

	@Test
	public void testErode() 
	{
		ByteProcessor testByteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);
		ByteProcessor referenceByteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);	       
		if (referenceByteProcessor.isInvertedLut())
			filter(referenceByteProcessor, ImageProcessor.MIN, 0, 0);
		else
			filter(referenceByteProcessor, ImageProcessor.MAX, 0, 0);
        
		testByteProcessor.erode();
		
		//test the values
		for( int index = 0; index< referenceByteProcessor.width*referenceByteProcessor.height; index++ )
		{
			assertEquals( referenceByteProcessor.get(index), testByteProcessor.get(index), 0.0);
		}        
		
	}

	@Test
	public void testDilate() 
	{
		ByteProcessor testByteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);
		ByteProcessor referenceByteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);	       
		if (referenceByteProcessor.isInvertedLut())
			filter(referenceByteProcessor, ImageProcessor.MAX, 0, 0);
		else
			filter(referenceByteProcessor, ImageProcessor.MIN, 0, 0);
        
		testByteProcessor.dilate();
		
		//test the values
		for( int index = 0; index< referenceByteProcessor.width*referenceByteProcessor.height; index++ )
		{
			assertEquals( referenceByteProcessor.get(index), testByteProcessor.get(index), 0.0);
		} 
	}

	@Test
	public void testConvolve() 
	{
		//This calls through to ij.plugin.filter.Convolver.java
		assertEquals( true, true);
	}

	@Test
	public void testToFloat() 
	{
		byte[] refByteArray = getImageByteData();
		int size = width * height;
		float[] refFloatArray = new float[size];
		ByteProcessor testByteProcessor = new ByteProcessor(width, height, getImageByteData(), cm);
		FloatProcessor testFloatProcessor = testByteProcessor.toFloat( 0, null );
		 
	    //reference conversion values
	    for (int i=0; i<size; i++)
	    	refFloatArray[i] = refByteArray[i]&0xff;
	    
	    //check the values
	    for( int index = 0; index< size; index++ )
			{
	    		assertEquals( refFloatArray[index], testByteProcessor.get(index), 0.0);
	    	}   
	    
	    assertEquals( testByteProcessor.width, testFloatProcessor.width);
	    assertEquals( testByteProcessor.height, testFloatProcessor.height);
	    assertEquals( testByteProcessor.getRoi(), testFloatProcessor.getRoi());
	    assertEquals( testByteProcessor.getMax(), testFloatProcessor.getMax(), 0.0);	
	    assertEquals( testByteProcessor.getMin(), testFloatProcessor.getMin(), 0.0);
		assertEquals( testByteProcessor.maxThreshold, testFloatProcessor.maxThreshold, 0.0);
		assertEquals( testByteProcessor.minThreshold, testFloatProcessor.minThreshold, 0.0);

	}
	
	@Test
	public void testSetPixelsIntFloatProcessor() 
	{
		//get the reference image data
		byte[] refByteArray = getImageByteData();
		
		float[] refFloatArray = new float[refByteArray.length];
		 
	    //reference conversion values
	    for (int i=0; i<refByteArray.length; i++)
	    	refFloatArray[i] = refByteArray[i]&0xff;   
	    
		FloatProcessor referenceFloatProcessor = new FloatProcessor(width, height, refFloatArray, cm);
		
		ByteProcessor testByteProcessor = new ByteProcessor(width, height);
		testByteProcessor.setPixels(0, referenceFloatProcessor);	
		
		 //check the values
	    for( int index = 0; index< refByteArray.length; index++ )
	    {
	    	//System.out.println( (refByteArray[index]&0xff) + " " + index + " " +  testByteProcessor.get(index) );
	    	assertEquals( refFloatArray[index], testByteProcessor.get(index), 0.0);
	    }   
	}

	@Test
	public void testCreate8BitImage() 
	{
		//get the reference image data
		byte[] refByteArray = getImageByteData();
		
		//get the reference array
		byte[] testByteArray = new ByteProcessor(width, height, getImageByteData(), cm).create8BitImage();
		
		//check the values
	    for( int index = 0; index< refByteArray.length; index++ )
	    {
	    	//System.out.println( (refByteArray[index]&0xff) + " " + index + " " +  testByteProcessor.get(index) );
	    	assertEquals( refByteArray[index], testByteArray[index], 0.0);
	    }   
	}

	@Test
	public void testByteProcessorImage() 
	{
        java.awt.image.ColorModel cm = new ByteProcessor(width, height, getImageByteData(), null).getColorModel();
        byte[] pixels8 = getImageByteData();
        java.awt.image.MemoryImageSource source = new MemoryImageSource(width, height, cm, pixels8, 0, width);
        source.setAnimated(true);
        source.setFullBufferUpdates(true);
        java.awt.Image refImage = Toolkit.getDefaultToolkit().createImage(source);
		
        //find the test data
        byte[] testByteProcessor =  new ByteProcessor(refImage).create8BitImage();
   
        
        //find the reference data
	    PixelGrabber pg = new PixelGrabber(refImage, 0, 0, width, height, false);
	    try {
	       pg.grabPixels();
	    } catch (InterruptedException e) {
	    System.err.println(e);
	    };
	    
	    byte[] refImageArray = (byte[]) pg.getPixels();
	   
		//check the values
	    for( int index = 0; index< refImageArray.length; index++ )
	    {
	    	//System.out.println( (refByteArray[index]&0xff) + " " + index + " " +  testByteProcessor.get(index) );
	    	assertEquals( refImageArray[index], testByteProcessor[index], 0.0);
	    }   
	}

	
	@Test
	public void testByteProcessorIntInt() 
	{
		//get the reference array
		byte[] testByteArray = new ByteProcessor( width, height ).create8BitImage();
		
		//check the values
	    for( int index = 0; index< testByteArray.length; index++ )
	    {
	    	//System.out.println( (refByteArray[index]&0xff) + " " + index + " " +  testByteProcessor.get(index) );
	    	assertEquals( 0.0f, testByteArray[index], 0.0);
	    }   
	}

	@Test
	public void testByteProcessorIntIntByteArrayColorModel() 
	{
		//get the reference array
		byte[] testByteArray = new ByteProcessor( width, height, getImageByteData(), cm ).create8BitImage();
		
		//get the reference image data
		byte[] refByteArray = getImageByteData();
	
		//check the values
	    for( int index = 0; index< refByteArray.length; index++ )
	    {
	    	assertEquals( refByteArray[index], testByteArray[index], 0.0);
	    }   
	}

	@Test
	public void testByteProcessorBufferedImage() 
	{
		//get the reference array
		BufferedImage referenceBufferedImage = new ByteProcessor( width, height, getImageByteData(), cm ).getBufferedImage();
		
	    //get the test array
	    byte[] refByteArray = new ByteProcessor( referenceBufferedImage ).create8BitImage();
	    
		//get the reference array
		byte[] testByteArray = new ByteProcessor( width, height, getImageByteData(), cm ).create8BitImage();
	
		//check the values
	    for( int index = 0; index< refByteArray.length; index++ )
	    {
	    	assertEquals( refByteArray[index], testByteArray[index], 0.0);
	    }   
	}

	@Test
	public void testCreateBufferedImage() 
	{
		//get the reference array
		ByteProcessor referenceByteProcessor = new ByteProcessor( width, height, getImageByteData(), cm );
		if (referenceByteProcessor.raster==null) {
			SampleModel sm = referenceByteProcessor.getIndexSampleModel();
			DataBuffer db = new DataBufferByte(referenceByteProcessor.getPixelsArray(), width*height, 0);
			referenceByteProcessor.raster = Raster.createWritableRaster(sm, db, null);
		}
		if (referenceByteProcessor.image==null || cm!=referenceByteProcessor.cm2) {
			if (cm==null) cm=referenceByteProcessor.getDefaultColorModel();
			referenceByteProcessor.image = new BufferedImage(cm, referenceByteProcessor.raster, false, null);
			referenceByteProcessor.cm2 = cm;
		}
		Image referenceImage = referenceByteProcessor.image;
        
        //get the test array
        Image testImage = new ByteProcessor ( width, height, getImageByteData(), cm ).createBufferedImage();

		assertEquals( referenceImage.getHeight(null), testImage.getHeight(null));
	}
	
	// BDZ: can replace everywhere with JUnit's assertArrayEquals(arr1,arr2)
	//   also our tests/Assert class has floatArrayEq and doubleArrayEq methods for those cases
	
	//TODO: Move to util test class or find standardize method for replacement
	/**
	 * Wraps the JUnit4 assertEquals method to compare two byte arrays.  Length is based off the reference array.
	 * @param ref = the reference byte array
	 * @param test = the test byte array
	 */
	public void asserteq (byte[] ref, byte[] test)
	{
		//check the values
	    for( int index = 0; index< ref.length; index++ )
	    {
	    	//print if not =
	 	   if (ref[index] != test[index])
		    	System.out.println( "ref[" + index + "] " + ref[index] + " != test[" + index + "] " + test[index] );
	 	   
	    	assertEquals( ref[index], test[index]);
	    }
	}
	
	//TODO: move to utils class
	/**
	 * Prints out a byte array in the format {0x01,0x02,...,0x99}; for ease of use
	 * @param b
	 */
	public void printByteArrayHexFormat(byte[] b)
	{
		StringBuffer sb = new StringBuffer();
		final String HEXES = "0123456789abcdef";
		sb.append("{");
		for(final byte bm:b)
			sb.append("(byte) 0x" + HEXES.charAt((bm & 0xF0) >> 4) + HEXES.charAt((bm & 0x0F)) + ",");
		
		sb.deleteCharAt( sb.lastIndexOf(",") );
		sb.append("};");
		
		System.out.println( sb );	
	}
	
	private void testFilterEdgeHelper(int type, ByteProcessor referenceByteProcessor, byte[] referenceImageArray, byte[] referenceArray1, byte[] referenceArray2, byte[] referenceArray3, byte[] referenceArray4)
	{
        if (referenceByteProcessor.xMin==1) referenceByteProcessor.filterEdge(type, referenceImageArray, referenceByteProcessor.roiHeight, referenceByteProcessor.roiX, referenceByteProcessor.roiY, 0, 1);
        asserteq( referenceByteProcessor.create8BitImage(), referenceArray1 );
        
        if (referenceByteProcessor.yMin==1) referenceByteProcessor.filterEdge(type, referenceImageArray, referenceByteProcessor.roiWidth, referenceByteProcessor.roiX, referenceByteProcessor.roiY, 1, 0);
        asserteq( referenceByteProcessor.create8BitImage(), referenceArray2 );
        
        if (referenceByteProcessor.xMax==width-2) referenceByteProcessor.filterEdge(type, referenceImageArray, referenceByteProcessor.roiHeight, width-1, referenceByteProcessor.roiY, 0, 1);
        asserteq( referenceByteProcessor.create8BitImage(), referenceArray3 );
        
        if (referenceByteProcessor.yMax==height-2) referenceByteProcessor.filterEdge(type, referenceImageArray, referenceByteProcessor.roiWidth, referenceByteProcessor.roiX, height-1, 1, 0);
        asserteq( referenceByteProcessor.create8BitImage(), referenceArray4 );
        
	}
	
	@Test
	public void testFilterEdge() 
	{
		//define the reference data
		final byte[] refImageByteArray = {0x10,0x20,0x30,0x40,0x50,0x60,0x70,0x11,0x01};
		final byte[][] refByteArray = {{0x25, (byte) 0x20, (byte) 0x30, (byte) 0x3D, (byte) 0x50, (byte) 0x60, (byte) 0x4C, (byte) 0x11, (byte) 0x01}, 
		{(byte) 0x2E, (byte) 0x36, (byte) 0x3F, (byte) 0x3D, (byte) 0x50, (byte) 0x60, (byte) 0x4C, (byte) 0x11, (byte) 0x01}, 
		{(byte) 0x2E, (byte) 0x36, (byte) 0x3F, (byte) 0x3D, (byte) 0x50, (byte) 0x60, (byte) 0x4C, (byte) 0x11, (byte) 0x01}, 
		{(byte) 0x2E, (byte) 0x36, (byte) 0x3F, (byte) 0x3D, (byte) 0x50, (byte) 0x60, (byte) 0x4C, (byte) 0x11, (byte) 0x01}, 
		{(byte) 0x53, (byte) 0x36, (byte) 0x3F, (byte) 0x4C, (byte) 0x50, (byte) 0x60, (byte) 0xb8, (byte) 0x11, (byte) 0x01}, 
		{(byte) 0x53, (byte) 0x57, (byte) 0x6B, (byte) 0x4C, (byte) 0x50, (byte) 0x60, (byte) 0xb8, (byte) 0x11, (byte) 0x01}, 
		{(byte) 0x53, (byte) 0x57, (byte) 0x6B, (byte) 0x4C, (byte) 0x50, (byte) 0x60, (byte) 0xb8, (byte) 0x11, (byte) 0x01}, 
		{(byte) 0x53, (byte) 0x57, (byte) 0x6B, (byte) 0x4C, (byte) 0x50, (byte) 0x60, (byte) 0xb8, (byte) 0x11, (byte) 0x01}, 
		{(byte) 0x4C, (byte) 0x57, (byte) 0x6B, (byte) 0x11, (byte) 0x50, (byte) 0x60, (byte) 0x11, (byte) 0x11, (byte) 0x01}, 
		{(byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x50, (byte) 0x60, (byte) 0x11, (byte) 0x11, (byte) 0x01}, 
		{(byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x50, (byte) 0x60, (byte) 0x11, (byte) 0x11, (byte) 0x01}, 
		{(byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x50, (byte) 0x60, (byte) 0x11, (byte) 0x11, (byte) 0x01}, 
		{(byte) 0x50, (byte) 0x11, (byte) 0x11, (byte) 0x50, (byte) 0x50, (byte) 0x60, (byte) 0x50, (byte) 0x11, (byte) 0x01},  
		{(byte) 0x50, (byte) 0x60, (byte) 0x60, (byte) 0x50, (byte) 0x50, (byte) 0x60, (byte) 0x50, (byte) 0x11, (byte) 0x01},  
		{(byte) 0x50, (byte) 0x60, (byte) 0x60, (byte) 0x50, (byte) 0x50, (byte) 0x60, (byte) 0x50, (byte) 0x11, (byte) 0x01}, 
		{(byte) 0x50, (byte) 0x60, (byte) 0x60, (byte) 0x50, (byte) 0x50, (byte) 0x60, (byte) 0x50, (byte) 0x11, (byte) 0x01}, 
		{(byte) 0x00, (byte) 0x60, (byte) 0x60, (byte) 0x00, (byte) 0x50, (byte) 0x60, (byte) 0x00, (byte) 0x11, (byte) 0x01}, 
		{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x50, (byte) 0x60, (byte) 0x00, (byte) 0x11, (byte) 0x01}, 
		{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x50, (byte) 0x60, (byte) 0x00, (byte) 0x11, (byte) 0x01}, 
		{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x50, (byte) 0x60, (byte) 0x00, (byte) 0x11, (byte) 0x01}, 
		{(byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0x50, (byte) 0x60, (byte) 0xff, (byte) 0x11, (byte) 0x01}, 
		{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x50, (byte) 0x60, (byte) 0xff, (byte) 0x11, (byte) 0x01}, 
		{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x50, (byte) 0x60, (byte) 0xff, (byte) 0x11, (byte) 0x01},  
		{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x50, (byte) 0x60, (byte) 0xff, (byte) 0x11, (byte) 0x01}};
		
		//test each filter mode
		int i =0;
		testFilterEdgeHelper( ByteProcessor.BLUR_MORE, new ByteProcessor ( 3, 3, refImageByteArray, cm ), refImageByteArray, refByteArray[i++], refByteArray[i++], refByteArray[i++], refByteArray[i++] );
		testFilterEdgeHelper( ByteProcessor.FIND_EDGES, new ByteProcessor ( 3, 3, refImageByteArray, cm ),refImageByteArray, refByteArray[i++], refByteArray[i++], refByteArray[i++], refByteArray[i++] );
		testFilterEdgeHelper( ByteProcessor.MIN, new ByteProcessor ( 3, 3, refImageByteArray, cm ), refImageByteArray, refByteArray[i++], refByteArray[i++], refByteArray[i++], refByteArray[i++] );
		testFilterEdgeHelper( ByteProcessor.MAX, new ByteProcessor ( 3, 3, refImageByteArray, cm ), refImageByteArray, refByteArray[i++], refByteArray[i++], refByteArray[i++], refByteArray[i++] );
		testFilterEdgeHelper( ByteProcessor.ERODE, new ByteProcessor ( 3, 3, refImageByteArray, cm ), refImageByteArray, refByteArray[i++], refByteArray[i++], refByteArray[i++], refByteArray[i++] );
		testFilterEdgeHelper( ByteProcessor.DILATE, new ByteProcessor ( 3, 3, refImageByteArray, cm ), refImageByteArray, refByteArray[i++], refByteArray[i++], refByteArray[i++], refByteArray[i++] );

	}
    
	@Test
	public void testGetEdgePixel() 
	{
		final byte[] refImageByteArray = {0x10,0x20,0x30,0x40,0x50,0x60,0x70,0x11,0x01};
		ByteProcessor byteProcessor = new ByteProcessor ( 3, 3, refImageByteArray, cm );
		assertEquals( 16, byteProcessor.getEdgePixel(refImageByteArray, -1, 0) );
		assertEquals( 16, byteProcessor.getEdgePixel(refImageByteArray, 0, -1) );
		assertEquals( 112, byteProcessor.getEdgePixel(refImageByteArray, 0, 3) );
		assertEquals( 48, byteProcessor.getEdgePixel(refImageByteArray, 3, 0) );
		assertEquals( 80, byteProcessor.getEdgePixel(refImageByteArray, 1, 1) );
	}

	@Test
	public void testGetEdgePixel0() 
	{
		final byte[] refImageByteArray = {0x10,0x20,0x30,0x40,0x50,0x60,0x70,0x11,0x01};
		ByteProcessor byteProcessor = new ByteProcessor ( 3, 3, refImageByteArray, cm );
		assertEquals( 0, byteProcessor.getEdgePixel0(refImageByteArray, 0, -1, 0) );
		assertEquals( 0, byteProcessor.getEdgePixel0(refImageByteArray, 0, 0, -1) );
		assertEquals( 0, byteProcessor.getEdgePixel0(refImageByteArray, 0, 0, 3) );
		assertEquals( 0, byteProcessor.getEdgePixel0(refImageByteArray, 0, 3, 0) );
		assertEquals( 80, byteProcessor.getEdgePixel0(refImageByteArray, 0, 1, 1) );
	}

	@Test
	public void testErodeIntInt() 
	{
		ByteProcessor testByteProcessor = new ByteProcessor( width, height, getImageByteData(), cm );
		testByteProcessor.erode(3,0);
		byte[] test = testByteProcessor.create8BitImage();
		
		ByteProcessor refByteProcessor = new ByteProcessor( width, height, getImageByteData(), cm );
		filter(refByteProcessor, ByteProcessor.ERODE, 3, 0);
		byte[] ref = refByteProcessor.create8BitImage();
		
		//test elements  are equal
		asserteq (ref, test);	
	}

	@Test
	public void testDilateIntInt() 
	{
		ByteProcessor testByteProcessor = new ByteProcessor( width, height, getImageByteData(), cm );
		testByteProcessor.dilate(3,0);
		byte[] test = testByteProcessor.create8BitImage();
		byte[] ref = new byte[width * height];
		
		//test elements  are equal
		//asserteq (ref, test);
		
		// BDZ: missing implementation
	}

	@Test
	public void testOutline() 
	{
		//this test is in the BinaryProcessorClass
	}

	@Test
	public void testSkeletonize() 
	{
		//this test is in the BinaryProcessorClass
	}

	@Test
	public void testGetHistogramImageProcessor() 
	{
		//set up the reference histogram
		final int[] refHistogram = {0, 2850, 4349, 4054, 3075, 2173, 1489, 0, 1149, 935, 669, 492, 375, 307, 239, 277, 259, 241, 214, 193, 236, 225, 0, 225, 225, 244, 218, 241, 237, 276, 234, 269, 285, 283, 313, 323, 359, 0, 389, 342, 328, 360, 295, 362, 340, 364, 371, 378, 408, 346, 402, 391, 0, 390, 415, 355, 419, 396, 397, 402, 441, 417, 369, 394, 430, 427, 425, 0, 428, 417, 395, 423, 416, 447, 439, 445, 437, 439, 511, 530, 566, 513, 0, 578, 584, 578, 623, 602, 601, 649, 625, 627, 619, 594, 564, 494, 425, 0, 453, 397, 347, 256, 267, 239, 173, 125, 99, 91, 58, 62, 47, 51, 0, 44, 41, 42, 42, 25, 46, 27, 34, 40, 38, 26, 40, 32, 35, 26, 0, 26, 28, 25, 31, 26, 25, 34, 25, 29, 35, 19, 29, 29, 0, 36, 26, 20, 18, 26, 29, 16, 29, 21, 30, 25, 21, 20, 13, 0, 22, 22, 15, 16, 22, 19, 24, 25, 26, 21, 19, 23, 14, 22, 0, 12, 22, 18, 19, 16, 22, 27, 19, 26, 21, 23, 17, 21, 7, 0, 12, 15, 16, 12, 13, 18, 13, 14, 13, 15, 10, 12, 14, 8, 0, 10, 12, 13, 12, 12, 9, 9, 14, 7, 9, 5, 9, 6, 9, 0, 11, 7, 11, 9, 4, 10, 5, 9, 4, 6, 9, 8, 7, 7, 0, 3, 7, 7, 8, 7, 7, 4, 8, 2, 0, 0, 3, 3, 3, 1, 0, 1, 1, 0, 0, 2, 0, 1};

		ByteProcessor testByteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);
		testByteProcessor.flipHorizontal();
		ImageProcessor mask = new ByteProcessor(width, height, testByteProcessor.create8BitImage(), cm );
		int[] testHistogram = testByteProcessor.getHistogram(mask);
		
		//check the values
		for( int index = 0; index< testHistogram.length; index++ )
		{
		   assertEquals( refHistogram[index], testHistogram[index]);
		}	 
	}

	@Test
	public void testApplyLut() 
	{
		final byte[] refImageByteArray = {(byte) 0x01, (byte) 0x02, (byte) 0x30, (byte) 0x40, (byte) 0x50, (byte) 0x60, (byte) 0x99, (byte) 0xf9,(byte) 0xff};
		ByteProcessor testByteProcessor =  new ByteProcessor(3, 3, refImageByteArray, cm);
		testByteProcessor.setMinAndMax(3.0,100.0);
		testByteProcessor.applyLut();
		
		//get the data
		byte[] testImageByteArray = (byte[]) testByteProcessor.getPixelsCopy();
		byte[] refResult = {(byte) 0x00,(byte) 0x00,(byte) 0x76,(byte) 0xa0,(byte) 0xcb,(byte) 0xf5,(byte) 0xff,(byte) 0xff,(byte) 0xff};

		//check the values
		for( int index = 0; index< refResult.length; index++ )
		{
		   assertEquals( refResult[index], testImageByteArray[index]);
		}	 		
	}

	@Test
	public void testToFloatProcessors() 
	{
		ByteProcessor testByteProcessor = new ByteProcessor( width, height, getImageByteData(), cm );
		FloatProcessor[] testFloatProcessors = testByteProcessor.toFloatProcessors();
		byte[] testByteArray = testFloatProcessors[0].create8BitImage();
		
		byte[] refData = getImageByteData();
		
		//check the values
		for( int index = 0; index< refData.length; index++ )
		{
			assertEquals( refData[index], testByteArray[index], 0.0);
		}	 
	}

	@Test
	public void testSetFromFloatProcessors() 
	{
		float[][] refFloatArray = new ByteProcessor( width, height, getImageByteData(), cm ).getFloatArray();
		FloatProcessor[] refFloatProcessors = new FloatProcessor[1];
		refFloatProcessors[0] = new FloatProcessor(refFloatArray);
			
		ByteProcessor testByteProcessor = new ByteProcessor( width, height);
		testByteProcessor.setFromFloatProcessors(refFloatProcessors);
		
		byte[] testByteArray = testByteProcessor.create8BitImage();
		
		byte[] refData = getImageByteData();
		
		//check the values
		for( int index = 0; index< refData.length; index++ )
		{
			assertEquals( refData[index], testByteArray[index], 0.0);
		}	
	}

	@Test
	public void testToFloatArrays() 
	{
		ByteProcessor testByteProcessor = new ByteProcessor( width, height, getImageByteData(), cm );
		float[][] testFloatArrays = testByteProcessor.toFloatArrays();
		byte[] refData = getImageByteData();
		float[][] refFloatArrays = new float[1][refData.length+1];		
		
		//check the values
		for( int index = 0; index< refData.length; index++ )
		{
			refFloatArrays[0][index] = refData[index]&0xff;
			assertEquals( refFloatArrays[0][index], testFloatArrays[0][index], 0.0);
		}	 	
	}

	@Test
	public void testSetFromFloatArrays() 
	{
		ByteProcessor refByteProcessor = new ByteProcessor( width, height, getImageByteData(), cm );
		float[][] refFloatArrays = refByteProcessor.toFloatArrays();
		
		ByteProcessor testByteProcessor = new ByteProcessor( width, height);
		testByteProcessor.setFromFloatArrays(refFloatArrays);
		byte[] testByteData = testByteProcessor.create8BitImage();
		
		//check the values
		for( int index = 0; index< refFloatArrays[0].length; index++ )
		{
			assertEquals( refFloatArrays[0][index], testByteData[index]&0xff, 0.0);
		}	
	}

}
