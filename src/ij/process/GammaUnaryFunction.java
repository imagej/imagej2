package ij.process;

import mpicbg.imglib.type.numeric.RealType;

public class GammaUnaryFunction implements UnaryFunction
{
	private RealType<?> targetType;
	private double constant;
	private double min;
	private double max;
	private boolean dataIsIntegral;
	private double range;
	
	public GammaUnaryFunction(RealType<?> targetType, double min, double max, double constant)
	{
		this.targetType = targetType;
		this.min = min;
		this.max = max;
		this.constant = constant;
		this.dataIsIntegral = TypeManager.isIntegralType(targetType);
		this.range = max - min;
	}
	
	@Override
	public void compute(RealType<?> result, RealType<?> input)
	{
		double current = input.getRealDouble();
		
		double value;
		
		if (this.dataIsIntegral)
		{
			if (this.range<=0.0 || current <= this.min)
				value = current;
			else					
				value = (int)(Math.exp(this.constant*Math.log((current-this.min)/this.range))*this.range+this.min);
			
			value = TypeManager.boundValueToType(result, value);
		}
		else // float
		{
			if (current <= 0)
				value = 0;
			else
				value = Math.exp(this.constant*Math.log(current));
		}
		
		result.setReal( value );
	}
}

