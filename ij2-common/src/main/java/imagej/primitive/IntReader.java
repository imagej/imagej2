package imagej.primitive;

public class IntReader implements DataReader
{
	private int[] pixels;
	
	public IntReader(Object pixels)
	{
		this.pixels = (int[])pixels;
	}
	
	public double getValue(int i)
	{
		return pixels[i];
	}
}

