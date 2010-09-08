package ij.process;

import mpicbg.imglib.type.numeric.RealType;

public class InvertUnaryFunction implements UnaryFunction
{
	private double min, max;
	private RealType<?> targetType;
	private boolean dataIsIntegral;
	
	public InvertUnaryFunction(RealType<?> targetType, double min, double max)
	{
		this.targetType = targetType;
		this.min = min;
		this.max = max;
		this.dataIsIntegral = TypeManager.isIntegralType(targetType);
	}
	
	@Override
	public void compute(RealType<?> result, RealType<?> input)
	{
		double value = this.max - (input.getRealDouble() - this.min);
		
		if (this.dataIsIntegral)
			value = TypeManager.boundValueToType(this.targetType, value);
		
		result.setReal( value );
	}
}
