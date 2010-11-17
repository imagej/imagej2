package imagej2.function.unary;

import imagej2.function.UnaryFunction;

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