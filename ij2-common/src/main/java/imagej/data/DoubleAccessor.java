package imagej.data;

public class DoubleAccessor implements DataAccessor
{
	private double[] doubles;
	
	public DoubleAccessor(Object data)
	{
		this.doubles = (double[]) data;
	}
	
	@Override
	public double getReal(int index)
	{
		return this.doubles[index];
	}

	@Override
	public void setReal(int index, double value)
	{
		this.doubles[index] = value;
	}

	@Override
	public long getIntegral(int index)
	{
		return (long) this.doubles[index];
	}

	@Override
	public void setIntegral(int index, long value)
	{
		this.doubles[index] = value;
	}

}
