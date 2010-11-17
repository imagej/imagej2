package imagej2.function.unary;

import imagej2.function.UnaryFunction;

public class MinUnaryFunction implements UnaryFunction
{
	private double constant;
	
	public MinUnaryFunction(double constant)
	{
		this.constant = constant;
	}
	
	public double compute(double input)
	{
		double value;
		if (input < this.constant)
			value = this.constant;
		else
			value = input;
	
		return value;
	}
}

