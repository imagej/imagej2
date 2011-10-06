package ij.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import ij.Assert;
import ij.CompositeImage;
import ij.ImagePlus;
import ij.process.DataConstants;
import ij.process.ShortProcessor;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import org.junit.Test;

public class FileOpenerTest {

	FileOpener fo;

	@Test
	public void testFileOpener() {
		// define one on a null file
		FileOpener opener = new FileOpener(null);
		assertNotNull(opener);

		// define one on a default file info object
		FileInfo info = new FileInfo();
		opener = new FileOpener(info);
		assertNotNull(opener);

		// define one on a modified FileInfo
		info.fileName = "miniTiled.tif";
		info.fileType = FileInfo.GRAY8;
		opener = new FileOpener(info);
		assertNotNull(opener);
	}

	// note - no need to test this - it just does a open(true) and true means display results which we can't test
	@Test
	public void testOpen() {
	}

	// note - only testing false case - true means display results which we can't test
	@Test
	public void testOpenBoolean() {
		FileInfo info;
		ImagePlus ip;

		// test for a non existent file

		// ideally open() would pass back the exception for an unknown file. As it is something lower catches the
		// exception, prints a stack trace, and does nothing

		info = new FileInfo();
		info.fileName = "SuperPuppy.tif";
		info.fileType = FileInfo.GRAY8;
		info.height = 3; // required
		info.width = 4; // required
		fo = new FileOpener(info);
		ip = fo.open(false);
		assertNull(ip);

		// test for a real 8 bit file
		info = new FileInfo();
		info.fileName = "gray8-2x3-sub1.tif";
		info.fileType = FileInfo.GRAY8;
		info.height = 2;
		info.width = 3;
		info.directory = DataConstants.DATA_DIR;
		fo = new FileOpener(info);
		ip = fo.open(false);
		assertNotNull(ip);
		assertEquals(8,ip.getBitDepth());
		assertEquals(1,ip.getBytesPerPixel());
		assertEquals(2,ip.getHeight());
		assertEquals(3,ip.getWidth());

		// test for a real 16 bit file
		info = new FileInfo();
		info.fileName = "gray16-2x3-sub1.tif";
		info.fileType = FileInfo.GRAY16_UNSIGNED;
		info.height = 2;
		info.width = 3;
		info.directory = DataConstants.DATA_DIR;
		fo = new FileOpener(info);
		ip = fo.open(false);
		assertNotNull(ip);
		assertEquals(16,ip.getBitDepth());
		assertEquals(2,ip.getBytesPerPixel());
		assertEquals(2,ip.getHeight());
		assertEquals(3,ip.getWidth());

		// test for a real 32 bit file
		info = new FileInfo();
		info.fileName = "gray32float-2x3-sub1.tif";
		info.fileType = FileInfo.GRAY32_FLOAT;
		info.height = 2;
		info.width = 3;
		info.directory = DataConstants.DATA_DIR;
		fo = new FileOpener(info);
		ip = fo.open(false);
		assertNotNull(ip);
		assertEquals(32,ip.getBitDepth());
		assertEquals(4,ip.getBytesPerPixel());
		assertEquals(2,ip.getHeight());
		assertEquals(3,ip.getWidth());

		// test for a real 24 bit file
		info = new FileInfo();
		info.fileName = "gray24-2x3-sub1.tif";
		info.fileType = FileInfo.RGB;
		info.height = 2;
		info.width = 3;
		info.directory = DataConstants.DATA_DIR;
		fo = new FileOpener(info);
		ip = fo.open(false);
		assertNotNull(ip);
		assertEquals(24,ip.getBitDepth());
		assertEquals(4,ip.getBytesPerPixel());
		assertEquals(2,ip.getHeight());
		assertEquals(3,ip.getWidth());
		assertEquals(1,ip.getNChannels());
		assertEquals(1,ip.getNSlices());
		assertEquals(1,ip.getNFrames());

		// test for a 48 bit file - use fake data for now - also testing Property() setting/getting
		info = new FileInfo();
		info.fileName = "head8bit.tif";
		info.fileType = FileInfo.RGB48_PLANAR;
		info.height = 256;
		info.width = 38;
		info.directory = DataConstants.DATA_DIR;
		info.info = "Yuletide Greetings";
		info.sliceLabels = new String[] {"Carrots"};
		fo = new FileOpener(info);
		ip = fo.open(false);
		assertNotNull(ip);
		assertEquals(16,ip.getBitDepth());
		assertEquals(2,ip.getBytesPerPixel());
		assertEquals(256,ip.getHeight());
		assertEquals(38,ip.getWidth());
		assertTrue(ip.getProcessor() instanceof ShortProcessor);
		assertNotNull(ip.getStack());
		assertEquals("Red",ip.getStack().getSliceLabel(1));
		assertEquals("Green",ip.getStack().getSliceLabel(2));
		assertEquals("Blue",ip.getStack().getSliceLabel(3));
		assertTrue(ip instanceof CompositeImage);
		assertNotNull(ip.getStack());
		assertEquals("Red",ip.getStack().getSliceLabel(1));
		assertEquals("Green",ip.getStack().getSliceLabel(2));
		assertEquals("Blue",ip.getStack().getSliceLabel(3));
		assertTrue(ip instanceof CompositeImage);
		assertEquals("Yuletide Greetings",ip.getProperty("Info"));
		assertEquals("Carrots",ip.getProperty("Label"));

		// try to open an image stack
		info = new FileInfo();
		info.fileName = "gray8-2x3-stack.tif";
		info.fileType = FileInfo.GRAY8;
		info.nImages = 2;
		info.height = 2;
		info.width = 3;
		info.directory = DataConstants.DATA_DIR;
		fo = new FileOpener(info);
		ip = fo.open(false);
		assertNotNull(ip);
		assertNotNull(ip.getStack());
		assertEquals(2,ip.getStack().getSize());
		assertEquals(2,ip.getStack().getHeight());
		assertEquals(3,ip.getStack().getWidth());
	}

	@Test
	public void testCreateColorModel() {
		FileInfo fi;
		ColorModel cm;
		byte[] lutVals = new byte[] {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};

		// test non COLOR8 file
		fi = new FileInfo();
		fi.fileType = FileInfo.GRAY8;
		fo = new FileOpener(fi);
		cm = fo.createColorModel(fi);
		assertTrue(cm instanceof IndexColorModel);
		// assert its a gray scale lut
		for (int i = 0; i < 256; i++)
		{
			assertEquals(i,cm.getRed(i));
			assertEquals(i,cm.getGreen(i));
			assertEquals(i,cm.getBlue(i));
		}

		// test a COLOR8 file with no attached lut data
		fi = new FileInfo();
		fi.fileType = FileInfo.COLOR8;
		fi.lutSize = 0;
		fo = new FileOpener(fi);
		cm = fo.createColorModel(fi);
		assertTrue(cm instanceof IndexColorModel);
		// assert its a gray scale lut
		for (int i = 0; i < 256; i++)
		{
			assertEquals(i,cm.getRed(i));
			assertEquals(i,cm.getGreen(i));
			assertEquals(i,cm.getBlue(i));
		}

		// test a COLOR8 file with lut info
		fi = new FileInfo();
		fi.fileType = FileInfo.COLOR8;
		fi.lutSize = lutVals.length;
		fi.reds   = lutVals;
		fi.greens = lutVals;
		fi.blues  = lutVals;
		fo = new FileOpener(fi);
		cm = fo.createColorModel(fi);
		assertTrue(cm instanceof IndexColorModel);
		for (int i = 0; i < lutVals.length; i++)
		{
			assertEquals(lutVals[i],cm.getRed(i));
			assertEquals(lutVals[i],cm.getGreen(i));
			assertEquals(lutVals[i],cm.getBlue(i));
		}
	}

	@Test
	public void testCreateInputStream() {
		FileInfo fi;
		InputStream istr;

		// note - won't test URL subcase

		// fi.inputStream != null
		fi = new FileInfo();
		fi.inputStream = new ByteArrayInputStream(new byte[] {1,2,3,4,5,6});
		fi.height = 2;
		fi.width = 3;
		fo = new FileOpener(fi);
		try {
			istr = fo.createInputStream(fi);
			assertNotNull(istr);
		} catch (Exception e) {
			fail();
		}

		// fi.inputStream != null and gzip
		fi = new FileInfo();
		fi.height = 2;
		fi.width = 3;
		fi.directory = DataConstants.DATA_DIR;
		fi.fileName = "fake.gz";
		try {
			fi.inputStream = new FileInputStream(DataConstants.DATA_DIR + "fake.gz");
			fo = new FileOpener(fi);
			istr = fo.createInputStream(fi);
			assertNotNull(istr);
			assertTrue(istr instanceof GZIPInputStream);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			fail();
		}

		// fi.inputStream != null and compression >= lzw
		fi = new FileInfo();
		fi.inputStream = new ByteArrayInputStream(new byte[] {1,2,3,4,5,6});
		fi.height = 2;
		fi.width = 3;
		fi.compression = FileInfo.LZW;
		fo = new FileOpener(fi);
		try {
			istr = fo.createInputStream(fi);
			assertNotNull(istr);
			assertTrue(istr instanceof RandomAccessStream);
		} catch (Exception e) {
			fail();
		}

		// fi.inputStream == null -> try to open file
		//   file is null
		//     can't get File to be null -- even on null input - this test may be impossible

		// fi.inputStream == null -> try to open file
		//   file is directory
		fi = new FileInfo();
		// use a known directory name
		fi.fileName = "data";
		fo = new FileOpener(fi);
		try {
			istr = fo.createInputStream(fi);
			assertNull(istr);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			fail();
		}

		// fi.inputStream == null -> try to open file
		//   can't validateFileInfo(file,fi)
		fi = new FileInfo();
		fo = new FileOpener(fi);
		// 0 width and height should cause this subcase
		try {
			istr = fo.createInputStream(fi);
			assertNull(istr);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			fail();
		}
	}

	@Test
	public void testDecodeDescriptionString() {
		FileInfo fi;
		Properties p;

		// fi.decription == null
		fi = new FileInfo();
		fo = new FileOpener(fi);
		assertNull(fo.decodeDescriptionString(fi));

		// fi.decription len < 7
		fi = new FileInfo();
		fi.description = "123456";
		fo = new FileOpener(fi);
		assertNull(fo.decodeDescriptionString(fi));

		// fi.decription not start with ImageJ
		fi = new FileInfo();
		fi.description = "NotImageJ";
		fo = new FileOpener(fi);
		assertNull(fo.decodeDescriptionString(fi));

		// otherwise from here on out descrip must start with ImageJ
		fi = new FileInfo();
		fi.description = "ImageJ=1.43n1\nc0=0\nc1=1\nc2=2\nc3=3\nc4=4\nunit=millidevs\ncf=13\nvunit=peeps\nimages=704\nspacing=-48.5";
		fo = new FileOpener(fi);
		p = fo.decodeDescriptionString(fi);
		assertNotNull(p);
		assertEquals("1.43n1",p.getProperty("ImageJ"));
		assertEquals("704",p.getProperty("images"));
		assertEquals(704,fi.nImages);
		assertEquals("13",p.getProperty("cf"));
		assertEquals(13,fi.calibrationFunction);
		assertEquals("millidevs",p.getProperty("unit"));
		assertEquals("millidevs",fi.unit);
		assertEquals("peeps",p.getProperty("vunit"));
		assertEquals("peeps",fi.valueUnit);
		assertEquals("-48.5",p.getProperty("spacing"));
		assertEquals(48.5,fi.pixelDepth,Assert.DOUBLE_TOL);
		assertEquals("0",p.getProperty("c0"));
		assertEquals(0,fi.coefficients[0],Assert.DOUBLE_TOL);
		assertEquals("1",p.getProperty("c1"));
		assertEquals(1,fi.coefficients[1],Assert.DOUBLE_TOL);
		assertEquals("2",p.getProperty("c2"));
		assertEquals(2,fi.coefficients[2],Assert.DOUBLE_TOL);
		assertEquals("3",p.getProperty("c3"));
		assertEquals(3,fi.coefficients[3],Assert.DOUBLE_TOL);
		assertEquals("4",p.getProperty("c4"));
		assertEquals(4,fi.coefficients[4],Assert.DOUBLE_TOL);
	}

	@Test
	public void testSetShowConflictMessage() {
		// nothing is testable except existence
		FileOpener.setShowConflictMessage(true);
		FileOpener.setShowConflictMessage(false);
	}


	// In method below
	//   open image
	//   save orig info
	//   make some changes
	//   assert their values
	//   revert
	//   assert vals back to original

	private void expectSuccess(String fname, int format, int nImages, int height, int width)
	{
		FileInfo fi;
		ImagePlus ip;
		int origPix,newPix;

		fi = new FileInfo();
		fi.directory = DataConstants.DATA_DIR;
		fi.fileName = fname;
		fi.fileFormat = format;
		fi.nImages = nImages;
		fi.height = height;
		fi.width = width;
		fo = new FileOpener(fi);
		//ip = fo.open(false);
		ip = new Opener().openImage(DataConstants.DATA_DIR,fname);
		assertNotNull(ip);
		origPix = ip.getProcessor().getPixel(0,0);
		ip.getProcessor().set(0,0,origPix+1);
		newPix = ip.getProcessor().getPixel(0,0);
		if (origPix == newPix)
			fail("Failed to set pixel correctly");
		//else
		//	System.out.println("OrigPix = "+origPix+" and newPix = "+newPix);
		fo.revertToSaved(ip);
		assertEquals(origPix,ip.getProcessor().getPixel(0,0));
	}

	private void expectSuccessReadPixelsCase(String fname, int format, int height, int width)
	{
		FileInfo fi;
		ImagePlus ip;
		int origPix,newPix;

		fi = new FileInfo();
		fi.directory = DataConstants.DATA_DIR;
		fi.fileName = fname;
		fi.fileFormat = format;
		fi.nImages = 1;
		fi.height = height;
		fi.width = width;
		fo = new FileOpener(fi);
		ip = fo.open(false);
		//ip = new Opener().openImage(DataConstants.DATA_DIR,fname);
		assertNotNull(ip);
		origPix = ip.getProcessor().getPixel(0,0);
		ip.getProcessor().set(0,0,origPix+1);
		newPix = ip.getProcessor().getPixel(0,0);
		if (origPix == newPix)
			fail("Failed to set pixel correctly");
		//else
		//	System.out.println("OrigPix = "+origPix+" and newPix = "+newPix);
		fo.revertToSaved(ip);
		assertEquals(origPix,ip.getProcessor().getPixel(0,0));
	}

	private void expectFailureReadPixelsCase(String fname, int format, int nImages, int height, int width)
	{
		FileInfo fi;
		ImagePlus ip;
		int origPix,newPix;

		fi = new FileInfo();
		fi.directory = DataConstants.DATA_DIR;
		fi.fileName = fname;
		fi.fileFormat = format;
		fi.nImages = nImages;
		fi.height = height;
		fi.width = width;
		fo = new FileOpener(fi);
		ip = fo.open(false);
		//ip = new Opener().openImage(DataConstants.DATA_DIR,fname);
		assertNotNull(ip);
		origPix = ip.getProcessor().getPixel(0,0);
		ip.getProcessor().set(0,0,origPix+1);
		newPix = ip.getProcessor().getPixel(0,0);
		if (origPix == newPix)
			fail("Failed to set pixel correctly");
		//else
		//	System.out.println("OrigPix = "+origPix+" and newPix = "+newPix);
		fo.revertToSaved(ip);
		assertFalse(origPix == ip.getProcessor().getPixel(0,0));
	}

	@Test
	public void testRevertToSaved() {

		// various file formats - supported ones
		expectSuccess("blobs.gif",FileInfo.GIF_OR_JPG,1,254,256);
		expectSuccess("Cell_Colony.jpg",FileInfo.GIF_OR_JPG,1,408,406);
		expectSuccess("embryos.bmp",FileInfo.BMP,1,1200,1600);
		expectSuccess("Tree_Rings.pgm",FileInfo.PGM,1,162,1796);
		expectSuccess("bat-cochlea-renderings.fits",FileInfo.FITS,1,154,284);
		expectSuccess("gray16.zip",FileInfo.ZIP_ARCHIVE,1,154,284);
		expectSuccess("lena-std.png",FileInfo.IMAGEIO,1,154,284);
		expectSuccess("01.dcm",FileInfo.DICOM,1,426,640);

		// unlike other formats tifs fall through to readPixels()
		expectSuccessReadPixelsCase("head8bit.tif",FileInfo.TIFF,228,256);

		// fall through case -> it calls readPixels()
		expectSuccessReadPixelsCase("clown.raw",FileInfo.UNKNOWN,100,100);

		// nImages > 1 and not a special case file -> no reversion
		expectFailureReadPixelsCase("clown.raw",FileInfo.UNKNOWN,2,256,228);

	}

}
