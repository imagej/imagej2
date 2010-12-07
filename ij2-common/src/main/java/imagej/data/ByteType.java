package imagej.data;

import imagej.StorageType;

public class ByteType implements Type {

	@Override
	public String getName()
	{
		return "8-bit signed";
	}

	@Override
	public boolean isFloat()
	{
		return false;
	}

	@Override
	public boolean isUnsigned()
	{
		return false;
	}

	@Override
	public int getNumBitsData()
	{
		return 8;
	}

	@Override
	public double getMinReal() 
	{
		return Byte.MIN_VALUE;
	}

	@Override
	public double getMaxReal()
	{
		return Byte.MAX_VALUE;
	}

	@Override
	public long getMinIntegral()
	{
		return Byte.MIN_VALUE;
	}

	@Override
	public long getMaxIntegral()
	{
		return Byte.MAX_VALUE;
	}

	@Override
	public DataAccessor allocateAccessor(Object array)
	{
		if ( ! isStorageCompatible(array) )
			throw new IllegalArgumentException("expected a byte[] but given storage of type "+array.getClass());

		return new ByteAccessor(array);
	}

	@Override
	public StorageType getStorageType()
	{
		return StorageType.INT8;
	}

	@Override
	public double getNumberOfStorageTypesPerValue()
	{
		return 1;
	}

	@Override
	public boolean isStorageCompatible(Object data)
	{
		return data instanceof byte[];
	}

	@Override
	public long calcNumStorageBytesFromPixelCount(long numPixels)
	{
		return numPixels;
	}

	@Override
	public long calcNumStorageUnitsFromPixelCount(long numPixels)
	{
		return numPixels;
	}

	@Override
	public Object allocateStorageArray(int numPixels)
	{
		return new byte[numPixels];
	}

}
