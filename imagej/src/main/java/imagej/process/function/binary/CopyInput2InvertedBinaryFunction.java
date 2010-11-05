package imagej.process.function.binary;

public class CopyInput2InvertedBinaryFunction implements BinaryFunction {

	private double max;
	private boolean isUnsignedByte;
	
	public CopyInput2InvertedBinaryFunction(double maxValue, boolean isUnsignedByte)
	{
		this.max = maxValue;
		this.isUnsignedByte = isUnsignedByte;
	}
	
	public double compute(double input1, double input2)
	{
		double value = input1;
		
		if (this.isUnsignedByte)
			value = this.max - input2;

		return value;
	}

}
