package imagej.process.operation;

import imagej.process.function.unary.UnaryComputation;
import imagej.process.function.unary.UnaryFunction;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class UnaryTransformPositionalOperation<T extends RealType<T>> extends PositionalSingleCursorRoiOperation<T>
{
	private UnaryComputation computer;
	
	public UnaryTransformPositionalOperation(Image<T> image, int[] origin, int[] span, UnaryFunction func) 
	{
		super(image, origin, span);
		this.computer = new UnaryComputation(func);
	}
	
	@Override
	public void beforeIteration(RealType<T> type)
	{
	}

	@Override
	public void insideIteration(int[] position, RealType<T> sample)
	{
		this.computer.compute(sample, sample);
	}

	@Override
	public void afterIteration()
	{
	}

}
