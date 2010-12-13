package imagej.data;

public class ByteArrayAccessor implements DataAccessor
{
	private byte[] bytes;
	
	public ByteArrayAccessor(Object array)
	{
		this.bytes = (byte[]) array;
	}
	
	public double getReal(long index)
	{
		return this.bytes[(int)index];
	}
	
	public long getIntegral(long index)
	{
		return this.bytes[(int)index];
	}
	
	public void setReal(long index, double value)
	{
		if (value < Byte.MIN_VALUE) value = Byte.MIN_VALUE;
		if (value > Byte.MAX_VALUE) value = Byte.MAX_VALUE;
		this.bytes[(int)index] = (byte) value;
	}
	
	public void setIntegral(long index, long value)
	{
		if (value < Byte.MIN_VALUE) value = Byte.MIN_VALUE;
		if (value > Byte.MAX_VALUE) value = Byte.MAX_VALUE;
		this.bytes[(int)index] = (byte) value;
	}
}
