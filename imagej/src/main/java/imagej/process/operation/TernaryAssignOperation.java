package imagej.process.operation;

import imagej.process.function.BinaryComputation;
import imagej.process.function.BinaryFunction;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class TernaryAssignOperation<T extends RealType<T>> extends ManyCursorRoiOperation<T>
{
	private BinaryComputation computer;
	
	@SuppressWarnings("unchecked")
	public TernaryAssignOperation(Image<T> image1, int[] origin1, int[] span1,
			Image<T> image2, int[] origin2, int[] span2,
			Image<T> image3, int[] origin3, int[] span3,
			BinaryFunction function)
	{
		// NOTE - compiler warning unavoidable - can't pass Image<T>[]
		super(new Image[]{image1,image2,image3}, new int[][]{origin1,origin2,origin3}, new int[][]{span1,span2,span3});
		computer = new BinaryComputation(function);
	}

	@Override
	public void beforeIteration(RealType<T> type) {
	}

	@Override
	public void insideIteration(RealType<T>[] samples) {
		this.computer.compute(samples[0], samples[1], samples[2]);
	}

	@Override
	public void afterIteration() {
	}

}
