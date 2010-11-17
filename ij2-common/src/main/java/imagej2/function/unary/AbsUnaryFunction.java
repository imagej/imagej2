package imagej2.function.unary;

import imagej2.function.UnaryFunction;

public class AbsUnaryFunction implements UnaryFunction
{
	public double compute(double input)
	{
		return Math.abs(input);
	}
}

