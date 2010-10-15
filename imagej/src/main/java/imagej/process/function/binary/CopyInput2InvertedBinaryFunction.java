package imagej.process.function.binary;

import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;

public class CopyInput2InvertedBinaryFunction implements BinaryFunction {

	private double max;
	private boolean isUnsignedByte;
	
	public CopyInput2InvertedBinaryFunction(RealType<?> targetType)
	{
		this.max = targetType.getMaxValue();
		this.isUnsignedByte = targetType instanceof UnsignedByteType;
	}
	
	public double compute(double input1, double input2)
	{
		double value = input1;
		
		if (isUnsignedByte)
			value = this.max - input2;

		return value;
	}

}
