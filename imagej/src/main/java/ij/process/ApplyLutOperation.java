package ij.process;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class ApplyLutOperation<T extends RealType<T>> extends SingleCursorRoiOperation<T>
{
	private int[] lut;
	
	public ApplyLutOperation(Image<T> image, int[] origin, int[] span, int[] lut)
	{
		super(image,origin,span);
	
		this.lut = lut;
	}
	
	@Override
	public void beforeIteration(RealType<T> type) {
	}

	@Override
	public void insideIteration(RealType<T> sample) {
		int value = this.lut[(int)sample.getRealDouble()];
		sample.setReal(value);
	}
	
	@Override
	public void afterIteration() {
	}

}

