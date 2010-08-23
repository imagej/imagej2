package ij.process;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class HistogramOperation<T extends RealType<T>> extends SingleCursorRoiOperation<T>
{
	ImageProcessor mask;
	int[] histogram;
	int pixIndex;
	
	HistogramOperation(Image<T> image, int[] origin, int[] span, ImageProcessor mask, int lutSize)
	{
		super(image,origin,span);
	
		this.mask = mask;
		
		this.histogram = new int[lutSize];
	}
	
	public int[] getHistogram()
	{
		return this.histogram;
	}
	
	@Override
	public void beforeIteration(RealType<?> type) {
		this.pixIndex = 0;
	}

	@Override
	public void insideIteration(RealType<?> sample) {
		if ((this.mask == null) || (this.mask.get(pixIndex) > 0))
			this.histogram[(int)sample.getRealDouble()]++;
		pixIndex++;
	}
	
	@Override
	public void afterIteration() {
	}

}

