package imagej.function.binary;

import imagej.function.BinaryFunction;

public class SubtractFloatBinaryFunction implements BinaryFunction {

	public SubtractFloatBinaryFunction()
	{
	}

	public double compute(double input1, double input2)
	{
		return input1 - input2;
	}

}
