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
	public DataAccessor allocateArrayAccessor(Object array)
	{
		Types.verifyCompatibility(this, array);

		return new UnsignedShortArrayAccessor(array);
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
	public boolean compatibleWith(Object data)
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
	public Object allocateStorageArray(long numPixels)
	{
		int numStorageUnits = Types.calcIntCompatibleStorageUnits(this, numPixels);
		
		return new short[numStorageUnits];
	}

}
