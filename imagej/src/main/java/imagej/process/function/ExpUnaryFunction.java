package imagej.process.function;

import imagej.process.TypeManager;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.GenericByteType;

public class ExpUnaryFunction implements UnaryFunction
{
	private RealType<?> targetType;
	private double max;
	private boolean isGenericByte;
	private boolean dataIsIntegral;
	
	public ExpUnaryFunction(RealType<?> targetType, double max)
	{
		this.targetType = targetType;
		this.max = max;
		this.isGenericByte = targetType instanceof GenericByteType<?>;
		this.dataIsIntegral = TypeManager.isIntegralType(targetType);
	}
	
	public double compute(double input)
	{
		double current = input;
		
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
			
			value = TypeManager.boundValueToType(targetType, value);
		}
		else // float
		{
			value = Math.exp(current);
		}
		
		return value;
	}
}

