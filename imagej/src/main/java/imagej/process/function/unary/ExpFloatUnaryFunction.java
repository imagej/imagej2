package imagej.process.function.unary;

public class ExpFloatUnaryFunction implements UnaryFunction
{
	public ExpFloatUnaryFunction()
	{
	}
	
	public double compute(double input)
	{
		return Math.exp(input);
	}
}

