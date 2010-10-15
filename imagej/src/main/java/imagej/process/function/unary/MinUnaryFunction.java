package imagej.process.function.unary;

public class MinUnaryFunction implements UnaryFunction
{
	private double constant;
	
	public MinUnaryFunction(double constant)
	{
		this.constant = constant;
	}
	
	public double compute(double input)
	{
		double value;
		if (input < this.constant)
			value = this.constant;
		else
			value = input;
	
		return value;
	}
}

