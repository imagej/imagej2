package imagej.process.operation;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

// TODO - this method could use a copy function rather than inline but maybe this way is faster

public class ImageCopierOperation<K extends RealType<K>> extends DualCursorRoiOperation<K>
{
	public ImageCopierOperation(Image<K> img1, int[] origin1, int[] span1,
				Image<K> img2, int[] origin2, int[] span2)
	{
		super(img1, origin1, span1, img2, origin2, span2);
	}

	@Override
	public void beforeIteration(RealType<K> type)
	{
	}

	@Override
	public void insideIteration(RealType<K> sample1, RealType<K> sample2)
	{
		double val = sample1.getRealDouble();
		sample2.setReal(val);
	}

	@Override
	public void afterIteration()
	{
	}
	
}
