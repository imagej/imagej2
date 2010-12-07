package imagej.data;

import imagej.StorageType;

public class FloatType implements Type
{
	@Override
	public String getName()
	{
		return "32-bit float";
	}

	@Override
	public boolean isFloat()
	{
		return true;
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
		return -Float.MAX_VALUE;
	}

	@Override
	public double getMaxReal()
	{
		return Float.MAX_VALUE;
	}

	@Override
	public long getMinIntegral()
	{
		return (long) -Float.MAX_VALUE;
	}

	@Override
	public long getMaxIntegral()
	{
		return (long) Float.MAX_VALUE;
	}

	@Override
	public DataAccessor allocateAccessor(Object array)
	{
		if ( ! isStorageCompatible(array) )
			throw new IllegalArgumentException("expected a float[] but given storage of type "+array.getClass());

		return new FloatAccessor(array);
	}

	@Override
	public StorageType getStorageType()
	{
		return StorageType.FLOAT32;
	}

	@Override
	public double getNumberOfStorageTypesPerValue()
	{
		return 1;
	}

	@Override
	public boolean isStorageCompatible(Object data)
	{
		return data instanceof float[];
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
		
		return new float[(int)numStorageUnits];
	}

}
