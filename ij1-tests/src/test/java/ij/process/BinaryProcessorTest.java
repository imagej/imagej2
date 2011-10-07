//
// BinaryProcessorTest.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package ij.process;

import static org.junit.Assert.assertEquals;
import ij.ImagePlus;
import ij.io.Opener;

import java.awt.image.ColorModel;

import org.junit.BeforeClass;
import org.junit.Test;

public class BinaryProcessorTest {

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
    String id = DataConstants.DATA_DIR + "head8bit.tif";
    ImagePlus imp = new Opener().openImage(id);
    width = imp.getWidth();
    height = imp.getHeight();
    imageByteData = (byte[]) imp.getProcessor().getPixels();

    //assign the color model
    cm = imp.getProcessor().getColorModel();
  }

  public byte[] getImageByteData()
	{
		return imageByteData.clone();
	}

	@Test
	public void testOutline()
	{
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);
		//ColorProcessorTest.displayGraphicsInNewJFrame(byteProcessor.getBufferedImage(), "source image", 3000);
		BinaryProcessor testBinaryProcessor = new BinaryProcessor( byteProcessor );
		testBinaryProcessor.outline();

		//ColorProcessorTest.displayGraphicsInNewJFrame(testBinaryProcessor.getBufferedImage(), "outline", 3000);
		byte[] resultPixelsArray = (byte[]) testBinaryProcessor.getPixels();

		assertEquals( -126, resultPixelsArray[134] );
	}

	@Test
	public void testSkeletonize()
	{
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);

		BinaryProcessor testBinaryProcessor = new BinaryProcessor( byteProcessor );
		testBinaryProcessor.skeletonize();

		byte[] resultPixelsArray = (byte[]) testBinaryProcessor.getPixels();

		assertEquals( 48, resultPixelsArray[29297] );
		//ColorProcessorTest.displayGraphicsInNewJFrame(testBinaryProcessor.getBufferedImage(), "skeletonize", 3000);
	}

	@Test
	public void testBinaryProcessor()
	{
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);

		BinaryProcessor testBinaryProcessor = new BinaryProcessor( byteProcessor );

		//assert dimensions, cm, and parent reference is the same
		assertEquals( testBinaryProcessor.getWidth(), byteProcessor.getWidth() );
		assertEquals( testBinaryProcessor.getHeight(), byteProcessor.getHeight() );
		assertEquals( testBinaryProcessor.getColorModel(), byteProcessor.getColorModel() );
		}

	@Test
	public void testProcess()
	{
		//Create a new ByteProcessor object for testing
		ByteProcessor byteProcessor =  new ByteProcessor(width, height, getImageByteData(), cm);

		BinaryProcessor testBinaryProcessor = new BinaryProcessor( byteProcessor );

		//OUTLINE
		testBinaryProcessor.process( BinaryProcessor.OUTLINE, 1);
		final byte[] resultPixelsArray = (byte[]) testBinaryProcessor.getPixels();

		assertEquals( -126, resultPixelsArray[134] );

		//ColorProcessorTest.displayGraphicsInNewJFrame(testBinaryProcessor.getBufferedImage(), "outline", 3000);
	}

	@Test
	public void testThin()
	{
		//not a public method...
	}

}
