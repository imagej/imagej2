package ij.process;

import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;

public class SqrUnaryFunction implements UnaryFunction
{
	private double max;
	private boolean isUnsignedShort;
	
	public SqrUnaryFunction(RealType<?> targetType, double max)
	{
		this.max = max;
		this.isUnsignedShort = targetType instanceof UnsignedShortType;
	}
	
	public void compute(RealType<?> result, RealType<?> input)
	{
		double current = input.getRealDouble();
		
		double value = current * current;
		
		// this needed for compatibility with old SHortProcessor
		if (this.isUnsignedShort)
			if (value > Integer.MAX_VALUE)
				value = 0;
		
		value = TypeManager.boundValueToType(result, value);
		
		result.setReal( value );
	}
}

