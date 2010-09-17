package imagej.process.operation;

import imagej.process.ImageUtils;
import imagej.process.Index;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public abstract class PositionalDualCursorRoiOperation<T extends RealType<T>>
{
	private Image<T> image1, image2;
	private int[] origin1, origin2;
	private int[] span1, span2;
	
	protected PositionalDualCursorRoiOperation(Image<T> image1, int[] origin1, int[] span1, Image<T> image2, int[] origin2, int[] span2)
	{
		this.image1 = image1;
		this.origin1 = origin1.clone();
		this.span1 = span1.clone();
	
		this.image2 = image2;
		this.origin2 = origin2.clone();
		this.span2 = span2.clone();
	
		ImageUtils.verifyDimensions(image1.getDimensions(), origin1, span1);
		ImageUtils.verifyDimensions(image2.getDimensions(), origin2, span2);
	}
	
	public Image<T> getImage1() { return image1; }
	public int[] getOrigin1() { return origin1; }
	public int[] getSpan1() { return span1; }
	
	public Image<T> getImage2() { return image2; }
	public int[] getOrigin2() { return origin2; }
	public int[] getSpan2() { return span2; }
	
	public abstract void beforeIteration(RealType<T> type);
	public abstract void insideIteration(int[] position1, RealType<T> sample1, int[] position2, RealType<T> sample2);
	public abstract void afterIteration();
	
	public void execute()
	{
		LocalizableByDimCursor<T> cursor1 = this.image1.createLocalizableByDimCursor();
		LocalizableByDimCursor<T> cursor2 = this.image2.createLocalizableByDimCursor();
		
		int[] position1 = this.origin1.clone();
		int[] position2 = this.origin2.clone();
		
		int[] position1Copy = position1.clone();
		int[] position2Copy = position2.clone();
		
		beforeIteration(cursor1.getType());
		
		while ((Index.isValid(position1, this.origin1, this.span1)) && (Index.isValid(position2, this.origin2, this.span2)))
		{
			cursor1.setPosition(position1);
			cursor2.setPosition(position2);
			
			// could clone these but may take longer and cause a lot of object creation/destruction
			for (int i = 0; i < position1.length; i++)
				position1Copy[i] = position1[i];
			for (int i = 0; i < position2.length; i++)
				position2Copy[i] = position2[i];
			
			// send them a copy of position so that users can manipulate without messing us up
			insideIteration(position1Copy, cursor1.getType(), position2Copy, cursor2.getType());
			
			Index.increment(position1,origin1,span1);
			Index.increment(position2,origin2,span2);
		}

		afterIteration();
	
		cursor1.close();
		cursor2.close();
	}
}
