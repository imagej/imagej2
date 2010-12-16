package ij.process;

import static org.junit.Assert.assertEquals;
import ij.gui.ProgressBar;
import ij.gui.Roi;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.junit.BeforeClass;
import org.junit.Test;

public class ImageProcessorTest
{
	public static int width;
	public static int height;
	private static byte[] rawByte;
	private static int[] imageIntData;



	@Test
	public void testInvertLut() {
		ImageProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		ImageProcessor testByteProcessor = testColorProcessor.convertToByte(false);
		testByteProcessor.invertLut();
		//displayGraphicsInNewJFrame(testByteProcessor.getBufferedImage(), "InvertLut", 3000);
		testImageStats( testByteProcessor, "stats[count=64000, mean=71.654640625, min=0.0, max=248.0] 148.8276150772684 72.22557495294814 160.0 100.0");
	}

	@Test
	public void testGetBestIndex()
	{
		ImageProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		int test = testColorProcessor.getBestIndex(new Color(1,128,255));
		assertEquals( 0 , test);
		test = testColorProcessor.getBestIndex(new Color(128,128,128));
		assertEquals( 0 , test);
		test = testColorProcessor.getBestIndex(new Color(220,220,220));
		assertEquals( 0 , test);
	}

	@Test
	public void testIsInvertedLut()
	{
		ImageProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		ImageProcessor testByteProcessor = testColorProcessor.convertToByte(false);
		testByteProcessor.invertLut();
		assertEquals( true, testByteProcessor.isInvertedLut() );
	}


	@Test
	public void testSetColorColor()
	{
		//abstract

	}

	@Test
	public void testSetColorInt() {
		//abstract
	}

	@Test
	public void testSetValue() {
		//abstract
	}

	@Test
	public void testSetBackgroundValue() {
		//abstract
	}

	@Test
	public void testGetBackgroundValue() {
		//abstract
	}

	@Test
	public void testGetMin() {
		//abstract
	}

	@Test
	public void testGetMax() {
		//abstract
	}

	@Test
	public void testSetMinAndMax() {
		//abstract
	}



	@Test
	public void testFindEdges() {
		//abstract
	}

	@Test
	public void testFlipVertical() {
		//abstract
	}



	@Test
	public void testFill() {
		ImageProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		testColorProcessor.fill();
		testImageStats( testColorProcessor, "stats[count=64000, mean=0.0, min=0.0, max=0.0] NaN NaN 160.0 100.0");

	}

	@Test
	public void testFillImageProcessor()
	{
		//abstract
	}

	@Test
	public void testGetPixels()
	{
		//abstract
	}

	@Test
	public void testGetPixelsCopy() {
		//abstract
	}

	@Test
	public void testGetPixelIntInt() {
		//abstract
	}


	@Test
	public void testGetIntInt() {
		//abstract
	}

	@Test
	public void testGetInt() {
		//abstract
	}

	@Test
	public void testSetIntIntInt() {
		//abstract
	}

	@Test
	public void testSetIntInt() {
		//abstract
	}

	@Test
	public void testGetfIntInt() {
		//abstract
	}

	@Test
	public void testGetfInt() {
		//abstract
	}

	@Test
	public void testSetfIntIntFloat() {
		//abstract
	}

	@Test
	public void testSetfIntFloat() {
		//abstract
	}


	@Test
	public void testGetPixelIntIntIntArray() {
		ImageProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		ImageProcessor testByteProcessor = testColorProcessor.convertToByte(false);
		int[] i = testByteProcessor.getPixel(0, 10, null);
		assertEquals( 125, i[0]);
	}

	@Test
	public void testPutPixelIntIntIntArray() {
		ImageProcessor testColorProcessor = new ColorProcessor(width, height  );
		int[] i = { 99 };
		ImageProcessor testByteProcessor = testColorProcessor.convertToByte(false);
		 testByteProcessor.putPixel(0, 10, i);
		 int[] r = testByteProcessor.getPixel(0, 10, null);
		assertEquals( 99, r[0] );
	}

	@Test
	public void testGetInterpolatedPixel() {
		//abstract
	}

	@Test
	public void testGetPixelInterpolated() {
		//abstract
	}


	@Test
	public void testPutPixelIntIntInt() {
		//abstract
	}

	@Test
	public void testGetPixelValue() {
		//abstract
	}

	@Test
	public void testPutPixelValue() {
		//abstract
	}

	@Test
	public void testDrawPixel() {
		//abstract
	}

	@Test
	public void testSetPixelsObject() {
		//abstract
	}

	@Test
	public void testCopyBits() {
		//abstract
	}

	@Test
	public void testApplyTable() {
		//abstract
	}


	@Test
	public void testCreateImage() {
		//abstract
	}


	@Test
	public void testCreateProcessor() {
		//abstract
	}

	@Test
	public void testSnapshot() {
		//abstract
	}

	@Test
	public void testReset() {
		//abstract
	}

	@Test
	public void testResetImageProcessor() {
		//abstract
	}

	@Test
	public void testSetSnapshotPixels() {
		//abstract
	}

	@Test
	public void testGetSnapshotPixels() {
		//abstract
	}

	@Test
	public void testConvolve3x3() {
		//abstract
	}

	@Test
	public void testFilter() {
		//abstract
	}

	@Test
	public void testMedianFilter() {
		//abstract
	}

	@Test
	public void testNoise() {
		//abstract
	}

	@Test
	public void testCrop() {
		//abstract
	}

	@Test
	public void testThreshold() {
		//abstract
	}

	@Test
	public void testDuplicate() {
		//abstract
	}

	@Test
	public void testScale() {
		//abstract
	}

	@Test
	public void testResizeIntInt() {
		//abstract
	}


	@Test
	public void testRotate() {
		//abstract
	}


	@Test
	public void testGetHistogram() {
		//abstract
	}

	@Test
	public void testErode() {
		//abstract
	}

	@Test
	public void testDilate() {
		//abstract
	}

	@Test
	public void testConvolve() {
		//abstract
	}

	@Test
	public void testAutoThreshold() {
		//abstract
	}


	@Test
	public void testGetNChannels() {
		//returns 1
	}

	@Test
	public void testToFloat() {
		//abstract
	}

	@Test
	public void testSetPixelsIntFloatProcessor() {
		//abstract
	}

	@Test
	public void testUpdateComposite() {
		ImageProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		int[] results = new int[width*height];
		testColorProcessor.updateComposite( results, 1 );
		testImageStats( new ColorProcessor(width, height, results), "stats[count=64000, mean=0.0, min=0.0, max=0.0] NaN NaN 160.0 100.0");
		testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		testColorProcessor.updateComposite( results, 2 );
		testImageStats( testColorProcessor, "stats[count=64000, mean=71.654640625, min=0.0, max=248.0] 148.83278391018192 72.21954665945063 160.0 100.0");
		testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		testColorProcessor.updateComposite( results, 3 );
		testImageStats( testColorProcessor, "stats[count=64000, mean=71.654640625, min=0.0, max=248.0] 148.83278391018192 72.21954665945063 160.0 100.0");
		testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		testColorProcessor.updateComposite( results, 4 );
		testImageStats( testColorProcessor, "stats[count=64000, mean=71.654640625, min=0.0, max=248.0] 148.83278391018192 72.21954665945063 160.0 100.0");
		testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		testColorProcessor.updateComposite( results, 5 );
		testImageStats( testColorProcessor, "stats[count=64000, mean=71.654640625, min=0.0, max=248.0] 148.83278391018192 72.21954665945063 160.0 100.0");
	}



	/*
	 * Open an known image test image for global use
	 */
	@BeforeClass
	public static void runBeforeClass() throws IOException
	{
		String id = DataConstants.DATA_DIR + "clown.raw";
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

	//TODO: add to utils class
	/**
	 * Converts a byte[] to an int[]; assumes native byte ordering
	 * @param b - Byte[]
	 * @return
	 */
	public static int[] byteArrayToIntArray(byte[] b )
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
	public void testShowProgress()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		testColorProcessor.showProgress(50);
	}

	@Test
	public void testHideProgress()
	{
		//obsolete
	}

	@Test
	public void testGetWidth()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		//check results
		assertEquals( width, testColorProcessor.getWidth(), 0.0 );
	}

	@Test
	public void testGetHeight()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		//check results
		assertEquals( height, testColorProcessor.getHeight(), 0.0 );

	}

	@Test
	public void testGetColorModel()
	{
		ByteProcessor testByteProcessor = new ByteProcessor(width, height);
		//check results

		byte[] r = new byte[256];
		byte[] g = new byte[256];
		byte[] b = new byte[256];
		for(int i=0; i<256; i++) {
			r[i]=(byte)i;
			g[i]=(byte)i;
			b[i]=(byte)i;
		}
		final ColorModel referenceColorModel = new IndexColorModel( 8, 256, r, g, b ); //(width, height, r, g, b);

		assertEquals( referenceColorModel, testByteProcessor.getColorModel() );
	}

	@Test
	public void testGetCurrentColorModel()
	{
		ByteProcessor testByteProcessor = new ByteProcessor(width, height);
		//check results

		byte[] r = new byte[256];
		byte[] g = new byte[256];
		byte[] b = new byte[256];
		for(int i=0; i<256; i++) {
			r[i]=(byte)i;
			g[i]=(byte)i;
			b[i]=(byte)i;
		}
		final ColorModel referenceColorModel = new IndexColorModel( 8, 256, r, g, b ); //(width, height, r, g, b);

		assertEquals( referenceColorModel, testByteProcessor.getCurrentColorModel() );
	}

	@Test
	public void testSetColorModel()
	{
		ByteProcessor testByteProcessor = new ByteProcessor(width, height);
		//check results

		byte[] r = new byte[256];
		byte[] g = new byte[256];
		byte[] b = new byte[256];
		for(int i=0; i<256; i++) {
			r[i]=(byte)i;
			g[i]=(byte)i;
			b[i]=(byte)i;
		}
		final ColorModel referenceColorModel = new IndexColorModel( 8, 256, r, g, b ); //(width, height, r, g, b);

		testByteProcessor.setColorModel( referenceColorModel );

		assertEquals( referenceColorModel, testByteProcessor.getCurrentColorModel() );

	}

	@Test
	public void testMakeDefaultColorModel()
	{
		ByteProcessor testByteProcessor = new ByteProcessor(width, height);
		//check results

		byte[] r = new byte[256];
		byte[] g = new byte[256];
		byte[] b = new byte[256];
		for(int i=0; i<256; i++)
		{
			r[i]=(byte)i;
			g[i]=(byte)i;
			b[i]=(byte)i;
		}

		final ColorModel referenceColorModel = new IndexColorModel( 8, 256, r, g, b ); //(width, height, r, g, b);

		testByteProcessor.makeDefaultColorModel();
		assertEquals( referenceColorModel, testByteProcessor.getCurrentColorModel() );
	}

	@Test
	public void testIsColorLut()
	{
		ByteProcessor testByteProcessor = new ByteProcessor(width, height);
		boolean testColorLUT = testByteProcessor.isColorLut();


		IndexColorModel icm = (IndexColorModel) testByteProcessor.getColorModel();
		int mapSize = icm.getMapSize();
		byte[] reds = new byte[mapSize];
		byte[] greens = new byte[mapSize];
		byte[] blues = new byte[mapSize];
		icm.getReds(reds);
		icm.getGreens(greens);
		icm.getBlues(blues);
		boolean refIsColor = false;
		for (int i=0; i<mapSize; i++)
		{
			if ((reds[i] != greens[i]) || (greens[i] != blues[i])) {
				refIsColor = true;
				break;
			}
		}

		assertEquals( refIsColor, testColorLUT );
	}

	@Test
	public void testIsPseudoColorLut()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		boolean testPseudoColorLUTCP = testColorProcessor.isPseudoColorLut();
		assertEquals( false, testPseudoColorLUTCP );

		ByteProcessor testByteProcessor = new ByteProcessor(width, height);
		boolean testPseudoColorLUTBP = testByteProcessor.isPseudoColorLut();
		assertEquals( false, testPseudoColorLUTBP );
	}

	@Test
	public void testIsDefaultLut()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		boolean testDefaultColorLUTCP = testColorProcessor.isDefaultLut();
		assertEquals( false, testDefaultColorLUTCP );

		ByteProcessor testByteProcessor = new ByteProcessor(width, height);
		boolean testDefaultColorLUTBP = testByteProcessor.isDefaultLut();
		assertEquals( true, testDefaultColorLUTBP );
	}

	@Test
	public void testResetMinAndMax()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );

		testColorProcessor.setMinAndMax( 12, 200 );
		testColorProcessor.resetMinAndMax();

		double testMinCP = testColorProcessor.getMin();
		double testMaxCP = testColorProcessor.getMax();

		assertEquals( 12.0, testMinCP, 0.0 );
		assertEquals( 200, testMaxCP, 0.0 );
	}

	@Test
	public void testSetThreshold()
	{
		//RED_LUT, BLACK_AND_WHITE_LUT, OVER_UNDER_LUT or NO_LUT_UPDATE
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		ByteProcessor testByteProcessor = (ByteProcessor) testColorProcessor.convertToByte(false);

		testByteProcessor.setThreshold( 12, 200, ImageProcessor.RED_LUT );
		assertEquals( 0,  testByteProcessor.rLUT1[0], 0.0 );
		assertEquals( -1, testByteProcessor.rLUT2[198], 0.0 );

		testByteProcessor = (ByteProcessor) ( testColorProcessor.convertToByte( false) ) ;
		testByteProcessor.setThreshold( 12, 200, ImageProcessor.BLACK_AND_WHITE_LUT );
		assertEquals( 0,  testByteProcessor.rLUT1[0], 0.0 );
		assertEquals( 0, testByteProcessor.rLUT2[198], 0.0 );
		assertEquals( 0,  testByteProcessor.gLUT1[0], 0.0 );
		assertEquals( 0, testByteProcessor.gLUT2[198], 0.0 );
		assertEquals( 0,  testByteProcessor.bLUT1[0], 0.0 );
		assertEquals( 0, testByteProcessor.bLUT2[198], 0.0 );

		testByteProcessor = (ByteProcessor) testColorProcessor.convertToByte(false);
		testByteProcessor.setThreshold( 128, 130, ImageProcessor.OVER_UNDER_LUT );
		assertEquals( 0,  testByteProcessor.rLUT1[0], 0.0 );
		assertEquals( 0, testByteProcessor.rLUT2[198], 0.0 );
		assertEquals( 0,  testByteProcessor.gLUT1[0], 0.0 );
		assertEquals( -1, testByteProcessor.gLUT2[198], 0.0 );

		testByteProcessor = (ByteProcessor) testColorProcessor.convertToByte(false);
		testByteProcessor.setThreshold( 12, 200, ImageProcessor.NO_LUT_UPDATE );
	}



	@Test
	public void testSetAutoThresholdString()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );

		double[] refArray = new double[] {
				187.21334132983154, 99.59616325994367
				, 171.30304713031046, 129.4460337520425
				, 184.02226974717172, 90.50813514351626
				, 187.21334132983154, 99.59616325994367
				, 187.21334132983154, 99.59616325994367
				, 175.230068469893, 120.92320323546852
				, 167.46769348774092, 82.31948628040253
				, 178.4646186466213, 113.04615207037888
				, 162.556407546626, 159.05625743073654
				, 154.37544863381942, 75.80056754637253
				, 188.33815088314768, 95.85283022962068
				, 187.69320164875393, 98.81616015651879
				, 175.230068469893, 120.92320323546852
				, 166.96979760715195, 82.04717497273947
				, 166.96979760715195, 82.04717497273947
				, 168.09920657986515, 146.01645935053864
				, 164.48888747597542, 80.63116891138247
				};
		
		ByteProcessor testBP = null;

		int refIndex = 0;

		//get the thresholding methods
		String[] methods = AutoThresholder.getMethods();
		for (String method : methods )
		{
			testBP = (ByteProcessor) testColorProcessor.convertToByte(false);
			testBP.setAutoThreshold( method );
			ImageStatistics stats = ImageStatistics.getStatistics(testBP, 0xffffffff, null);
			stats.getCentroid( testBP );
			assertEquals( refArray[refIndex++], stats.xCenterOfMass, 0.0 );
			assertEquals( refArray[refIndex++], stats.yCenterOfMass, 0.0 );
		}
	}

	@Test
	public void testSetAutoThresholdStringBooleanInt()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		
		double[] refArray = 
			{187.21334132983154, 99.59616325994367, 171.30304713031046, 129.4460337520425, 184.02226974717172, 90.50813514351626,
				187.21334132983154, 99.59616325994367, 187.21334132983154, 99.59616325994367, 175.230068469893, 120.92320323546852,
				167.46769348774092, 82.31948628040253, 178.4646186466213, 113.04615207037888, 162.556407546626, 159.05625743073654,
				154.37544863381942, 75.80056754637253, 188.33815088314768, 95.85283022962068, 187.69320164875393, 98.81616015651879,
				175.230068469893, 120.92320323546852, 166.96979760715195, 82.04717497273947, 166.96979760715195, 82.04717497273947,
				168.09920657986515, 146.01645935053864, 164.48888747597542, 80.63116891138247};		
		ByteProcessor testBP = null;
		
		int refIndex = 0;

		//get the thresholding methods
		String[] methods = AutoThresholder.getMethods();
		for (String method : methods )
		{
			testBP = (ByteProcessor) testColorProcessor.convertToByte(false);
			testBP.setAutoThreshold(method, false, ImageProcessor.NO_LUT_UPDATE);
			ImageStatistics stats = ImageStatistics.getStatistics(testBP, 0xffffffff, null);
			stats.getCentroid( testBP );
			assertEquals( refArray[refIndex++], stats.xCenterOfMass, 0.0 );
			assertEquals( refArray[refIndex++], stats.yCenterOfMass, 0.0 );
		}
	}

	@Test
	public void testSetAutoThresholdIntInt()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		double[] refArray =
		{
			187.21334132983154, 99.59616325994367, 187.21334132983154, 99.59616325994367, 187.21334132983154, 99.59616325994367,
			187.21334132983154, 99.59616325994367, 171.30304713031046, 129.4460337520425, 171.30304713031046, 129.4460337520425,
			171.30304713031046, 129.4460337520425, 171.30304713031046, 129.4460337520425, 184.02226974717172, 90.50813514351626,
			184.02226974717172, 90.50813514351626, 184.02226974717172, 90.50813514351626, 184.02226974717172, 90.50813514351626,
			187.21334132983154, 99.59616325994367, 187.21334132983154, 99.59616325994367, 187.21334132983154, 99.59616325994367,
			187.21334132983154, 99.59616325994367, 187.21334132983154, 99.59616325994367, 187.21334132983154, 99.59616325994367,
			187.21334132983154, 99.59616325994367, 187.21334132983154, 99.59616325994367, 175.230068469893, 120.92320323546852,
			175.230068469893, 120.92320323546852, 175.230068469893, 120.92320323546852, 175.230068469893, 120.92320323546852,
			167.46769348774092, 82.31948628040253, 167.46769348774092, 82.31948628040253, 167.46769348774092, 82.31948628040253,
			167.46769348774092, 82.31948628040253, 178.4646186466213, 113.04615207037888, 178.4646186466213, 113.04615207037888,
			178.4646186466213, 113.04615207037888, 178.4646186466213, 113.04615207037888, 162.556407546626, 159.05625743073654,
			162.556407546626, 159.05625743073654, 162.556407546626, 159.05625743073654, 162.556407546626, 159.05625743073654,
			154.37544863381942, 75.80056754637253, 154.37544863381942, 75.80056754637253, 154.37544863381942, 75.80056754637253,
			154.37544863381942, 75.80056754637253, 188.33815088314768, 95.85283022962068, 188.33815088314768, 95.85283022962068,
			188.33815088314768, 95.85283022962068, 188.33815088314768, 95.85283022962068, 187.69320164875393, 98.81616015651879,
			187.69320164875393, 98.81616015651879, 187.69320164875393, 98.81616015651879, 187.69320164875393, 98.81616015651879,
			175.230068469893, 120.92320323546852, 175.230068469893, 120.92320323546852, 175.230068469893, 120.92320323546852,
			175.230068469893, 120.92320323546852, 166.96979760715195, 82.04717497273947, 166.96979760715195, 82.04717497273947,
			166.96979760715195, 82.04717497273947, 166.96979760715195, 82.04717497273947, 166.96979760715195, 82.04717497273947,
			166.96979760715195, 82.04717497273947, 166.96979760715195, 82.04717497273947, 166.96979760715195, 82.04717497273947,
			168.09920657986515, 146.01645935053864, 168.09920657986515, 146.01645935053864, 168.09920657986515, 146.01645935053864,
			168.09920657986515, 146.01645935053864, 164.48888747597542, 80.63116891138247, 164.48888747597542, 80.63116891138247,
			164.48888747597542, 80.63116891138247, 164.48888747597542, 80.63116891138247
		};
		ByteProcessor testBP = null;
		int refIndex = 0;
		int[] LUT = { ImageProcessor.RED_LUT, ImageProcessor.BLACK_AND_WHITE_LUT, ImageProcessor.OVER_UNDER_LUT, ImageProcessor.NO_LUT_UPDATE };

		//get the thresholding methods
		String[] methods = AutoThresholder.getMethods();
		for (String method : methods )
		{
			for(int l:LUT)
			{
				testBP = (ByteProcessor) testColorProcessor.convertToByte(false);
				testBP.setAutoThreshold(method, false, l);
				ImageStatistics stats = ImageStatistics.getStatistics(testBP, 0xffffffff, null);
				stats.getCentroid( testBP );
				assertEquals( refArray[refIndex++], stats.xCenterOfMass, 0.0 );
				assertEquals( refArray[refIndex++], stats.yCenterOfMass, 0.0 );
			}
		}
	}

	@Test
	public void testResetThreshold()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		testColorProcessor.setThreshold(19.99, 200.99, ImageProcessor.BLACK_AND_WHITE_LUT);
		testColorProcessor.resetThreshold();
		assertEquals( null, testColorProcessor.rLUT1 );
		assertEquals( null, testColorProcessor.rLUT2 );
		assertEquals( ImageProcessor.NO_THRESHOLD, testColorProcessor.minThreshold, 0.0 );
		assertEquals( false, testColorProcessor.inversionTested );
		assertEquals( true, testColorProcessor.newPixels );
		assertEquals( null, testColorProcessor.source );

	}

	@Test
	public void testGetMinThreshold()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		assertEquals( -808080.0, testColorProcessor.getMinThreshold(), 0.0 );
	}

	@Test
	public void testGetMaxThreshold()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		assertEquals( -808080.0, testColorProcessor.getMaxThreshold(), 0.0 );
	}

	@Test
	public void testGetLutUpdateMode()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		ByteProcessor testBP = ( ByteProcessor ) testColorProcessor.convertToByte(false);
		testBP.setThreshold(0, 255, ImageProcessor.NO_LUT_UPDATE);
		int testLut = testBP.getLutUpdateMode();
		assertEquals( ImageProcessor.NO_LUT_UPDATE, testLut );
	}

	@Test
	public void testSetBinaryThreshold()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		ByteProcessor testBP = ( ByteProcessor ) testColorProcessor.convertToByte(false);
		testBP.setBinaryThreshold();
		assertEquals( 0.0, testBP.getMinThreshold(), 0.0 );
		assertEquals( 0.0, testBP.getMaxThreshold(), 0.0 );
	}

	@Test
	public void testResetBinaryThreshold()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		ByteProcessor testBP = ( ByteProcessor ) testColorProcessor.convertToByte(false);
		testBP.resetBinaryThreshold();
		assertEquals( -808080.0, testBP.getMinThreshold(), 0.0 );
		assertEquals( -808080.0, testBP.getMaxThreshold(), 0.0 );
	}

	@Test
	public void testSetRoiRectangle()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		Rectangle refRect = new Rectangle( 0, 0, width, height) ;

		testColorProcessor.setRoi( refRect );
		Rectangle testRect = testColorProcessor.getRoi();

		assertEquals( refRect, testRect );
	}

	@Test
	public void testSetRoiIntIntIntInt()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		Rectangle refRect = new Rectangle( 0, 0, width, height) ;

		testColorProcessor.setRoi( refRect.x, refRect.y, refRect.width, refRect.height );
		Rectangle testRect = testColorProcessor.getRoi();

		assertEquals( refRect, testRect );
	}

	@Test
	public void testSetRoiRoi()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		Rectangle refRect = new Rectangle( 0, 0, width, height) ;

		testColorProcessor.setRoi( refRect );
		Rectangle testRect = testColorProcessor.getRoi();

		assertEquals( refRect, testRect );
	}

	@Test
	public void testSetRoiPolygon()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		Polygon refRect = new Polygon( ) ;
		refRect.addPoint( 0, 0 );
		refRect.addPoint( width, height );

		testColorProcessor.setRoi( refRect );
		Rectangle testRect = testColorProcessor.getRoi();

		Polygon testPoly = new Polygon();
		testPoly.addPoint( testRect.x, testRect.y );
		testPoly.addPoint( testRect.width, testRect.height );

		assertEquals( refRect.getBounds(), testPoly.getBounds() );
	}

	@Test
	public void testResetRoi()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		Rectangle refRect = new Rectangle( 0, 0, width, height) ;
		Rectangle inputRect = new Rectangle( 0, 0, width/2, height/2) ;
		testColorProcessor.setRoi( inputRect );
		testColorProcessor.resetRoi();
		Rectangle testRect = testColorProcessor.getRoi();

		assertEquals( refRect, testRect );
	}

	@Test
	public void testGetRoi()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		Rectangle refRect = new Rectangle( 0, 0, width, height) ;
		Rectangle inputRect = new Rectangle( 0, 0, width/2, height/2) ;
		testColorProcessor.setRoi( inputRect );
		testColorProcessor.resetRoi();
		Rectangle testRect = testColorProcessor.getRoi();

		assertEquals( refRect, testRect );
	}

	@Test
	public void testSetMask()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );

		ImageProcessor mask = new ByteProcessor( width, height );
		byte[] pixels = new byte[ width*height ];

		for (int i = 0; i < pixels.length; i++)
		{
			if( (i%2) == 0)
			{
				pixels[i] = (byte) 0x00;
			}
		}

		mask.setPixels( pixels );
		testColorProcessor.setMask( mask );
		testColorProcessor.autoThreshold();
		ByteProcessor maskProcessor = ( ByteProcessor ) testColorProcessor.getMask();

		assertEquals( mask, maskProcessor );

	}

	@Test
	public void testGetMask()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		byte[] pixels = new byte[ width*height ];
		ImageProcessor mask = new ByteProcessor( width, height, pixels, null );


		testColorProcessor.setMask( mask );
		ImageProcessor refMask = testColorProcessor.getMask();
		assertEquals( refMask, mask );
	}

	@Test
	public void testGetMaskArray()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		byte[] pixels = new byte[ width*height ];
		ImageProcessor mask = new ByteProcessor( width, height, pixels, null );


		testColorProcessor.setMask( mask );
		byte[]  refMask = testColorProcessor.getMaskArray();
		assertEquals( refMask, pixels );
	}

	@Test
	public void testSetProgressBar()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		ProgressBar pb = new ProgressBar(1, 2);
		testColorProcessor.setProgressBar( pb );
		assertEquals( 0.5, pb.getAlignmentX(), 0.0 );
		assertEquals( 0.5, pb.getAlignmentY(), 0.0  );
	}

	@Test
	public void testSetInterpolate()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		testColorProcessor.setInterpolate(false);
		assertEquals( false, testColorProcessor.getInterpolate() );
	}

	@Test
	public void testSetInterpolationMethod()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );

		int[] methods = {ImageProcessor.NONE,ImageProcessor.BILINEAR, ImageProcessor.BICUBIC};

		for (int m:methods)
		{
			testColorProcessor.setInterpolationMethod( m );
		}
	}

	@Test
	public void testGetInterpolationMethods()
	{
		String[] refInterpolationMethods = new String[] {"None", "Bilinear", "Bicubic"};

		List<String> l = Arrays.asList( refInterpolationMethods );
		for (String m:refInterpolationMethods)
		{
			assertEquals( true, l.contains(m) );

		}
	}

	@Test
	public void testGetInterpolate()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		assertEquals( false, testColorProcessor.getInterpolate() );
	}

	@Test
	public void testIsKillable()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		boolean testKillable = testColorProcessor.isKillable();
		assertEquals( false, testKillable );

	}

	@Test
	public void testGetLine()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		double[] d = testColorProcessor.getLine(0, 0, width, height);
		double[] refVals = {147.6666717529297, 143.6666717529297, 152.6666717529297, 139.3333282470703, 139.3333282470703, 118.33333587646484, 125.33333587646484, 137.3333282470703, 137.3333282470703, 138.6666717529297, 138.6666717529297, 142.0, 130.0, 105.0, 118.0, 125.33333587646484, 132.3333282470703, 129.3333282470703, 121.33333587646484, 128.3333282470703, 125.33333587646484, 129.3333282470703, 131.6666717529297, 119.66666412353516, 120.66666412353516, 118.66666412353516, 116.66666412353516, 112.66666412353516, 115.0, 126.0, 116.66666412353516, 120.66666412353516, 120.0, 108.0, 109.0, 122.0, 122.0, 116.66666412353516, 118.0, 117.0, 121.33333587646484, 113.33333587646484, 108.0, 88.33333587646484, 94.0, 105.33333587646484, 104.33333587646484, 114.33333587646484, 118.33333587646484, 110.33333587646484, 106.33333587646484, 109.0, 104.0, 106.33333587646484, 100.33333587646484, 103.33333587646484, 123.33333587646484, 123.33333587646484, 112.33333587646484, 138.0, 124.33333587646484, 106.66666412353516, 83.0, 83.0, 100.0, 114.0, 86.33333587646484, 81.0, 120.33333587646484, 103.0, 103.0, 107.0, 106.0, 121.66666412353516, 128.3333282470703, 115.0, 113.0, 112.66666412353516, 113.0, 144.6666717529297, 185.6666717529297, 222.6666717529297, 228.0, 228.0, 218.6666717529297, 221.6666717529297, 217.3333282470703, 222.0, 217.6666717529297, 204.0, 199.0, 137.0, 51.66666793823242, 22.33333396911621, 47.66666793823242, 62.0, 51.66666793823242, 66.0, 65.66666412353516, 65.33333587646484, 61.66666793823242, 64.66666412353516, 63.0, 62.66666793823242, 69.0, 47.0, 34.66666793823242, 132.3333282470703, 134.6666717529297, 184.6666717529297, 184.6666717529297, 190.0, 190.0, 219.6666717529297, 206.0, 185.6666717529297, 174.6666717529297, 172.0, 166.6666717529297, 171.3333282470703, 165.0, 171.3333282470703, 151.3333282470703, 151.3333282470703, 164.0, 122.66666412353516, 42.0, 36.66666793823242, 25.0, 25.0, 44.33333206176758, 66.0, 126.0, 91.0, 113.66666412353516, 61.0, 61.0, 53.0, 50.66666793823242, 39.33333206176758, 36.33333206176758, 36.66666793823242, 51.66666793823242, 63.33333206176758, 55.33333206176758, 57.33333206176758, 58.33333206176758, 67.0, 79.66666412353516, 79.66666412353516, 76.0, 112.66666412353516, 96.66666412353516, 98.66666412353516, 119.66666412353516, 120.66666412353516, 113.66666412353516, 107.66666412353516, 151.0, 161.0, 148.0, 152.0, 142.0, 142.0, 155.0, 151.6666717529297, 161.6666717529297, 160.6666717529297, 174.3333282470703, 163.3333282470703, 173.6666717529297, 165.6666717529297, 181.6666717529297, 190.6666717529297, 201.3333282470703, 216.0, 216.0, 208.0, 201.0, 219.3333282470703, 219.3333282470703, 213.3333282470703, 208.6666717529297, 209.6666717529297, 219.6666717529297, 209.6666717529297, 198.6666717529297, 202.6666717529297, 202.3333282470703, 202.3333282470703, 183.3333282470703, 135.3333282470703, 207.3333282470703, 185.3333282470703, 163.3333282470703, 193.6666717529297, 177.0, 217.0, 201.0, 175.0, 185.0, 160.0, 160.0, 174.6666717529297, 172.6666717529297, 137.6666717529297, 141.6666717529297, 186.0, 169.3333282470703, 159.3333282470703, 150.3333282470703, 22.66666603088379, 49.0, 62.0, 11.666666984558105, 11.666666984558105, 20.33333396911621, 9.666666984558105, 6.333333492279053, 7.0, 13.0, 10.333333015441895, 33.66666793823242, 31.66666603088379, 91.33333587646484, 80.33333587646484, 73.66666412353516, 77.66666412353516, 92.66666412353516, 92.66666412353516, 109.66666412353516, 126.66666412353516, 125.0, 120.0, 112.0, 109.33333587646484, 116.33333587646484, 109.33333587646484, 105.0, 90.0, 87.0, 84.0, 84.0, 87.66666412353516, 79.0, 70.0, 70.33333587646484, 65.33333587646484, 51.33333206176758, 51.33333206176758, 58.33333206176758, 59.0, 60.0, 57.66666793823242, 56.0, 56.0, 52.0, 46.66666793823242, 44.66666793823242, 42.66666793823242, 34.33333206176758, 33.33333206176758, 37.33333206176758, 35.0, 32.66666793823242, 33.0, 33.66666793823242, 33.66666793823242, 33.66666793823242, 31.33333396911621, 30.33333396911621, 23.66666603088379, 22.66666603088379, 23.33333396911621, 23.33333396911621, 22.66666603088379, 21.66666603088379, 18.33333396911621, 17.66666603088379, 16.66666603088379, 16.33333396911621, 16.0, 15.666666984558105, 15.666666984558105, 15.666666984558105, 16.66666603088379, 13.333333015441895, 16.33333396911621, 15.333333015441895, 16.33333396911621, 14.333333015441895, 12.333333015441895, 11.333333015441895, 11.333333015441895, 11.666666984558105, 11.666666984558105, 11.666666984558105, 11.666666984558105, 11.666666984558105, 11.666666984558105, 11.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 8.0, 8.0, 8.0, 8.0, 8.666666984558105, 8.666666984558105, 8.666666984558105, 8.0, 8.0, 8.0, 8.0, 8.0, 8.0, 7.666666507720947, 10.0, 6.666666507720947, 7.0, 6.666666507720947, 6.666666507720947, 6.666666507720947, 6.666666507720947, 6.666666507720947, 7.0, 7.0, 6.333333492279053, 6.333333492279053, 7.333333492279053, 7.0, 7.0, 7.666666507720947, 8.0, 8.0, 8.0, 8.333333015441895, 8.0, 8.0, 7.666666507720947, 8.333333015441895, 8.0, 7.333333492279053, 7.0, 7.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.666666507720947, 5.666666507720947, 5.666666507720947, 5.666666507720947, 5.666666507720947, 5.666666507720947, 5.666666507720947, 5.666666507720947, 5.666666507720947, 5.666666507720947, 5.666666507720947, 5.666666507720947, 5.666666507720947, 5.666666507720947, 0.0,};

		for(int refIndex = 0; refIndex < refVals.length; refIndex++)
			assertEquals( refVals[ refIndex ],d[refIndex], 0.0 );
	}


	@Test
	public void testGetRow()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		int[] testArray = new int[ width ];
		testColorProcessor.getRow( 0, 0, testArray, width );
		int[] refArray = getRefImageArray();

		for(int refIndex = 0; refIndex < width; refIndex++)
		{
			assertEquals( refArray[ refIndex ], testArray[refIndex], 0.0 );
		}
	}

	@Test
	public void testGetColumn()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height, getRefImageArray() );
		int[] testArray = new int[ height ];
		testColorProcessor.getColumn(0, 0, testArray, height);
		int[] refArray = getRefImageArray();

		for(int refIndex = 0; refIndex < height; refIndex++)
		{
			assertEquals( refArray[ refIndex*width ], testArray[refIndex], 0.0 );
		}
	}

	@Test
	public void testPutRow()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height );
		int[] refArray = getRefImageArray();

		testColorProcessor.putRow(0, 0, refArray, width);

	}

	@Test
	public void testPutColumn()
	{
		ColorProcessor testColorProcessor = new ColorProcessor(width, height );
		int[] refArray = getRefImageArray();

		for(int refIndex = 0; refIndex < width; refIndex++)
		{
			testColorProcessor.putColumn(0, 0, refArray, height);
		}
	}

	@Test
	public void testMoveTo()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.moveTo( width/2,height/2 );
		testColorProcessor.lineTo( 0, 0 );
		//double[] testLine = testColorProcessor.getLine(width/2, height/2, 0, 0);
	}

	@Test
	public void testSetLineWidth()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.setLineWidth( 2 );
		int results = testColorProcessor.getLineWidth();
		assertEquals( 2, results, 0.0 );

		//TODO find out if getline / setline should set and get the same values
		//Now they do not match
	}

	@Test
	public void testGetLineWidth()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.setLineWidth( 2 );
		int results = testColorProcessor.getLineWidth();
		assertEquals( 2, results, 0.0 );
	}

	@Test
	public void testLineTo()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.moveTo( width/2,height/2 );
		testColorProcessor.lineTo( 0, 0 );
		int[] results = testColorProcessor.getPixel(0, 0, null);

		assertEquals( 0, results[0] + results[1] + results[2] );
	}

	@Test
	public void testDrawLine()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.drawLine(0, 0, width/2, height/2 );
		int[] results = testColorProcessor.getPixel(0, 0, null);

		assertEquals( 0, results[0] + results[1] + results[2] );
	}

	@Test
	public void testDrawRect()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.drawRect(0, 0, width/2, height/2);
		testImageStats( testColorProcessor, "stats[count=64000, mean=70.7655, min=0.0, max=248.0] 149.50175043519965 72.67734854470383 160.0 100.0");
	}

	@Test
	public void testDrawOval()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.drawOval( width/2, height/2, width/3, height/3 );
		testImageStats( testColorProcessor, "stats[count=64000, mean=71.363203125, min=0.0, max=248.0] 148.63831829694618 72.00391685705614 160.0 100.0");
	}

	@Test
	public void testFillOval()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.fillOval( width/2, height/2, width/3, height/3 );
		testImageStats( testColorProcessor, "stats[count=64000, mean=67.004484375, min=0.0, max=248.0] 145.30313053056489 68.1761175494732 160.0 100.0");
	}

	@Test
	public void testDrawPolygon()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		Polygon p = new Polygon();
		p.addPoint(0, 0); p.addPoint(width, 0); p.addPoint(0, height);

		testColorProcessor.drawPolygon(p);
		testImageStats( testColorProcessor, "stats[count=64000, mean=70.52384375, min=0.0, max=248.0] 148.92131550551778 72.86184684438823 160.0 100.0");

	}

	@Test
	public void testFillPolygon()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		Polygon p = new Polygon();
		p.addPoint(0, 0); p.addPoint(width, 0); p.addPoint(0, height);

		testColorProcessor.fillPolygon(p);
		testImageStats( testColorProcessor, "stats[count=64000, mean=23.8383125, min=0.0, max=248.0] 209.8867525101403 117.7659003890493 160.0 100.0");
	}

	@Test
	public void testDrawDot2()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		Polygon p = new Polygon();
		p.addPoint(0, 0); p.addPoint(width, 0); p.addPoint(0, height);

		testColorProcessor.drawDot2(width/2, height/2);
		testImageStats( testColorProcessor, "stats[count=64000, mean=71.64184375, min=0.0, max=248.0] 148.8307924489489 72.21458640258378 160.0 100.0");
	}

	@Test
	public void testDrawDot()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		Polygon p = new Polygon();
		p.addPoint(0, 0); p.addPoint(width, 0); p.addPoint(0, height);

		testColorProcessor.drawDot(width/2, height/2);
		testImageStats( testColorProcessor, "stats[count=1, mean=0.0, min=0.0, max=0.0] NaN NaN 160.5 100.5");
	}

	/* Removed 7-21-10
	 * because hudson, which runs without a gui, does not like this test
	@Test
	public void testDrawStringString()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		String s="On Target";
		testColorProcessor.moveTo( width/2, height/2 );
		testColorProcessor.drawString(s);
		testImageStats( testColorProcessor, "stats[count=64000, mean=71.49615625, min=0.0, max=248.0] 148.76518804520842 72.17345675917691 160.0 100.0");
	}
	*/

	/* Removed 7-21-10
	 * because hudson, which runs without a gui, does not like this test
	@Test
	public void testDrawStringStringIntInt()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		String s="On Target";

		testColorProcessor.drawString(s, width/3, height/3 );
		testImageStats( testColorProcessor, "stats[count=64000, mean=71.47834375, min=0.0, max=248.0] 148.86700358257028 72.25213205729175 160.0 100.0");
	}
	*/

	/* Removed 7-20-10
	 * because hudson, which runs without a gui, does not like this test
	@Test
	public void testSetJustification()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.setJustification( 1 );  //Center_Justify
		String s="On Target";

		testColorProcessor.drawString(s, width/3, height/3 );
		testImageStats( testColorProcessor, "stats[count=64000, mean=71.399046875, min=0.0, max=248.0] 149.00210838264618 72.2657735809548 160.0 100.0");
	}
	*/

	/* Removed 7-20-10
	 * because hudson, which runs without a gui, does not like this test
	@Test
	public void testSetFont()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		Font font = new Font("SansSerif", Font.BOLD, 22);
		testColorProcessor.setFont(font);
		String s = "On Target";

		testColorProcessor.drawString(s, width/3, height/3 );
		testImageStats( testColorProcessor, "stats[count=64000, mean=70.683, min=0.0, max=248.0] 148.62772399015125 72.4740740303997 160.0 100.0");
	}
	*/

	/* Removed 7-20-10
	 * because hudson, which runs without a gui, does not like this test
	@Test
	public void testSetAntialiasedText()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		Font font = new Font("SansSerif", Font.BOLD, 22);
		testColorProcessor.setAntialiasedText( true );
		testColorProcessor.setFont(font);
		String s = "On Target";

		testColorProcessor.drawString(s, width/3, height/3 );
		testImageStats( testColorProcessor, "stats[count=64000, mean=70.636875, min=0.0, max=248.0] 148.63751391466442 72.48489824867364 160.0 100.0");
	}
	*/

	/* Removed 7-21-10
	 * because hudson, which runs without a gui, does not like this test
	@Test
	public void testGetStringWidth()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		Font font = new Font("SansSerif", Font.BOLD, 22);
		testColorProcessor.setAntialiasedText( true );
		testColorProcessor.setFont(font);
		String s = "On Target";

		assertEquals( 114, testColorProcessor.getStringWidth(s) );
	}
	*/

	@Test
	public void testGetFont()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		Font font = new Font("SansSerif", Font.BOLD, 22);
		testColorProcessor.setFont(font);
		assertEquals( font, testColorProcessor.getFont() );
	}

	/* Removed 7-20-10
	 * because hudson, which runs without a gui, does not like this test
	@Test
	public void testGetFontMetrics()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		Font font = new Font("SansSerif", Font.BOLD, 22);
		testColorProcessor.setFont(font);
		FontMetrics refFM = testColorProcessor.getFontMetrics();
		assertEquals( "sun.font.FontDesignMetrics[font=java.awt.Font[family=SansSerif,name=SansSerif,style=bold,size=22]ascent=22, descent=5, height=27]", refFM.toString() );
	}
	*/

	@Test
	public void testSmooth()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.smooth();
		ImageStatistics imageStatistics = ImageStatistics.getStatistics( testColorProcessor, 0xFFFFFFFF, null );
		assertEquals( "stats[count=64000, mean=71.237984375, min=1.0, max=247.0]",imageStatistics.toString() );
	}

	@Test
	public void testSharpen()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.sharpen();
		ImageStatistics imageStatistics = ImageStatistics.getStatistics( testColorProcessor, 0xFFFFFFFF, null );

		assertEquals( "stats[count=64000, mean=72.121640625, min=0.0, max=255.0]",imageStatistics.toString() );
	}

	@Test
	public void testFlipHorizontal()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.flipHorizontal();
		testImageStats( testColorProcessor, "stats[count=64000, mean=71.654640625, min=0.0, max=248.0] 171.16721608981808 72.21954665945063 160.0 100.0");
	}

	@Test
	public void testRotateRight()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.rotateRight();
		testImageStats( testColorProcessor, "stats[count=64000, mean=71.654640625, min=0.0, max=248.0] 148.83278391018192 72.21954665945063 160.0 100.0");
	}

	@Test
	public void testRotateLeft()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.rotateLeft();
		ImageStatistics imageStatistics = ImageStatistics.getStatistics( testColorProcessor, 0xFFFFFFFF, null );
		imageStatistics.getCentroid( testColorProcessor );

		testImageStats( testColorProcessor, "stats[count=64000, mean=71.654640625, min=0.0, max=248.0] 148.83278391018192 72.21954665945063 160.0 100.0");
	}

	@Test
	public void testInsert()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.insert( testColorProcessor, width/2, height/2);

		testImageStats( testColorProcessor, "stats[count=64000, mean=91.480078125, min=1.0, max=248.0] 172.30434558207622 89.04315176797896 160.0 100.0");
	}

	@Test
	public void testToString()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		assertEquals( "ip[width=320, height=200, min=0.0, max=255.0]", testColorProcessor.toString() );
	}

	@Test
	public void testFillRoi()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		Roi roi = new Roi( new Rectangle( 0,0, width/2, height/2 ) );
		testColorProcessor.fill( roi );

		testImageStats( testColorProcessor, "stats[count=64000, mean=42.080078125, min=0.0, max=248.0] 193.48463296820586 90.87446511361193 160.0 100.0" );
	}

	@Test
	public void testFillOutside()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		Roi roi = new Roi( new Rectangle( 0,0, width/2, height/2 ) );
		testColorProcessor.fillOutside( roi );

		testImageStats( testColorProcessor, "stats[count=64000, mean=29.5745625, min=0.0, max=247.0] 85.29998052854751 45.676429380958616 160.0 100.0");
	}

	/**
	 * TODO: move to utils
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
	public void testDraw()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		Roi roi = new Roi( new Rectangle( 0,0, width/2, height/2 ) );
		testColorProcessor.draw( roi );

		testImageStats( testColorProcessor, "stats[count=64000, mean=70.76409375, min=0.0, max=248.0] 149.48427697226265 72.67949449898215 160.0 100.0");
	}

	@Test
	public void testSetCalibrationTable()
	{
		//create a calibration table
		float[] cTable = new float[256];
		for( int i = 0; i < cTable.length; i++ )
		{
			cTable[i] = i%2;
		}

		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		ByteProcessor testByteProcessor = (ByteProcessor) testColorProcessor.convertToByte( false );

		testByteProcessor.setCalibrationTable(cTable);

		testImageStats( testColorProcessor, "stats[count=64000, mean=71.654640625, min=0.0, max=248.0] 148.83278391018192 72.21954665945063 160.0 100.0");
	}

	@Test
	public void testGetCalibrationTable()
	{
		//create a calibration table
		float[] cTable = new float[256];
		for( int i = 0; i < cTable.length; i++ )
		{
			cTable[i] = i%2;
		}

		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		ByteProcessor testByteProcessor = (ByteProcessor) testColorProcessor.convertToByte( false );

		testByteProcessor.setCalibrationTable(cTable);
		float[] testCalibrationTable = testByteProcessor.getCalibrationTable();

		for (int i = 0; i < cTable.length; i++)
		{
			assertEquals( cTable[i], testCalibrationTable[i], 0.0 );
		}
	}

	@Test
	public void testSetHistogramSize()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		final int histRefSize = 1024;
		testColorProcessor.setHistogramSize( histRefSize );
		assertEquals( histRefSize, testColorProcessor.getHistogramSize() );
	}

	@Test
	public void testGetHistogramSize()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		final int histRefSize = 1024;
		testColorProcessor.setHistogramSize( histRefSize );
		assertEquals( histRefSize, testColorProcessor.getHistogramSize() );
	}

	@Test
	public void testSetHistogramRange()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		FloatProcessor testFloatProcessor = new FloatProcessor( testColorProcessor.getFloatArray() );
		final int histRefMin = 24;
		final int histRefMax = 155;
		testFloatProcessor.setHistogramRange( histRefMin, histRefMax );
		testFloatProcessor.getHistogramMax();
		testFloatProcessor.getHistogramMin();

		assertEquals( histRefMax, testFloatProcessor.getHistogramMax(), 0.0 );
		assertEquals( histRefMin, testFloatProcessor.getHistogramMin(), 0.0 );
	}

	@Test
	public void testGetHistogramMin()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		FloatProcessor testFloatProcessor = new FloatProcessor( testColorProcessor.getFloatArray() );
		final int histRefMin = 24;
		final int histRefMax = 155;
		testFloatProcessor.setHistogramRange( histRefMin, histRefMax );
		testFloatProcessor.getHistogramMax();
		testFloatProcessor.getHistogramMin();

		assertEquals( histRefMin, testFloatProcessor.getHistogramMin(), 0.0 );
	}

	@Test
	public void testGetHistogramMax()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		FloatProcessor testFloatProcessor = new FloatProcessor( testColorProcessor.getFloatArray() );
		final int histRefMin = 24;
		final int histRefMax = 155;
		testFloatProcessor.setHistogramRange( histRefMin, histRefMax );
		testFloatProcessor.getHistogramMax();
		testFloatProcessor.getHistogramMin();

		assertEquals( histRefMax, testFloatProcessor.getHistogramMax(), 0.0 );
	}

	@Test
	public void testGetPixelCount()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		final int refPixelCount = 64000;
		assertEquals( refPixelCount, testColorProcessor.getPixelCount() );
	}

	@Test
	public void testGetIntArray()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		int[][] testImageIntArray = testColorProcessor.getIntArray();
		final int[] refImageArray = getRefImageArray();

		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++ )
			{
				final int index = width*i + j;
				assertEquals( refImageArray[index], testImageIntArray[j][i] );
			}
		}
	}

	@Test
	public void testSetIntArray()
	{
		ColorProcessor refColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		int[][] testImageIntArray = refColorProcessor.getIntArray();

		ColorProcessor testColorProcessor = new ColorProcessor( width, height);
		testColorProcessor.setIntArray( testImageIntArray );

		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++ )
			{
				final int index = width*i + j;
				assertEquals( refColorProcessor.get( index ), testColorProcessor.get( index ) );
			}
		}
	}

	@Test
	public void testGetFloatArray()
	{
		FloatProcessor refFloatProcessor = new FloatProcessor( width, height, getRefImageArray() );
		float[][] testFloatImageArray = refFloatProcessor.getFloatArray();
		final int[] refImageArray = getRefImageArray();

		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++ )
			{
				final int index = width*i + j;
				assertEquals(  refImageArray[index], testFloatImageArray[j][i], 0.0 );
			}
		}
	}

	@Test
	public void testSetFloatArray()
	{
		FloatProcessor refFloatProcessor = new FloatProcessor( width, height, getRefImageArray() );
		float[][] refFloatImageArray = refFloatProcessor.getFloatArray();

		FloatProcessor testFloatProcessor = new FloatProcessor( width, height);
		testFloatProcessor.setFloatArray( refFloatImageArray );

		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++ )
			{
				assertEquals(  refFloatImageArray[x][y], testFloatProcessor.getf(x, y), 0.0 );
			}
		}
	}

	@Test
	public void testGetInterpolatedValue()
	{
		ColorProcessor refColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		assertEquals( 147.6666717529297, refColorProcessor.getInterpolatedValue(0, 0), 0.0);
	}

	@Test
	public void testGetBicubicInterpolatedPixel()
	{
		ColorProcessor refColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		assertEquals( 147.6666717529297, refColorProcessor.getBicubicInterpolatedPixel(0, 0, refColorProcessor), 0.0);
	}

	@Test
	public void testGetBilinearInterpolatedPixel()
	{
		ColorProcessor refColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		assertEquals( 147.6666717529297, refColorProcessor.getBilinearInterpolatedPixel( 0, 0 ), 0.0);
	}

	@Test
	public void testCubic()
	{
		ColorProcessor refColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		assertEquals( 0.0, refColorProcessor.cubic( 19.99 ), 0.0 );
	}

	@Test
	public void testInvert()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.invert();

		testImageStats( testColorProcessor, "stats[count=64000, mean=183.345359375, min=7.0, max=255.0] 164.36390363871388 110.8559930952934 160.0 100.0");
	}

	@Test
	public void testAddInt()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.add( 9 );

		testImageStats( testColorProcessor, "stats[count=64000, mean=80.62746875, min=9.0, max=251.0] 150.0939848200588 75.32777764983751 160.0 100.0");
	}

	@Test
	public void testAddDouble()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.add( 19.99 );

		testImageStats( testColorProcessor, "stats[count=64000, mean=90.5365, min=19.0, max=254.0] 151.22497604482783 78.07985948715985 160.0 100.0");
	}

	@Test
	public void testMultiply()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.multiply( 1.999 );

		testImageStats( testColorProcessor, "stats[count=64000, mean=118.727328125, min=0.0, max=255.0] 153.5953861156189 75.47588667346254 160.0 100.0");
	}

	@Test
	public void testAnd()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.and( 0x0000ff00 );

		testImageStats( testColorProcessor, "stats[count=64000, mean=0.0, min=0.0, max=0.0] NaN NaN 160.0 100.0");
	}

	@Test
	public void testOr() {
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.or( 0x0000ff00 );

		testImageStats( testColorProcessor, "stats[count=64000, mean=255.0, min=255.0, max=255.0] 160.0 100.0 160.0 100.0");
	}

	@Test
	public void testXor() {
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.multiply( 1.999 );

		testImageStats( testColorProcessor, "stats[count=64000, mean=118.727328125, min=0.0, max=255.0] 153.5953861156189 75.47588667346254 160.0 100.0");
	}

	@Test
	public void testGamma()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.gamma( 19.99 );

		testImageStats( testColorProcessor, "stats[count=64000, mean=2.522265625, min=0.0, max=183.0] 112.1317265002847 51.83257461714362 160.0 100.0");
	}

	@Test
	public void testLog()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.log();

		testImageStats( testColorProcessor, "stats[count=64000, mean=161.185109375, min=0.0, max=253.0] 158.27323000046908 88.12311447568864 160.0 100.0");
	}

	@Test
	public void testExp()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.exp();

		testImageStats( testColorProcessor, "stats[count=64000, mean=17.521890625, min=1.0, max=221.0] 129.39503045122964 60.711569477324005 160.0 100.0");
	}

	@Test
	public void testSqr() {
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.sqr();

		testImageStats( testColorProcessor, "stats[count=64000, mean=196.252421875, min=0.0, max=255.0] 160.77230372687907 88.24657268957972 160.0 100.0");
	}

	@Test
	public void testSqrt()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.sqrt();

		testImageStats( testColorProcessor, "stats[count=64000, mean=6.825234375, min=0.0, max=15.0] 154.6467428375634 81.38245385185688 160.0 100.0");
	}

	@Test
	public void testAbs()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.abs();

		testImageStats( testColorProcessor, "stats[count=64000, mean=71.654640625, min=0.0, max=248.0] 148.83278391018192 72.21954665945063 160.0 100.0");
	}

	@Test
	public void testMin()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		ByteProcessor testByteProcessor = (ByteProcessor) testColorProcessor.convertToByte(false);
		testByteProcessor.min( 199.9 );

		testImageStats( testByteProcessor, "stats[count=64000, mean=199.625765625, min=199.0, max=248.0] 159.85932564128393 99.86286405914693 160.0 100.0");
	}

	@Test
	public void testMax()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		ByteProcessor testByteProcessor = (ByteProcessor) testColorProcessor.convertToByte(false);
		testByteProcessor.max( 199.9 );

		testImageStats( testByteProcessor, "stats[count=64000, mean=71.028875, min=0.0, max=199.0] 149.12454969897806 72.3663006330172 160.0 100.0");
	}

	@Test
	public void testGetBufferedImage()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		BufferedImage testBufferedImage = testColorProcessor.getBufferedImage();

		Raster testRaster = testBufferedImage.getData();
		float[] testFloatArray = new float[width*height];
		testRaster.getPixel( 0, 0, testFloatArray );
		testImageStats( testColorProcessor, "stats[count=64000, mean=71.654640625, min=0.0, max=248.0] 148.83278391018192 72.21954665945063 160.0 100.0");
	}

	@Test
	public void testResizeInt()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.resize( width/2 );
		testImageStats( testColorProcessor, "stats[count=64000, mean=71.654640625, min=0.0, max=248.0] 148.83278391018192 72.21954665945063 160.0 100.0");
	}

	@Test
	public void testTranslateDoubleDouble()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.translate( 0.1, 19.9 );
		testImageStats( testColorProcessor, "stats[count=64000, mean=69.13840625, min=0.0, max=247.0] 149.75832087032828 86.8824061330147 160.0 100.0");
	}

	@Test
	public void testTranslateIntIntBoolean()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.translate( 0, 0, false );
		testImageStats( testColorProcessor, "stats[count=64000, mean=71.654640625, min=0.0, max=248.0] 148.83278391018192 72.21954665945063 160.0 100.0");
	}

	@Test
	public void testSetLutAnimation()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.setLutAnimation( true );
		//TODO: Not sure how to best test

	}

	@Test
	public void testResetPixels()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.resetPixels( null );
		testImageStats( testColorProcessor, "stats[count=64000, mean=71.654640625, min=0.0, max=248.0] 148.83278391018192 72.21954665945063 160.0 100.0");
	}

	@Test
	public void testConvertToByte()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		ImageProcessor testImageProcessor = testColorProcessor.convertToByte( false );

		testImageStats( testImageProcessor, "stats[count=64000, mean=71.654640625, min=0.0, max=248.0] 148.8276150772684 72.22557495294814 160.0 100.0");
	}

	@Test
	public void testConvertToShort()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		ImageProcessor testImageProcessor = testColorProcessor.convertToShort( false );
		testImageStats( testImageProcessor, "stats[count=64000, mean=71.654640625, min=0.0, max=248.0] 148.8276150772684 72.22557495294814 160.0 100.0");
	}

	@Test
	public void testConvertToFloat()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		ImageProcessor testImageProcessor = testColorProcessor.convertToFloat();
		testImageStats( testImageProcessor, "stats[count=64000, mean=71.654640625, min=0.0, max=248.0] 148.8276150772684 72.22557495294814 160.0 100.0");
	}

	@Test
	public void testConvertToRGB()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		ImageProcessor testImageProcessor = testColorProcessor.convertToRGB();
		testImageStats( testImageProcessor, "stats[count=64000, mean=71.654640625, min=0.0, max=248.0] 148.83278391018192 72.21954665945063 160.0 100.0");
	}

	@Test
	public void testGetAutoThreshold()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.getAutoThreshold();
		testImageStats( testColorProcessor, "stats[count=64000, mean=71.654640625, min=0.0, max=248.0] 148.83278391018192 72.21954665945063 160.0 100.0");
	}

	@Test
	public void testGetAutoThresholdIntArray()
	{
		int[] refHistogram = new int[256];
		for (int i =0;i<refHistogram.length;i++)
			refHistogram[i]=(width*height)/refHistogram.length;
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.getAutoThreshold( refHistogram );

		testImageStats( testColorProcessor, "stats[count=64000, mean=71.654640625, min=0.0, max=248.0] 148.83278391018192 72.21954665945063 160.0 100.0");
	}

	@Test
	public void testSetClipRect()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		Rectangle rect = new Rectangle(0,0,width/2, height/2);
		testColorProcessor.setClipRect(rect);
		testColorProcessor.drawLine(0,0, width/2, height/2);
		testImageStats( testColorProcessor, "stats[count=64000, mean=71.337546875, min=0.0, max=248.0] 149.12377971311082 72.30785690943182 160.0 100.0");
	}

	@Test
	public void testMaskSizeError()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		assertEquals( "Mask size (320x200) != ROI size (320x200)", testColorProcessor.maskSizeError(testColorProcessor) );
	}

	@Test
	public void testGetIndexSampleModel()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		SampleModel testSampleModel = testColorProcessor.getIndexSampleModel();

		assertEquals( height, testSampleModel.getHeight() );
		assertEquals( width, testSampleModel.getWidth() );
	}

	@Test
	public void testGetDefaultColorModel()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		ColorModel cm = testColorProcessor.getDefaultColorModel();
		assertEquals( "8 1 false ",cm.getPixelSize() + " " + cm.getTransparency() + " " + cm.isAlphaPremultiplied() + " " );

	}

	@Test
	public void testSetSnapshotCopyMode()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		testColorProcessor.setSnapshotCopyMode( true );

	}

	@Test
	public void testMinValue()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		assertEquals( 0.0, testColorProcessor.minValue(), 0.0 );
	}

	@Test
	public void testMaxValue()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		assertEquals( 0.0, testColorProcessor.minValue(), 0.0 );
	}

	@Test
	public void testCreate8BitImage()
	{
		ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		//TODO: not sure how to best test this yet..
		assertEquals(null, testColorProcessor.create8BitImage() );
	}

	@Test
	public void testUpdateLutBytes()
	{
		//ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		//testColorProcessor.updateLutBytes();
		//TODO: Update Lut Bytes throws an exception
	}

	@Test
	public void testSetOverColor()
	{
		//ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		ImageProcessor.setOverColor(0, 0, 0);
	}

	@Test
	public void testSetUnderColor()
	{
		//ColorProcessor testColorProcessor = new ColorProcessor( width, height, getRefImageArray() );
		ImageProcessor.setUnderColor( 0, 0, 0 );
	}

	@Test
	public void testIsBinary()
	{
		//returns false
	}

	@Test
	public void testSetUseBicubic()
	{
		//sets hidden private field
	}



}
