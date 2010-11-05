package imagej.process.function.unary;

public class MultiplyFloatUnaryFunction implements UnaryFunction
{
	private double constant;
	
	public MultiplyFloatUnaryFunction(double constant)
	{
		this.constant = constant;
	}
	
	public double compute(double input)
	{
		return input * this.constant;
	}
}