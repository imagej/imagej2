package imagej.imglib.examples.function.function;

import mpicbg.imglib.type.numeric.RealType;

public class ConstantFunction<T extends RealType<T>> implements RealFunction<T>
{
	final double value;
	
	public ConstantFunction(double value)
	{
		this.value = value;
	}
	
	@Override
	public boolean canAccept(int numParameters)
	{
		return numParameters >= 0;
	}

	@Override
	public void compute(T[] inputs, T output)
	{
		output.setReal(value);
	}

}
