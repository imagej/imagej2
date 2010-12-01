package imagej.primitive;

public class DoubleReader implements DataReader
{
	private double[] pixels;
	
	public DoubleReader(Object pixels)
	{
		this.pixels = (double[])pixels;
	}
	
	public double getValue(int i)
	{
		return pixels[i];
	}
}
