package ij.process;

import mpicbg.imglib.type.numeric.RealType;

public class SqrtUnaryFunction implements UnaryFunction
{
	private boolean dataIsIntegral;
	
	public SqrtUnaryFunction(RealType<?> targetType)
	{
		this.dataIsIntegral = TypeManager.isIntegralType(targetType);
	}
	
	@Override
	public void compute(RealType<?> result, RealType<?> input)
	{
		double current = input.getRealDouble();
		
		double value;
		if (current < 0)
			value = 0;
		else
			value = Math.sqrt(current);
	
		if (this.dataIsIntegral)
			value = (int) value;
		
		result.setReal( value );
	}
}

