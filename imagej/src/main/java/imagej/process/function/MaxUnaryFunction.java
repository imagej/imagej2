package imagej.process.function;

public class MaxUnaryFunction implements UnaryFunction
{
	private double constant;
	
	public MaxUnaryFunction(double constant)
	{
		this.constant = constant;
	}
	
	public double compute(double input)
	{
		double value;
		if (input > this.constant)
			value = this.constant;
		else
			value = input;
	
		return value;
	}
}


