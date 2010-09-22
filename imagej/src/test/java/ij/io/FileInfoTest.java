package ij.io;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import ij.io.FileInfo;

public class FileInfoTest {

	FileInfo fInfo = null;
	
	private void setFileInfoJunkData(FileInfo fInfo)
	{
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
	}
	
	// matches() - returns true if two FileInfo objects have the same contents
	
	private boolean matches(FileInfo fInfo, FileInfo other){
		if ((fInfo.fileFormat == other.fileFormat) &&
				(fInfo.fileType == other.fileType) &&
				(fInfo.fileName == other.fileName) &&
				(fInfo.directory == other.directory) &&
				(fInfo.url == other.url) &&
				(fInfo.width == other.width) &&
				(fInfo.height == other.height) &&
				(fInfo.offset == other.offset) &&
				(fInfo.nImages == other.nImages) &&
				(fInfo.gapBetweenImages == other.gapBetweenImages) &&
				(fInfo.whiteIsZero == other.whiteIsZero) &&
				(fInfo.intelByteOrder == other.intelByteOrder) &&
				(fInfo.compression == other.compression) &&
				(fInfo.stripOffsets == other.stripOffsets) &&
				(fInfo.stripLengths == other.stripLengths) &&
				(fInfo.rowsPerStrip == other.rowsPerStrip) &&
				(fInfo.lutSize == other.lutSize) &&
				(fInfo.reds == other.reds) &&
				(fInfo.greens == other.greens) &&
				(fInfo.blues == other.blues) &&
				(fInfo.pixels == other.pixels) &&
				(fInfo.debugInfo == other.debugInfo) &&
				(fInfo.sliceLabels == other.sliceLabels) &&
				(fInfo.info == other.info) &&
				(fInfo.inputStream == other.inputStream) &&
				(fInfo.pixelWidth == other.pixelWidth) &&
				(fInfo.pixelHeight == other.pixelHeight) &&
				(fInfo.pixelDepth == other.pixelDepth) &&
				(fInfo.unit == other.unit) &&
				(fInfo.calibrationFunction == other.calibrationFunction) &&
				(fInfo.coefficients == other.coefficients) &&
				(fInfo.valueUnit == other.valueUnit) &&
				(fInfo.frameInterval == other.frameInterval) &&
				(fInfo.description == other.description) &&
				(fInfo.longOffset == other.longOffset) &&
				(fInfo.metaDataTypes == other.metaDataTypes) &&
				(fInfo.metaData == other.metaData) &&
				(fInfo.displayRanges == other.displayRanges) &&
				(fInfo.channelLuts == other.channelLuts) &&
				(fInfo.samplesPerPixel == other.samplesPerPixel))		
		  return true;
		
		return false; // some difference detected
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
		
	   	// check that the constructor's default values match what we expect
		
		assertEquals(FileInfo.UNKNOWN,fInfo.fileFormat);
		assertEquals(FileInfo.GRAY8,fInfo.fileType);
		assertEquals("Untitled",fInfo.fileName);
		assertEquals("",fInfo.directory);
		assertEquals("",fInfo.url);
		assertEquals(1,fInfo.nImages);
		assertEquals(FileInfo.COMPRESSION_NONE,fInfo.compression);
		assertEquals(1,fInfo.samplesPerPixel);    	
		assertEquals(0,fInfo.width);
		assertEquals(0,fInfo.height);
		assertEquals(0,fInfo.offset);
		assertEquals(0,fInfo.gapBetweenImages);
		assertEquals(false,fInfo.whiteIsZero);
		assertEquals(false,fInfo.intelByteOrder);
		assertNull(fInfo.stripOffsets); 
		assertNull(fInfo.stripLengths);
		assertEquals(0,fInfo.rowsPerStrip);
		assertEquals(0,fInfo.lutSize);
		assertNull(fInfo.reds);
		assertNull(fInfo.greens);
		assertNull(fInfo.blues);
		assertNull(fInfo.pixels);	
		assertNull(fInfo.debugInfo);
		assertNull(fInfo.sliceLabels);
		assertNull(fInfo.info);
		assertNull(fInfo.inputStream);
		assertEquals(1.0,fInfo.pixelWidth,0.0);
		assertEquals(1.0,fInfo.pixelHeight,0.0);
		assertEquals(1.0,fInfo.pixelDepth,0.0);
		assertNull(fInfo.unit);
		assertEquals(0,fInfo.calibrationFunction);
		assertNull(fInfo.coefficients);
		assertNull(fInfo.valueUnit);
		assertEquals(0.0,fInfo.frameInterval,0.0);
		assertNull(fInfo.description);
		assertEquals(0L,fInfo.longOffset);
		assertNull(fInfo.metaDataTypes);
		assertNull(fInfo.metaData);
		assertNull(fInfo.displayRanges);
		assertNull(fInfo.channelLuts);
	}

	@Test
	public void testFileInfoConstants(){
		
		// test the existence and value of all the public constants
		//   this will make sure that the all the constants exist and have the right values
		
		// Sample layouts
		assertEquals(0,FileInfo.GRAY8);
		assertEquals(1,FileInfo.GRAY16_SIGNED);
		assertEquals(2,FileInfo.GRAY16_UNSIGNED);
		assertEquals(3,FileInfo.GRAY32_INT);
		assertEquals(4,FileInfo.GRAY32_FLOAT);
		assertEquals(5,FileInfo.COLOR8);
		assertEquals(6,FileInfo.RGB);	
		assertEquals(7,FileInfo.RGB_PLANAR);
		assertEquals(8,FileInfo.BITMAP);
		assertEquals(9,FileInfo.ARGB);
		assertEquals(10,FileInfo.BGR);
		assertEquals(11,FileInfo.GRAY32_UNSIGNED);
		assertEquals(12,FileInfo.RGB48);	
		assertEquals(13,FileInfo.GRAY12_UNSIGNED);	
		assertEquals(14,FileInfo.GRAY24_UNSIGNED);	
		assertEquals(15,FileInfo.BARG);	
		assertEquals(16,FileInfo.GRAY64_FLOAT);	
		assertEquals(17,FileInfo.RGB48_PLANAR);	
		assertEquals(18,FileInfo.ABGR);	
		assertEquals(19,FileInfo.GRAY32_SIGNED);	
		assertEquals(20,FileInfo.GRAY64_SIGNED);	

		// File formats
		assertEquals(0,FileInfo.UNKNOWN);
		assertEquals(1,FileInfo.RAW);
		assertEquals(2,FileInfo.TIFF);
		assertEquals(3,FileInfo.GIF_OR_JPG);
		assertEquals(4,FileInfo.FITS);
		assertEquals(5,FileInfo.BMP);
		assertEquals(6,FileInfo.DICOM);
		assertEquals(7,FileInfo.ZIP_ARCHIVE);
		assertEquals(8,FileInfo.PGM);
		assertEquals(9,FileInfo.IMAGEIO);

		// Compression modes
		assertEquals(0,FileInfo.COMPRESSION_UNKNOWN);
		assertEquals(1,FileInfo.COMPRESSION_NONE);
		assertEquals(2,FileInfo.LZW);
		assertEquals(3,FileInfo.LZW_WITH_DIFFERENCING);
		assertEquals(4,FileInfo.JPEG);
		assertEquals(5,FileInfo.PACK_BITS);
		
	}
	
	@Test
	public void testFileInfoFields(){
		
		// test that we can set all the instance public vars
		//   this will make sure that later ports define all the same fields

		setFileInfoJunkData(fInfo);
		
		assertEquals(true,true);
	}
	
	@Test
	public void testGetBytesPerPixel() {
		
		// make sure getPytesPerPixel() is returning correct values for all possible fileTypes
		
		fInfo.fileType = FileInfo.GRAY8;
		assertEquals(1,fInfo.getBytesPerPixel());
	
		fInfo.fileType = FileInfo.COLOR8;
		assertEquals(1,fInfo.getBytesPerPixel());

		fInfo.fileType = FileInfo.BITMAP;
		assertEquals(1,fInfo.getBytesPerPixel());
		
		fInfo.fileType = FileInfo.GRAY16_SIGNED;
		assertEquals(2,fInfo.getBytesPerPixel());
		
		fInfo.fileType = FileInfo.GRAY16_UNSIGNED;
		assertEquals(2,fInfo.getBytesPerPixel());
		
		fInfo.fileType = FileInfo.GRAY32_UNSIGNED;
		assertEquals(4,fInfo.getBytesPerPixel());
		
		fInfo.fileType = FileInfo.GRAY32_INT;
		assertEquals(4,fInfo.getBytesPerPixel());
		
		fInfo.fileType = FileInfo.GRAY32_FLOAT;
		assertEquals(4,fInfo.getBytesPerPixel());
		
		fInfo.fileType = FileInfo.ARGB;
		assertEquals(4,fInfo.getBytesPerPixel());
		
		fInfo.fileType = FileInfo.BARG;
		assertEquals(4,fInfo.getBytesPerPixel());
		
		fInfo.fileType = FileInfo.RGB;
		assertEquals(3,fInfo.getBytesPerPixel());
		
		fInfo.fileType = FileInfo.BGR;
		assertEquals(3,fInfo.getBytesPerPixel());
		
		fInfo.fileType = FileInfo.RGB_PLANAR;
		assertEquals(3,fInfo.getBytesPerPixel());
		
		fInfo.fileType = FileInfo.RGB48;
		assertEquals(6,fInfo.getBytesPerPixel());
		
		fInfo.fileType = FileInfo.RGB48_PLANAR;
		assertEquals(6,fInfo.getBytesPerPixel());
		
		fInfo.fileType = FileInfo.GRAY64_FLOAT;
		assertEquals(8,fInfo.getBytesPerPixel());
		
		fInfo.fileType = FileInfo.GRAY24_UNSIGNED;
		assertEquals(4,fInfo.getBytesPerPixel());
		
		fInfo.fileType = 99999;
		assertEquals(0,fInfo.getBytesPerPixel());

		fInfo.fileType = -1;
		assertEquals(0,fInfo.getBytesPerPixel());
	}

	@Test
	public void testToString() {
		
		// expected values in this test generated by original IJ code from given inputs
		// this test is more for the future than the current implementation
		
		String expVal = "name=Untitled, dir=, url=, width=0, height=0, nImages=1, type=byte, format=0, offset=0, whiteZero=f, Intel=f, lutSize=0, comp=1, ranges=null, samples=1";
		
		// run toString on a default FileInfo and test versus expected
		
		assertEquals(expVal,fInfo.toString());
		
		// set all relevant FileInfo fields and try again 
		
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
		
		expVal = "name=Joe's file, dir=/data/joe, url=http://loci.wisc.edu, width=75, height=133, nImages=17, type=float, format=0, offset=48, whiteZero=t, Intel=t, lutSize=106, comp=0, ranges=2, samples=405";
		
		assertEquals(expVal,fInfo.toString());		
	}

	@Test
	public void testClone() {
		
		// call clone()
		Object obj = fInfo.clone();
		
		// make sure it is not null, its an instance of FileInfo, and that contents of clone matches original FileInfo
		assertNotNull(obj);
		assertTrue(obj instanceof FileInfo);
		assertTrue(matches(fInfo,(FileInfo)obj));
		
		// now set the fields of the original FileInfo to precooked data
		setFileInfoJunkData(fInfo);
		
		// call clone again
		obj = fInfo.clone();
		
		// make sure it is not null, its an instance of FileInfo, and that contents of clone matches original FileInfo
		assertNotNull(obj);
		assertTrue(obj instanceof FileInfo);
		assertTrue(matches(fInfo,(FileInfo)obj));
	}

}
