package ij.process;

import mpicbg.imglib.type.numeric.RealType;

public class LogUnaryFunction implements UnaryFunction
{
	private double min;
	private double max;
	private boolean dataIsIntegral;
	
	public LogUnaryFunction(RealType<?> targetType, double min, double max)
	{
		this.min = min;
		this.max = max;
		this.dataIsIntegral = TypeManager.isIntegralType(targetType);
	}
	
	public void compute(RealType<?> result, RealType<?> input)
	{
		double current = input.getRealDouble();
		
		double value;
		
		if (this.dataIsIntegral)
		{
			if (current <= 0)
				value = 0;
			else 
				value = (int)(Math.log(current)*(this.max/Math.log(this.max)));
			
			value = TypeManager.boundValueToType(result, value);
		}
		else // float
		{
			if (current <= 0)
				value = 0;
			else
				value = Math.log(current);
		}
		
		result.setReal( value );
	}
}

