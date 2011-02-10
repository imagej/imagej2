package imagej.function.unary;

import imagej.function.UnaryFunction;

public class AbsUnaryFunction implements UnaryFunction
{
	public double compute(double input)
	{
		return Math.abs(input);
	}
}

