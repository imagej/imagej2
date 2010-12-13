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
		if (value < Integer.MIN_VALUE) value = Integer.MIN_VALUE;
		if (value > Integer.MAX_VALUE) value = Integer.MAX_VALUE;
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
		if (value < Integer.MIN_VALUE) value = Integer.MIN_VALUE;
		if (value > Integer.MAX_VALUE) value = Integer.MAX_VALUE;
		this.ints[(int)index] = (int)value;
	}

}
