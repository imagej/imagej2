package imagej.types;

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
		// TODO : Imglib sets values that out of range by wrapping them to other side (neg->pos or pos->neg).
		// Determine who needs to fix code. 
		//if (value < Integer.MIN_VALUE) value = Integer.MIN_VALUE;
		//if (value > Integer.MAX_VALUE) value = Integer.MAX_VALUE;
		value += (0.5d * Math.signum( value ) );  // TODO - this is essentially what imglib does
		this.ints[(int)index] = (int) (long) value;  // (long) cast needed or we differ from Imglib
	}

	@Override
	public long getIntegral(long index)
	{
		return this.ints[(int)index];
	}

	@Override
	public void setIntegral(long index, long value)
	{
		// TODO : Imglib sets values that out of range by wrapping them to other side (neg->pos or pos->neg).
		// Determine who needs to fix code. 
		//if (value < Integer.MIN_VALUE) value = Integer.MIN_VALUE;
		//if (value > Integer.MAX_VALUE) value = Integer.MAX_VALUE;
		this.ints[(int)index] = (int)value;
	}

}
