package imagej.process.operation;

import imagej.process.ImageUtils;
import imagej.process.Observer;
import imagej.selection.SelectionFunction;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.cursor.special.RegionOfInterestCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

/**
 * DualCursorRoiOperation is the prototypical operation that can be done between two Images. It is the base class
 * for any operation that manipulates one value of an Image at a time while referencing a region synchronized
 * Image. Either, both, or neither Image can be modified as desired. In addition, the implementor of this abstract
 * interface can create any reference data it needs to decide how to transform values in the Images if desired.
 * 
 * This operation works on user defined N dimensional regions of the input Images. It can be constrained to only
 * apply to sample values that are filtered via user specified SelectionFunctions. It will update an Observer
 * as the iteration takes place if one is attached to this operation.
 *
 * Implementors of the abstract interface define beforeIteration(), insideIteration(), and afterIteration()
 * methods.
 */

public abstract class DualCursorRoiOperation<T extends RealType<T>>
{
	/** the Images to operate upon */
	private Image<T> img1, img2;
	
	/** the N dimensional regions within the Images that the operation will apply to */
	private int[] origin1, span1, origin2, span2;
	
	/** an Observer that is interested in our progress through the iteration */
	private Observer observer;

	/** SelectionFunctions that filter which of the samples are of interest */
	private SelectionFunction selector1, selector2;
	
	/** constructor that takes two Image and region definitions */
	protected DualCursorRoiOperation(Image<T> img1, int[] origin1, int[] span1, Image<T> img2, int[] origin2, int[] span2)
	{
		this.img1 = img1;
		this.origin1 = origin1.clone();
		this.span1 = span1.clone();

		this.img2 = img2;
		this.origin2 = origin2.clone();
		this.span2 = span2.clone();
	
		this.observer = null;
		this.selector1 = null;
		this.selector2 = null;
		
		ImageUtils.verifyDimensions(img1.getDimensions(), origin1, span1);
		ImageUtils.verifyDimensions(img2.getDimensions(), origin2, span2);
		
		if (ImageUtils.getTotalSamples(span1) != ImageUtils.getTotalSamples(span2))
			throw new IllegalArgumentException("DualCursorRoiOperation(): span sizes differ");
	}
	
	public Image<T> getImage1()   { return img1; }
	public int[] getOrigin1() { return origin1; }
	public int[] getSpan1()   { return span1; }
	
	public Image<T> getImage2()   { return img2; }
	public int[] getOrigin2() { return origin2; }
	public int[] getSpan2()   { return span2; }

	/** allows (one) Observer to watch the iteration as it takes place. The Observer is updated every time a
	 * sample is loaded (and not just when insideIteration() is invoked).
	 * */
	public void addObserver(Observer o) { observer = o; }
	
	/** allows user to specify which subset of samples will be passed on to insideIteration(). Note that it is
	 * more performant to pass null as a selection function rather than one that accepts all samples.  */
	public void setSelectionFunctions(SelectionFunction f1, SelectionFunction f2)
	{
		selector1 = f1;
		selector2 = f2;
	}
	
	/** abstract - implemented by subclass */
	protected abstract void beforeIteration(RealType<T> type);

	/** abstract - implemented by subclass */
	protected abstract void insideIteration(RealType<T> sample1, RealType<T> sample2);

	/** abstract - implemented by subclass */
	protected abstract void afterIteration();
	
	/** runs the operation. does the iteration and calls subclass methods as appropriate */
	public void execute()
	{
		if (this.observer != null)
			observer.init();
		
		LocalizableByDimCursor<T> image1Cursor = this.img1.createLocalizableByDimCursor();
		LocalizableByDimCursor<T> image2Cursor = this.img2.createLocalizableByDimCursor();

		RegionOfInterestCursor<T> image1RoiCursor = new RegionOfInterestCursor<T>(image1Cursor, this.origin1, this.span1);
		RegionOfInterestCursor<T> image2RoiCursor = new RegionOfInterestCursor<T>(image2Cursor, this.origin2, this.span2);
	
		beforeIteration(image1Cursor.getType());
		
		while (image1RoiCursor.hasNext() && image2RoiCursor.hasNext())
		{
			image1RoiCursor.fwd();
			image2RoiCursor.fwd();
			
			RealType<T> sample1 = image1RoiCursor.getType();
			RealType<T> sample2 = image2RoiCursor.getType();
			
			if ((this.selector1 == null) || (this.selector1.include(null, sample1.getRealDouble())))
				if ((this.selector2 == null) || (this.selector2.include(null, sample2.getRealDouble())))
					insideIteration(sample1, sample2);

			if (this.observer != null)
				observer.update();
		}
		
		afterIteration();
		
		image1RoiCursor.close();
		image2RoiCursor.close();
		image1Cursor.close();
		image2Cursor.close();

		if (this.observer != null)
			observer.done();
	}
}

