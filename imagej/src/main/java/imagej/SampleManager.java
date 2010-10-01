package imagej;

import imagej.SampleInfo.SampleType;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.ByteType;
import mpicbg.imglib.type.numeric.integer.IntType;
import mpicbg.imglib.type.numeric.integer.LongType;
import mpicbg.imglib.type.numeric.integer.ShortType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import mpicbg.imglib.type.numeric.integer.UnsignedIntType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;
import mpicbg.imglib.type.numeric.real.DoubleType;
import mpicbg.imglib.type.numeric.real.FloatType;

public class SampleManager {

	static SampleInfo[] sampleInfoArray;

	static RealType[] realTypeArray;
	
	static
	{
		sampleInfoArray = new SampleInfo[SampleType.values().length];
		
		sampleInfoArray[SampleType.BYTE.ordinal()] = new Sample8BitSigned();
		sampleInfoArray[SampleType.UBYTE.ordinal()] = new Sample8BitUnsigned();
		sampleInfoArray[SampleType.SHORT.ordinal()] = new Sample16BitSigned();
		sampleInfoArray[SampleType.USHORT.ordinal()] = new Sample16BitUnsigned();
		sampleInfoArray[SampleType.INT.ordinal()] = new Sample32BitSigned();
		sampleInfoArray[SampleType.UINT.ordinal()] = new Sample32BitUnsigned();
		sampleInfoArray[SampleType.FLOAT.ordinal()] = new Sample32BitFloat();
		sampleInfoArray[SampleType.LONG.ordinal()] = new Sample64BitSigned();
		sampleInfoArray[SampleType.DOUBLE.ordinal()] = new Sample64BitFloat();
		
		realTypeArray = new RealType[SampleType.values().length];

		realTypeArray[SampleType.BYTE.ordinal()] = new ByteType();
		realTypeArray[SampleType.UBYTE.ordinal()] = new UnsignedByteType();
		realTypeArray[SampleType.SHORT.ordinal()] = new ShortType();
		realTypeArray[SampleType.USHORT.ordinal()] = new UnsignedShortType();
		realTypeArray[SampleType.INT.ordinal()] = new IntType();
		realTypeArray[SampleType.UINT.ordinal()] = new UnsignedIntType();
		realTypeArray[SampleType.FLOAT.ordinal()] = new FloatType();
		realTypeArray[SampleType.LONG.ordinal()] = new LongType();
		realTypeArray[SampleType.DOUBLE.ordinal()] = new DoubleType();
	}
	
	private SampleManager() {}
	
	public static SampleType getSampleType(RealType<?> imglib)
	{
		SampleType sampleType;
		
		if (imglib instanceof ByteType)
			sampleType = SampleType.BYTE; 
		else if (imglib instanceof UnsignedByteType)
			sampleType = SampleType.UBYTE; 
		else if (imglib instanceof ShortType)
			sampleType = SampleType.SHORT; 
		else if (imglib instanceof UnsignedShortType)
			sampleType = SampleType.USHORT; 
		else if (imglib instanceof IntType)
			sampleType = SampleType.INT; 
		else if (imglib instanceof UnsignedIntType)
			sampleType = SampleType.UINT; 
		else if (imglib instanceof LongType)
			sampleType = SampleType.LONG; 
		else if (imglib instanceof FloatType)
			sampleType = SampleType.FLOAT; 
		else if (imglib instanceof DoubleType)
			sampleType = SampleType.DOUBLE; 
		else
			throw new IllegalArgumentException("unknown Imglib type : "+imglib);
		
		return sampleType;
	}
	
	public static RealType<?> getRealType(SampleType type)
	{
		return realTypeArray[type.ordinal()];
	}
	
	public static SampleInfo getSampleInfo(SampleType type)
	{
		return sampleInfoArray[type.ordinal()];
	}
	
	private static class Sample8BitSigned implements SampleInfo
	{
		public SampleType getValueType() { return SampleType.BYTE; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 8; }

		public int getNumBits() { return 8; }

		public String getName() { return "8-bit signed"; }
	}
	
	private static class Sample8BitUnsigned implements SampleInfo
	{
		public SampleType getValueType() { return SampleType.UBYTE; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 8; }

		public int getNumBits() { return 8; }

		public String getName() { return "8-bit unsigned"; }
	}
	
	private static class Sample16BitSigned implements SampleInfo
	{
		public SampleType getValueType() { return SampleType.SHORT; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 16; }

		public int getNumBits() { return 16; }

		public String getName() { return "16-bit signed"; }
	}
	
	private static class Sample16BitUnsigned implements SampleInfo
	{
		public SampleType getValueType() { return SampleType.USHORT; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 16; }

		public int getNumBits() { return 16; }

		public String getName() { return "16-bit unsigned"; }
	}
	
	private static class Sample32BitSigned implements SampleInfo
	{
		public SampleType getValueType() { return SampleType.INT; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 32; }

		public int getNumBits() { return 32; }

		public String getName() { return "32-bit signed"; }
	}
	
	private static class Sample32BitUnsigned implements SampleInfo
	{
		public SampleType getValueType() { return SampleType.UINT; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 32; }

		public int getNumBits() { return 32; }

		public String getName() { return "32-bit unsigned"; }
	}
	
	private static class Sample32BitFloat implements SampleInfo
	{
		public SampleType getValueType() { return SampleType.FLOAT; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 32; }

		public int getNumBits() { return 32; }

		public String getName() { return "32-bit float"; }
	}
	
	private static class Sample64BitSigned implements SampleInfo
	{
		public SampleType getValueType() { return SampleType.LONG; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 64; }

		public int getNumBits() { return 64; }

		public String getName() { return "64-bit signed"; }
	}
	
	private static class Sample64BitFloat implements SampleInfo
	{
		public SampleType getValueType() { return SampleType.DOUBLE; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 64; }

		public int getNumBits() { return 64; }

		public String getName() { return "64-bit float"; }
	}
}
