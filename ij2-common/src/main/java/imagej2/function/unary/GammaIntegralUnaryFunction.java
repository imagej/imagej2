package imagej2.function.unary;

import imagej2.Utils;
import imagej2.function.UnaryFunction;

public class GammaIntegralUnaryFunction implements UnaryFunction
{
	private double rangeMin;
	private double rangeMax;
	private double currMin;
	private double constant;
	private double currRange;
	
	public GammaIntegralUnaryFunction(double rangeMin, double rangeMax, double currMin, double currMax, double constant)
	{
		this.rangeMin = rangeMin;
		this.rangeMax = rangeMax;
		this.currMin = currMin;
		this.constant = constant;
		this.currRange = currMax - currMin;
	}
	
	public double compute(double input)
	{
		double value;
		
		if (this.currRange<=0.0 || input <= this.currMin)
			value = input;
		else					
			value = (long)(Math.exp(this.constant*Math.log((input-this.currMin)/this.currRange))*this.currRange+this.currMin);
		
		return Utils.boundToRange(this.rangeMin, this.rangeMax, value);
	}
}
