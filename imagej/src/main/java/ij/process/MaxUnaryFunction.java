package ij.process;

import mpicbg.imglib.type.numeric.RealType;

public class MaxUnaryFunction implements UnaryFunction
{
	private double constant;
	
	public MaxUnaryFunction(double constant)
	{
		this.constant = constant;
	}
	
	@Override
	public void compute(RealType<?> result, RealType<?> input)
	{
		double current = input.getRealDouble();
		
		double value;
		if (current > this.constant)
			value = this.constant;
		else
			value = current;
	
		result.setReal( value );
	}
}


