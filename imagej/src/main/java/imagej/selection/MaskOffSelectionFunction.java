package imagej.selection;

import imagej.process.ImageUtils;
import imagej.process.Index;

public class MaskOffSelectionFunction implements SelectionFunction
{
	private int[] maskOrigin;
	private int[] maskSpan;
	private byte[] mask;
	private int[] relativePosition;
	
	public MaskOffSelectionFunction(int[] maskOrigin, int[] maskSpan, byte[] mask)
	{
		this.maskOrigin = maskOrigin;
		this.maskSpan = maskSpan;
		this.mask = mask;
		
		this.relativePosition = new int[maskOrigin.length];
		
		if (ImageUtils.getTotalSamples(maskSpan) != mask.length)
			throw new IllegalArgumentException("mask size does not match mask span size");
	}
	
	public boolean include(int[] position, double sample)
	{
		int maskPosition = calcMaskPosition(position);
		
		if (maskPosition >= this.mask.length)
			return false;
		
		return this.mask[maskPosition] == 0;
	}
	
	private int calcMaskPosition(int[] position)
	{
		for (int i = 0; i < this.maskSpan.length; i++)
			this.relativePosition[i] = position[i] - this.maskOrigin[i];
		
		//GOOD long sampleNumber = this.getSampleNumber(this.maskSpan, this.relativePosition);
		//BAD long sampleNumber = ImageUtils.getSampleNumber(this.maskSpan, this.relativePosition);
		long sampleNumber = Index.positionToRaster(this.maskSpan, this.relativePosition);
		
		if (sampleNumber > Integer.MAX_VALUE)
			return this.mask.length;
		
		return (int) sampleNumber;
	}

	// TODO - may point out that the version in ImageUtils is WRONG
	// similar to ImageUtils but calcs plane counts in opposite direction
	private long getSampleNumber(int[] dimensions, int[] indexValue)
	{
		if (indexValue.length != dimensions.length)
			throw new IllegalArgumentException("index arrays have incompatible lengths");
		
		long planeNum = 0;
		
		int numDims = dimensions.length;
		
		for (int dim = numDims-1; dim >= 0; dim--)
		{
			int thisIndexVal = indexValue[dim];
			
			long multiplier = 1;
			for (int j = dim-1; j >= 0; j--)
				multiplier *= dimensions[j];
			
			planeNum += thisIndexVal * multiplier;
		}
		
		return planeNum;
	}

}
