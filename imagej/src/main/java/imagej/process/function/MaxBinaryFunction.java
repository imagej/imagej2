package imagej.process.function;

public class MaxBinaryFunction implements BinaryFunction {

	public double compute(double input1, double input2)
	{
		if (input1 > input2)
			return input1;
		else
			return input2;
	}

}
