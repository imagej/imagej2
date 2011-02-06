package imagej.imglib.examples.function.function;

import mpicbg.imglib.type.numeric.RealType;

public interface RealFunction<T extends RealType<T>>
{
	boolean canAccept(int numParameters);
	double compute(T[] inputs);
}

