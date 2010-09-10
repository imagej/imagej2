package ij.process;

import mpicbg.imglib.type.numeric.RealType;

public class MinUnaryFunction implements UnaryFunction
{
	private double constant;
	
	public MinUnaryFunction(double constant)
	{
		this.constant = constant;
	}
	
	public void compute(RealType<?> result, RealType<?> input)
	{
		double current = input.getRealDouble();
		
		double value;
		if (current < this.constant)
			value = this.constant;
		else
			value = current;
	
		result.setReal( value );
	}
}

