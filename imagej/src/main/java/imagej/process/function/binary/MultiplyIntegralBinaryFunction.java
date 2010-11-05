package imagej.process.function.binary;

public class MultiplyIntegralBinaryFunction implements BinaryFunction {

	private double max;
	
	public MultiplyIntegralBinaryFunction(double max)
	{
		this.max = max;
	}

	public double compute(double input1, double input2)
	{
		double value = input1 * input2;
		if (value > this.max)
			value = this.max;
		return value;
	}

}
