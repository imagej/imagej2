package imagej.process.function;

import imagej.process.TypeManager;
import mpicbg.imglib.type.numeric.RealType;

public class OrUnaryFunction implements UnaryFunction
{
	private boolean dataIsIntegral;
	private double constant;
	
	public OrUnaryFunction(RealType<?> targetType, double constant)
	{
		this.constant = constant;
		this.dataIsIntegral = TypeManager.isIntegralType(targetType);
	}
	
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

