package imagej.data;

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
		this.floats[(int)index] = (float) value;
	}

}
