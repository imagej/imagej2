package imagej.process.operation;

import imagej.process.ImageUtils;
import imagej.process.Observer;
import imagej.selection.SelectionFunction;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.cursor.special.RegionOfInterestCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public abstract class SingleCursorRoiOperation<T extends RealType<T>>
{
	private Image<T> image;
	private int[] origin, span;
	private Observer observer;
	private SelectionFunction selector;

	protected SingleCursorRoiOperation(Image<T> image, int[] origin, int[] span)
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
	
	public void addObserver(Observer o) { this.observer = o; }
	public void setSelectionFunction(SelectionFunction f) { this.selector = f; }

	public abstract void beforeIteration(RealType<T> type);
	public abstract void insideIteration(RealType<T> sample);
	public abstract void afterIteration();
	
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
