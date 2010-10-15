package imagej.process.operation;

// TODO - add a ProgressTracker???

import imagej.process.function.binary.BinaryComputation;
import imagej.process.function.binary.BinaryFunction;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class BinaryTransformOperation<T extends RealType<T>> extends DualCursorRoiOperation<T>
{
	private BinaryComputation computer;
	
	public BinaryTransformOperation(Image<T> image1, int[] origin1, int[] span1,
							Image<T> image2, int[] origin2, int[] span2,
							BinaryFunction function)
	{
		super(image1, origin1, span1, image2, origin2, span2);
		this.computer = new BinaryComputation(function);
	}

	@Override
	public void beforeIteration(RealType<T> type)
	{
	}

	@Override
	public void insideIteration(RealType<T> sample1, RealType<T> sample2)
	{
		this.computer.compute(sample1, sample1, sample2);
	}
	
	@Override
	public void afterIteration()
	{
	}
}
