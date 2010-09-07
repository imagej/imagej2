package ij.process;

import mpicbg.imglib.type.numeric.RealType;

public class CopyInput1BinaryFunction implements BinaryFunction {

	@Override
	public void compute(RealType<?> result, RealType<?> input1, RealType<?> input2)
	{
		double value = input1.getRealDouble();
		result.setReal(value);
	}

}
