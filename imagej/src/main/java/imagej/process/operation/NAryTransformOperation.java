package imagej.process.operation;

import imagej.process.function.NAryComputation;
import imagej.process.function.NAryFunction;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class NAryTransformOperation<T extends RealType<T>> extends ManyCursorRoiOperation<T>
{
	private NAryComputation computer;
	
	public NAryTransformOperation(Image<T>[] images, int[][] origins, int[][] spans, NAryFunction function) {
		super(images, origins, spans);
		computer = new NAryComputation(images.length,function);
	}

	@Override
	public void beforeIteration(RealType<T> type) {
	}

	@Override
	public void insideIteration(RealType<T>[] samples) {
		this.computer.compute(samples[0], samples);
	}

	@Override
	public void afterIteration() {
	}

}
