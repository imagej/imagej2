package imagej.function.binary;

import imagej.function.BinaryFunction;

public class AddFloatBinaryFunction implements BinaryFunction {

	public double compute(double input1, double input2)
	{
		return input1 + input2;
	}

}
