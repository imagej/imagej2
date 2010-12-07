package imagej.data;

import imagej.StorageType;

public class UnsignedShortType implements Type
{
	@Override
	public String getName()
	{
		return "16-bit unsigned";
	}

	@Override
	public boolean isFloat()
	{
		return false;
	}

	@Override
	public boolean isUnsigned()
	{
		return true;
	}

	@Override
	public int getNumBitsData()
	{
		return 16;
	}

	@Override
	public double getMinReal()
	{
		return 0;
	}

	@Override
	public double getMaxReal()
	{
		return 0xffff;
	}

	@Override
	public long getMinIntegral()
	{
		return 0;
	}

	@Override
	public long getMaxIntegral()
	{
		return 0xffff;
	}

	@Override
	public DataAccessor allocateAccessor(Object array)
	{
		if ( ! isStorageCompatible(array) )
			throw new IllegalArgumentException("expected a short[] but given storage of type "+array.getClass());

		return new UnsignedShortAccessor(array);
	}

	@Override
	public StorageType getStorageType()
	{
		return StorageType.UINT16;
	}

	@Override
	public double getNumberOfStorageTypesPerValue()
	{
		return 1;
	}

	@Override
	public boolean isStorageCompatible(Object data)
	{
		return data instanceof short[];
	}

	@Override
	public long calcNumStorageBytesFromPixelCount(long numPixels)
	{
		return 2 * calcNumStorageUnitsFromPixelCount(numPixels);
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
		
		return new short[(int)numStorageUnits];
	}

}
