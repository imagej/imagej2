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
		int byteVal = (int) value;
		
		if (byteVal < 0) byteVal = 0;
		if (byteVal > 255) byteVal = 255;
		
		this.bytes[(int)index] = (byte) (byteVal & 0xff);
	}
	
	public void setIntegral(long index, long value)
	{
		int byteVal = (int) value;
		
		if (byteVal < 0) byteVal = 0;
		if (byteVal > 255) byteVal = 255;
		
		this.bytes[(int)index] = (byte) (byteVal & 0xff);
	}
}

