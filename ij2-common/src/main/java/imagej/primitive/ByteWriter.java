package imagej.primitive;

public class ByteWriter implements DataWriter
{
	private byte[] bytes;
	
	public ByteWriter(Object bytes)
	{
		this.bytes = (byte[])bytes;
	}
	
	public void setValue(int index, double value)
	{
		if (value < Byte.MIN_VALUE)
			value = Byte.MIN_VALUE;
		
		if (value > Byte.MAX_VALUE)
			value = Byte.MAX_VALUE;
		
		bytes[index] = (byte) value;
	}
	
}
