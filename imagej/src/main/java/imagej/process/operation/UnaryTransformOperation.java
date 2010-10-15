package imagej.process.operation;

// TODO - add a ProgressTracker???

import imagej.process.function.unary.UnaryComputation;
import imagej.process.function.unary.UnaryFunction;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class UnaryTransformOperation<T extends RealType<T>> extends SingleCursorRoiOperation<T>
{
	private UnaryComputation computer;
	
	public UnaryTransformOperation(Image<T> image, int[] origin, int[] span, UnaryFunction function)
	{
		super(image, origin, span);
		this.computer = new UnaryComputation(function);
	}

	@Override
	public void beforeIteration(RealType<T> type)
	{
	}

	@Override
	public void insideIteration(RealType<T> sample)
	{
		this.computer.compute(sample, sample);
	}
	
	@Override
	public void afterIteration()
	{
	}
}
