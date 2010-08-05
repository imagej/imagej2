package ij.process;

import static org.junit.Assert.*;
import org.junit.Before;

import ij.ImagePlus;

import mpicbg.imglib.container.ContainerFactory;
import mpicbg.imglib.container.array.ArrayContainerFactory;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.io.LOCI;
import mpicbg.imglib.type.numeric.integer.ByteType;
import mpicbg.imglib.type.numeric.integer.IntType;
import mpicbg.imglib.type.numeric.integer.ShortType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import mpicbg.imglib.type.numeric.integer.UnsignedIntType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;
import mpicbg.imglib.type.numeric.real.DoubleType;
import mpicbg.imglib.type.numeric.real.FloatType;

import org.junit.Test;

public class ImgLibProcessorTest {

	// ************* Instance variables ***********************************************
	
	//Image<UnsignedByteType>  ubImage;
	//Image<ByteType>          bImage;
	//Image<UnsignedShortType> usImage;
	//Image<ShortType>         sImage;
	//Image<UnsignedIntType>   uiImage;
	//Image<IntType>           iImage;
	//Image<FloatType>         fImage;
	//Image<DoubleType>        dImage;
	
	ImgLibProcessor<UnsignedByteType> iProc;
	ByteProcessor bProc;

	// ************* Helper methods ***********************************************

	/*

	private byte[] getRow(ImgLibProcessor<UnsignedByteType> proc, int r)
	{
		int cols = proc.getWidth();
		
		byte[] row = new byte[cols];
		
		for (int col = 0; col < cols; col++)
		{
			row[col] = (byte) proc.get(col, r);
		}
		
		return row;
	}
	
	private byte[] getCol(ImgLibProcessor<UnsignedByteType> proc, int c)
	{
		int rows = proc.getHeight();
		
		byte[] column = new byte[rows];
		
		for (int row = 0; row < rows; row++)
		{
			column[row] = (byte) proc.get(c, row);
		}
		
		return column;
	}

	*/
	
	// ************* Helper tests ***********************************************

	private void compareData(ByteProcessor bProc, ImgLibProcessor<UnsignedByteType> iProc)
	{
		int width = bProc.getWidth();
		int height = bProc.getHeight();
		
		assertEquals(height,iProc.getHeight());
		assertEquals(width,iProc.getWidth());
	
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				if (bProc.get(x,y) != iProc.get(x,y))
					fail("processor data differs at ("+x+","+y+") : ij(" + bProc.get(x,y) +") imglib("+iProc.get(x,y)+")");
	}
	
	@Before
	public void initialize()
	{
		String filename = "data/head8bit.tif";
		
		ImagePlus imp = new ImagePlus(filename);
		bProc = (ByteProcessor) imp.getProcessor();
		
		final ContainerFactory containerFactory = new ArrayContainerFactory();
		Image<UnsignedByteType> image = LOCI.openLOCIUnsignedByteType(filename, containerFactory);
		iProc = new ImgLibProcessor<UnsignedByteType>(image, new UnsignedByteType());
		
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
	public void testBoundIntValueToType() {
		fail("Not yet implemented");
	}

	@Test
	public void testConvolve() {
		fail("Not yet implemented");
	}

	@Test
	public void testConvolve3x3() {
		fail("Not yet implemented");
	}

	@Test
	public void testCopyBits() {
		fail("Not yet implemented");
	}

	@Test
	public void testCreate8BitImage() {
		fail("Not yet implemented");
	}

	@Test
	public void testCreateImage() {
		fail("Not yet implemented");
	}

	@Test
	public void testCreateImagePlus() {
		fail("Not yet implemented");
	}

	@Test
	public void testCreateProcessor() {
		fail("Not yet implemented");
	}

	@Test
	public void testCrop() {
		fail("Not yet implemented");
	}

	@Test
	public void testDilate() {
		fail("Not yet implemented");
	}

	@Test
	public void testDisplay() {
		fail("Not yet implemented");
	}

	@Test
	public void testDrawPixel() {
		fail("Not yet implemented");
	}

	@Test
	public void testDuplicate() {
		fail("Not yet implemented");
	}

	@Test
	public void testErode() {
		fail("Not yet implemented");
	}

	@Test
	public void testFillImageProcessor() {
		fail("Not yet implemented");
	}

	@Test
	public void testFilter() {
		fail("Not yet implemented");
	}

	@Test
	public void testFlipVertical() {
		iProc.flipVertical();
		bProc.flipVertical();
		compareData(bProc,iProc);
	}

	@Test
	public void testGetBackgroundValue() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetfInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetfIntInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetHistogram() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetIntInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetInterpolatedPixel() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetMax() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetMin() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPixelInterpolated() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPixelIntInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPixels() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPixelsArray() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPixelsCopy() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPixelValue() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneData() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneDataImageOfTIntIntIntArray() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneBytes() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneUnsignedBytes() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneShorts() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneUnsignedShorts() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneInts() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneUnsignedInts() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneLongs() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneFloats() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneDoubles() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetSnapshotPixels() {
		fail("Not yet implemented");
	}

	@Test
	public void testImgLibProcessor() {
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
	public void testPutPixelIntIntInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testPutPixelValue() {
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
	public void testResizeIntInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testRotate() {
		fail("Not yet implemented");
	}

	@Test
	public void testScale() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetBackgroundValue() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetColorColor() {

		fail("Not yet implemented");
	}

	@Test
	public void testSetfIntFloat() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetfIntIntFloat() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetIntInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetIntIntInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetMinAndMax() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetPixelsIntFloatProcessor() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetPixelsObject() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetSnapshotPixels() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetValue() {
		fail("Not yet implemented");
	}

	@Test
	public void testSnapshot() {
		fail("Not yet implemented");
	}

	@Test
	public void testThreshold() {
		fail("Not yet implemented");
	}

	@Test
	public void testToFloat() {
		fail("Not yet implemented");
	}
}
