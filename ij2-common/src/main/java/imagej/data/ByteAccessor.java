package imagej.data;

public class ByteAccessor implements DataAccessor
{
	private byte[] bytes;
	
	public ByteAccessor(Object array)
	{
		this.bytes = (byte[]) array;
	}
	
	public double getReal(int index)
	{
		return this.bytes[index];
	}
	
	public long getIntegral(int index)
	{
		return this.bytes[index];
	}
	
	public void setReal(int index, double value)
	{
		this.bytes[index] = (byte) value;
	}
	
	public void setIntegral(int index, long value)
	{
		this.bytes[index] = (byte) value;
	}
}
