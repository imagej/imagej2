package imagej.process.operation;

// TODO - add a ProgressTracker???

import imagej.process.function.unary.UnaryComputation;
import imagej.process.function.unary.UnaryFunction;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

/** UnaryTransformOperation transforms the values in a dataset using the result of applying a UnaryFunction
 *  computation that uses the values in the dataset. The computation takes a sample value from the dataset
 *  and returns a value as defined by the given UnaryFunction. The Negate() function would be an example
 *  of a UnaryFunction that returns the result of multiplying -1 * its input value.
 * */
public class UnaryTransformOperation<T extends RealType<T>> extends SingleCursorRoiOperation<T>
{
	private UnaryComputation computer;
	
	public UnaryTransformOperation(Image<T> image, int[] origin, int[] span, UnaryFunction function)
	{
		super(image, origin, span);
		this.computer = new UnaryComputation(function);
	}

	@Override
	protected void beforeIteration(RealType<T> type)
	{
	}

	@Override
	protected void insideIteration(RealType<T> sample)
	{
		this.computer.compute(sample, sample);
	}
	
	@Override
	protected void afterIteration()
	{
	}
}
