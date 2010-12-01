package imagej.primitive;

public class DoubleWriter implements DataWriter
{
	private double[] doubles;
	
	public DoubleWriter(Object doubles)
	{
		this.doubles = (double[])doubles;
	}
	
	public void setValue(int index, double value)
	{
		doubles[index] = value;
	}
	
}
