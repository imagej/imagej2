package imagej.function.unary;

import imagej.function.UnaryFunction;

public class GammaFloatUnaryFunction implements UnaryFunction
{
	private double constant;
	
	public GammaFloatUnaryFunction(double constant)
	{
		this.constant = constant;
	}
	
	public double compute(double input)
	{
		if (input <= 0)
			return 0;
		
		return Math.exp(this.constant * Math.log(input));
	}
}
