package ij.process;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class MaskedFillOperation<K extends RealType<K>> extends PositionalRoiOperation<K>
{
	private byte[] mask;
	private double fillColor;
	private int[] origin, span;

	protected MaskedFillOperation(Image<K> image, int[] origin, int[] span, byte[] mask, double fillColor) {
		super(image, origin, span);
		this.mask = mask;
		this.fillColor = fillColor;
		this.origin = origin;
		this.span = span;
		
		if (mask.length != ImageUtils.getTotalSamples(span))
			throw new IllegalArgumentException("MaskedFillOperation(): mask is different size than region of interest");
	}

	@Override
	public void beforeIteration(RealType<K> type)
	{
	}

	@Override
	public void insideIteration(int[] position, RealType<K> sample)
	{
		int maskPos = calcMaskPosition(position[0], position[1]);
		
		if (this.mask[maskPos] != 0)
			sample.setReal(this.fillColor);
	}

	@Override
	public void afterIteration()
	{
	}
	
	private int calcMaskPosition(int x, int y)
	{
		int val = 0;
		val += (y - origin[1]) * span[0];
		val += (x - origin[0]);
		return val;
	}
	
}
