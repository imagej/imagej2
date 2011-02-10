package imagej.function.unary;

import imagej.DoubleRange;
import imagej.function.UnaryFunction;

public class LogIntegralUnaryFunction implements UnaryFunction
{
	private double rangeMin;
	private double rangeMax;
	private double currMax;
	
	public LogIntegralUnaryFunction(double rangeMin, double rangeMax, double currMax)
	{
		this.rangeMin = rangeMin;
		this.rangeMax = rangeMax;
		this.currMax = currMax;
	}
	
	public double compute(double input)
	{
		double value;
		
		if (input <= 0)
			value = 0;
		else 
			value = (long)(Math.log(input)*(this.currMax/Math.log(this.currMax)));
			
		return DoubleRange.bound(this.rangeMin, this.rangeMax, value);
	}
}

