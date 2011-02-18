package imagej.types;

public class FloatArrayAccessor implements DataAccessor
{
	private float[] floats;
	
	public FloatArrayAccessor(Object data)
	{
		this.floats = (float[]) data;
	}
	
	@Override
	public double getReal(long index)
	{
		return this.floats[(int)index];
	}

	@Override
	public void setReal(long index, double value)
	{
		if (value < -Float.MAX_VALUE) value = -Float.MAX_VALUE;
		if (value > Float.MAX_VALUE) value = Float.MAX_VALUE;
		this.floats[(int)index] = (float) value;
	}

	@Override
	public long getIntegral(long index)
	{
		return (long) this.floats[(int)index];
	}

	@Override
	public void setIntegral(long index, long value)
	{
		if (value < -Float.MAX_VALUE) value = (long)-Float.MAX_VALUE;
		if (value > Float.MAX_VALUE) value = (long)Float.MAX_VALUE;
		this.floats[(int)index] = (float) value;
	}

}
