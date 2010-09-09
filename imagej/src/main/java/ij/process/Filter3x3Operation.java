package ij.process;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

// TODO - this was refactored as common code. Its a bit messy. Should try to improve.

public abstract class Filter3x3Operation<K extends RealType<K>> extends PositionalRoiOperation<K>
{
	protected double[] v;
	protected boolean dataIsIntegral;
	private double[] pixelsCopy;
	private int width;
	private int height;
	private ProgressTracker tracker;
	
	protected Filter3x3Operation(Image<K> image, int[] origin, int[] span, ImgLibProcessor<K> ip, double[] values)
	{
		super(image, origin, span);
		
		this.v = values;

		this.pixelsCopy = ip.getPlaneData();
		this.width = ip.getWidth();
		this.height = ip.getHeight();
		
		long updateFrequency = ip.getWidth() * 25;
		
		this.tracker = new ProgressTracker(ip, ImageUtils.getTotalSamples(span), updateFrequency);
		
		if (values.length != 9)
			throw new IllegalArgumentException("Filter3x3Operation() - requires an input array of 9 values");
	}

	public double[] getValueArray() { return v; }
	
	@Override
	public void beforeIteration(RealType<?> type)
	{
		this.dataIsIntegral = TypeManager.isIntegralType(type);
		this.tracker.init();
	}

	@Override
	public void insideIteration(int[] position, RealType<?> sample)
	{
		initPixelVals(position, sample);

		double value = calcSampleValue(sample);
		
		sample.setReal(value);
		
		this.tracker.didOneMore();
	}
	
	protected abstract double calcSampleValue(RealType<?> targetType);
	
	@Override
	public void afterIteration()
	{
		this.tracker.done();
	}

	private double valueOfPixelFromCopy(int x, int y)
	{
		int index = y*this.width + x;
		return this.pixelsCopy[index];
	}
	
	private void initPixelVals(int[] position, RealType<?> sample)
	{
		// TODO : optimize later - do straightforward for now
		
		int x = position[0];
		int y = position[1];
		
		// v0
		if ((x > 0) && (y > 0))
			v[0] = valueOfPixelFromCopy(x-1,y-1);
		else if ((x == 0) && (y > 0))
			v[0] = valueOfPixelFromCopy(0,y-1);
		else if ((y == 0) && (x > 0))
			v[0] = valueOfPixelFromCopy(x-1,0);
		else
			v[0] = valueOfPixelFromCopy(0,0);
		
		// v1
		if (y > 0)
			v[1] = valueOfPixelFromCopy(x,y-1);
		else
			v[1] = valueOfPixelFromCopy(x,0);;
		
		// v2
		if ((x < width-1) && (y > 0))
			v[2] = valueOfPixelFromCopy(x+1,y-1);
		else if ((x == width-1) && (y > 0))
			v[2] = valueOfPixelFromCopy(width-1,y-1);
		else if ((y == 0) && (x < width-1))
			v[2] = valueOfPixelFromCopy(x+1,0);
		else
			v[2] = valueOfPixelFromCopy(width-1,0);
		
		// v3
		if (x > 0)
			v[3] = valueOfPixelFromCopy(x-1,y);
		else
			v[3] = valueOfPixelFromCopy(0,y);
		
		// v4
		v[4] = sample.getRealDouble();
		
		// v5
		if (x < width-1)
			v[5] = valueOfPixelFromCopy(x+1,y);
		else
			v[5] = valueOfPixelFromCopy(width-1,y);
		
		// v6
		if ((x > 0) && (y < height-1))
			v[6] = valueOfPixelFromCopy(x-1,y+1);
		else if ((x == 0) && (y < height-1))
			v[6] = valueOfPixelFromCopy(0,y+1);
		else if ((y == height-1) && (x > 0))
			v[6] = valueOfPixelFromCopy(x-1,height-1);
		else
			v[6] = valueOfPixelFromCopy(0,height-1);
		
		// v7
		if (y < height-1)
			v[7] = valueOfPixelFromCopy(x,y+1);
		else
			v[7] = valueOfPixelFromCopy(x,height-1);
		
		// v8
		if ((x < width-1) && (y < height-1))
			v[8] = valueOfPixelFromCopy(x+1,y+1);
		else if ((x == width-1) && (y < height-1))
			v[8] = valueOfPixelFromCopy(width-1,y+1);
		else if ((y == height-1) && (x < width-1))
			v[8] = valueOfPixelFromCopy(x+1,height-1);
		else
			v[8] = valueOfPixelFromCopy(width-1,height-1);
		
	}
}

