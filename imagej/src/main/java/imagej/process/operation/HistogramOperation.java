package imagej.process.operation;

import ij.process.ImageProcessor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class HistogramOperation<T extends RealType<T>> extends PositionalSingleCursorRoiOperation<T>
{
	private ImageProcessor mask;
	private int[] histogram;
	private int[] origin;
	private int[] span;
	
	public HistogramOperation(Image<T> image, int[] origin, int[] span, ImageProcessor mask, int lutSize)
	{
		super(image,origin,span);
	
		this.origin = origin;
		this.span = span;
		this.mask = mask;
		
		this.histogram = new int[lutSize];
	}
	
	@Override
	public void beforeIteration(RealType<T> type)
	{
	}

	@Override
	public void insideIteration(int[] position, RealType<T> sample)
	{
		int pixIndex = calcMaskPosition(position[0], position[1], this.origin, this.span);
		
		if ((this.mask == null) || (this.mask.get(pixIndex) > 0))
			this.histogram[(int)sample.getRealDouble()]++;
	}
	
	@Override
	public void afterIteration() {
	}

	private int calcMaskPosition(int x, int y, int[] origin, int[] span)
	{
		int val = 0;
		val += (y - origin[1]) * span[0];
		val += (x - origin[0]);
		return val;
	}

	public int[] getHistogram()
	{
		return this.histogram;
	}
}

