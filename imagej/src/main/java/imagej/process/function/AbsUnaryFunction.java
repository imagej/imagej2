package imagej.process.function;

public class AbsUnaryFunction implements UnaryFunction
{
	public double compute(double input)
	{
		return Math.abs(input);
	}
}

