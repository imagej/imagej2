package imagej.process.function.unary;

import imagej.process.TypeManager;
import mpicbg.imglib.type.numeric.RealType;

public class GammaUnaryFunction implements UnaryFunction
{
	private RealType<?> targetType;
	private double constant;
	private double min;
	private boolean dataIsIntegral;
	private double range;
	
	public GammaUnaryFunction(RealType<?> targetType, double min, double max, double constant)
	{
		this.targetType = targetType;
		this.min = min;
		this.constant = constant;
		this.dataIsIntegral = TypeManager.isIntegralType(targetType);
		this.range = max - min;
	}
	
	public double compute(double input)
	{
		double value;
		
		if (this.dataIsIntegral)
		{
			if (this.range<=0.0 || input <= this.min)
				value = input;
			else					
				value = (long)(Math.exp(this.constant*Math.log((input-this.min)/this.range))*this.range+this.min);
			
			value = TypeManager.boundValueToType(targetType, value);
		}
		else // float
		{
			if (input <= 0)
				value = 0;
			else
				value = Math.exp(this.constant*Math.log(input));
		}
		
		return value;
	}
}

