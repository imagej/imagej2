package imagej.data;

public class ShortArrayAccessor implements DataAccessor
{
	private short[] shorts;
	
	public ShortArrayAccessor(Object data)
	{
		this.shorts = (short[]) data;
	}
	
	@Override
	public double getReal(long index)
	{
		return this.shorts[(int)index];
	}

	@Override
	public void setReal(long index, double value)
	{
		// TODO : Imglib sets values that out of range by wraping them to other side (neg to pos or pos to neg). Determine who needs to fix code. 
		if (value < Short.MIN_VALUE) value = Short.MIN_VALUE;
		if (value > Short.MAX_VALUE) value = Short.MAX_VALUE;
		this.shorts[(int)index] = (short) value; // TODO - closer to Imglib : Math.round(value);
	}

	@Override
	public long getIntegral(long index)
	{
		return this.shorts[(int)index];
	}

	@Override
	public void setIntegral(long index, long value)
	{
		if (value < Short.MIN_VALUE) value = Short.MIN_VALUE;
		if (value > Short.MAX_VALUE) value = Short.MAX_VALUE;
		this.shorts[(int)index] = (short) value;
	}

}
