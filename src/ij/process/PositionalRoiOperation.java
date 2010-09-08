package ij.process;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public abstract class PositionalRoiOperation<T extends RealType<T>>
{
	private Image<T> image;
	private int[] origin;
	private int[] span;
	
	protected PositionalRoiOperation(Image<T> image, int[] origin, int[] span)
	{
		this.image = image;
		this.origin = origin.clone();
		this.span = span.clone();
	
		ImageUtils.verifyDimensions(image.getDimensions(), origin, span);
	}
	
	public Image<T> getImage() { return image; }
	public int[] getOrigin() { return origin; }
	public int[] getSpan() { return span; }
	
	public abstract void beforeIteration(RealType<?> type);
	public abstract void insideIteration(int[] position, RealType<?> sample);
	public abstract void afterIteration();
}

