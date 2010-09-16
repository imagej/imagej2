package imagej.process.function;

import imagej.process.TypeManager;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.GenericByteType;

public class ExpUnaryFunction implements UnaryFunction
{
	private boolean isGenericByte;
	private double min;
	private double max;
	private boolean dataIsIntegral;
	
	public ExpUnaryFunction(RealType<?> targetType, double min, double max)
	{
		this.isGenericByte = targetType instanceof GenericByteType<?>;
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
			if (this.isGenericByte)
			{
				value = (int)(Math.exp(current*(Math.log(255)/255)));
			}
			else // generic short or int
			{
				value = (int)(Math.exp(current*(Math.log(this.max)/this.max)));
			}
			
			value = Math.floor(value);
			
			value = TypeManager.boundValueToType(result, value);
		}
		else // float
		{
			value = Math.exp(current);
		}
		
		result.setReal( value );
	}
}

