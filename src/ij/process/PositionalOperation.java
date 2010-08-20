package ij.process;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public abstract class PositionalOperation<T extends RealType<T>>
{
	Image<T> image;
	int[] origin;
	int[] span;
	
	PositionalOperation(Image<T> image, int[] origin, int[] span)
	{
		this.image = image;
		this.origin = origin.clone();
		this.span = span.clone();
		
		if (image.getNumDimensions() != origin.length)
			throw new IllegalArgumentException("PositionalOperation(): image dimensions do not match origin dimensions");

		if (span.length != origin.length)
			throw new IllegalArgumentException("PositionalOperation(): span dimensions do not match origin dimensions");
	}
	
	public Image<T> getImage() { return image; }
	public int[] getOrigin() { return origin; }
	public int[] getSpan() { return span; }
	
	public abstract void beforeIteration(RealType<?> type);
	public abstract void insideIteration(int[] position, RealType<?> sample);
	public abstract void afterIteration();
}

