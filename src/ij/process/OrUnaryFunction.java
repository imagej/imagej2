package ij.process;

import mpicbg.imglib.type.numeric.RealType;

public class OrUnaryFunction implements UnaryFunction
{
	private boolean dataIsIntegral;
	private RealType<?> targetType;
	private double constant;
	
	public OrUnaryFunction(RealType<?> targetType, double constant)
	{
		this.targetType = targetType;
		this.constant = constant;
		this.dataIsIntegral = TypeManager.isIntegralType(targetType);
	}
	
	@Override
	public void compute(RealType<?> result, RealType<?> input)
	{
		double value;
		
		if (this.dataIsIntegral)
		{
			value = ((int)input.getRealDouble()) | ((int)constant);
			
			value = TypeManager.boundValueToType(result, value);
		}
		else
			value = input.getRealDouble();
		
		result.setReal( value );
	}
}

