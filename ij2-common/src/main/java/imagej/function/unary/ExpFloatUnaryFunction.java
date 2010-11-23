package imagej.function.unary;

import imagej.function.UnaryFunction;

public class ExpFloatUnaryFunction implements UnaryFunction
{
	public ExpFloatUnaryFunction()
	{
	}
	
	public double compute(double input)
	{
		return Math.exp(input);
	}
}

