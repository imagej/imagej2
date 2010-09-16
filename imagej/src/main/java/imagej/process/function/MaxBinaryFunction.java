package imagej.process.function;

import mpicbg.imglib.type.numeric.RealType;

public class MaxBinaryFunction implements BinaryFunction {

	public void compute(RealType<?> result, RealType<?> input1, RealType<?> input2)
	{
		double val1 = input1.getRealDouble();
		double val2 = input2.getRealDouble();
		
		if (val1 > val2)
			result.setReal( val1 );
		else
			result.setReal( val2 );
	}

}
