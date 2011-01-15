package imagej.data;

public class BooleanArrayAccessor implements DataAccessor
{
	private boolean[] bools;
	
	public BooleanArrayAccessor(Object data)
	{
		this.bools = (boolean[]) data;
	}
	
	@Override
	public double getReal(long index)
	{
		if (this.bools[(int)index])
			return 1;
		else
			return 0;
	}

	@Override
	public void setReal(long index, double value)
	{
		if (value == 0)
			this.bools[(int)index] = false;
		else
			this.bools[(int)index] = true;
	}

	@Override
	public long getIntegral(long index)
	{
		if (this.bools[(int)index])
			return 1;
		else
			return 0;
	}

	@Override
	public void setIntegral(long index, long value)
	{
		if (value == 0)
			this.bools[(int)index] = false;
		else
			this.bools[(int)index] = true;
	}

}
