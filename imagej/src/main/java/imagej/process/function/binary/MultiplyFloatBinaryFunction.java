package imagej.process.function.binary;

public class MultiplyFloatBinaryFunction implements BinaryFunction {

	public MultiplyFloatBinaryFunction()
	{
	}

	public double compute(double input1, double input2)
	{
		return input1 * input2;
	}

}
