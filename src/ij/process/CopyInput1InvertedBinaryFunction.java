package ij.process;

import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;

public class CopyInput1InvertedBinaryFunction implements BinaryFunction {

	private double max;
	
	public CopyInput1InvertedBinaryFunction(double max)
	{
		this.max = max;
	}
	
	@Override
	public void compute(RealType<?> result, RealType<?> input1, RealType<?> input2)
	{
		// dst=255-src (8-bits and RGB)
		if (input1 instanceof UnsignedByteType)
			result.setReal( this.max - input1.getRealDouble() );
		else
			result.setReal( input1.getRealDouble() );

	}

}
