package imagej.process.function.unary;

import imagej.Utils;


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
		
		return Utils.boundToRange(this.min, this.max, value);
	}
}

