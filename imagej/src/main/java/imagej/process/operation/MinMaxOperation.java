package imagej.process.operation;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.real.FloatType;

// NOTE - could move this Operation to a Query. I did so originally but since queries use a Positional iterator
//   total runtime for ImgLibProcessorTest increased by 12%. For speed optimization leave as an Operation.

public class MinMaxOperation<T extends RealType<T>> extends SingleCursorRoiOperation<T>
{
	private double min, max, negInfinity, posInfinity;
	
	public MinMaxOperation(Image<T> image, int[] origin, int[] span)
	{
		super(image,origin,span);
	}
	
	public double getMax() { return this.max; }
	public double getMin() { return this.min; }
	
	@Override
	public void beforeIteration(RealType<T> type)
	{
		this.min = type.getMaxValue();
		this.max = type.getMinValue();
		// CTR: HACK: Workaround for compiler issue with instanceof and generics.
		//if (type instanceof FloatType)
		if (FloatType.class.isAssignableFrom(type.getClass()))
		{
			this.posInfinity = Float.POSITIVE_INFINITY;
			this.negInfinity = Float.NEGATIVE_INFINITY;
		}
		else
		{
			this.posInfinity = Double.POSITIVE_INFINITY;
			this.negInfinity = Double.NEGATIVE_INFINITY;
		}
	}
	
	@Override
	public void insideIteration(RealType<T> sample)
	{
		double value = sample.getRealDouble();
		
		if (value >= this.posInfinity) return;
		if (value <= this.negInfinity) return;
		
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

