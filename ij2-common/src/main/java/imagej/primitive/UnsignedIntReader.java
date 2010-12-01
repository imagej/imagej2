package imagej.primitive;

public class UnsignedIntReader implements DataReader
{
	private int[] pixels;
	
	public UnsignedIntReader(Object pixels)
	{
		this.pixels = (int[])pixels;
	}
	
	public double getValue(int i)
	{
		double pixel = pixels[i];
		if (pixel < 0)
			pixel = 4294967296.0 + pixel;
		return pixel;
	}
}
