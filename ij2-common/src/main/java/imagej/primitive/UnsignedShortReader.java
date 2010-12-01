package imagej.primitive;

public class UnsignedShortReader implements DataReader
{
	private short[] pixels;
	
	public UnsignedShortReader(Object pixels)
	{
		this.pixels = (short[])pixels;
	}
	
	public double getValue(int i)
	{
		double pixel = pixels[i];
		if (pixel < 0)
			pixel = 65536.0 + pixel;
		return pixel;
	}
}
