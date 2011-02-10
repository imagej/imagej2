package imagej.function.unary;

import imagej.DoubleRange;
import imagej.function.UnaryFunction;

public class AddIntegralUnaryFunction implements UnaryFunction
{
	private double min;
	private double max;
	private double constant;
	
	public AddIntegralUnaryFunction(double min, double max, double constant)
	{
		this.min = min;
		this.max = max;
		this.constant = constant;
	}
	
	public double compute(double input)
	{
		double value = input + this.constant;
		
		value = Math.floor(value);
		
		value = DoubleRange.bound(this.min, this.max, value);
		
		return value;
	}
}
