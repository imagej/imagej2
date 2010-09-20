package imagej.process.operation;

import imagej.process.ImgLibProcessor;
import imagej.process.Index;
import imagej.process.ProgressTracker;
import imagej.process.Span;
import imagej.process.function.BinaryComputation;
import imagej.process.function.BinaryFunction;
import mpicbg.imglib.type.numeric.RealType;

// TODO : replace with BinaryTransform

public class BlitterOperation<T extends RealType<T>> extends DualCursorRoiOperation<T>
{
	private BinaryComputation computer;
	
	public BlitterOperation(ImgLibProcessor<T> ip, ImgLibProcessor<T> other, int xloc, int yloc, BinaryFunction function)
	{
		super(ip.getImage(),
				Index.create(xloc, yloc, ip.getPlanePosition()),
				Span.singlePlane(other.getWidth(), other.getHeight(), ip.getImage().getNumDimensions()),
				other.getImage(),
				Index.create(2),
				Span.singlePlane(other.getWidth(), other.getHeight(), 2)
				);
		//super(other.getImage(),
		//		Index.create(2),
		//		Span.singlePlane(other.getWidth(), other.getHeight(), 2),
		//		ip.getImage(),
		//		Index.create(xloc, yloc, ip.getPlanePosition()),
		//		Span.singlePlane(other.getWidth(), other.getHeight(), ip.getImage().getNumDimensions()));
		
		this.computer = new BinaryComputation(function);
		
		setObserver(new ProgressTracker(ip, other.getTotalSamples(), 20*ip.getWidth()));
	}
	
	@Override
	public void beforeIteration(RealType<T> type)
	{
	}

	@Override
	public void insideIteration(RealType<T> sample1, RealType<T> sample2)
	{
		this.computer.compute(sample1, sample1, sample2);
	}

	@Override
	public void afterIteration()
	{
	}
}
