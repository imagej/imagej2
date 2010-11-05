package imagej.process.function.unary;

public class SqrFloatUnaryFunction implements UnaryFunction
{
	public SqrFloatUnaryFunction()
	{
	}
	
	public double compute(double input)
	{
		return input * input;
	}
}