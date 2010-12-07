package imagej.data;

public class UnsignedIntAccessor implements DataAccessor {

	private int[] ints;
	
	public UnsignedIntAccessor(Object data)
	{
		this.ints = (int[]) data;
	}
	
	@Override
	public double getReal(int index)
	{
		double value = this.ints[index];
		
		if (value < 0)
			value = 4294967296.0 + value;
		
		return value;
	}

	@Override
	public void setReal(int index, double value)
	{
		if (value < 0) value = 0;
		if (value > 0xffffffffL) value = 0xffffffffL;
		this.ints[index] = (int) ((long)value & 0xffffffffL);
	}

	@Override
	public long getIntegral(int index)
	{
		long value = this.ints[index];
		
		if (value < 0)
			value = 4294967296L + value;
		
		return value;
	}

	@Override
	public void setIntegral(int index, long value)
	{
		if (value < 0) value = 0;
		if (value > 0xffffffffL) value = 0xffffffffL;
		this.ints[index] = (int) (value & 0xffffffffL);
	}

}
