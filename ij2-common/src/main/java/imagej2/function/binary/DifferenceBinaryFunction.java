package imagej2.function.binary;

import imagej2.function.BinaryFunction;

public class DifferenceBinaryFunction implements BinaryFunction {

	public double compute(double input1, double input2)
	{
		return ( Math.abs(input2 - input1) );
	}

}
