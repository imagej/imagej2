package imagej.imglib.examples.function.function;

import mpicbg.imglib.type.numeric.RealType;

public class AverageFunction<T extends RealType<T>> implements RealFunction<T>
{
	@Override
	public boolean canAccept(int numParameters) { return true; }
	
	@Override
	public double compute(T[] inputs)
	{
		int numElements = inputs.length;
		
		if (numElements == 0)
			return 0;
		
		double sum = 0;

		for (T element : inputs)
			sum += element.getRealDouble();
		
		return sum / numElements;
	}
	
}

