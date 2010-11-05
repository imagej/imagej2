package imagej.process.function.binary;

public class OrBinaryFunction implements BinaryFunction {

	public double compute(double input1, double input2)
	{
		return ((long)input1) | ((long)(input2));
	}

}
