package imagej.data;

public class FloatAccessor implements DataAccessor {

	private float[] floats;
	
	public FloatAccessor(Object data)
	{
		this.floats = (float[]) data;
	}
	
	@Override
	public double getReal(int index)
	{
		return this.floats[index];
	}

	@Override
	public void setReal(int index, double value)
	{
		this.floats[index] = (float) value;
	}

	@Override
	public long getIntegral(int index)
	{
		return (long) this.floats[index];
	}

	@Override
	public void setIntegral(int index, long value)
	{
		this.floats[index] = (float) value;
	}

}
