package imagej.primitive;

public class UnsignedTwelveBitReader implements DataReader
{
	private int[] ints;
	
	public UnsignedTwelveBitReader(Object ints)
	{
		this.ints = (int[])ints;
	}
	
	public double getValue(int index)
	{
		// divide pixels into buckets of nibbles. each bucket is 96 bits (three 32 bit ints = eight 12 bit ints)
		int bucketNumber = index / 8;
		
		int intIndexOfBucketStart = bucketNumber * 3;
		
		int indexOfFirstNibble = index % 8;
		
		int nibble1, nibble2, nibble3;
		
		switch (indexOfFirstNibble)
		
		{
			case 0:
				nibble1 = getNibble(intIndexOfBucketStart,   0);
				nibble2 = getNibble(intIndexOfBucketStart,   1);
				nibble3 = getNibble(intIndexOfBucketStart,   2);
				break;
			case 1:
				nibble1 = getNibble(intIndexOfBucketStart,   3);
				nibble2 = getNibble(intIndexOfBucketStart,   4);
				nibble3 = getNibble(intIndexOfBucketStart,   5);
				break;
			case 2:
				nibble1 = getNibble(intIndexOfBucketStart,   6);
				nibble2 = getNibble(intIndexOfBucketStart,   7);
				nibble3 = getNibble(intIndexOfBucketStart+1, 0);
				break;
			case 3:
				nibble1 = getNibble(intIndexOfBucketStart+1, 1);
				nibble2 = getNibble(intIndexOfBucketStart+1, 2);
				nibble3 = getNibble(intIndexOfBucketStart+1, 3);
				break;
			case 4:
				nibble1 = getNibble(intIndexOfBucketStart+1, 4);
				nibble2 = getNibble(intIndexOfBucketStart+1, 5);
				nibble3 = getNibble(intIndexOfBucketStart+1, 6);
				break;
			case 5:
				nibble1 = getNibble(intIndexOfBucketStart+1, 7);
				nibble2 = getNibble(intIndexOfBucketStart+2, 0);
				nibble3 = getNibble(intIndexOfBucketStart+2, 1);
				break;
			case 6:
				nibble1 = getNibble(intIndexOfBucketStart+2, 2);
				nibble2 = getNibble(intIndexOfBucketStart+2, 3);
				nibble3 = getNibble(intIndexOfBucketStart+2, 4);
				break;
			case 7:
				nibble1 = getNibble(intIndexOfBucketStart+2, 5);
				nibble2 = getNibble(intIndexOfBucketStart+2, 6);
				nibble3 = getNibble(intIndexOfBucketStart+2, 7);
				break;
			default:
				throw new IllegalStateException();
		}
		
		return (nibble3 << 8) + (nibble2 << 4) + (nibble1 << 0);
	}
	
	private int getNibble(int intNumber, int nibbleNumber)
	{
		int currValue = this.ints[intNumber];
		
		int alignedMask = 15 << (4 * nibbleNumber);

		int shiftedValue = currValue & alignedMask;
		
		return shiftedValue >> (4 * nibbleNumber);
	}
}
