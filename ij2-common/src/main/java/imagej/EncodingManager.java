package imagej;

public class EncodingManager
{

	// ***** members  ************************************************/
	
	private static DataEncoding[] encodingArray;

	//***** static initialization **********************************************/
	
	/** initialize the type lists */
	static
	{
		encodingArray = new DataEncoding[DataType.values().length];

		encodingArray[DataType.BIT.ordinal()] = new BitEncoding();
		encodingArray[DataType.BYTE.ordinal()] = new ByteEncoding();
		encodingArray[DataType.UBYTE.ordinal()] = new UnsignedByteEncoding();
		encodingArray[DataType.UINT12.ordinal()] = new Unsigned12BitEncoding();
		encodingArray[DataType.SHORT.ordinal()] = new ShortEncoding();
		encodingArray[DataType.USHORT.ordinal()] = new UnsignedShortEncoding();
		encodingArray[DataType.INT.ordinal()] = new IntEncoding();
		encodingArray[DataType.UINT.ordinal()] = new UnsignedIntEncoding();
		encodingArray[DataType.FLOAT.ordinal()] = new FloatEncoding();
		encodingArray[DataType.LONG.ordinal()] = new LongEncoding();
		encodingArray[DataType.DOUBLE.ordinal()] = new DoubleEncoding();
	}

	// ***** public interface  ************************************************/

	/** lookup the DataEncoding associated with the given user type */
	public static DataEncoding getEncoding(DataType dataType)
	{
		return encodingArray[dataType.ordinal()];
	}
	
	/** returns the number of storage units required to fully store a given number of pixels with a specified encoding */
	public static int calcStorageUnitsRequired(DataEncoding encoding, int numPixels)
	{
		return (int) Math.ceil(encoding.getNumTypesPerValue() * numPixels);
	}
	
	/** returns the maximum number of pixels a number of storage units can contain using a specified encoding */
	public static int calcMaxPixelsStorable(DataEncoding encoding, int numStorageUnits)
	{
		return (int) Math.floor(numStorageUnits / encoding.getNumTypesPerValue());
	}
	
	/** verifies that an input array is compatible with a specified input type. Throws an exception if not. */
	public static void verifyTypeCompatibility(Object pixels, StorageType storageType)
	{
		switch (storageType)
		{
			case INT8:
			case UINT8:
				if (pixels instanceof byte[])
					return;
				break;
			
			case INT16:
			case UINT16:
				if (pixels instanceof short[])
					return;
				break;
				
			case INT32:
			case UINT32:
				if (pixels instanceof int[])
					return;
				break;
			
			case FLOAT32:
				if (pixels instanceof float[])
					return;
				break;
				
			case INT64:
				if (pixels instanceof long[])
					return;
				break;
				
			case FLOAT64:
				if (pixels instanceof double[])
					return;
				break;
				
			default:
				break;
		}

		throw new IllegalArgumentException("unsupported type/pixel combination: expectedType ("+storageType+
				") and passed pixel Object type ("+pixels.getClass().toString()+")");
	}
	
	/** verifies that an input array is compatible with a specified input type. Throws an exception if not. */
	public static void verifyTypeCompatibility(Object pixels, DataType dataType)
	{
		verifyTypeCompatibility(pixels, getEncoding(dataType).getBackingType());
	}

	/** allocates and returns an array of specified StorageType and number of elements */
	public static Object allocateCompatibleArray(StorageType backingType, int numElements)
	{
		switch (backingType)
		{
			case INT8:
			case UINT8:
				return new byte[numElements];
			case INT16:
			case UINT16:
				return new short[numElements];
			case INT32:
			case UINT32:
				return new int[numElements];
			case FLOAT32:
				return new float[numElements];
			case INT64:
				return new long[numElements];
			case FLOAT64:
				return new double[numElements];
			default:
				throw new IllegalStateException("unknown storage type : "+backingType);
		}
	}
	
	/** allocates and returns an array of specified DataType and number of elements */
	public static Object allocateCompatibleArray(DataType type, int numElements)
	{
		DataEncoding encoding = getEncoding(type);
		
		int storageUnitsNeeded = calcStorageUnitsRequired(encoding, numElements);
		
		return allocateCompatibleArray(encoding.getBackingType(), storageUnitsNeeded);
	}

	// ***** private support  ************************************************/

	private EncodingManager() {}
	
	private static class BitEncoding implements DataEncoding
	{
		@Override
		public StorageType getBackingType() { return StorageType.UINT32; }

		@Override
		public double getNumTypesPerValue() { return 1.0 / 32.0; }
	}
	
	private static class ByteEncoding implements DataEncoding
	{
		@Override
		public StorageType getBackingType() { return StorageType.INT8; }

		@Override
		public double getNumTypesPerValue() { return 1.0; }
	}
	
	private static class UnsignedByteEncoding implements DataEncoding
	{
		@Override
		public StorageType getBackingType() { return StorageType.UINT8; }

		@Override
		public double getNumTypesPerValue() { return 1.0; }
	}
	
	private static class Unsigned12BitEncoding implements DataEncoding
	{
		@Override
		public StorageType getBackingType() { return StorageType.UINT32; }

		@Override
		public double getNumTypesPerValue() { return 12.0 / 32.0; }
	}

	private static class ShortEncoding implements DataEncoding
	{
		@Override
		public StorageType getBackingType() { return StorageType.INT16; }

		@Override
		public double getNumTypesPerValue() { return 1.0; }
	}
	
	private static class UnsignedShortEncoding implements DataEncoding
	{
		@Override
		public StorageType getBackingType() { return StorageType.UINT16; }

		@Override
		public double getNumTypesPerValue() { return 1.0; }
	}
	
	private static class IntEncoding implements DataEncoding
	{
		@Override
		public StorageType getBackingType() { return StorageType.INT32; }

		@Override
		public double getNumTypesPerValue() { return 1.0; }
	}
	
	private static class UnsignedIntEncoding implements DataEncoding
	{
		@Override
		public StorageType getBackingType() { return StorageType.UINT32; }

		@Override
		public double getNumTypesPerValue() { return 1.0; }
	}
	
	private static class FloatEncoding implements DataEncoding
	{
		@Override
		public StorageType getBackingType() { return StorageType.FLOAT32; }

		@Override
		public double getNumTypesPerValue() { return 1.0; }
	}
	
	private static class LongEncoding implements DataEncoding
	{
		@Override
		public StorageType getBackingType() { return StorageType.INT64; }

		@Override
		public double getNumTypesPerValue() { return 1.0; }
	}
	
	private static class DoubleEncoding implements DataEncoding
	{
		@Override
		public StorageType getBackingType() { return StorageType.FLOAT64; }

		@Override
		public double getNumTypesPerValue() { return 1.0; }
	}
}
