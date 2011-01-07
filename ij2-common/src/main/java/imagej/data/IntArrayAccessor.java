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
		// TODO : Imglib sets values that out of range by wraping them to other side (neg to pos or pos to neg). Determine who needs to fix code. 
		if (value < Integer.MIN_VALUE) value = Integer.MIN_VALUE;
		if (value > Integer.MAX_VALUE) value = Integer.MAX_VALUE;
		this.ints[(int)index] = (int) value; // TODO - closer to Imglib : Math.round(value);
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
