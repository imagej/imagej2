package imagej.primitive;

public class LongReader implements DataReader
{
	private long[] pixels;
	
	public LongReader(Object pixels)
	{
		this.pixels = (long[])pixels;
	}
	
	public double getValue(int i)
	{
		return pixels[i];
	}
}
