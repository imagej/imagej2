package ij.io;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ij.io.FileInfo;

public class FileInfoTest {

	FileInfo fInfo = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		fInfo = new FileInfo();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFileInfoDefaults() {
	   	// check default values
    	assertEquals(fInfo.fileFormat,FileInfo.UNKNOWN);
    	assertEquals(fInfo.fileType,FileInfo.GRAY8);
    	assertEquals(fInfo.fileName,"Untitled");
    	assertEquals(fInfo.directory,"");
    	assertEquals(fInfo.url,"");
    	assertEquals(fInfo.nImages,1);
    	assertEquals(fInfo.compression,FileInfo.COMPRESSION_NONE);
    	assertEquals(fInfo.samplesPerPixel,1);    	
    	assertEquals(fInfo.width,0);
    	assertEquals(fInfo.height,0);
    	assertEquals(fInfo.offset,0);
    	assertEquals(fInfo.gapBetweenImages,0);
    	assertEquals(fInfo.whiteIsZero,false);
    	assertEquals(fInfo.intelByteOrder,false);
    	assertNull(fInfo.stripOffsets); 
    	assertNull(fInfo.stripLengths);
    	assertEquals(fInfo.rowsPerStrip,0);
    	assertEquals(fInfo.lutSize,0);
    	assertNull(fInfo.reds);
    	assertNull(fInfo.greens);
    	assertNull(fInfo.blues);
    	assertNull(fInfo.pixels);	
    	assertNull(fInfo.debugInfo);
    	assertNull(fInfo.sliceLabels);
    	assertNull(fInfo.info);
    	assertNull(fInfo.inputStream);
    	assertEquals(fInfo.pixelWidth,1.0,0.0);
    	assertEquals(fInfo.pixelHeight,1.0,0.0);
    	assertEquals(fInfo.pixelDepth,1.0,0.0);
    	assertNull(fInfo.unit);
    	assertEquals(fInfo.calibrationFunction,0);
    	assertNull(fInfo.coefficients);
    	assertNull(fInfo.valueUnit);
    	assertEquals(fInfo.frameInterval,0.0,0.0);
    	assertNull(fInfo.description);
    	assertEquals(fInfo.longOffset,0);
    	assertNull(fInfo.metaDataTypes);
    	assertNull(fInfo.metaData);
    	assertNull(fInfo.displayRanges);
    	assertNull(fInfo.channelLuts);
	}

	@Test
	public void testFileInfoConstants(){
		// test the existence and value of all the public constants
		//   this will make sure that the all the constants exist and have the right values
		//     I'm assuming this is necessary to avoid recompilation of old plugins - maybe a bad assumption
		//   is this a useless test?
		
		// Sample layouts
		assertEquals(FileInfo.GRAY8 ,0);
		assertEquals(FileInfo.GRAY16_SIGNED,1);
		assertEquals(FileInfo.GRAY16_UNSIGNED,2);
		assertEquals(FileInfo.GRAY32_INT,3);
		assertEquals(FileInfo.GRAY32_FLOAT,4);
		assertEquals(FileInfo.COLOR8,5);
		assertEquals(FileInfo.RGB,6);	
		assertEquals(FileInfo.RGB_PLANAR,7);
		assertEquals(FileInfo.BITMAP,8);
		assertEquals(FileInfo.ARGB,9);
		assertEquals(FileInfo.BGR,10);
		assertEquals(FileInfo.GRAY32_UNSIGNED,11);
		assertEquals(FileInfo.RGB48,12);	
		assertEquals(FileInfo.GRAY12_UNSIGNED,13);	
		assertEquals(FileInfo.GRAY24_UNSIGNED,14);	
		assertEquals(FileInfo.BARG,15);	
		assertEquals(FileInfo.GRAY64_FLOAT,16);	
		assertEquals(FileInfo.RGB48_PLANAR,17);	

		// File formats
		assertEquals(FileInfo.UNKNOWN,0);
		assertEquals(FileInfo.RAW,1);
		assertEquals(FileInfo.TIFF,2);
		assertEquals(FileInfo.GIF_OR_JPG,3);
		assertEquals(FileInfo.FITS,4);
		assertEquals(FileInfo.BMP,5);
		assertEquals(FileInfo.DICOM,6);
		assertEquals(FileInfo.ZIP_ARCHIVE,7);
		assertEquals(FileInfo.PGM,8);
		assertEquals(FileInfo.IMAGEIO,9);

		// Compression modes
		assertEquals(FileInfo.COMPRESSION_UNKNOWN,0);
		assertEquals(FileInfo.COMPRESSION_NONE,1);
		assertEquals(FileInfo.LZW,2);
		assertEquals(FileInfo.LZW_WITH_DIFFERENCING,3);
		assertEquals(FileInfo.JPEG,4);
		assertEquals(FileInfo.PACK_BITS,5);
		
	}
	
	@Test
	public void testFileInfoFields(){
		// test that we can set all the instance public vars
		//   this will make sure that later ports define all the same fields
		//   is this a useless test?

		// make sure we can assign to all the fields : random data - really just a compile time check
		fInfo.fileFormat = 9392;
		fInfo.fileType = 1673;
		fInfo.fileName = "photo.tif";
		fInfo.directory = "/data/WUIproject";
		fInfo.url = "http:/loci.wisc.edu";
		fInfo.width = 400;
		fInfo.height = 600;
		fInfo.offset = 77;
		fInfo.nImages = 43;
		fInfo.gapBetweenImages = 200;
		fInfo.whiteIsZero = true;
		fInfo.intelByteOrder = true;
		fInfo.compression = FileInfo.LZW;
		fInfo.stripOffsets = new int[] {10, 50, 100};
		fInfo.stripLengths = new int[] {48, 48, 48};
		fInfo.rowsPerStrip = 3;
		fInfo.lutSize = 768;
		fInfo.reds = new byte[] {1,7,4,8,99,23,15};
		fInfo.greens = new byte[] {1,7,4,8,99,23,15};;
		fInfo.blues = new byte[] {1,7,4,8,99,23,15};;
		fInfo.pixels = new byte[] {1,2,3,4,5,6,7,8,9,10};	
		fInfo.debugInfo = "fake data";
		fInfo.sliceLabels = new String[] {"Jane", "Doe"};
		fInfo.info = "Fake Info";
		fInfo.inputStream = new ByteArrayInputStream(new byte[] {88,44,22,11});
		fInfo.pixelWidth = 47.0;
		fInfo.pixelHeight = 23.5;
		fInfo.pixelDepth = 4.0;
		fInfo.unit = "nanometer";
		fInfo.calibrationFunction = 88;
		fInfo.coefficients = new double[] {100.0, 6.3, 94.2};
		fInfo.valueUnit = "valueUnit";
		fInfo.frameInterval = 14.0;
		fInfo.description = "descrip";
		fInfo.longOffset = 1024L;
		fInfo.metaDataTypes = new int[] {-3, 66, 82, 101, 2048};
		fInfo.metaData = new byte[][] {{1,1,1},{2,2,2},{3,3,3}};
		fInfo.displayRanges = new double[] {7,6,5,4};
		fInfo.channelLuts = new byte[][] {{1},{2},{3}};
		fInfo.samplesPerPixel = 42;

		assertEquals(true,true);
	}
	
	@Test
	public void testGetBytesPerPixel() {
		fInfo.fileType = FileInfo.GRAY8;
		assertEquals(fInfo.getBytesPerPixel(),1);
	
		fInfo.fileType = FileInfo.COLOR8;
		assertEquals(fInfo.getBytesPerPixel(),1);

		fInfo.fileType = FileInfo.BITMAP;
		assertEquals(fInfo.getBytesPerPixel(),1);
		
		fInfo.fileType = FileInfo.GRAY16_SIGNED;
		assertEquals(fInfo.getBytesPerPixel(),2);
		
		fInfo.fileType = FileInfo.GRAY16_UNSIGNED;
		assertEquals(fInfo.getBytesPerPixel(),2);
		
		fInfo.fileType = FileInfo.GRAY32_UNSIGNED;
		assertEquals(fInfo.getBytesPerPixel(),4);
		
		fInfo.fileType = FileInfo.GRAY32_INT;
		assertEquals(fInfo.getBytesPerPixel(),4);
		
		fInfo.fileType = FileInfo.GRAY32_FLOAT;
		assertEquals(fInfo.getBytesPerPixel(),4);
		
		fInfo.fileType = FileInfo.ARGB;
		assertEquals(fInfo.getBytesPerPixel(),4);
		
		fInfo.fileType = FileInfo.BARG;
		assertEquals(fInfo.getBytesPerPixel(),4);
		
		fInfo.fileType = FileInfo.RGB;
		assertEquals(fInfo.getBytesPerPixel(),3);
		
		fInfo.fileType = FileInfo.BGR;
		assertEquals(fInfo.getBytesPerPixel(),3);
		
		fInfo.fileType = FileInfo.RGB_PLANAR;
		assertEquals(fInfo.getBytesPerPixel(),3);
		
		fInfo.fileType = FileInfo.RGB48;
		assertEquals(fInfo.getBytesPerPixel(),6);
		
		fInfo.fileType = FileInfo.RGB48_PLANAR;
		assertEquals(fInfo.getBytesPerPixel(),6);
		
		fInfo.fileType = FileInfo.GRAY64_FLOAT;
		assertEquals(fInfo.getBytesPerPixel(),8);
		
		fInfo.fileType = FileInfo.GRAY24_UNSIGNED;
		assertEquals(fInfo.getBytesPerPixel(),4);
		
		fInfo.fileType = 99999;
		assertEquals(fInfo.getBytesPerPixel(),0);

		fInfo.fileType = -1;
		assertEquals(fInfo.getBytesPerPixel(),0);
	}

	@Test
	public void testToString() {
		// expected values generated by original IJ code from given inputs
		// this test is more for the future than the current implementation
		String expVal = "name=Untitled, dir=, url=, width=0, height=0, nImages=1, type=byte, offset=0, whiteZero=f, Intel=f, lutSize=0, comp=1, ranges=null, samples=1";
		
		assertEquals(fInfo.toString(),expVal);
		
		fInfo.fileName = "Joe's file";
		fInfo.directory = "/data/joe";
		fInfo.url = "http://loci.wisc.edu";
		fInfo.width = 75;
		fInfo.height = 133;
		fInfo.nImages = 17;
		fInfo.fileType = FileInfo.GRAY32_FLOAT;
		fInfo.offset = 48;
		fInfo.whiteIsZero = true;
		fInfo.intelByteOrder = true;
		fInfo.lutSize = 106;
		fInfo.compression = FileInfo.COMPRESSION_UNKNOWN;
		fInfo.displayRanges = new double [4];
		fInfo.samplesPerPixel = 405;
		
		expVal = "name=Joe's file, dir=/data/joe, url=http://loci.wisc.edu, width=75, height=133, nImages=17, type=float, offset=48, whiteZero=t, Intel=t, lutSize=106, comp=0, ranges=2, samples=405";
		assertEquals(fInfo.toString(),expVal);		
	}

	@Test
	public void testClone() {
		
		Object obj = fInfo.clone();
		
		assertNotNull(obj);

		assertTrue(obj instanceof FileInfo);
	}

}
