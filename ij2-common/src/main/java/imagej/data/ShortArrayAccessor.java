package imagej.data;

public class ShortArrayAccessor implements DataAccessor
{
	private short[] shorts;
	
	public ShortArrayAccessor(Object data)
	{
		this.shorts = (short[]) data;
	}
	
	@Override
	public double getReal(long index) {
		return this.shorts[(int)index];
	}

	@Override
	public void setReal(long index, double value) {
		this.shorts[(int)index] = (short) value;
	}

	@Override
	public long getIntegral(long index) {
		return this.shorts[(int)index];
	}

	@Override
	public void setIntegral(long index, long value) {
		this.shorts[(int)index] = (short) value;
	}

}
