package imagej.primitive;

public class UnsignedIntWriter implements DataWriter
{
	private int[] ints;
	
	public UnsignedIntWriter(Object ints)
	{
		this.ints = (int[])ints;
	}
	
	public void setValue(int index, double value)
	{
		if (value < 0)
			value = 0;
		
		if (value > (double)(0xffffffffL))
			value = (double)(0xffffffffL);
		
		ints[index] = (int) ((long)value & 0xffffffffL);
	}
	
}
