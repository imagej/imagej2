package imagej2.imglib.process.operation;

import imagej2.Utils;
import imagej2.process.Observer;
import imagej2.selection.SelectionFunction;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.cursor.special.RegionOfInterestCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

/**
 * SingleCursorRoiOperation is the prototypical operation that can be done to an Image. It is the base class
 * for any operation that manipulates one value of an Image at a time with no reference to any other Image.
 * However, the implementor of this abstract interface can create any reference data it needs to decide how
 * to transform values in the Image if desired.
 * 
 * This operation works on a user defined N dimensional region of an image. It can be constrained to only
 * apply to sample values that are filtered via a user specified SelectionFunction. It will update an Observer
 * as the iteration takes place if one is attached to this operation.  
 *
 * Implementors of the abstract interface define beforeIteration(), insideIteration(), and afterIteration()
 * methods.
 */

public abstract class SingleCursorRoiOperation<T extends RealType<T>>
{
	/** the Image to operate upon */
	private Image<T> image;
	
	/** the N dimensional region within the Image that the operation will apply to */
	private int[] origin, span;
	
	/** an Observer that is interested in our progress through the iteration */
	private Observer observer;
	
	/** a SelectionFunction that filters which of the samples are of interest */
	private SelectionFunction selector;

	/** constructor that takes Image and region definition */
	protected SingleCursorRoiOperation(Image<T> image, int[] origin, int[] span)
	{
		this.image = image;
		this.origin = origin.clone();
		this.span = span.clone();
		
		this.observer = null;
		this.selector = null;

		Utils.verifyDimensions(image.getDimensions(), origin, span);
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
	protected abstract void insideIteration(RealType<T> sample);
	
	/** abstract - implemented by subclass */
	protected abstract void afterIteration();
	
	/** runs the operation. does the iteration and calls subclass methods as appropriate */
	public void execute()
	{
		if (this.observer != null)
			observer.init();
		
		final LocalizableByDimCursor<T> imageCursor = this.image.createLocalizableByDimCursor();
		final RegionOfInterestCursor<T> imageRoiCursor = new RegionOfInterestCursor<T>( imageCursor, this.origin, this.span );
		
		beforeIteration(imageRoiCursor.getType());
		
		//iterate over all the pixels, of the selected image plane
		for (T sample : imageRoiCursor)
		{
			// note that the include() method call below passes null as position. This operation is not positionally
			// aware for efficiency. Use a positional operation in the imagej.process.operation package if needed.
			
			if ((this.selector == null) || (this.selector.include(null, sample.getRealDouble())))
				insideIteration(sample);

			if (this.observer != null)
				observer.update();
		}
		
		afterIteration();
		
		imageRoiCursor.close();
		imageCursor.close();

		if (this.observer != null)
			observer.done();
	}
	
}
