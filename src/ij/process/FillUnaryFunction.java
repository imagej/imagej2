package ij.process;

import mpicbg.imglib.type.numeric.RealType;

public class FillUnaryFunction implements UnaryFunction
{
	private double fillColor;

	public FillUnaryFunction(double fillColor)
	{
		this.fillColor = fillColor;
	}
	
	@Override
	public void compute(RealType<?> result, RealType<?> input)
	{
		result.setReal( this.fillColor );
	}
}
