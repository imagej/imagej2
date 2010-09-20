package imagej.process.function;

public class CopyInput2ZeroTransparentBinaryFunction implements BinaryFunction {

	public double compute(double input1, double input2)
	{
		if (input2 != 0 )
			return input2;
		else
			return input1;
	}

}
