package ij.process;

import mpicbg.imglib.type.numeric.RealType;

public class AbsUnaryFunction implements UnaryFunction
{
	@Override
	public void compute(RealType<?> result, RealType<?> input)
	{
		double value = Math.abs(input.getRealDouble());
		result.setReal(value);
	}
}

