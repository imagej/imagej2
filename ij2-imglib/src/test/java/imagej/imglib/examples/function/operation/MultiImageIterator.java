package imagej.imglib.examples.function.operation;

import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.cursor.special.RegionOfInterestCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

//TODO
//Figure out Imglib's preferred way to handle linked cursors. Can they work where span dimensionality differs?
//    (a 2D Image to run against a plane in a 5D Image)  Or do I avoid ROICurs and use some transformational view
//    where dims exactly match?

@SuppressWarnings("unchecked")
public class MultiImageIterator<T extends RealType<T>>  // don't want to implement full Cursor API
{
	private Image<T>[] images;
	private int[][] origins;
	private int[][] spans;
	private RegionOfInterestCursor<T>[] cursors;
	
	// -----------------  public interface ------------------------------------------

	public MultiImageIterator(Image<T>[] images)
	{
		this.images = images;
		int totalImages = images.length;
		origins = new int[totalImages][];
		spans = new int[totalImages][];
		for (int i = 0; i < totalImages; i++)
		{
			origins[i] = new int[images[i].getNumDimensions()];
			spans[i] = images[i].getDimensions().clone();
		}
		cursors = new RegionOfInterestCursor[totalImages];
	}

	public void setRegion(int i, int[] origin, int[] span)
	{
		origins[i] = origin;
		spans[i] = span;
	}
	
	public RegionOfInterestCursor<T>[] getSubcursors()
	{
		return cursors;
	}

	/** call after subregions defined and before first hasNext() or fwd() call. tests that all subregions defined are compatible. */
	void initialize()  // could call lazily in hasNext() or fwd() but a drag on performance
	{
		// make sure all specified regions are shape compatible : for now just test num elements in spans are same
		long totalSamples = numInSpan(spans[0]);
		for (int i = 1; i < spans.length; i++)
			if (numInSpan(spans[i]) != totalSamples)
				throw new IllegalArgumentException("incompatible span shapes");

		for (int i = 0; i < images.length; i++)
		{
			LocalizableByDimCursor<T> dimCursor = images[i].createLocalizableByDimCursor();
			cursors[i] = new RegionOfInterestCursor<T>(dimCursor, origins[i], spans[i]);
		}
	}
	
	public boolean hasNext()
	{
		boolean hasNext = cursors[0].hasNext();
		
		for (int i = 1; i < cursors.length; i++)
			if (hasNext != cursors[i].hasNext())
				throw new IllegalArgumentException("linked cursors are out of sync");
		
		return hasNext;
	}
	
	public void fwd()
	{
		for (int i = 0; i < cursors.length; i++)
			cursors[i].fwd();
	}
	
	// -----------------  private interface ------------------------------------------

	private long numInSpan(int[] span)  // TODO - call Imglib equivalent instead
	{
		long total = 1;
		for (int axisLen : span)
			total *= axisLen;
		return total;
	}
}

