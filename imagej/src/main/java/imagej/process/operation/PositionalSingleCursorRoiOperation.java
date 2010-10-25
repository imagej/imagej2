package imagej.process.operation;

import imagej.process.ImageUtils;
import imagej.process.Index;
import imagej.process.Observer;
import imagej.selection.SelectionFunction;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

/**
 * PositionalSingleCursorRoiOperation is the prototypical positional operation that can be done to an Image. It is
 * the base class for any operation that manipulates one value of an Image (using information about its position in
 * the Image) with no reference to any other Image. However, the implementor of this abstract interface can create
 * any reference data it needs to decide how to transform values in the Image if desired.
 * 
 * This operation works on a user defined N dimensional region of an image. It can be constrained to only
 * apply to sample values that are filtered via a user specified SelectionFunction. It will update an Observer
 * as the iteration takes place if one is attached to this operation.  
 *
 * Implementors of the abstract interface define beforeIteration(), insideIteration(), and afterIteration()
 * methods.
 * 
 * Note that Positional cursor operations are slower than their nonpositional counterparts
 * (i.e. SingleCursorRoiOperation vs. PositionalSingleCursorRoiOperation.
 */

public abstract class PositionalSingleCursorRoiOperation<T extends RealType<T>>
{
	/** the Image to operate upon */
	private Image<T> image;

	/** the origin of the N dimensional region within the Image that the operation will apply to */
	private int[] origin;

	/** the range of the N dimensional region within the Image that the operation will apply to */
	private int[] span;
	
	/** an Observer that is interested in our progress through the iteration */
	private Observer observer;

	/** a SelectionFunction that filters which of the samples are of interest */
	private SelectionFunction selector;
	
	/** constructor that takes Image and region definition */
	protected PositionalSingleCursorRoiOperation(Image<T> image, int[] origin, int[] span)
	{
		this.image = image;
		this.origin = origin.clone();
		this.span = span.clone();
		
	    this.observer = null;
	    this.selector = null;
	    
		ImageUtils.verifyDimensions(image.getDimensions(), origin, span);
	}
	
	public Image<T> getImage() { return image; }
	public int[] getOrigin() { return origin; }
	public int[] getSpan() { return span; }
	
	/** allows (one) Observer to watch the iterations as it takes place. The Observer is updated every time a
	 * sample is loaded (and not just when insideIteration() is invoked).
	 * */
	public void addObserver(Observer o) { this.observer = o; }

	/** allows user to specify which subset of samples will be passed on to insideIteration(). Note that it is
	 * more performant to pass null as a selection function rather than one that accepts all samples. */
	public void setSelectionFunction(SelectionFunction f) { this.selector = f; }
	
	/** abstract - implemented by subclass */
	protected abstract void beforeIteration(RealType<T> type);

	/** abstract - implemented by subclass */
	protected abstract void insideIteration(int[] position, RealType<T> sample);

	/** abstract - implemented by subclass */
	protected abstract void afterIteration();
	
	/** runs the operation. does the iteration and calls subclass methods as appropriate */
	public void execute()
	{
		if (this.observer != null)
			observer.init();
		
		LocalizableByDimCursor<T> cursor = this.image.createLocalizableByDimCursor();
		
		int[] position = this.origin.clone();
		
		int[] positionCopy = this.origin.clone();
		
		beforeIteration(cursor.getType());
		
		while (Index.isValid(position,this.origin, this.span))
		{
			cursor.setPosition(position);
			
			RealType<T> sample = cursor.getType();
			
			if ((this.selector == null) || (this.selector.include(position, sample.getRealDouble())))
			{
				// could clone but may take longer and cause a lot of object creation/destruction
				for (int i = 0; i < position.length; i++)
					positionCopy[i] = position[i];
				
				// send them a copy so that users can manipulate without messing us up
				insideIteration(positionCopy,sample);
			}
			
			if (this.observer != null)
				observer.update();

			Index.increment(position,origin,span);
		}

		afterIteration();
	
		cursor.close();

		if (this.observer != null)
			observer.done();
	}
}

