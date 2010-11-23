package imagej.imglib.process.operation;

import imagej.function.BinaryFunction;
import imagej.imglib.computation.BinaryComputation;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

/** BinaryTransformPositionalOperation transforms the values in a destination dataset using the result of applying a
 *  BinaryFunction computation that uses the values in both the source and destination datasets. The computation takes
 *  two sample values from the datasets and returns a value as defined by the given BinaryFunction. The Add() function
 *  would be an example of a BinaryFunction that returns the addition of its two input values. This operation is
 *  Positional so that you can use positional information in any attached SelectionFunction. See base class for more
 *  information about SelectionFunctions.
 * */
public class BinaryTransformPositionalOperation<T extends RealType<T>> extends PositionalDualCursorRoiOperation<T>
{
	private BinaryComputation computer;
	
	public BinaryTransformPositionalOperation(Image<T> image1, int[] origin1, int[] span1,
							Image<T> image2, int[] origin2, int[] span2,
							BinaryFunction function)
	{
		super(image1, origin1, span1, image2, origin2, span2);
		this.computer = new BinaryComputation(function);
	}

	@Override
	protected void beforeIteration(RealType<T> type)
	{
	}

	@Override
	protected void insideIteration(int[] position1, RealType<T> sample1, int[] position2, RealType<T> sample2)
	{
		this.computer.compute(sample1, sample1, sample2);
	}
	
	@Override
	protected void afterIteration()
	{
	}

}
