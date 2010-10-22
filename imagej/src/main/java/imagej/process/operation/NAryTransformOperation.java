package imagej.process.operation;

import imagej.process.function.nary.NAryComputation;
import imagej.process.function.nary.NAryFunction;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

/** NAryTransformOperation tranforms the values in a destination dataset using the result of applying a NAryFunction
 *  computation that uses the values in all input datasets. The computation takes multiple sample values from the
 *  datasets and returns a value as defined by the given NAryFunction. The Avg() function would be an example of an
 *  NAryFunction that returns the average of its input values.
 * */
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
