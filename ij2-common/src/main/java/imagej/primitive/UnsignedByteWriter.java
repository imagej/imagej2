package imagej.primitive;

public class UnsignedByteWriter implements DataWriter
{
	private byte[] bytes;
	
	public UnsignedByteWriter(Object bytes)
	{
		this.bytes = (byte[])bytes;
	}
	
	public void setValue(int index, double value)
	{
		if (value < 0)
			value = 0;
		
		if (value > 0xff)
			value = 0xff;
		
		bytes[index] = (byte) ((int)value & 0xff);
	}
	
}
