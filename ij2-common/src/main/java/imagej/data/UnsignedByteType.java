package imagej.data;

import imagej.StorageType;

public class UnsignedByteType implements Type
{
	@Override
	public String getName()
	{
		return "8-bit unsigned";
	}

	@Override
	public boolean isFloat()
	{
		return false;
	};
	
	@Override
	public boolean isUnsigned()
	{
		return true;
	}
	
	@Override
	public int getNumBitsData()
	{ 
		return 8;
	}
	
	@Override
	public double getMinReal()
	{
		return 0;
	}
	
	@Override
	public double getMaxReal()
	{
		return 255;
	}
	
	@Override
	public long getMinIntegral()
	{
		return 0;
	}
	
	@Override
	public long getMaxIntegral()
	{
		return 255;
	}
	
	@Override
	public DataAccessor allocateAccessor(Object array)
	{
		if ( ! isStorageCompatible(array) )
			throw new IllegalArgumentException("expected a byte[] but given storage of type "+array.getClass());

		return new UnsignedByteAccessor(array);
	}
	
	@Override
	public StorageType getStorageType()
	{
		return StorageType.UINT8;
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
		return 1 * calcNumStorageUnitsFromPixelCount(numPixels);
	}
	
	@Override
	public long calcNumStorageUnitsFromPixelCount(long numPixels)
	{
		return numPixels;
	}
	
	@Override
	public Object allocateStorageArray(int numPixels)
	{
		long numStorageUnits = calcNumStorageUnitsFromPixelCount(numPixels);
		
		return new byte[(int)numStorageUnits];
	}
	
}

