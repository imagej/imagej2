package imagej.types;

// TODO - check that the order we access the bits matches the order imglib accesses them

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
		int intNumber = (int) (index / 32);
		
		int bitNumber = (int) (index % 32);

		return getBit(intNumber, bitNumber);
	}

	@Override
	public void setReal(long index, double value)
	{
		if (value >= 0.5) // TODO - now like imglib : was 1.0
			value = 1;
		else
			value = 0;

		int intNumber = (int) (index / 32);
		
		int bitNumber = (int) (index % 32);

		setBit(intNumber, bitNumber, (int)value);
	}

	@Override
	public long getIntegral(long index)
	{
		int intNumber = (int) (index / 32);
		
		int bitNumber = (int) (index % 32);

		return getBit(intNumber, bitNumber);
	}

	@Override
	public void setIntegral(long index, long value)
	{
		if (value >= 1)
			value = 1;
		else
			value = 0;

		int intNumber = (int) (index / 32);
		
		int bitNumber = (int) (index % 32);

		setBit(intNumber, bitNumber, (int)value);
	}

	private int getBit(int intNumber, int bitNumber)
	{
		int currValue = this.ints[intNumber];
		
		int alignedMask = 1 << (bitNumber);
		
		if ((currValue & alignedMask) != 0)
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
