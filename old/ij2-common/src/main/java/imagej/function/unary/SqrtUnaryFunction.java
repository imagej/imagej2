package imagej.function.unary;

import imagej.function.UnaryFunction;

public class SqrtUnaryFunction implements UnaryFunction
{
	private boolean dataIsIntegral;
	
	public SqrtUnaryFunction(boolean isIntegral)
	{
		this.dataIsIntegral = isIntegral;
	}
	
	public double compute(double input)
	{
		double value;
		if (input < 0)
			value = 0;
		else
			value = Math.sqrt(input);
	
		if (this.dataIsIntegral)
			value = (long) value;
		
		return value;
	}
}

