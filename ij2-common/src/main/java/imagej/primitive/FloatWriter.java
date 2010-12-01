package imagej.primitive;

public class FloatWriter implements DataWriter
{
	private float[] floats;
	
	public FloatWriter(Object floats)
	{
		this.floats = (float[])floats;
	}
	
	public void setValue(int index, double value)
	{
		floats[index] = (float) value;
	}
	
}
