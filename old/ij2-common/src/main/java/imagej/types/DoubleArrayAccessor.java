package imagej.types;

public class DoubleArrayAccessor implements DataAccessor
{
	private double[] doubles;
	
	public DoubleArrayAccessor(Object data)
	{
		this.doubles = (double[]) data;
	}
	
	@Override
	public double getReal(long index)
	{
		return this.doubles[(int)index];
	}

	@Override
	public void setReal(long index, double value)
	{
		this.doubles[(int)index] = value;
	}

	@Override
	public long getIntegral(long index)
	{
		return (long) this.doubles[(int)index];
	}

	@Override
	public void setIntegral(long index, long value)
	{
		this.doubles[(int)index] = value;
	}

}
