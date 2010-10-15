package imagej.process.operation;

import imagej.process.function.unary.UnaryComputation;
import imagej.process.function.unary.UnaryFunction;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

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
	public void beforeIteration(RealType<T> type)
	{
	}

	@Override
	public void insideIteration(RealType<T> sample1, RealType<T> sample2)
	{
		this.computer.compute(sample1, sample2);
	}

	@Override
	public void afterIteration()
	{
	}

}
