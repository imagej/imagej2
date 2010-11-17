package imagej2.function.binary;

import imagej2.function.BinaryFunction;

public class SubtractIntegralBinaryFunction implements BinaryFunction {

	private double min;
	
	public SubtractIntegralBinaryFunction(double minValue)
	{
		this.min = minValue;
	}

	public double compute(double input1, double input2)
	{
		double value = input1 - input2;
		
		if (value < this.min)
			value = this.min;
		
		return value;
	}

}
