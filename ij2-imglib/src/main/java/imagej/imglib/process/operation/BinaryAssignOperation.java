package imagej.imglib.process.operation;

import imagej.function.UnaryFunction;
import imagej.imglib.computation.UnaryComputation;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

/** BinaryAssignOperation assigns to a destination dataset the result of applying a UnaryFunction computation
 * to a source dataset. The computation takes a single sample value from the source dataset and returns a
 * value as defined by the given UnaryFunction. the Sqr() function would be an example of a UnaryFunction that
 * returns the square of its input value.
 * */
public class BinaryAssignOperation<T extends RealType<T>> extends DualCursorRoiOperation<T>
{
	private UnaryComputation computer;
	
	public BinaryAssignOperation(Image<T> img1, int[] origin1, int[] span1,
									Image<T> img2, int[] origin2, int[] span2,
									UnaryFunction function)
	{
		super(img1, origin1, span1, img2, origin2, span2);
		this.computer = new UnaryComputation(function);
	}

	@Override
	protected void beforeIteration(RealType<T> type)
	{
	}

	@Override
	protected void insideIteration(RealType<T> sample1, RealType<T> sample2)
	{
		this.computer.compute(sample1, sample2);
	}

	@Override
	protected void afterIteration()
	{
	}

}
