package imagej2.function.unary;

import imagej2.Utils;
import imagej2.function.UnaryFunction;

public class XorUnaryFunction implements UnaryFunction
{
	private double rangeMin;
	private double rangeMax;
	private double constant;
	
	public XorUnaryFunction(double rangeMin, double rangeMax, double constant)
	{
		this.rangeMin = rangeMin;
		this.rangeMax = rangeMax;
		this.constant = constant;
	}
	
	public double compute(double input)
	{
		double value = ((long)input) ^ ((long)constant);
			
		return Utils.boundToRange(this.rangeMin, this.rangeMax, value);
	}
}

