package imagej.data;

public class IntAccessor implements DataAccessor
{
	private int[] ints;
	
	public IntAccessor(Object data)
	{
		this.ints = (int[]) data;
	}
	
	@Override
	public double getReal(int index)
	{
		return this.ints[index];
	}

	@Override
	public void setReal(int index, double value)
	{
		this.ints[index] = (int)value;
	}

	@Override
	public long getIntegral(int index)
	{
		return this.ints[index];
	}

	@Override
	public void setIntegral(int index, long value)
	{
		this.ints[index] = (int)value;
	}

}
