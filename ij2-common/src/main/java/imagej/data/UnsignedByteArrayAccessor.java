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
			byteVal += 256;
		
		return byteVal;
	}
	
	public long getIntegral(long index)
	{
		long byteVal = this.bytes[(int)index];
		
		if (byteVal < 0)
			byteVal += 256;
		
		return byteVal;
	}
	
	public void setReal(long index, double value)
	{
		// TODO : Imglib sets values that out of range by wraping them to other side (neg to pos or pos to neg). Determine who needs to fix code. 
		if (value < 0) value = 0;
		if (value > 255) value = 255;

		int byteVal = (int) value;  // TODO - closer to ImgLib : Math.round(value);
		
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

