package imagej.primitive;

public class BitReader implements DataReader
{
	private int[] ints;
	
	public BitReader(Object ints)
	{
		this.ints = (int[])ints;
	}
	
	public double getValue(int index)
	{
		int intNumber = index / 32;
		
		int bitNumber = index % 32;

		return getBit(intNumber, bitNumber);
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
}
