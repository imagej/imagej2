package ij.process;

import mpicbg.imglib.type.numeric.RealType;

public class MultiplyBinaryFunction implements BinaryFunction {

	private boolean dataIsIntegral;
	private double max;
	
	MultiplyBinaryFunction(boolean isIntegral, double max)
	{
		this.dataIsIntegral = isIntegral;
		this.max = max;
	}

	@Override
	public void compute(RealType<?> result, RealType<?> input1, RealType<?> input2)
	{
		double value = input1.getRealDouble() * input2.getRealDouble();
		if ((this.dataIsIntegral) && (value > this.max))
			value = this.max;
		result.setReal( value );
	}

}
