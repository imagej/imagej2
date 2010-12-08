package imagej.data;

public class IntArrayAccessor implements DataAccessor
{
	private int[] ints;
	
	public IntArrayAccessor(Object data)
	{
		this.ints = (int[]) data;
	}
	
	@Override
	public double getReal(long index)
	{
		return this.ints[(int)index];
	}

	@Override
	public void setReal(long index, double value)
	{
		this.ints[(int)index] = (int)value;
	}

	@Override
	public long getIntegral(long index)
	{
		return this.ints[(int)index];
	}

	@Override
	public void setIntegral(long index, long value)
	{
		this.ints[(int)index] = (int)value;
	}

}
