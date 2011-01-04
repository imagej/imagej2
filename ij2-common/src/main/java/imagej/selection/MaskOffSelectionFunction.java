package imagej.selection;

import imagej.Dimensions;
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
		
		if (Dimensions.getTotalSamples(maskSpan) != mask.length)
			throw new IllegalArgumentException("mask size does not match mask span size");
	}
	
	public boolean include(int[] position, double sample)
	{
		int maskPosition = calcMaskPosition(position);
		
		return this.mask[maskPosition] == 0;
	}
	
	private int calcMaskPosition(int[] position)
	{
		for (int i = 0; i < this.maskSpan.length; i++)
			this.relativePosition[i] = position[i] - this.maskOrigin[i];
		
		long sampleNumber = Index.positionToRaster(this.maskSpan, this.relativePosition);
		
		if (sampleNumber > Integer.MAX_VALUE)
			throw new IllegalArgumentException("mask index is too large");
		
		return (int) sampleNumber;
	}

}
