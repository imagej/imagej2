package imagej.imglib.process.operation;

import imagej.Utils;
import imagej.process.Index;
import imagej.process.Observer;
import imagej.selection.SelectionFunction;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;


/**
 * PositionalDualCursorRoiOperation is the prototypical positional operation that can be done between two Images. It
 * is the base class for any operation that manipulates samples within two Images (using information about their position
 * in their respective Images). However, the implementor of this abstract interface can create any additional reference
 * data it needs to decide how to transform values in the Images if desired.
 * 
 * This operation works on a user defined N dimensional region of the images. It can be constrained to only
 * apply to sample values that are filtered via user specified SelectionFunctions. It will update an Observer
 * as the iteration takes place if one is attached to this operation.  
 *
 * Implementors of the abstract interface define beforeIteration(), insideIteration(), and afterIteration()
 * methods.
 * 
 * Note that Positional cursor operations are slower than their nonpositional counterparts
 * (i.e. SingleCursorRoiOperation vs. PositionalSingleCursorRoiOperation.
 */

public abstract class PositionalDualCursorRoiOperation<T extends RealType<T>>
{
	/** the Images to operate upon */
	private Image<T> image1, image2;

	/** the origins of the N dimensional regions within the Images that the operation will apply to */
	private int[] origin1, origin2;

	/** the ranges of the N dimensional regions within the Images that the operation will apply to */
	private int[] span1, span2;

	/** an Observer that is interested in our progress through the iteration */
	private Observer observer;

	/** SelectionFunctions that filter which of the samples are of interest */
	private SelectionFunction selector1, selector2;
	
	/** constructor that takes two Image and region definitions */
	protected PositionalDualCursorRoiOperation(Image<T> image1, int[] origin1, int[] span1, Image<T> image2, int[] origin2, int[] span2)
	{
		this.image1 = image1;
		this.origin1 = origin1.clone();
		this.span1 = span1.clone();
	
		this.image2 = image2;
		this.origin2 = origin2.clone();
		this.span2 = span2.clone();
	
		this.observer = null;
		this.selector1 = null;
		this.selector2 = null;
		
		Utils.verifyDimensions(image1.getDimensions(), origin1, span1);
		Utils.verifyDimensions(image2.getDimensions(), origin2, span2);
		
		if (Utils.getTotalSamples(span1) != Utils.getTotalSamples(span2))
			throw new IllegalArgumentException("PositionalDualCursorRoiOperation(): span sizes differ");
	}
	
	public Image<T> getImage1() { return image1; }
	public int[] getOrigin1() { return origin1; }
	public int[] getSpan1() { return span1; }
	
	public Image<T> getImage2() { return image2; }
	public int[] getOrigin2() { return origin2; }
	public int[] getSpan2() { return span2; }
	
	/** allows (one) Observer to watch the iteration as it takes place. The Observer is updated every time a
	 * sample is loaded (and not just when insideIteration() is invoked).
	 * */
	public void addObserver(Observer o) { this.observer = o; }

	/** allows user to specify which subset of samples will be passed on to insideIteration(). Note that it is
	 * more performant to pass null as a selection function rather than one that accepts all samples. */
	public void setSelectionFunctions(SelectionFunction f1, SelectionFunction f2)
	{
		this.selector1 = f1;
		this.selector2 = f2;
	}
	
	/** abstract - implemented by subclass */
	protected abstract void beforeIteration(RealType<T> type);

	/** abstract - implemented by subclass */
	protected abstract void insideIteration(int[] position1, RealType<T> sample1, int[] position2, RealType<T> sample2);

	/** abstract - implemented by subclass */
	protected abstract void afterIteration();
	
	/** runs the operation. does the iteration and calls subclass methods as appropriate */
	public void execute()
	{
		if (this.observer != null)
			observer.init();
		
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

			RealType<T> sample1 = cursor1.getType();
			RealType<T> sample2 = cursor2.getType();
			
			if ((this.selector1 == null) || (this.selector1.include(position1, sample1.getRealDouble())))
			{
				if ((this.selector2 == null) || (this.selector2.include(position2, sample2.getRealDouble())))
				{
					// could clone these but may take longer and cause a lot of object creation/destruction
					for (int i = 0; i < position1.length; i++)
						position1Copy[i] = position1[i];
					for (int i = 0; i < position2.length; i++)
						position2Copy[i] = position2[i];
					
					// send them a copy of position so that users can manipulate without messing us up
					insideIteration(position1Copy, sample1, position2Copy, sample2);
				}
			}
			
			if (this.observer != null)
				observer.update();

			Index.increment(position1,origin1,span1);
			Index.increment(position2,origin2,span2);
		}

		afterIteration();
	
		cursor1.close();
		cursor2.close();

		if (this.observer != null)
			observer.done();
	}
}
