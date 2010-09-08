package ij.process;

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
	
	@Override
	public void compute(RealType<?> result, RealType<?> input)
	{
		double value = input.getRealDouble() + this.constant;
		
		if (this.dataIsIntegral)
		{
			value = Math.floor(value);
			value = TypeManager.boundValueToType(this.targetType, value);
		}
		
		result.setReal( value );
	}
}


