package imagej.primitive;

public class UnsignedByteReader implements DataReader
{
	private byte[] pixels;
	
	public UnsignedByteReader(Object pixels)
	{
		this.pixels = (byte[])pixels;
	}
	
	public double getValue(int i)
	{
		double pixel = pixels[i];
		if (pixel < 0)
			pixel = 256.0 + pixel;
		return pixel;
	}
}
