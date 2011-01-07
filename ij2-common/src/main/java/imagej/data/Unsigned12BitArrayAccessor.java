package imagej.data;

public class Unsigned12BitArrayAccessor implements DataAccessor
{
	private int[] ints;
	
	public Unsigned12BitArrayAccessor(Object data)
	{
		this.ints = (int[]) data;
	}
	
	@Override
	public double getReal(long index)
	{
		return readValue((int)index);
	}

	@Override
	public void setReal(long index, double value)
	{
		// TODO : Imglib sets values that out of range by wraping them to other side (neg to pos or pos to neg). Determine who needs to fix code. 
		if (value < 0) value = 0;
		if (value > 4095) value = 4095;
		placeValue((int)index, (int)value);  // TODO - closer to Imglib : (int)Math.round(value)
	}

	@Override
	public long getIntegral(long index)
	{
		return readValue((int)index);
	}

	@Override
	public void setIntegral(long index, long value)
	{
		if (value < 0) value = 0;
		if (value > 4095) value = 4095;
		placeValue((int)index, (int)value);
	}

	private int readValue(int index)
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
		
		return shiftedValue >>> (4 * nibbleNumber);
	}

	private void setNibble(int intNumber, int nibbleNumber, int nibbleValue)
	{
		int currValue = this.ints[intNumber];
		
		int alignedNibble = (nibbleValue) << (4 * nibbleNumber);
		
		int alignedMask = (0xf) << (4 * nibbleNumber);
		
		this.ints[intNumber] = (currValue & ~alignedMask) | alignedNibble;
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
				setNibble(intIndexOfBucketStart,   0, ((value >>> 0) & 15));
				setNibble(intIndexOfBucketStart,   1, ((value >>> 4) & 15));
				setNibble(intIndexOfBucketStart,   2, ((value >>> 8) & 15));
				break;
			case 1:
				setNibble(intIndexOfBucketStart,   3, ((value >>> 0) & 15));
				setNibble(intIndexOfBucketStart,   4, ((value >>> 4) & 15));
				setNibble(intIndexOfBucketStart,   5, ((value >>> 8) & 15));
				break;
			case 2:
				setNibble(intIndexOfBucketStart,   6, ((value >>> 0) & 15));
				setNibble(intIndexOfBucketStart,   7, ((value >>> 4) & 15));
				setNibble(intIndexOfBucketStart+1, 0, ((value >>> 8) & 15));
				break;
			case 3:
				setNibble(intIndexOfBucketStart+1, 1, ((value >>> 0) & 15));
				setNibble(intIndexOfBucketStart+1, 2, ((value >>> 4) & 15));
				setNibble(intIndexOfBucketStart+1, 3, ((value >>> 8) & 15));
				break;
			case 4:
				setNibble(intIndexOfBucketStart+1, 4, ((value >>> 0) & 15));
				setNibble(intIndexOfBucketStart+1, 5, ((value >>> 4) & 15));
				setNibble(intIndexOfBucketStart+1, 6, ((value >>> 8) & 15));
				break;
			case 5:
				setNibble(intIndexOfBucketStart+1, 7, ((value >>> 0) & 15));
				setNibble(intIndexOfBucketStart+2, 0, ((value >>> 4) & 15));
				setNibble(intIndexOfBucketStart+2, 1, ((value >>> 8) & 15));
				break;
			case 6:
				setNibble(intIndexOfBucketStart+2, 2, ((value >>> 0) & 15));
				setNibble(intIndexOfBucketStart+2, 3, ((value >>> 4) & 15));
				setNibble(intIndexOfBucketStart+2, 4, ((value >>> 8) & 15));
				break;
			case 7:
				setNibble(intIndexOfBucketStart+2, 5, ((value >>> 0) & 15));
				setNibble(intIndexOfBucketStart+2, 6, ((value >>> 4) & 15));
				setNibble(intIndexOfBucketStart+2, 7, ((value >>> 8) & 15));
				break;
			default:
				throw new IllegalStateException();
		}
	}
}
