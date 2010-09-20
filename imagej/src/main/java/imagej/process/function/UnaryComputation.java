package imagej.process.function;

import mpicbg.imglib.type.numeric.RealType;

public class UnaryComputation
{
	private UnaryFunction function;
	
	public UnaryComputation(UnaryFunction function)
	{
		this.function = function;
	}
	
	public void compute(RealType<?> result, RealType<?> input)
	{
		double value1 = input.getRealDouble();
		
		double resultValue = function.compute(value1);
		
		result.setReal(resultValue);
	}
}
