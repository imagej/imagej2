package imagej.process.function.binary;

public class AverageIntegralBinaryFunction implements BinaryFunction {

	public AverageIntegralBinaryFunction()
	{
	}

	public double compute(double input1, double input2)
	{
		return ((long)input1 + (long)input2) / 2;
	}

}
