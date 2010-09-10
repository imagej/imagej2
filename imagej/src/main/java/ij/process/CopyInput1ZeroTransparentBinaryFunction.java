package ij.process;

import mpicbg.imglib.type.numeric.RealType;

public class CopyInput1ZeroTransparentBinaryFunction implements BinaryFunction {

	public void compute(RealType<?> result, RealType<?> input1, RealType<?> input2)
	{
		if ( input1.getRealDouble() != 0 )
			result.setReal( input1.getRealDouble() );
	}

}
