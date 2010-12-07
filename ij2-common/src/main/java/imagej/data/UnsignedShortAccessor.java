package imagej.data;

public class UnsignedShortAccessor implements DataAccessor
{
	private short[] shorts;
	
	public UnsignedShortAccessor(Object data)
	{
		this.shorts = (short[]) data;
	}
	
	@Override
	public double getReal(int index)
	{
		int value = this.shorts[index];
		
		if (value < 0)
			value = 65536 + value;
		
		return value;
	}

	@Override
	public void setReal(int index, double value)
	{
		if (value < 0) value = 0;
		if (value > 0xffff) value = 0xffff;
		this.shorts[index] = (short) ((int) value & 0xffff);
	}

	@Override
	public long getIntegral(int index)
	{
		int value = this.shorts[index];
		
		if (value < 0)
			value += 65536 + value;
		
		return value;
	}

	@Override
	public void setIntegral(int index, long value)
	{
		if (value < 0) value = 0;
		if (value > 0xffff) value = 0xffff;
		this.shorts[index] = (short) ((int) value & 0xffff);
	}

}
