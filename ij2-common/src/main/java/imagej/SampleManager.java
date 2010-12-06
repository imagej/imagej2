package imagej;

/** SampleManager manages the information related to all supported types in ImageJ */
public class SampleManager
{

	//***** private members **********************************************/
	
	/** the internal list of imagej type sample data info */
	private static SampleInfo[] sampleInfoArray;

	//***** static initialization **********************************************/
	
	/** initialize the type lists */
	static
	{
		sampleInfoArray = new SampleInfo[DataType.values().length];
		
		sampleInfoArray[DataType.BIT.ordinal()] = new Sample1BitUnsigned();
		sampleInfoArray[DataType.BYTE.ordinal()] = new Sample8BitSigned();
		sampleInfoArray[DataType.UBYTE.ordinal()] = new Sample8BitUnsigned();
		sampleInfoArray[DataType.UINT12.ordinal()] = new Sample12BitUnsigned();
		sampleInfoArray[DataType.SHORT.ordinal()] = new Sample16BitSigned();
		sampleInfoArray[DataType.USHORT.ordinal()] = new Sample16BitUnsigned();
		sampleInfoArray[DataType.INT.ordinal()] = new Sample32BitSigned();
		sampleInfoArray[DataType.UINT.ordinal()] = new Sample32BitUnsigned();
		sampleInfoArray[DataType.FLOAT.ordinal()] = new Sample32BitFloat();
		sampleInfoArray[DataType.LONG.ordinal()] = new Sample64BitSigned();
		sampleInfoArray[DataType.DOUBLE.ordinal()] = new Sample64BitFloat();
	}
	
	//***** constructor **********************************************/
	
	/** make this class uninstantiable */
	private SampleManager() {}

	//***** public interface **********************************************/
	
	/** get a SampleInfo associated with a DataType */
	public static SampleInfo getSampleInfo(DataType type)
	{
		return sampleInfoArray[type.ordinal()];
	}
	
	/** get the SampleInfo associated with a SampleInfo name. Returns null if not found. */
	public static SampleInfo findSampleInfo(String name)
	{
		if (name == null)
			return null;
		
		for (SampleInfo s : sampleInfoArray)
			if (name.equals(s.getName()))
				return s;
		
		return null;
	}

	// *************** Private helpers follow **********************************************************
	
	/** helper function - return the total number of bits in a sample described by a SampleInfo object */
	private static int calcNumBits(SampleInfo s)
	{
		return s.getNumValues() * s.getNumBitsPerValue();
	}

	/** SampleInfo that describes a 1 bit unsigned type */
	private static class Sample1BitUnsigned implements SampleInfo
	{
		public DataType getDataType() { return DataType.BIT; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 1; }

		public int getNumBits() { return calcNumBits(this); }

		public boolean isUnsigned() { return true; }
		
		public boolean isFloat() { return false; }

		public String getName() { return "1-bit unsigned"; }
	}
	
	/** SampleInfo that describes IJ's 8 bit signed type */
	private static class Sample8BitSigned implements SampleInfo
	{
		public DataType getDataType() { return DataType.BYTE; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 8; }

		public int getNumBits() { return calcNumBits(this); }

		public boolean isUnsigned() { return false; }
		
		public boolean isFloat() { return false; }

		public String getName() { return "8-bit signed"; }
	}
	
	/** SampleInfo that describes IJ's 8 bit unsigned type */
	private static class Sample8BitUnsigned implements SampleInfo
	{
		public DataType getDataType() { return DataType.UBYTE; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 8; }

		public int getNumBits() { return calcNumBits(this); }

		public boolean isUnsigned() { return true; }
		
		public boolean isFloat() { return false; }

		public String getName() { return "8-bit unsigned"; }
	}
	
	/** SampleInfo that describes IJ's 12 bit unsigned type */
	private static class Sample12BitUnsigned implements SampleInfo
	{
		public DataType getDataType() { return DataType.UINT12; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 12; }

		public int getNumBits() { return calcNumBits(this); }

		public boolean isUnsigned() { return true; }
		
		public boolean isFloat() { return false; }

		public String getName() { return "12-bit unsigned"; }
	}
	
	/** SampleInfo that describes IJ's 16 bit signed type */
	private static class Sample16BitSigned implements SampleInfo
	{
		public DataType getDataType() { return DataType.SHORT; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 16; }

		public int getNumBits() { return calcNumBits(this); }

		public boolean isUnsigned() { return false; }
		
		public boolean isFloat() { return false; }

		public String getName() { return "16-bit signed"; }
	}
	
	/** SampleInfo that describes IJ's 16 bit unsigned type */
	private static class Sample16BitUnsigned implements SampleInfo
	{
		public DataType getDataType() { return DataType.USHORT; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 16; }

		public int getNumBits() { return calcNumBits(this); }

		public boolean isUnsigned() { return true; }
		
		public boolean isFloat() { return false; }

		public String getName() { return "16-bit unsigned"; }
	}
	
	/** SampleInfo that describes IJ's 32 bit signed type */
	private static class Sample32BitSigned implements SampleInfo
	{
		public DataType getDataType() { return DataType.INT; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 32; }

		public int getNumBits() { return calcNumBits(this); }

		public boolean isUnsigned() { return false; }
		
		public boolean isFloat() { return false; }

		public String getName() { return "32-bit signed"; }
	}
	
	/** SampleInfo that describes IJ's 32 bit unsigned type */
	private static class Sample32BitUnsigned implements SampleInfo
	{
		public DataType getDataType() { return DataType.UINT; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 32; }

		public int getNumBits() { return calcNumBits(this); }

		public boolean isUnsigned() { return true; }
		
		public boolean isFloat() { return false; }

		public String getName() { return "32-bit unsigned"; }
	}
	
	/** SampleInfo that describes IJ's 32 bit float type */
	private static class Sample32BitFloat implements SampleInfo
	{
		public DataType getDataType() { return DataType.FLOAT; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 32; }

		public int getNumBits() { return calcNumBits(this); }

		public boolean isUnsigned() { return false; }
		
		public boolean isFloat() { return true; }

		public String getName() { return "32-bit float"; }
	}
	
	/** SampleInfo that describes IJ's 64 bit signed type */
	private static class Sample64BitSigned implements SampleInfo
	{
		public DataType getDataType() { return DataType.LONG; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 64; }

		public int getNumBits() { return calcNumBits(this); }

		public boolean isUnsigned() { return false; }
		
		public boolean isFloat() { return false; }

		public String getName() { return "64-bit signed"; }
	}
	
	/** SampleInfo that describes IJ's 64 bit float type */
	private static class Sample64BitFloat implements SampleInfo
	{
		public DataType getDataType() { return DataType.DOUBLE; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 64; }

		public int getNumBits() { return calcNumBits(this); }

		public boolean isUnsigned() { return false; }
		
		public boolean isFloat() { return true; }

		public String getName() { return "64-bit float"; }
	}
}
