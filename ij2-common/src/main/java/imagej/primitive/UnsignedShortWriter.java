package imagej.primitive;

public class UnsignedShortWriter implements DataWriter
{
	private short[] shorts;
	
	public UnsignedShortWriter(Object shorts)
	{
		this.shorts = (short[])shorts;
	}
	
	public void setValue(int index, double value)
	{
		if (value < 0)
			value = 0;
		
		if (value > 0xffff)
			value = 0xffff;
		
		shorts[index] = (short) ((int)value & 0xffff);
	}
	
}
