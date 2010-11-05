package imagej.process.function.binary;

public class SubtractFloatBinaryFunction implements BinaryFunction {

	public SubtractFloatBinaryFunction()
	{
	}

	public double compute(double input1, double input2)
	{
		return input1 - input2;
	}

}
