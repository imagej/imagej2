package imagej.data;

import imagej.StorageType;

public class LongType implements Type
{
	@Override
	public String getName()
	{
		return "64-bit signed";
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
		return 64;
	}

	@Override
	public double getMinReal()
	{
		return Long.MIN_VALUE;
	}

	@Override
	public double getMaxReal()
	{
		return Long.MAX_VALUE;
	}

	@Override
	public long getMinIntegral()
	{
		return Long.MIN_VALUE;
	}

	@Override
	public long getMaxIntegral()
	{
		return Long.MAX_VALUE;
	}

	@Override
	public DataAccessor allocateArrayAccessor(Object array)
	{
		Types.verifyCompatibility(this, array);

		return new LongArrayAccessor(array);
	}

	@Override
	public StorageType getStorageType()
	{
		return StorageType.INT64;
	}

	@Override
	public double getNumberOfStorageTypesPerValue()
	{
		return 1;
	}

	@Override
	public boolean canAccept(Object data)
	{
		return data instanceof long[];
	}

	@Override
	public long calcNumStorageBytesFromPixelCount(long numPixels)
	{
		return 8 * calcNumStorageUnitsFromPixelCount(numPixels);
	}

	@Override
	public long calcNumStorageUnitsFromPixelCount(long numPixels)
	{
		return numPixels;
	}

	@Override
	public Object allocateStorageArray(long numPixels)
	{
		long numStorageUnits = calcNumStorageUnitsFromPixelCount(numPixels);
		
		if (numStorageUnits > Integer.MAX_VALUE)
			throw new IllegalArgumentException("more storage units requested ("+numStorageUnits+") than Java can allocate ("+Integer.MAX_VALUE+")");
		
		return new long[(int)numStorageUnits];
	}

}
