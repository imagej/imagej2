package imagej2.function.unary;

import imagej2.function.UnaryFunction;

public class AddFloatUnaryFunction implements UnaryFunction
{
	private double constant;
	
	public AddFloatUnaryFunction(double constant)
	{
		this.constant = constant;
	}
	
	public double compute(double input)
	{
		return input + this.constant;
	}
}
