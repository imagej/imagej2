package imagej.process.operation;

import ij.process.ImageProcessor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class SetFloatValuesOperation<T extends RealType<T>> extends PositionalSingleCursorRoiOperation<T>
{
	private ImageProcessor proc;
	
	public SetFloatValuesOperation(Image<T> image, int[] origin, int[] span, ImageProcessor proc)
	{
		super(image,origin,span);
		this.proc = proc;
	}

	@Override
	public void beforeIteration(RealType<T> type) {
	}

	@Override
	public void insideIteration(int[] position, RealType<T> sample) {
		float floatVal = sample.getRealFloat();
		this.proc.setf(position[0], position[1], floatVal);
	}

	@Override
	public void afterIteration() {
	}
}

