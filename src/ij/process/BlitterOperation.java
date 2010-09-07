package ij.process;

import mpicbg.imglib.type.numeric.RealType;

public class BlitterOperation<T extends RealType<T>> extends DualCursorRoiOperation<T>
{
	private ImgLibProcessor<?> ip;
	
	private long numPixels;
	private long pixelsSoFar;
	private long numPixelsInTwentyRows;
	
	private BinaryFunction function;
	
	BlitterOperation(ImgLibProcessor<T> ip, ImgLibProcessor<T> other, int xloc, int yloc, BinaryFunction function)
	{
		super(other.getImage(),
				Index.create(2),
				Span.singlePlane(other.getWidth(), other.getHeight(), 2),
				ip.getImage(),
				Index.create(xloc, yloc, ip.getPlanePosition()),
				Span.singlePlane(other.getWidth(), other.getHeight(), ip.getImage().getNumDimensions()));
		this.function = function;
		this.ip = ip;
		this.numPixels = other.getTotalSamples();
		this.numPixelsInTwentyRows = (this.numPixels * 20) / other.getHeight();
	}
	
	@Override
	public void beforeIteration(RealType<?> type1, RealType<?> type2)
	{
		this.pixelsSoFar= 0;
	}

	@Override
	public void insideIteration(RealType<?> sample1, RealType<?> sample2)
	{
		this.function.compute(sample2, sample1, sample2);
		
		this.pixelsSoFar++;

		if (( this.pixelsSoFar % this.numPixelsInTwentyRows) == 0) 
			this.ip.showProgress( (double)this.pixelsSoFar / this.numPixels );
	}

	@Override
	public void afterIteration()
	{
		this.ip.showProgress(1.0);
	}
}
