package imagej.function.unary;

import imagej.DoubleRange;
import imagej.function.UnaryFunction;

public class SqrUshortUnaryFunction implements UnaryFunction
{
	private double rangeMin;
	private double rangeMax;
	
	public SqrUshortUnaryFunction(double rangeMin, double rangeMax)
	{
		this.rangeMin = rangeMin;
		this.rangeMax = rangeMax;
	}
	
	public double compute(double input)
	{
		double value = input * input;
		
		// this needed for compatibility with old SHortProcessor
		if (value > Integer.MAX_VALUE)
			value = 0;

		return DoubleRange.bound(this.rangeMin, this.rangeMax, value);
	}
}