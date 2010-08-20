package ij.process;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public abstract class DualCursorRoiOperation<T extends RealType<T>>
{
	Image<T> img1, img2;
	int[] origin1, span1, origin2, span2;
	
	protected DualCursorRoiOperation(Image<T> img1, int[] origin1, int[] span1, Image<T> img2, int[] origin2, int[] span2)
	{
		this.img1 = img1;
		this.origin1 = origin1.clone();
		this.span1 = span1.clone();

		this.img2 = img2;
		this.origin2 = origin2.clone();
		this.span2 = span2.clone();
		
		ImageUtils.verifyDimensions(img1.getDimensions(), origin1, span1);
		ImageUtils.verifyDimensions(img2.getDimensions(), origin2, span2);
	}
	
	public Image<T> getImage1()   { return img1; }
	public int[] getOrigin1() { return origin1; }
	public int[] getSpan1()   { return span1; }
	
	public Image<T> getImage2()   { return img2; }
	public int[] getOrigin2() { return origin2; }
	public int[] getSpan2()   { return span2; }

	public abstract void beforeIteration(RealType<?> type1, RealType<?> type2);
	public abstract void insideIteration(RealType<?> sample1, RealType<?> sample2);
	public abstract void afterIteration();
}

