package imagej.data;

public class UnsignedShortArrayAccessor implements DataAccessor
{
	private short[] shorts;
	
	public UnsignedShortArrayAccessor(Object data)
	{
		this.shorts = (short[]) data;
	}
	
	@Override
	public double getReal(long index)
	{
		int value = this.shorts[(int)index];
		
		if (value < 0)
			value = 65536 + value;
		
		return value;
	}

	@Override
	public void setReal(long index, double value)
	{
		if (value < 0) value = 0;
		if (value > 0xffff) value = 0xffff;
		this.shorts[(int)index] = (short) ((int)value & 0xffff);
	}

	@Override
	public long getIntegral(long index)
	{
		int value = this.shorts[(int)index];
		
		if (value < 0)
			value += 65536 + value;
		
		return value;
	}

	@Override
	public void setIntegral(long index, long value)
	{
		if (value < 0) value = 0;
		if (value > 0xffff) value = 0xffff;
		this.shorts[(int)index] = (short) ((int)value & 0xffff);
	}

}
