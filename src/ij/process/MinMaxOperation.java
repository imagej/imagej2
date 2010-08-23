package ij.process;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class MinMaxOperation<T extends RealType<T>> extends SingleCursorRoiOperation<T>
{
	double min, max;
	
	MinMaxOperation(Image<T> image, int[] origin, int[] span)
	{
		super(image,origin,span);
	}
	
	public double getMax() { return this.max; }
	public double getMin() { return this.min; }
	
	@Override
	public void beforeIteration(RealType<?> type)
	{
		this.min = type.getMaxValue();
		this.max = type.getMinValue();
	}
	
	@Override
	public void insideIteration(RealType<?> sample)
	{
		double value = sample.getRealDouble();
		
		if ( value > this.max )
			this.max = value;

		if ( value < this.min )
			this.min = value;
	}
	
	@Override
	public void afterIteration()
	{
	}
}

