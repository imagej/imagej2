package ij.process;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class SetFloatValuesOperation<T extends RealType<T>> extends PositionalRoiOperation<T>
{
	ImageProcessor proc;
	
	SetFloatValuesOperation(Image<T> image, int[] origin, int[] span, ImageProcessor proc)
	{
		super(image,origin,span);
		this.proc = proc;
	}

	@Override
	public void beforeIteration(RealType<?> type) {
	}

	@Override
	public void insideIteration(int[] position, RealType<?> sample) {
		float floatVal = sample.getRealFloat();
		this.proc.setf(position[0], position[1], floatVal);
	}

	@Override
	public void afterIteration() {
	}
}

