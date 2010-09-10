package ij.process;

import mpicbg.imglib.type.numeric.RealType;

public class MaxBinaryFunction implements BinaryFunction {

	public void compute(RealType<?> result, RealType<?> input1, RealType<?> input2)
	{
		if (input1.getRealDouble() > input2.getRealDouble())
			result.setReal( input1.getRealDouble() );
		else
			result.setReal( input2.getRealDouble() );
	}

}
