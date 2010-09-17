package imagej.process.operation;

import ij.process.ImageProcessor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class ResetUsingMaskOperation<T extends RealType<T>> extends PositionalDualCursorRoiOperation<T>
{
	private byte[] maskPixels;
	private int[] origin1;
	private int[] span1;
	
	public ResetUsingMaskOperation(Image<T> img1, int[] origin1, int[] span1, Image<T> img2, int[] origin2, int[] span2, ImageProcessor mask)
	{
		super(img1,origin1,span1,img2,origin2,span2);
		
		this.maskPixels = (byte[])mask.getPixels();
		this.origin1 = origin1;
		this.span1 = span1;
	}
	
	@Override
	public void beforeIteration(RealType<T> type) {
	}

	@Override
	public void insideIteration(int[] position1, RealType<T> sample1, int[] position2, RealType<T> sample2)
	{
		int pixNum = calcMaskPosition(position1[0], position1[1], this.origin1, this.span1);
		
		if (this.maskPixels[pixNum] == 0)
		{
			double pix = sample1.getRealDouble();
			sample2.setReal(pix);
		}
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
	
}

