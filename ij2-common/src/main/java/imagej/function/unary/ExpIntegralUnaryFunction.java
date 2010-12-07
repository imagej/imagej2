package imagej.function.unary;

import imagej.DoubleRange;
import imagej.function.UnaryFunction;

public class ExpIntegralUnaryFunction implements UnaryFunction
{
	private boolean isUnsignedByte;
	private double min;
	private double max;
	private double inputMax;
	
	public ExpIntegralUnaryFunction(boolean isUnsignedByte, double min, double max, double inputMax)
	{
		this.isUnsignedByte = isUnsignedByte;
		this.min = min;
		this.max = max;
		this.inputMax = inputMax;
	}
	
	public double compute(double input)
	{
		double value;
		
		if (this.isUnsignedByte)
		{
			value = (long)(Math.exp(input*(Math.log(255)/255)));
		}
		else // generic short or int
		{
			value = (long)(Math.exp(input*(Math.log(this.inputMax)/this.inputMax)));
		}
		
		value = Math.floor(value);
		
		return DoubleRange.bound(this.min, this.max, value);
	}
}

