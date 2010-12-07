package imagej.data;


public class UnsignedByteAccessor implements DataAccessor
{
	private byte[] bytes;
	
	public UnsignedByteAccessor(Object array)
	{
		this.bytes = (byte[]) array;
	}
	
	public double getReal(int index)
	{
		double byteVal = this.bytes[index];
		
		if (byteVal < 0)
			byteVal = 256 + byteVal;
		
		return byteVal;
	}
	
	public long getIntegral(int index)
	{
		long byteVal = this.bytes[index];
		
		if (byteVal < 0)
			byteVal = 256 + byteVal;
		
		return byteVal;
	}
	
	public void setReal(int index, double value)
	{
		int byteVal = (int) value;
		
		if (byteVal < 0) byteVal = 0;
		if (byteVal > 255) byteVal = 255;
		
		this.bytes[index] = (byte) (byteVal & 0xff);
	}
	
	public void setIntegral(int index, long value)
	{
		int byteVal = (int) value;
		
		if (byteVal < 0) byteVal = 0;
		if (byteVal > 255) byteVal = 255;
		
		this.bytes[index] = (byte) (byteVal & 0xff);
	}
}

