package imagej.imglib.examples.function.function;

import mpicbg.imglib.type.numeric.RealType;

/** NullFunction is a function that does not change the output. It accepts any number of parameters.
 * 
 * This class is useful for keeping an AssignOperation from changing its output image values. If one adds an Observer to
 * the AssignOperation one can do anything with the iteration. For example one could gather statistics. Eliminates the
 * need for a QueryOperation class. 
 */
public class NullFunction<T extends RealType<T>> implements RealFunction<T>
{
	@Override
	public boolean canAccept(int numParameters) {
		return true;
	}

	@Override
	public void compute(T[] inputs, T output)
	{
		// DO NOTHING
	}

}
