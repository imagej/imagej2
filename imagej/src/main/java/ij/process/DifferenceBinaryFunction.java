package ij.process;

import mpicbg.imglib.type.numeric.RealType;

public class DifferenceBinaryFunction implements BinaryFunction {

	@Override
	public void compute(RealType<?> result, RealType<?> input1, RealType<?> input2)
	{
		result.setReal( Math.abs(input2.getRealDouble() - input1.getRealDouble()) );
	}

}
