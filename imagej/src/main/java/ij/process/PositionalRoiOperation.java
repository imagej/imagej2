package ij.process;

import mpicbg.imglib.cursor.LocalizableByDimCursor;
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
	
	public abstract void beforeIteration(RealType<T> type);
	public abstract void insideIteration(int[] position, RealType<T> sample);
	public abstract void afterIteration();
	
	public void execute()
	{
		LocalizableByDimCursor<T> cursor = this.image.createLocalizableByDimCursor();
		
		int[] position = this.origin.clone();
		
		int[] positionCopy = this.origin.clone();
		
		beforeIteration(cursor.getType());
		
		while (Index.isValid(position,this.origin, this.span))
		{
			cursor.setPosition(position);
			
			for (int i = 0; i < position.length; i++)  // could clone but may take longer and cause a lot of object creation/destruction
				positionCopy[i] = position[i];
			
			insideIteration(positionCopy,cursor.getType());  // send them a copy so that users can manipulate without messing us up
			
			Index.increment(position,origin,span);
		}

		afterIteration();
	
		cursor.close();
	}
}

