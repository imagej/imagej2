package imagej.function.unary;

import imagej.DoubleRange;
import imagej.function.UnaryFunction;


public class AndUnaryFunction implements UnaryFunction
{
	private double min;
	private double max;
	private double constant;
	
	public AndUnaryFunction(double min, double max, double constant)
	{
		this.min = min;
		this.max = max;
		this.constant = constant;
	}
	
	public double compute(double input)
	{
		double value = ((long)input) & ((long)this.constant);
		
		return DoubleRange.bound(this.min, this.max, value);
	}
}

