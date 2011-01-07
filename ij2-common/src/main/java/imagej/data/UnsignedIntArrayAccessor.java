package imagej.data;

public class UnsignedIntArrayAccessor implements DataAccessor
{
	private int[] ints;
	
	public UnsignedIntArrayAccessor(Object data)
	{
		this.ints = (int[]) data;
	}
	
	@Override
	public double getReal(long index)
	{
		double value = this.ints[(int)index];
		
		if (value < 0)
			value += 4294967296.0;
		
		return value;
	}

	@Override
	public void setReal(long index, double value)
	{
		// TODO : Imglib sets values that out of range by wraping them to other side (neg to pos or pos to neg). Determine who needs to fix code. 
		if (value < 0) value = 0;
		if (value > 0xffffffffL) value = 0xffffffffL;
		this.ints[(int)index] = (int) ((long)value & 0xffffffffL);  // TODO - closer to ImgLib : (int)((long)Math.round(value) & ...)
	}

	@Override
	public long getIntegral(long index)
	{
		long value = this.ints[(int)index];
		
		if (value < 0)
			value += 4294967296L;
		
		return value;
	}

	@Override
	public void setIntegral(long index, long value)
	{
		if (value < 0) value = 0;
		if (value > 0xffffffffL) value = 0xffffffffL;
		this.ints[(int)index] = (int) (value & 0xffffffffL);
	}

}
