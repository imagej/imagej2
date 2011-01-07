package imagej.data;

public class LongArrayAccessor implements DataAccessor
{
	private long[] longs;
	
	public LongArrayAccessor(Object data)
	{
		this.longs = (long[]) data;
	}
	
	@Override
	public double getReal(long index)
	{
		return this.longs[(int)index];
	}

	@Override
	public void setReal(long index, double value)
	{
		// TODO : Imglib sets values that out of range by wraping them to other side (neg to pos or pos to neg). Determine who needs to fix code. 
		if (value < Long.MIN_VALUE) value = Long.MIN_VALUE;
		if (value > Long.MAX_VALUE) value = Long.MAX_VALUE;
		this.longs[(int)index] = (long) value; // TODO - closer to Imglib : Math.round(value);
	}

	@Override
	public long getIntegral(long index)
	{
		return this.longs[(int)index];
	}

	@Override
	public void setIntegral(long index, long value)
	{
		this.longs[(int)index] = value;
	}

}
