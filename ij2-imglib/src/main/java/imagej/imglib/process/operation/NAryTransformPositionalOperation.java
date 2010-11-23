package imagej.imglib.process.operation;

import imagej.function.NAryFunction;
import imagej.imglib.computation.NAryComputation;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

/** NAryTransformPositionalOperation transforms the values in a destination dataset using the result of applying a
 *  NAryFunction computation that uses the values in all the input datasets. The computation takes
 *  N sample values from the datasets and returns a value as defined by the given NAryFunction. The Avg() function
 *  would be an example of a NAryFunction that returns the average of its N input values. This operation is
 *  positional so that you can use positional information in any attached SelectionFunction. See base class for more
 *  information about SelectionFunctions.
 * */

public class NAryTransformPositionalOperation<T extends RealType<T>> extends PositionalManyCursorRoiOperation<T>
{
	private NAryComputation computer;
	
	public NAryTransformPositionalOperation(Image<T>[] images, int[][] origins, int[][] spans,
							NAryFunction function)
	{
		super(images, origins, spans);
		this.computer = new NAryComputation(images.length, function);
	}

	@Override
	protected void beforeIteration(RealType<T> type)
	{
	}

	@Override
	protected void insideIteration(int[][] positions, RealType<T>[] samples)
	{
		this.computer.compute(samples[0], samples);
	}
	
	@Override
	protected void afterIteration()
	{
	}

}
