package imagej2.function.binary;

import imagej2.function.BinaryFunction;

public class OrBinaryFunction implements BinaryFunction {

	public double compute(double input1, double input2)
	{
		return ((long)input1) | ((long)(input2));
	}

}
