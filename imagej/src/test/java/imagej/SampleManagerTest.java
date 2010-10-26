package imagej;

import static org.junit.Assert.*;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;
import imagej.SampleInfo.ValueType;
import imagej.process.ImageUtils;
import imagej.process.ImgLibProcessor;

import mpicbg.imglib.container.array.ArrayContainerFactory;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.logic.BitType;
import mpicbg.imglib.type.numeric.integer.ByteType;
import mpicbg.imglib.type.numeric.integer.IntType;
import mpicbg.imglib.type.numeric.integer.LongType;
import mpicbg.imglib.type.numeric.integer.ShortType;
import mpicbg.imglib.type.numeric.integer.Unsigned12BitType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import mpicbg.imglib.type.numeric.integer.UnsignedIntType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;
import mpicbg.imglib.type.numeric.real.DoubleType;
import mpicbg.imglib.type.numeric.real.FloatType;

import org.junit.Test;

public class SampleManagerTest {

	@Test
	public void testGetRealType()
	{
		assertTrue(SampleManager.getRealType(SampleInfo.ValueType.BIT) instanceof BitType);
		assertTrue(SampleManager.getRealType(SampleInfo.ValueType.BYTE) instanceof ByteType);
		assertTrue(SampleManager.getRealType(SampleInfo.ValueType.UBYTE) instanceof UnsignedByteType);
		assertTrue(SampleManager.getRealType(SampleInfo.ValueType.UINT12) instanceof Unsigned12BitType);
		assertTrue(SampleManager.getRealType(SampleInfo.ValueType.SHORT) instanceof ShortType);
		assertTrue(SampleManager.getRealType(SampleInfo.ValueType.USHORT) instanceof UnsignedShortType);
		assertTrue(SampleManager.getRealType(SampleInfo.ValueType.INT) instanceof IntType);
		assertTrue(SampleManager.getRealType(SampleInfo.ValueType.UINT) instanceof UnsignedIntType);
		assertTrue(SampleManager.getRealType(SampleInfo.ValueType.FLOAT) instanceof FloatType);
		assertTrue(SampleManager.getRealType(SampleInfo.ValueType.DOUBLE) instanceof DoubleType);
		assertTrue(SampleManager.getRealType(SampleInfo.ValueType.LONG) instanceof LongType);
	}

	@Test
	public void testGetSampleInfo() 
	{
		SampleInfo info;
		
		info = SampleManager.getSampleInfo(ValueType.BIT);
		assertEquals("1-bit unsigned",info.getName());
		assertEquals(1,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(1,info.getNumBits());
		assertEquals(ValueType.BIT,info.getValueType());
		assertFalse(info.isFloat());
		assertTrue(info.isIntegral());
		assertFalse(info.isSigned());
		assertTrue(info.isUnsigned());
		
		info = SampleManager.getSampleInfo(ValueType.BYTE);
		assertEquals("8-bit signed",info.getName());
		assertEquals(8,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(8,info.getNumBits());
		assertEquals(ValueType.BYTE,info.getValueType());
		assertFalse(info.isFloat());
		assertTrue(info.isIntegral());
		assertTrue(info.isSigned());
		assertFalse(info.isUnsigned());

		info = SampleManager.getSampleInfo(ValueType.UBYTE);
		assertEquals("8-bit unsigned",info.getName());
		assertEquals(8,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(8,info.getNumBits());
		assertEquals(ValueType.UBYTE,info.getValueType());
		assertFalse(info.isFloat());
		assertTrue(info.isIntegral());
		assertFalse(info.isSigned());
		assertTrue(info.isUnsigned());

		info = SampleManager.getSampleInfo(ValueType.UINT12);
		assertEquals("12-bit unsigned",info.getName());
		assertEquals(12,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(12,info.getNumBits());
		assertEquals(ValueType.UINT12,info.getValueType());
		assertFalse(info.isFloat());
		assertTrue(info.isIntegral());
		assertFalse(info.isSigned());
		assertTrue(info.isUnsigned());

		info = SampleManager.getSampleInfo(ValueType.SHORT);
		assertEquals("16-bit signed",info.getName());
		assertEquals(16,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(16,info.getNumBits());
		assertEquals(ValueType.SHORT,info.getValueType());
		assertFalse(info.isFloat());
		assertTrue(info.isIntegral());
		assertTrue(info.isSigned());
		assertFalse(info.isUnsigned());

		info = SampleManager.getSampleInfo(ValueType.USHORT);
		assertEquals("16-bit unsigned",info.getName());
		assertEquals(16,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(16,info.getNumBits());
		assertEquals(ValueType.USHORT,info.getValueType());
		assertFalse(info.isFloat());
		assertTrue(info.isIntegral());
		assertFalse(info.isSigned());
		assertTrue(info.isUnsigned());

		info = SampleManager.getSampleInfo(ValueType.INT);
		assertEquals("32-bit signed",info.getName());
		assertEquals(32,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(32,info.getNumBits());
		assertEquals(ValueType.INT,info.getValueType());
		assertFalse(info.isFloat());
		assertTrue(info.isIntegral());
		assertTrue(info.isSigned());
		assertFalse(info.isUnsigned());

		info = SampleManager.getSampleInfo(ValueType.UINT);
		assertEquals("32-bit unsigned",info.getName());
		assertEquals(32,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(32,info.getNumBits());
		assertEquals(ValueType.UINT,info.getValueType());
		assertFalse(info.isFloat());
		assertTrue(info.isIntegral());
		assertFalse(info.isSigned());
		assertTrue(info.isUnsigned());

		info = SampleManager.getSampleInfo(ValueType.FLOAT);
		assertEquals("32-bit float",info.getName());
		assertEquals(32,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(32,info.getNumBits());
		assertEquals(ValueType.FLOAT,info.getValueType());
		assertTrue(info.isFloat());
		assertFalse(info.isIntegral());
		assertTrue(info.isSigned());
		assertFalse(info.isUnsigned());

		info = SampleManager.getSampleInfo(ValueType.DOUBLE);
		assertEquals("64-bit float",info.getName());
		assertEquals(64,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(64,info.getNumBits());
		assertEquals(ValueType.DOUBLE,info.getValueType());
		assertTrue(info.isFloat());
		assertFalse(info.isIntegral());
		assertTrue(info.isSigned());
		assertFalse(info.isUnsigned());

		info = SampleManager.getSampleInfo(ValueType.LONG);
		assertEquals("64-bit signed",info.getName());
		assertEquals(64,info.getNumBitsPerValue());
		assertEquals(1,info.getNumValues());
		assertEquals(64,info.getNumBits());
		assertEquals(ValueType.LONG,info.getValueType());
		assertFalse(info.isFloat());
		assertTrue(info.isIntegral());
		assertTrue(info.isSigned());
		assertFalse(info.isUnsigned());
	}

	@Test
	public void testGetValueTypeRealType()
	{
		assertEquals(ValueType.BIT, SampleManager.getValueType(new BitType()));
		assertEquals(ValueType.BYTE, SampleManager.getValueType(new ByteType()));
		assertEquals(ValueType.UBYTE, SampleManager.getValueType(new UnsignedByteType()));
		assertEquals(ValueType.UINT12, SampleManager.getValueType(new Unsigned12BitType()));
		assertEquals(ValueType.SHORT, SampleManager.getValueType(new ShortType()));
		assertEquals(ValueType.USHORT, SampleManager.getValueType(new UnsignedShortType()));
		assertEquals(ValueType.INT, SampleManager.getValueType(new IntType()));
		assertEquals(ValueType.UINT, SampleManager.getValueType(new UnsignedIntType()));
		assertEquals(ValueType.FLOAT, SampleManager.getValueType(new FloatType()));
		assertEquals(ValueType.DOUBLE, SampleManager.getValueType(new DoubleType()));
		assertEquals(ValueType.LONG, SampleManager.getValueType(new LongType()));
	}

	@Test
	public void testGetValueTypeImageProcessor()
	{
		ByteProcessor bProc = new ByteProcessor(1, 1, new byte[1], null);
		assertEquals(ValueType.UBYTE, SampleManager.getValueType(bProc));
		
		ShortProcessor sProc = new ShortProcessor(1, 1, new short[1], null);
		assertEquals(ValueType.USHORT, SampleManager.getValueType(sProc));

		FloatProcessor fProc = new FloatProcessor(1, 1, new float[1], null);
		assertEquals(ValueType.FLOAT, SampleManager.getValueType(fProc));

		ColorProcessor cProc = new ColorProcessor(1, 1, new int[1]);
		assertEquals(ValueType.UINT, SampleManager.getValueType(cProc));

		Image<UnsignedShortType> image =
			ImageUtils.createImage(new UnsignedShortType(), new ArrayContainerFactory(), new int[]{6,4,2});
		
		ImgLibProcessor<?> iProc = new ImgLibProcessor<UnsignedShortType>(image, 0);
		
		assertEquals(ValueType.USHORT, SampleManager.getValueType(iProc));
	}

	@Test
	public void testGetValueTypeImagePlus()
	{
		ImagePlus imp;
		
		ByteProcessor bProc = new ByteProcessor(1, 1, new byte[1], null);
		imp = new ImagePlus("zacko", bProc);
		assertEquals(ValueType.UBYTE, SampleManager.getValueType(imp));
		
		ShortProcessor sProc = new ShortProcessor(1, 1, new short[1], null);
		imp = new ImagePlus("zacko", sProc);
		assertEquals(ValueType.USHORT, SampleManager.getValueType(imp));

		FloatProcessor fProc = new FloatProcessor(1, 1, new float[1], null);
		imp = new ImagePlus("zacko", fProc);
		assertEquals(ValueType.FLOAT, SampleManager.getValueType(imp));

		ColorProcessor cProc = new ColorProcessor(1, 1, new int[1]);
		imp = new ImagePlus("zacko", cProc);
		assertEquals(ValueType.UINT, SampleManager.getValueType(imp));

		Image<FloatType> image =
			ImageUtils.createImage(new FloatType(), new ArrayContainerFactory(), new int[]{6,4,2});
		ImgLibProcessor<?> iProc = new ImgLibProcessor<FloatType>(image, 0);
		imp = new ImagePlus("zacko", iProc);
		assertEquals(ValueType.FLOAT, SampleManager.getValueType(imp));
	}

	@Test
	public void testFindSampleInfo()
	{
		assertNull(SampleManager.findSampleInfo(null));
		assertNull(SampleManager.findSampleInfo("ShakyJones"));
		assertEquals(SampleManager.getSampleInfo(ValueType.BIT), SampleManager.findSampleInfo("1-bit unsigned"));
		assertEquals(SampleManager.getSampleInfo(ValueType.BYTE), SampleManager.findSampleInfo("8-bit signed"));
		assertEquals(SampleManager.getSampleInfo(ValueType.UBYTE), SampleManager.findSampleInfo("8-bit unsigned"));
		assertEquals(SampleManager.getSampleInfo(ValueType.UINT12), SampleManager.findSampleInfo("12-bit unsigned"));
		assertEquals(SampleManager.getSampleInfo(ValueType.SHORT), SampleManager.findSampleInfo("16-bit signed"));
		assertEquals(SampleManager.getSampleInfo(ValueType.USHORT), SampleManager.findSampleInfo("16-bit unsigned"));
		assertEquals(SampleManager.getSampleInfo(ValueType.INT), SampleManager.findSampleInfo("32-bit signed"));
		assertEquals(SampleManager.getSampleInfo(ValueType.UINT), SampleManager.findSampleInfo("32-bit unsigned"));
		assertEquals(SampleManager.getSampleInfo(ValueType.FLOAT), SampleManager.findSampleInfo("32-bit float"));
		assertEquals(SampleManager.getSampleInfo(ValueType.DOUBLE), SampleManager.findSampleInfo("64-bit float"));
		assertEquals(SampleManager.getSampleInfo(ValueType.LONG), SampleManager.findSampleInfo("64-bit signed"));
	}

}
