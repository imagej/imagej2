package ij.process;

import mpicbg.imglib.type.numeric.RealType;

public class BlitterOperation<T extends RealType<T>> extends DualCursorRoiOperation<T>
{
	private BinaryFunction function;
	private ProgressTracker tracker;
	
	public BlitterOperation(ImgLibProcessor<T> ip, ImgLibProcessor<T> other, int xloc, int yloc, BinaryFunction function)
	{
		super(other.getImage(),
				Index.create(2),
				Span.singlePlane(other.getWidth(), other.getHeight(), 2),
				ip.getImage(),
				Index.create(xloc, yloc, ip.getPlanePosition()),
				Span.singlePlane(other.getWidth(), other.getHeight(), ip.getImage().getNumDimensions()));
		
		this.function = function;
		
		this.tracker = new ProgressTracker(ip, other.getTotalSamples(), 20*ip.getWidth());
	}
	
	@Override
	public void beforeIteration(RealType<T> type)
	{
		this.tracker.init();
	}

	@Override
	public void insideIteration(RealType<T> sample1, RealType<T> sample2)
	{
		this.function.compute(sample2, sample1, sample2);
		
		this.tracker.didOneMore();
	}

	@Override
	public void afterIteration()
	{
		this.tracker.done();
	}
}
