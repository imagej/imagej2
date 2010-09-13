package ij.process;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class ThresholdOperation<T extends RealType<T>> extends SingleCursorRoiOperation<T>
{
	private double threshold;
	
	public ThresholdOperation(Image<T> image, int[] origin, int[] span, double threshold)
	{
		super(image,origin,span);
		
		this.threshold = threshold;
	}
	
	@Override
	public void beforeIteration(RealType<T> type)
	{
		this.threshold = TypeManager.boundValueToType(type, this.threshold);
	}
	
	@Override
	public void insideIteration(RealType<T> sample)
	{
		if (sample.getRealDouble() <= this.threshold)
			sample.setReal(0);
		else
			sample.setReal(255);
	}
	
	@Override
	public void afterIteration()
	{
	}

}

