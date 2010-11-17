package imagej2.function.binary;

import imagej2.function.BinaryFunction;

public class AddFloatBinaryFunction implements BinaryFunction {

	public AddFloatBinaryFunction()
	{
	}
	
	public double compute(double input1, double input2)
	{
		return input1 + input2;
	}

}
