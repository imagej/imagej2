package imagej.data;

import imagej.StorageType;

public class IntType implements Type
{

	@Override
	public String getName()
	{
		return "32-bit signed";
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
		return 32;
	}

	@Override
	public double getMinReal()
	{
		return Integer.MIN_VALUE;
	}

	@Override
	public double getMaxReal()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public long getMinIntegral()
	{
		return Integer.MIN_VALUE;
	}

	@Override
	public long getMaxIntegral()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public DataAccessor allocateArrayAccessor(Object array)
	{
		Types.verifyCompatibility(this, array);

		return new IntArrayAccessor(array);
	}

	@Override
	public StorageType getStorageType()
	{
		return StorageType.INT32;
	}

	@Override
	public double getNumberOfStorageTypesPerValue()
	{
		return 1;
	}

	@Override
	public boolean isStorageCompatible(Object data)
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
	public Object allocateStorageArray(int numPixels)
	{
		long numStorageUnits = calcNumStorageUnitsFromPixelCount(numPixels);
		
		return new int[(int)numStorageUnits];
	}

}
