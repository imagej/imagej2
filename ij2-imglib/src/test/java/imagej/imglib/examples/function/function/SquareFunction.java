package imagej.imglib.examples.function.function;

import mpicbg.imglib.type.numeric.RealType;

public class SquareFunction<T extends RealType<T>> implements RealFunction<T>
{
	@Override
	public boolean canAccept(int numParameters) { return numParameters == 1; }

	@Override
	public void compute(T[] inputs, T output)
	{
		double inValue = inputs[0].getRealDouble();
		output.setReal(inValue * inValue);
	}
	
}
