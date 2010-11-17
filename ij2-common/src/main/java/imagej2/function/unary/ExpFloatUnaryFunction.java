package imagej2.function.unary;

import imagej2.function.UnaryFunction;

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

