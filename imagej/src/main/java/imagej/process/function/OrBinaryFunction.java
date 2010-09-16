package imagej.process.function;

import mpicbg.imglib.type.numeric.RealType;

public class OrBinaryFunction implements BinaryFunction {

	public void compute(RealType<?> result, RealType<?> input1, RealType<?> input2)
	{
		result.setReal( ((int)input1.getRealDouble()) | ((int)(input2.getRealDouble())) );
	}

}
