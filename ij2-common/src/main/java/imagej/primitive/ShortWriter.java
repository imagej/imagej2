package imagej.primitive;

public class ShortWriter implements DataWriter
{
	private short[] shorts;
	
	public ShortWriter(Object shorts)
	{
		this.shorts = (short[])shorts;
	}
	
	public void setValue(int index, double value)
	{
		if (value < Short.MIN_VALUE)
			value = Short.MIN_VALUE;
		
		if (value > Short.MAX_VALUE)
			value = Short.MAX_VALUE;
		
		shorts[index] = (short) value;
	}
	
}
