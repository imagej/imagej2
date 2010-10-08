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
