package imagej2.function.unary;

import imagej2.function.UnaryFunction;

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

