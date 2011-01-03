package imagej.data;

import imagej.StorageType;

public class UnsignedIntType implements Type
{
	@Override
	public String getName()
	{
		return "32-bit unsigned";
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
		return 32;
	}

	@Override
	public double getMinReal()
	{
		return 0;
	}

	@Override
	public double getMaxReal()
	{
		return (double) 0xffffffffL;
	}

	@Override
	public long getMinIntegral()
	{
		return 0;
	}

	@Override
	public long getMaxIntegral()
	{
		return 0xffffffffL;
	}

	@Override
	public DataAccessor allocateArrayAccessor(Object array)
	{
		Types.verifyCompatibility(this, array);

		return new UnsignedIntArrayAccessor(array);
	}

	@Override
	public StorageType getStorageType()
	{
		return StorageType.UINT32;
	}

	@Override
	public double getNumberOfStorageTypesPerValue()
	{
		return 1;
	}

	@Override
	public boolean compatibleWith(Object data)
	{
		return data instanceof int[];
	}

	@Override
	public long calcNumStorageBytesFromPixelCount(long numPixels)
	{
		return 4 * calcNumStorageUnitsFromPixelCount(numPixels);
	}

	@Override
	public long calcNumStorageUnitsFromPixelCount(long numPixels)
	{
		return numPixels;
	}

	@Override
	public Object allocateStorageArray(long numPixels)
	{
		int numStorageUnits = Types.calcIntCompatibleStorageUnits(this, numPixels);
		
		return new int[numStorageUnits];
	}

}
