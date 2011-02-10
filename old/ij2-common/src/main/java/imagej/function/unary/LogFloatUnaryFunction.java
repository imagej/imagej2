package imagej.function.unary;

import imagej.function.UnaryFunction;

public class LogFloatUnaryFunction implements UnaryFunction
{
	public LogFloatUnaryFunction()
	{
	}
	
	public double compute(double input)
	{
		if (input <= 0)
			return 0;
		
		return Math.log(input);
	}
}

