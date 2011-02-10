package imagej.function.unary;

import imagej.DoubleRange;
import imagej.function.UnaryFunction;

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
		
		return DoubleRange.bound(this.rangeMin, this.rangeMax, value);
	}
}