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

// TODO - add comparisons to FloatProcessor and ShortProcessor also

public class ImgLibProcessorTest {

	// ************* Instance variables ***********************************************
	
	int width;
	int height;
	ImgLibProcessor<UnsignedByteType> iProc;
	ByteProcessor bProc;
	FloatProcessor fProc;
	ShortProcessor sProc;
	ColorProcessor cProc;  // may not need to test this for comparison

	// ************* Helper methods ***********************************************

	
	// ************* Helper tests ***********************************************

	private void compareData(int w, int h, ImageProcessor bProc, ImageProcessor iProc)
	{
		assertEquals(h,bProc.getHeight());
		assertEquals(w,bProc.getWidth());
		
		assertEquals(h,iProc.getHeight());
		assertEquals(w,iProc.getWidth());
	
		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++)
				if (Math.abs(bProc.getf(x,y)-iProc.getf(x,y)) > Assert.FLOAT_TOL)
					fail("processor data differs at ("+x+","+y+") : ij(" + bProc.getf(x,y) +") imglib("+iProc.getf(x,y)+")");
	}

	// the following initialization code runs before every test
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
		
		compareData(width,height,bProc,iProc);
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
		compareData(width,height,bProc,iProc);
	}

	@Test
	public void testConvolve() {
		
		/* TODO - enable
		float[][] kernel2d = new float[][] {{-1.2f,-1.2f,-1.2f,-1.2f,-1.2f},
											{-1.2f,-2.4f,-2.4f,-2.4f,-1.2f},
											{-1.2f,-2.4f,38.4f,-2.4f,-1.2f},
											{-1.2f,-2.4f,-2.4f,-2.4f,-1.2f},
											{-1.2f,-1.2f,-1.2f,-1.2f,-1.2f}};

		int height = kernel2d.length;
		int width = kernel2d[0].length;
		
		float[] kernel = new float[height * width];
		int i = 0;
		for (int x=0; x < width; x++)
			for (int y=0; y < height; y++)
				kernel[i++] = kernel2d[x][y];
		
		bProc.convolve(kernel, width, height);
		iProc.convolve(kernel, width, height);
		compareData(bProc,iProc);
		*/
	}

	@Test
	public void testConvolve3x3() {
		
		/* TODO - enable
		int[][] kernel2d = new int[][] {{1,3,1},{3,-16,3},{1,3,1}};

		int height = kernel2d.length;
		int width = kernel2d[0].length;
		
		int[] kernel = new int[height * width];
		int i = 0;
		for (int x=0; x < width; x++)
			for (int y=0; y < height; y++)
				kernel[i++] = kernel2d[x][y];
		
		bProc.convolve3x3(kernel);
		iProc.convolve3x3(kernel);
		compareData(bProc,iProc);
		*/
	}

	@Test
	public void testCopyBits() {
		
		/* TODO - enable
		ImageProcessor data = new ByteProcessor(5,5,new byte[]{-1,1,-2,2,-3,3,1,2,3,4,5,6,7,8,11,10,9,8,7,6,5,4,3,2,1},null);
		
		for (int mode = ImageProcessor.INVERT; mode <= ImageProcessor.ABS; mode++)
		{
			bProc.copyBits(data, 23, 19, mode);
			iProc.copyBits(data, 23, 19, mode);
			compareData(bProc,iProc);
		}
		*/
	}

	@Test
	public void testCreate8BitImage() {
		//fail("Not yet implemented");
	}

	@Test
	public void testCreateImage() {
		java.awt.Image bImage = bProc.createImage(); 
		java.awt.Image iImage = iProc.createImage(); 

		assertNotNull(bImage);
		assertNotNull(iImage);
		
		// TODO - do some other kind of comparisons?
	}

	@Test
	public void testCreateImagePlus() {
		// TODO - test if we keep it around
	}

	@Test
	public void testCreateProcessor() {
		
		int width = 73;
		int height = 22;
		
		ImageProcessor newProc = iProc.createProcessor(width,height);
		
		assertEquals(width,newProc.getWidth());
		assertEquals(height,newProc.getHeight());
		
		assertEquals(iProc.getMin(),newProc.getMin(),Assert.DOUBLE_TOL);
		/* TODO - enable when min/max calc working
		assertEquals(iProc.getMax(),newProc.getMax(),Assert.DOUBLE_TOL);
		*/
		assertEquals(iProc.getColorModel(),newProc.getColorModel());
		assertEquals(iProc.getInterpolate(),newProc.getInterpolate());
	}

	@Test
	public void testCrop() {
	
		int ox = 22;
		int oy = 53;
		int w = 107;
		int h = 214;
		
		bProc.setRoi(ox,oy,w,h);
		iProc.setRoi(ox,oy,w,h);
		
		ImageProcessor baseline = bProc.crop();
		ImageProcessor result = iProc.crop();
		
		compareData(baseline.getWidth(),baseline.getHeight(),baseline,result);
	}

	@Test
	public void testDilate() {
		
		/* TODO - enable
		iProc.dilate();
		bProc.dilate();
		compareData(bProc,iProc);

		iProc.dilate();
		bProc.dilate();
		compareData(bProc,iProc);

		iProc.dilate();
		bProc.dilate();
		compareData(bProc,iProc);
		*/
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

		/* TODO - enable
		iProc.erode();
		bProc.erode();
		compareData(bProc,iProc);

		iProc.erode();
		bProc.erode();
		compareData(bProc,iProc);

		iProc.erode();
		bProc.erode();
		compareData(bProc,iProc);
		*/
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
		compareData(width,height,bProc,iProc);
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
	public void testResetImageProcessor() {

		ByteProcessor mask = new ByteProcessor(7, 7);
		for (int x = 0; x < 7; x++)
			for (int y = 0; y < 7; y++)
				mask.set(x,y,(x+y)%2);
		
		bProc.setRoi(1, 2, 7, 7);
		iProc.setRoi(1, 2, 7, 7);
		
		bProc.snapshot();
		iProc.snapshot();
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				bProc.set(x,y,(x+y)%256);
				iProc.set(x,y,(x+y)%256);
			}
		}
		
		compareData(width,height,bProc,iProc);

		bProc.reset(mask);
		iProc.reset(mask);
		
		compareData(width,height,bProc,iProc);
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
		
		compareData(width,height,bProc,iProc);
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
		
		compareData(width,height,bProc,iProc);
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
		
		compareData(width,height,bProc,iProc);
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
		
		compareData(width,height,bProc,iProc);
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
	public void testSnapshotAndReset() {
		
		bProc.snapshot();
		iProc.snapshot();
	
		for (int i = 0; i < width*height; i++)
		{
			bProc.set(i, i%256);
			iProc.set(i, i%256);
		}
		
		compareData(width,height,bProc,iProc);
		
		bProc.reset();
		iProc.reset();
		
		compareData(width,height,bProc,iProc);
	}

	@Test
	public void testThreshold() {
		//fail("Not yet implemented");
	}

	@Test
	public void testToFloat() {
		
		FloatProcessor bFloat, iFloat;
		
		bFloat = bProc.toFloat(0, null);
		iFloat = iProc.toFloat(0, null);
		
		compareData(width,height,bProc,iProc);

		bFloat = bProc.toFloat(1, null);
		iFloat = iProc.toFloat(1, null);
		
		compareData(width,height,bProc,iProc);
	}
}
