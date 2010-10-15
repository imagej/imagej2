package imagej.process.function.unary;

import imagej.process.TypeManager;
import mpicbg.imglib.type.numeric.RealType;

public class AddUnaryFunction implements UnaryFunction
{
	private boolean dataIsIntegral;
	private RealType<?> targetType;
	private double constant;
	
	public AddUnaryFunction(RealType<?> targetType, double constant)
	{
		this.targetType = targetType;
		this.constant = constant;
		this.dataIsIntegral = TypeManager.isIntegralType(targetType);
	}
	
	public double compute(double input)
	{
		double value = input + this.constant;
		
		if (this.dataIsIntegral)
		{
			value = Math.floor(value);
			value = TypeManager.boundValueToType(this.targetType, value);
		}
		
		return value;
	}
}
