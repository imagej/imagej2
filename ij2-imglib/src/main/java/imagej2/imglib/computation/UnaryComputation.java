package imagej2.imglib.computation;

import imagej2.function.UnaryFunction;
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
