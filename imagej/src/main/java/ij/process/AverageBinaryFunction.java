package ij.process;

import mpicbg.imglib.type.numeric.RealType;

public class AverageBinaryFunction implements BinaryFunction {

	private boolean dataIsIntegral;
	
	public AverageBinaryFunction(boolean isIntegral)
	{
		this.dataIsIntegral = isIntegral;
	}

	public void compute(RealType<?> result, RealType<?> input1, RealType<?> input2)
	{
		if (this.dataIsIntegral)
			result.setReal( ((int)input1.getRealDouble() + (int)input2.getRealDouble()) / 2 );
		else
			result.setReal( (input1.getRealDouble() + input2.getRealDouble()) / 2.0 );
	}

}
