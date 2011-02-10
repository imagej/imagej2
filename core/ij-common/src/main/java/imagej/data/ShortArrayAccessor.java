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
		// TODO : Imglib sets values that out of range by wrapping them to other side (neg->pos or pos->neg).
		// Determine who needs to fix code. 
		//if (value < Short.MIN_VALUE) value = Short.MIN_VALUE;
		//if (value > Short.MAX_VALUE) value = Short.MAX_VALUE;
		value += (0.5d * Math.signum( value ) );  // TODO - this is essentially what imglib does
		this.shorts[(int)index] = (short) value;
	}

	@Override
	public long getIntegral(long index)
	{
		return this.shorts[(int)index];
	}

	@Override
	public void setIntegral(long index, long value)
	{
		// TODO : Imglib sets values that out of range by wrapping them to other side (neg->pos or pos->neg).
		// Determine who needs to fix code. 
		//if (value < Short.MIN_VALUE) value = Short.MIN_VALUE;
		//if (value > Short.MAX_VALUE) value = Short.MAX_VALUE;
		this.shorts[(int)index] = (short) value;
	}

}
