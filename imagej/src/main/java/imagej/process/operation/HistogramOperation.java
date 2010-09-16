package imagej.process.operation;

import ij.process.ImageProcessor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class HistogramOperation<T extends RealType<T>> extends SingleCursorRoiOperation<T>
{
	private ImageProcessor mask;
	private int[] histogram;
	private int pixIndex;
	
	public HistogramOperation(Image<T> image, int[] origin, int[] span, ImageProcessor mask, int lutSize)
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
	public void beforeIteration(RealType<T> type) {
		this.pixIndex = 0;
	}

	@Override
	public void insideIteration(RealType<T> sample) {
		if ((this.mask == null) || (this.mask.get(pixIndex) > 0))
			this.histogram[(int)sample.getRealDouble()]++;
		pixIndex++;
	}
	
	@Override
	public void afterIteration() {
	}

}

