package imagej.process.function.binary;

public class AddFloatBinaryFunction implements BinaryFunction {

	public AddFloatBinaryFunction()
	{
	}
	
	public double compute(double input1, double input2)
	{
		return input1 + input2;
	}

}
