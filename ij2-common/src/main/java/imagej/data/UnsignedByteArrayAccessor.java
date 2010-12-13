package imagej.data;


public class UnsignedByteArrayAccessor implements DataAccessor
{
	private byte[] bytes;
	
	public UnsignedByteArrayAccessor(Object array)
	{
		this.bytes = (byte[]) array;
	}
	
	public double getReal(long index)
	{
		double byteVal = this.bytes[(int)index];
		
		if (byteVal < 0)
			byteVal = 256 + byteVal;
		
		return byteVal;
	}
	
	public long getIntegral(long index)
	{
		long byteVal = this.bytes[(int)index];
		
		if (byteVal < 0)
			byteVal = 256 + byteVal;
		
		return byteVal;
	}
	
	public void setReal(long index, double value)
	{
		if (value < 0) value = 0;
		if (value > 255) value = 255;

		int byteVal = (int) value;
		
		this.bytes[(int)index] = (byte) (byteVal & 0xff);
	}
	
	public void setIntegral(long index, long value)
	{
		if (value < 0) value = 0;
		if (value > 255) value = 255;

		int byteVal = (int) value;
		
		this.bytes[(int)index] = (byte) (byteVal & 0xff);
	}
}

