package imagej2;

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
		sampleInfoArray = new SampleInfo[UserType.values().length];
		
		sampleInfoArray[UserType.BIT.ordinal()] = new Sample1BitUnsigned();
		sampleInfoArray[UserType.BYTE.ordinal()] = new Sample8BitSigned();
		sampleInfoArray[UserType.UBYTE.ordinal()] = new Sample8BitUnsigned();
		sampleInfoArray[UserType.UINT12.ordinal()] = new Sample12BitUnsigned();
		sampleInfoArray[UserType.SHORT.ordinal()] = new Sample16BitSigned();
		sampleInfoArray[UserType.USHORT.ordinal()] = new Sample16BitUnsigned();
		sampleInfoArray[UserType.INT.ordinal()] = new Sample32BitSigned();
		sampleInfoArray[UserType.UINT.ordinal()] = new Sample32BitUnsigned();
		sampleInfoArray[UserType.FLOAT.ordinal()] = new Sample32BitFloat();
		sampleInfoArray[UserType.LONG.ordinal()] = new Sample64BitSigned();
		sampleInfoArray[UserType.DOUBLE.ordinal()] = new Sample64BitFloat();
	}
	
	//***** constructor **********************************************/
	
	/** make this class uninstantiable */
	private SampleManager() {}

	//***** public interface **********************************************/
	
	/** get a SampleInfo associated with a UserType */
	public static SampleInfo getSampleInfo(UserType type)
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

	/** verifies that an input array is compatible with a specified input type. Throws an exception if not. */
	public static void verifyTypeCompatibility(Object pixels, UserType inputType)
	{
		switch (inputType)
		{
			case BYTE:
			case UBYTE:
				if (pixels instanceof byte[])
					return;
				break;
				
			case SHORT:
			case USHORT:
				if (pixels instanceof short[])
					return;
				break;
				
			case INT:
			case UINT:
				if (pixels instanceof int[])
					return;
				break;
				
			case LONG:
				if (pixels instanceof long[])
					return;
				break;
				
			case FLOAT:
				if (pixels instanceof float[])
					return;
				break;
				
			case DOUBLE:
				if (pixels instanceof double[])
					return;
				break;
	
			case UINT12:
				if (pixels instanceof int[])  // unintuitive but this is how Imglib handles UINT12 data: a huge list of bits
					return;                   //   encoded 32 bits at a time within an int array
				break;
			
			case BIT:
				if (pixels instanceof int[])  // unintuitive. I think this is how Imglib handles BIT data: a huge list of bits
					return;                   //   encoded 32 bits at a time within an int array
				break;
				
			default:
				break;
		}
		
		throw new IllegalArgumentException("unsupported type/pixel combination: expectedType ("+inputType+
												") and passed pixel Object type ("+pixels.getClass().toString()+")");
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
		public UserType getUserType() { return UserType.BIT; }

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
		public UserType getUserType() { return UserType.BYTE; }

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
		public UserType getUserType() { return UserType.UBYTE; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 8; }

		public int getNumBits() { return calcNumBits(this); }

		public boolean isUnsigned() { return true; }
		
		public boolean isFloat() { return false; }

		public String getName() { return "8-bit unsigned"; }
	}
	
	/** SampleInfo that describes IJ's 16 bit signed type */
	private static class Sample16BitSigned implements SampleInfo
	{
		public UserType getUserType() { return UserType.SHORT; }

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
		public UserType getUserType() { return UserType.USHORT; }

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
		public UserType getUserType() { return UserType.INT; }

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
		public UserType getUserType() { return UserType.UINT; }

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
		public UserType getUserType() { return UserType.FLOAT; }

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
		public UserType getUserType() { return UserType.LONG; }

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
		public UserType getUserType() { return UserType.DOUBLE; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 64; }

		public int getNumBits() { return calcNumBits(this); }

		public boolean isUnsigned() { return false; }
		
		public boolean isFloat() { return true; }

		public String getName() { return "64-bit float"; }
	}

	/** SampleInfo that describes IJ's 12 bit unsigned type */
	private static class Sample12BitUnsigned implements SampleInfo
	{
		public UserType getUserType() { return UserType.UINT12; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 12; }

		public int getNumBits() { return calcNumBits(this); }

		public boolean isUnsigned() { return true; }
		
		public boolean isFloat() { return false; }

		public String getName() { return "12-bit unsigned"; }
	}
	
}
