package imagej.primitive;

public class BitWriter implements DataWriter
{
	private int[] ints;
	
	public BitWriter(Object ints)
	{
		this.ints = (int[])ints;
	}

	public void setValue(int index, double value)
	{
		if (value < 0) value = 0;
		if (value > 1) value = 1;
		placeValue(index, (int)value);
	}
	
	private void placeValue(int index, int value)
	{
		// divide pixels into buckets of nibbles. each bucket is 96 bits (three 32 bit ints = eight 12 bit ints)
		int intNumber = index / 32;
		
		int bitNumber = index % 32;

		setBit(intNumber, bitNumber, value);
	}
	
	private void setBit(int intNumber, int bitNumber, int bitValue)
	{
		int currValue = this.ints[intNumber];
		
		int alignedBit = (bitValue) << (bitNumber);
		
		int alignedMask = 1 << (bitNumber);
		
		this.ints[intNumber] = (currValue & ~alignedMask) | alignedBit;
	}
}
