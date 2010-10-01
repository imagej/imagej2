package imagej.process.function;

import imagej.process.TypeManager;
import mpicbg.imglib.type.numeric.RealType;

public class LogUnaryFunction implements UnaryFunction
{
	private RealType<?> targetType;
	private double max;
	private boolean dataIsIntegral;
	
	public LogUnaryFunction(RealType<?> targetType, double max)
	{
		this.targetType = targetType;
		this.max = max;
		this.dataIsIntegral = TypeManager.isIntegralType(targetType);
	}
	
	public double compute(double input)
	{
		double value;
		
		if (this.dataIsIntegral)
		{
			if (input <= 0)
				value = 0;
			else 
				value = (int)(Math.log(input)*(this.max/Math.log(this.max)));
			
			value = TypeManager.boundValueToType(this.targetType, value);
		}
		else // float
		{
			if (input <= 0)
				value = 0;
			else
				value = Math.log(input);
		}
		
		return value;
	}
}

