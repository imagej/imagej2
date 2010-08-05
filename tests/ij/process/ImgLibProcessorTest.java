package ij.process;

import static org.junit.Assert.*;
import org.junit.Before;

import ij.Assert;
import ij.ImagePlus;

import mpicbg.imglib.container.ContainerFactory;
import mpicbg.imglib.container.array.ArrayContainerFactory;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.io.LOCI;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;

import org.junit.Test;

public class ImgLibProcessorTest {

	// ************* Instance variables ***********************************************
	
	int width;
	int height;
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

	private void compareData(ImageProcessor bProc, ImageProcessor iProc)
	{
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
		width = bProc.getWidth();
		height = bProc.getHeight();
		
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
		//fail("Not yet implemented");
	}

	@Test
	public void testConvolve() {
		//fail("Not yet implemented");
	}

	@Test
	public void testConvolve3x3() {
		//fail("Not yet implemented");
	}

	@Test
	public void testCopyBits() {
		//fail("Not yet implemented");
	}

	@Test
	public void testCreate8BitImage() {
		//fail("Not yet implemented");
	}

	@Test
	public void testCreateImage() {
		assertNotNull(iProc.createImage());
	}

	@Test
	public void testCreateImagePlus() {
		//fail("Not yet implemented");
	}

	@Test
	public void testCreateProcessor() {
		//fail("Not yet implemented");
	}

	@Test
	public void testCrop() {
		//fail("Not yet implemented");
	}

	@Test
	public void testDilate() {
		//fail("Not yet implemented");
	}

	@Test
	public void testDisplay() {
		//fail("Not yet implemented");
	}

	@Test
	public void testDrawPixel() {
		//fail("Not yet implemented");
	}

	@Test
	public void testDuplicate() {
		//fail("Not yet implemented");
	}

	@Test
	public void testErode() {
		//fail("Not yet implemented");
	}

	@Test
	public void testFillImageProcessor() {
		//fail("Not yet implemented");
	}

	@Test
	public void testFilter() {
		//fail("Not yet implemented");
	}

	@Test
	public void testFlipVertical() {
		iProc.flipVertical();
		bProc.flipVertical();
		compareData(bProc,iProc);
	}

	@Test
	public void testGetBackgroundValue() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetfInt() {

		int maxPixels = width*height;
		
		assertEquals(bProc.getf(0*maxPixels/5), iProc.getf(0*maxPixels/5), Assert.FLOAT_TOL);
		assertEquals(bProc.getf(1*maxPixels/5), iProc.getf(1*maxPixels/5), Assert.FLOAT_TOL);
		assertEquals(bProc.getf(2*maxPixels/5), iProc.getf(2*maxPixels/5), Assert.FLOAT_TOL);
		assertEquals(bProc.getf(3*maxPixels/5), iProc.getf(3*maxPixels/5), Assert.FLOAT_TOL);
		assertEquals(bProc.getf(4*maxPixels/5), iProc.getf(4*maxPixels/5), Assert.FLOAT_TOL);
	}

	@Test
	public void testGetfIntInt() {
		
		assertEquals(bProc.getf(0,0), iProc.getf(0,0), Assert.FLOAT_TOL);
		assertEquals(bProc.getf(width-1,0), iProc.getf(width-1,0), Assert.FLOAT_TOL);
		assertEquals(bProc.getf(0,height-1), iProc.getf(0,height-1), Assert.FLOAT_TOL);
		assertEquals(bProc.getf(width-1,height-1), iProc.getf(width-1,height-1), Assert.FLOAT_TOL);
		assertEquals(bProc.getf(width/2,height/2), iProc.getf(width/2,height/2), Assert.FLOAT_TOL);
	}

	@Test
	public void testGetHistogram() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetInt() {

		int maxPixels = width*height;
		
		assertEquals(bProc.get(0*maxPixels/5), iProc.get(0*maxPixels/5), Assert.FLOAT_TOL);
		assertEquals(bProc.get(1*maxPixels/5), iProc.get(1*maxPixels/5), Assert.FLOAT_TOL);
		assertEquals(bProc.get(2*maxPixels/5), iProc.get(2*maxPixels/5), Assert.FLOAT_TOL);
		assertEquals(bProc.get(3*maxPixels/5), iProc.get(3*maxPixels/5), Assert.FLOAT_TOL);
		assertEquals(bProc.get(4*maxPixels/5), iProc.get(4*maxPixels/5), Assert.FLOAT_TOL);
	}

	@Test
	public void testGetIntInt() {
		assertEquals(bProc.get(0,0), iProc.get(0,0), Assert.FLOAT_TOL);
		assertEquals(bProc.get(width-1,0), iProc.get(width-1,0), Assert.FLOAT_TOL);
		assertEquals(bProc.get(0,height-1), iProc.get(0,height-1), Assert.FLOAT_TOL);
		assertEquals(bProc.get(width-1,height-1), iProc.get(width-1,height-1), Assert.FLOAT_TOL);
		assertEquals(bProc.get(width/2,height/2), iProc.get(width/2,height/2), Assert.FLOAT_TOL);
	}

	@Test
	public void testGetInterpolatedPixel() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetMax() {
		assertEquals(bProc.getMax(), iProc.getMax(), Assert.DOUBLE_TOL);
	}

	@Test
	public void testGetMin() {
		assertEquals(bProc.getMin(), iProc.getMin(), Assert.DOUBLE_TOL);
	}

	@Test
	public void testGetPixelInterpolated() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetPixelIntInt() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetPixels() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetPixelsArray() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetPixelsCopy() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetPixelValue() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneData() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneDataImageOfTIntIntIntArray() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneBytes() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneUnsignedBytes() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneShorts() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneUnsignedShorts() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneInts() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneUnsignedInts() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneLongs() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneFloats() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetPlaneDoubles() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetSnapshotPixels() {
		//fail("Not yet implemented");
	}

	@Test
	public void testImgLibProcessor() {
		assertNotNull(iProc);
	}

	@Test
	public void testMedianFilter() {
		//fail("Not yet implemented");
	}

	@Test
	public void testNoise() {
		//fail("Not yet implemented");
	}

	@Test
	public void testPutPixelIntIntInt() {
		//fail("Not yet implemented");
	}

	@Test
	public void testPutPixelValue() {
		//fail("Not yet implemented");
	}

	@Test
	public void testReset() {
		//fail("Not yet implemented");
	}

	@Test
	public void testResetImageProcessor() {
		//fail("Not yet implemented");
	}

	@Test
	public void testResizeIntInt() {
		//fail("Not yet implemented");
	}

	@Test
	public void testRotate() {
		//fail("Not yet implemented");
	}

	@Test
	public void testScale() {
		//fail("Not yet implemented");
	}

	@Test
	public void testSetBackgroundValue() {
		//fail("Not yet implemented");
	}

	@Test
	public void testSetColorColor() {

		//fail("Not yet implemented");
	}

	@Test
	public void testSetfIntFloat() {
		int maxPixels = width*height;
		
		// set the ByteProcessor
		bProc.setf(0*maxPixels/5, 14.1f);
		bProc.setf(1*maxPixels/5, 15.2f);
		bProc.setf(2*maxPixels/5, 16.3f);
		bProc.setf(3*maxPixels/5, 17.4f);
		bProc.setf(4*maxPixels/5, 18.5f);

		// set the ImgLibProcessor
		iProc.setf(0*maxPixels/5, 14.1f);
		iProc.setf(1*maxPixels/5, 15.2f);
		iProc.setf(2*maxPixels/5, 16.3f);
		iProc.setf(3*maxPixels/5, 17.4f);
		iProc.setf(4*maxPixels/5, 18.5f);
		
		compareData(bProc,iProc);
	}

	@Test
	public void testSetfIntIntFloat() {
		
		// set the ByteProcessor
		bProc.setf(0, 0, 11.1f);
		bProc.setf(0, height-1, 22.3f);
		bProc.setf(width-1, 0, 33.5f);
		bProc.setf(width-1, height-1, 44.7f);
		bProc.setf(width/2,height/2, 55.9f);

		// set the ImgLibProcessor
		iProc.setf(0, 0, 11.1f);
		iProc.setf(0, height-1, 22.3f);
		iProc.setf(width-1, 0, 33.5f);
		iProc.setf(width-1, height-1, 44.7f);
		iProc.setf(width/2,height/2, 55.9f);
		
		compareData(bProc,iProc);
	}

	@Test
	public void testSetIntInt() {
		
		int maxPixels = width*height;
		
		// set the ByteProcessor
		bProc.set(0*maxPixels/5, 50);
		bProc.set(1*maxPixels/5, 60);
		bProc.set(2*maxPixels/5, 70);
		bProc.set(3*maxPixels/5, 80);
		bProc.set(4*maxPixels/5, 90);

		// set the ImgLibProcessor
		iProc.set(0*maxPixels/5, 50);
		iProc.set(1*maxPixels/5, 60);
		iProc.set(2*maxPixels/5, 70);
		iProc.set(3*maxPixels/5, 80);
		iProc.set(4*maxPixels/5, 90);
		
		compareData(bProc,iProc);
	}

	@Test
	public void testSetIntIntInt() {
		
		// set the ByteProcessor
		bProc.set(0, 0, 50);
		bProc.set(0, height-1, 60);
		bProc.set(width-1, 0, 70);
		bProc.set(width-1, height-1, 80);
		bProc.set(width/2,height/2,90);

		// set the ImgLibProcessor
		iProc.set(0, 0, 50);
		iProc.set(0, height-1, 60);
		iProc.set(width-1, 0, 70);
		iProc.set(width-1, height-1, 80);
		iProc.set(width/2,height/2,90);
		
		compareData(bProc,iProc);
	}

	@Test
	public void testSetMinAndMax() {
		//fail("Not yet implemented");
	}

	@Test
	public void testSetPixelsIntFloatProcessor() {
		//fail("Not yet implemented");
	}

	@Test
	public void testSetPixelsObject() {
		//fail("Not yet implemented");
	}

	@Test
	public void testSetSnapshotPixels() {
		//fail("Not yet implemented");
	}

	@Test
	public void testSetValue() {
		//fail("Not yet implemented");
	}

	@Test
	public void testSnapshot() {
		//fail("Not yet implemented");
	}

	@Test
	public void testThreshold() {
		//fail("Not yet implemented");
	}

	@Test
	public void testToFloat() {
		FloatProcessor bFloat = bProc.toFloat(0, null);
		FloatProcessor iFloat = iProc.toFloat(0, null);
		
		compareData(bFloat,iFloat);
	}
}
