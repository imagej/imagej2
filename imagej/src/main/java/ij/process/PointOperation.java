package ij.process;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class PointOperation<T extends RealType<T>> extends SingleCursorRoiOperation<T>
{
	private UnaryFunction function;
	
	public PointOperation(Image<T> image, int[] origin, int[] span, UnaryFunction function)
	{
		super(image, origin, span);
		this.function = function;
	}

	@Override
	public void beforeIteration(RealType<?> type)
	{
	}

	@Override
	public void insideIteration(RealType<?> sample)
	{
		this.function.compute(sample, sample);
	}
	
	@Override
	public void afterIteration()
	{
	}
}

