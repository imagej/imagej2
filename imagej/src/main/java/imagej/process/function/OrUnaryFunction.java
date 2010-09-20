package imagej.process.function;

import imagej.process.TypeManager;
import mpicbg.imglib.type.numeric.RealType;

public class OrUnaryFunction implements UnaryFunction
{
	private RealType<?> targetType;
	private double constant;
	
	public OrUnaryFunction(RealType<?> targetType, double constant)
	{
		this.targetType = targetType;
		this.constant = constant;
	}
	
	public double compute(double input)
	{
		double value = ((int)input) | ((int)constant);
			
		value = TypeManager.boundValueToType(this.targetType, value);
		
		return value;
	}
}

