package imagej.primitive;

public class ByteReader implements DataReader
{
	private byte[] pixels;
	
	public ByteReader(Object pixels)
	{
		this.pixels = (byte[])pixels;
	}
	
	public double getValue(int i)
	{
		return pixels[i];
	}
}
