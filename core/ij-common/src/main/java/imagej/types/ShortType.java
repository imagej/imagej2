package imagej.types;

import imagej.StorageType;

public class ShortType extends AbstractType
{
	@Override
	public String getName()
	{
		return "16-bit signed";
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
		return 16;
	}

	@Override
	public double getMinReal()
	{
		return Short.MIN_VALUE;
	}

	@Override
	public double getMaxReal()
	{
		return Short.MAX_VALUE;
	}

	@Override
	public long getMinIntegral()
	{
		return Short.MIN_VALUE;
	}

	@Override
	public long getMaxIntegral()
	{
		return Short.MAX_VALUE;
	}

	@Override
	public DataAccessor allocateArrayAccessor(Object array)
	{
		Types.verifyCompatibility(this, array);

		return new ShortArrayAccessor(array);
	}

	@Override
	public StorageType getStorageType()
	{
		return StorageType.INT16;
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
