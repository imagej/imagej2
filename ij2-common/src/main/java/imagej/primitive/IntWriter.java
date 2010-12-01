package imagej.primitive;

public class IntWriter implements DataWriter
{
	private int[] ints;
	
	public IntWriter(Object ints)
	{
		this.ints = (int[])ints;
	}
	
	public void setValue(int index, double value)
	{
		if (value < Integer.MIN_VALUE)
			value = Integer.MIN_VALUE;
		
		if (value > Integer.MAX_VALUE)
			value = Integer.MAX_VALUE;
		
		ints[index] = (int) value;
	}
	
}
