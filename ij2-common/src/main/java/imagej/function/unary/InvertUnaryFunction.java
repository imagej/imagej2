package imagej.function.unary;

import imagej.DoubleRange;
import imagej.function.UnaryFunction;

public class InvertUnaryFunction implements UnaryFunction
{
	private double rangeMin, rangeMax;
	private double currMin, currMax;
	
	public InvertUnaryFunction(double rangeMin, double rangeMax, double currMin, double currMax)
	{
		this.rangeMin = rangeMin;
		this.rangeMax = rangeMax;
		this.currMin = currMin;
		this.currMax = currMax;
	}
	
	public double compute(double input)
	{
		double value = this.currMax - (input - this.currMin);

		return DoubleRange.bound(this.rangeMin, this.rangeMax, value);
	}
}
