package imagej.function.unary;

import imagej.function.UnaryFunction;

public class SqrFloatUnaryFunction implements UnaryFunction
{
	public SqrFloatUnaryFunction()
	{
	}
	
	public double compute(double input)
	{
		return input * input;
	}
}