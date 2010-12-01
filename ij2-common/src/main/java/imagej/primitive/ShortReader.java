package imagej.primitive;

public class ShortReader implements DataReader
{
	private short[] pixels;
	
	public ShortReader(Object pixels)
	{
		this.pixels = (short[])pixels;
	}
	
	public double getValue(int i)
	{
		return pixels[i];
	}
}
