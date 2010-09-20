package imagej.process.function;

import mpicbg.imglib.type.numeric.RealType;
import imagej.process.TypeManager;

public class SubtractBinaryFunction implements BinaryFunction {

	private boolean isIntegral;
	private double min;
	
	public SubtractBinaryFunction(RealType<?> targetType)
	{
		this.isIntegral = TypeManager.isIntegralType(targetType);
		this.min = targetType.getMinValue();
	}

	public double compute(double input1, double input2)
	{
		double value = input1 - input2;
		
		if ((this.isIntegral) && (value < this.min))
			value = this.min;
		
		return value;
	}

}
