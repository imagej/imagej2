package ij.process;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class ImageCopier<K extends RealType<K>> extends DualCursorRoiOperation<K>
{
	protected ImageCopier(Image<K> img1, int[] origin1, int[] span1,
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
		double real = sample1.getPowerDouble();
		double image = sample1.getPhaseDouble();
		
		//System.out.println("Source values are " + real + " and " + complex );
	
		//set the destination value
		sample2.setComplexNumber(real, image);
	}

	@Override
	public void afterIteration()
	{
	}
	
}

