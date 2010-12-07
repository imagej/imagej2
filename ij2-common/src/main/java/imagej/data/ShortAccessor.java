package imagej.data;

public class ShortAccessor implements DataAccessor {

	private short[] shorts;
	
	public ShortAccessor(Object data)
	{
		this.shorts = (short[]) data;
	}
	
	@Override
	public double getReal(int index) {
		return this.shorts[index];
	}

	@Override
	public void setReal(int index, double value) {
		this.shorts[index] = (short) value;
	}

	@Override
	public long getIntegral(int index) {
		return this.shorts[index];
	}

	@Override
	public void setIntegral(int index, long value) {
		this.shorts[index] = (short) value;
	}

}
