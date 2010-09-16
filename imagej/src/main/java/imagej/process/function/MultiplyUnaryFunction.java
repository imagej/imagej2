package imagej.process.function;

import imagej.process.TypeManager;
import mpicbg.imglib.type.numeric.RealType;

public class MultiplyUnaryFunction implements UnaryFunction
{
	private boolean dataIsIntegral;
	private double constant;
	
	public MultiplyUnaryFunction(RealType<?> targetType, double constant)
	{
		this.constant = constant;
		this.dataIsIntegral = TypeManager.isIntegralType(targetType);
	}
	
	public void compute(RealType<?> result, RealType<?> input)
	{
		double value = input.getRealDouble() * this.constant;
		
		if (this.dataIsIntegral)
		{
			value = Math.floor(value);
			value = TypeManager.boundValueToType(result, value);
		}
		
		result.setReal( value );
	}
}

