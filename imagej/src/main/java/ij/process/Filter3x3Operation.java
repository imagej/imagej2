package ij.process;

import mpicbg.imglib.type.numeric.RealType;

// TODO - this was refactored as common code. Its a bit messy. Should try to improve.

public abstract class Filter3x3Operation<K extends RealType<K>> extends PositionalRoiOperation<K>
{
	private final double[] neighborhood;
	private final double[] pixelsCopy;
	private final int width;
	private final int height;
	private final ProgressTracker tracker;
	
	protected Filter3x3Operation(ImgLibProcessor<K> ip, int[] origin, int[] span)
	{
		super(ip.getImage(), origin, span);
		
		this.neighborhood = new double[9];

		this.pixelsCopy = ip.getPlaneData();
		this.width = ip.getWidth();
		this.height = ip.getHeight();
		
		long updateFrequency = (long)span[0] * 25;
		
		this.tracker = new ProgressTracker(ip, ImageUtils.getTotalSamples(span), updateFrequency);
	}

	public final double[] getNeighborhood() { return neighborhood; }
	
	@Override
	public void beforeIteration(RealType<K> type)
	{
		this.tracker.init();
	}

	@Override
	public void insideIteration(final int[] position, RealType<K> sample)
	{
		calcNeighborhood(position);

		double value = calcSampleValue(this.neighborhood);
		
		sample.setReal(value);
		
		this.tracker.didOneMore();
	}
	
	protected abstract double calcSampleValue(final double[] neighborhood);
	
	@Override
	public void afterIteration()
	{
		this.tracker.done();
	}

	private final double valueOfPixelFromCopy(final int x, final int y)
	{
		int index = y*this.width + x;
		return this.pixelsCopy[index];
	}
	
	private void calcNeighborhood(final int[] position)
	{
		// TODO : optimize later - do straightforward for now
		
		int x = position[0];
		int y = position[1];
		
		// neighborhood0
		if ((x > 0) && (y > 0))
			neighborhood[0] = valueOfPixelFromCopy(x-1,y-1);
		else if ((x == 0) && (y > 0))
			neighborhood[0] = valueOfPixelFromCopy(0,y-1);
		else if ((y == 0) && (x > 0))
			neighborhood[0] = valueOfPixelFromCopy(x-1,0);
		else
			neighborhood[0] = valueOfPixelFromCopy(0,0);
		
		// v1
		if (y > 0)
			neighborhood[1] = valueOfPixelFromCopy(x,y-1);
		else
			neighborhood[1] = valueOfPixelFromCopy(x,0);;
		
		// v2
		if ((x < width-1) && (y > 0))
			neighborhood[2] = valueOfPixelFromCopy(x+1,y-1);
		else if ((x == width-1) && (y > 0))
			neighborhood[2] = valueOfPixelFromCopy(width-1,y-1);
		else if ((y == 0) && (x < width-1))
			neighborhood[2] = valueOfPixelFromCopy(x+1,0);
		else
			neighborhood[2] = valueOfPixelFromCopy(width-1,0);
		
		// v3
		if (x > 0)
			neighborhood[3] = valueOfPixelFromCopy(x-1,y);
		else
			neighborhood[3] = valueOfPixelFromCopy(0,y);
		
		// v4
		neighborhood[4] = valueOfPixelFromCopy(x,y);
		
		// v5
		if (x < width-1)
			neighborhood[5] = valueOfPixelFromCopy(x+1,y);
		else
			neighborhood[5] = valueOfPixelFromCopy(width-1,y);
		
		// v6
		if ((x > 0) && (y < height-1))
			neighborhood[6] = valueOfPixelFromCopy(x-1,y+1);
		else if ((x == 0) && (y < height-1))
			neighborhood[6] = valueOfPixelFromCopy(0,y+1);
		else if ((y == height-1) && (x > 0))
			neighborhood[6] = valueOfPixelFromCopy(x-1,height-1);
		else
			neighborhood[6] = valueOfPixelFromCopy(0,height-1);
		
		// v7
		if (y < height-1)
			neighborhood[7] = valueOfPixelFromCopy(x,y+1);
		else
			neighborhood[7] = valueOfPixelFromCopy(x,height-1);
		
		// v8
		if ((x < width-1) && (y < height-1))
			neighborhood[8] = valueOfPixelFromCopy(x+1,y+1);
		else if ((x == width-1) && (y < height-1))
			neighborhood[8] = valueOfPixelFromCopy(width-1,y+1);
		else if ((y == height-1) && (x < width-1))
			neighborhood[8] = valueOfPixelFromCopy(x+1,height-1);
		else
			neighborhood[8] = valueOfPixelFromCopy(width-1,height-1);
		
	}
}

