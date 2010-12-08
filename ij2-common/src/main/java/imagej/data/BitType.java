package imagej.data;

import imagej.StorageType;

public class BitType implements Type
{
	@Override
	public String getName()
	{
		return "1-bit unsigned";
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
		return 1;
	}

	@Override
	public double getMinReal()
	{
		return 0;
	}

	@Override
	public double getMaxReal()
	{
		return 1;
	}

	@Override
	public long getMinIntegral()
	{
		return 0;
	}

	@Override
	public long getMaxIntegral()
	{
		return 1;
	}

	@Override
	public DataAccessor allocateArrayAccessor(Object array)
	{
		Types.verifyCompatibility(this, array);

		return new BitArrayAccessor(array);
	}

	@Override
	public StorageType getStorageType()
	{
		return StorageType.UINT32;
	}

	@Override
	public double getNumberOfStorageTypesPerValue()
	{
		return 1.0 / 32.0;
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
		long numUnits = numPixels / 32;
		if (numPixels % 32 != 0)
			numUnits++;
		return numUnits;
	}

	@Override
	public Object allocateStorageArray(int numPixels)
	{
		long numStorageUnits = calcNumStorageUnitsFromPixelCount(numPixels);
		
		return new int[(int)numStorageUnits];
	}

}
