package imagej.process.operation;

import imagej.process.function.unary.UnaryComputation;
import imagej.process.function.unary.UnaryFunction;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

/** BinaryAssignPositionalOperation assigns to a destination dataset the result of applying a UnaryFunction computation
 * to a source dataset. The computation takes a single sample value from the source dataset and returns a
 * value as defined by the given UnaryFunction. The Sqr() function would be an example of a UnaryFunction that
 * returns the square of its input value. This class differs from BinaryAssignOperation in that its ieration is
 * positional. Thus it is somewhat slower but allows more powerful use of positional SelectionFunctions.
 * */
public class BinaryAssignPositionalOperation<T extends RealType<T>> extends PositionalDualCursorRoiOperation<T>
{
	private UnaryComputation computer;
	
	public BinaryAssignPositionalOperation(Image<T> img1, int[] origin1, int[] span1,
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
	protected void insideIteration(int[] position1, RealType<T> sample1, int[] position2, RealType<T> sample2)
	{
		this.computer.compute(sample1, sample2);
	}

	@Override
	protected void afterIteration()
	{
	}

}
