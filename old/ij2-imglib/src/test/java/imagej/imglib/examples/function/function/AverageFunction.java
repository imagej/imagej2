package imagej.imglib.examples.function.function;

import mpicbg.imglib.type.numeric.RealType;

public class AverageFunction<T extends RealType<T>> implements RealFunction<T>
{
	@Override
	public boolean canAccept(int numParameters) { return numParameters >= 0; }
	
	@Override
	public void compute(T[] inputs, T output)
	{
		int numElements = inputs.length;
		
		if (numElements == 0)
			return;
		
		double sum = 0;

		for (T element : inputs)
			sum += element.getRealDouble();
		
		output.setReal(sum / numElements);
	}
	
}

