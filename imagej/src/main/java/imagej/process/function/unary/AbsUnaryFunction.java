package imagej.process.function.unary;

public class AbsUnaryFunction implements UnaryFunction
{
	public double compute(double input)
	{
		return Math.abs(input);
	}
}

