package imagej.data;

import imagej.StorageType;

public class Unsigned12BitType implements Type
{
	@Override
	public String getName()
	{
		return "12-bit unsigned";
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
		return 12;
	}

	@Override
	public double getMinReal()
	{
		return 0;
	}

	@Override
	public double getMaxReal()
	{
		return 4095;
	}

	@Override
	public long getMinIntegral()
	{
		return 0;
	}

	@Override
	public long getMaxIntegral()
	{
		return 4095;
	}

	@Override
	public DataAccessor allocateArrayAccessor(Object array)
	{
		Types.verifyCompatibility(this, array);

		return new Unsigned12BitArrayAccessor(array);
	}

	@Override
	public StorageType getStorageType()
	{
		return StorageType.UINT32;
	}

	@Override
	public double getNumberOfStorageTypesPerValue()
	{
		return 12.0 / 32.0; // this mirrors imglib (packed 12 bits at a time in an array of 32 bit ints)
	}						// I think it would be more performant to store them packed 12 bits at a time
							// in an array of bytes and access them via byte and nibble lookup

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
		long numBits = numPixels * 12;
		long numUnits = numBits / 32;
		if (numBits % 32 != 0)
			numUnits++;
		return numUnits;
	}

	@Override
	public Object allocateStorageArray(int numPixels)
	{
		long numInts = calcNumStorageUnitsFromPixelCount(numPixels);
		
		return new int[(int)numInts];
	}

}
