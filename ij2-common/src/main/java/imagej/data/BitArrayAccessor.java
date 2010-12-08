package imagej.data;

public class BitArrayAccessor implements DataAccessor
{
	private int[] ints;
	
	public BitArrayAccessor(Object data)
	{
		this.ints = (int[]) data;
	}
	
	@Override
	public double getReal(long index)
	{
		int intNumber = (int) index / 32;
		
		int bitNumber = (int) index % 32;

		return getBit(intNumber, bitNumber);
	}

	@Override
	public void setReal(long index, double value)
	{
		if (value < 0) value = 0;
		if (value > 1) value = 1;
		placeValue((int)index, (int)value);
	}

	@Override
	public long getIntegral(long index)
	{
		int intNumber = (int) index / 32;
		
		int bitNumber = (int) index % 32;

		return getBit(intNumber, bitNumber);
	}

	@Override
	public void setIntegral(long index, long value)
	{
		if (value < 0) value = 0;
		if (value > 1) value = 1;
		placeValue((int)index, (int)value);
	}

	private void placeValue(int index, int value)
	{
		int intNumber = index / 32;
		
		int bitNumber = index % 32;

		setBit(intNumber, bitNumber, value);
	}
	
	private int getBit(int intNumber, int bitNumber)
	{
		int currValue = this.ints[intNumber];
		
		int alignedMask = 1 << (bitNumber);
		
		if ((currValue & alignedMask) > 0)
			return 1;
		else
			return 0;
	}
	
	private void setBit(int intNumber, int bitNumber, int bitValue)
	{
		int currValue = this.ints[intNumber];
		
		int alignedBit = (bitValue) << (bitNumber);
		
		int alignedMask = 1 << (bitNumber);
		
		this.ints[intNumber] = (currValue & ~alignedMask) | alignedBit;
	}
}
