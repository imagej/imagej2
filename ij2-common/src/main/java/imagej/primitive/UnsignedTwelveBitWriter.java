package imagej.primitive;

public class UnsignedTwelveBitWriter implements DataWriter
{
	private int[] ints;
	
	public UnsignedTwelveBitWriter(Object ints)
	{
		this.ints = (int[])ints;
	}

	public void setValue(int index, double value)
	{
		if (value < 0) value = 0;
		if (value > 4095) value = 4095;
		placeValue(index, (int)value);
	}
	
	private void placeValue(int index, int value)
	{
		// divide pixels into buckets of nibbles. each bucket is 96 bits (three 32 bit ints = eight 12 bit ints)
		int bucketNumber = index / 8;
		
		int intIndexOfBucketStart = bucketNumber * 3;
		
		int indexOfFirstNibble = index % 8;

		switch (indexOfFirstNibble)
		{
			case 0:
				setNibble(intIndexOfBucketStart,   0, ((value >> 0) & 15));
				setNibble(intIndexOfBucketStart,   1, ((value >> 4) & 15));
				setNibble(intIndexOfBucketStart,   2, ((value >> 8) & 15));
				break;
			case 1:
				setNibble(intIndexOfBucketStart,   3, ((value >> 0) & 15));
				setNibble(intIndexOfBucketStart,   4, ((value >> 4) & 15));
				setNibble(intIndexOfBucketStart,   5, ((value >> 8) & 15));
				break;
			case 2:
				setNibble(intIndexOfBucketStart,   6, ((value >> 0) & 15));
				setNibble(intIndexOfBucketStart,   7, ((value >> 4) & 15));
				setNibble(intIndexOfBucketStart+1, 0, ((value >> 8) & 15));
				break;
			case 3:
				setNibble(intIndexOfBucketStart+1, 1, ((value >> 0) & 15));
				setNibble(intIndexOfBucketStart+1, 2, ((value >> 4) & 15));
				setNibble(intIndexOfBucketStart+1, 3, ((value >> 8) & 15));
				break;
			case 4:
				setNibble(intIndexOfBucketStart+1, 4, ((value >> 0) & 15));
				setNibble(intIndexOfBucketStart+1, 5, ((value >> 4) & 15));
				setNibble(intIndexOfBucketStart+1, 6, ((value >> 8) & 15));
				break;
			case 5:
				setNibble(intIndexOfBucketStart+1, 7, ((value >> 0) & 15));
				setNibble(intIndexOfBucketStart+2, 0, ((value >> 4) & 15));
				setNibble(intIndexOfBucketStart+2, 1, ((value >> 8) & 15));
				break;
			case 6:
				setNibble(intIndexOfBucketStart+2, 2, ((value >> 0) & 15));
				setNibble(intIndexOfBucketStart+2, 3, ((value >> 4) & 15));
				setNibble(intIndexOfBucketStart+2, 4, ((value >> 8) & 15));
				break;
			case 7:
				setNibble(intIndexOfBucketStart+2, 5, ((value >> 0) & 15));
				setNibble(intIndexOfBucketStart+2, 6, ((value >> 4) & 15));
				setNibble(intIndexOfBucketStart+2, 7, ((value >> 8) & 15));
				break;
			default:
				throw new IllegalStateException();
		}
	}

	private void setNibble(int intNumber, int nibbleNumber, int nibbleValue)
	{
		int currValue = this.ints[intNumber];
		
		int alignedNibble = (nibbleValue) << (4 * nibbleNumber);
		
		int alignedMask = (0xf) << (4 * nibbleNumber);
		
		this.ints[intNumber] = (currValue & ~alignedMask) | alignedNibble;
	}
}
