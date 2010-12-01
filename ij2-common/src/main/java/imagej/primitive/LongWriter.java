package imagej.primitive;

public class LongWriter implements DataWriter
{
	private long[] longs;
	
	public LongWriter(Object longs)
	{
		this.longs = (long[])longs;
	}
	
	public void setValue(int index, double value)
	{
		if (value < Long.MIN_VALUE)
			value = Long.MIN_VALUE;
		
		if (value > Long.MAX_VALUE)
			value = Long.MAX_VALUE;
		
		longs[index] = (long) value;
	}
	
}
