package ij.process;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class ThresholdOperation<T extends RealType<T>> extends SingleCursorRoiOperation<T>
{
	double threshold, min, max;
	
	ThresholdOperation(Image<T> image, int[] origin, int[] span, double threshold)
	{
		super(image,origin,span);
		
		this.threshold = threshold;
	}
	
	@Override
	public void beforeIteration(RealType<?> type)
	{
		this.threshold = TypeManager.boundValueToType(type, this.threshold);
		this.min = type.getMinValue();
		this.max = type.getMaxValue();
	}
	
	@Override
	public void insideIteration(RealType<?> sample)
	{
		if (sample.getRealDouble() <= this.threshold)
			sample.setReal(this.min);
		else
			sample.setReal(this.max);
	}
	
	@Override
	public void afterIteration()
	{
	}

}

