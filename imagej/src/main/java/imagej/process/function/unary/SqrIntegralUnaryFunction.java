package imagej.process.function.unary;

import imagej.Utils;

public class SqrIntegralUnaryFunction implements UnaryFunction
{
	private double rangeMin;
	private double rangeMax;

	public SqrIntegralUnaryFunction(double rangeMin, double rangeMax)
	{
		this.rangeMin = rangeMin;
		this.rangeMax = rangeMax;
	}
	
	public double compute(double input)
	{
		double value = input * input;
		
		return Utils.boundToRange(this.rangeMin, this.rangeMax, value);
	}
}