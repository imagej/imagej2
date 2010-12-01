package imagej.primitive;

public class FloatReader implements DataReader
{
	private float[] pixels;
	
	public FloatReader(Object pixels)
	{
		this.pixels = (float[])pixels;
	}
	
	public double getValue(int i)
	{
		return pixels[i];
	}
}
