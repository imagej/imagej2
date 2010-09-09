package ij.process;

import mpicbg.imglib.type.numeric.RealType;

public class SubtractBinaryFunction implements BinaryFunction {

	private boolean isIntegral;
	private double min;
	
	public SubtractBinaryFunction(boolean isIntegral, double min)
	{
		this.isIntegral = isIntegral;
		this.min = min;
	}

	@Override
	public void compute(RealType<?> result, RealType<?> input1, RealType<?> input2)
	{
		double value = input2.getRealDouble() - input1.getRealDouble();
		if ((this.isIntegral) && (value < this.min))
			value = this.min;
		result.setReal( value );
	}

}
