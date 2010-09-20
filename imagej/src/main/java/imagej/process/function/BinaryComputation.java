package imagej.process.function;

import mpicbg.imglib.type.numeric.RealType;

public class BinaryComputation
{
	private BinaryFunction function;
	
	public BinaryComputation(BinaryFunction function)
	{
		this.function = function;
	}
	
	public void compute(RealType<?> result, RealType<?> input1, RealType<?> input2)
	{
		double value1 = input1.getRealDouble();
		
		double value2 = input2.getRealDouble();
		
		double resultValue = function.compute(value1,value2);
		
		result.setReal(resultValue);
	}
}
