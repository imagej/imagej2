package imagej.process.function.unary;

import imagej.process.TypeManager;
import mpicbg.imglib.type.numeric.RealType;

public class InvertUnaryFunction implements UnaryFunction
{
	private RealType<?> targetType;
	private double min, max;
	private boolean dataIsIntegral;
	
	public InvertUnaryFunction(RealType<?> targetType, double min, double max)
	{
		this.targetType = targetType;
		this.min = min;
		this.max = max;
		this.dataIsIntegral = TypeManager.isIntegralType(targetType);
	}
	
	public double compute(double input)
	{
		double value = this.max - (input - this.min);
		
		if (this.dataIsIntegral)
			value = TypeManager.boundValueToType(targetType, value);
		
		return value;
	}
}
