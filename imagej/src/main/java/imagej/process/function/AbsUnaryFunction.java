package imagej.process.function;

import mpicbg.imglib.type.numeric.RealType;

public class AbsUnaryFunction implements UnaryFunction
{
	public void compute(RealType<?> result, RealType<?> input)
	{
		double value = Math.abs(input.getRealDouble());
		result.setReal(value);
	}
}

