package imagej.process.function;

import imagej.process.TypeManager;
import mpicbg.imglib.type.numeric.RealType;

public class SqrtUnaryFunction implements UnaryFunction
{
	private boolean dataIsIntegral;
	
	public SqrtUnaryFunction(RealType<?> targetType)
	{
		this.dataIsIntegral = TypeManager.isIntegralType(targetType);
	}
	
	public double compute(double input)
	{
		double value;
		if (input < 0)
			value = 0;
		else
			value = Math.sqrt(input);
	
		if (this.dataIsIntegral)
			value = (long) value;
		
		return value;
	}
}

