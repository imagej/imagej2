package ij.process;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.*;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.real.*;
import mpicbg.imglib.container.array.*;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.cursor.special.RegionOfInterestCursor;
import mpicbg.imglib.image.*;

public class Snapshot<T extends RealType<T>>
{
	/** The start index in the referenced image that the snapshot copies. */
	private int[] dimensionOrigins;
	
	/** The widths of each dimension */
	private int[] dimensionSpans;

	/** Internal multidimensional storage reference. Has same number of dimensions as referenced data set but actual sizes may differ. */
	public Image<T> snapshot;
	
	
	public Snapshot(Image<T> image, int[] origin, int[] span)
	{
		setSnapshot(image,origin,span);
	}
	
	/*
	 * Return the 2d Image<T> snapshot
	 */
	public Image<T> getImageSnapshot( )
	{
		return snapshot;
	}
	
	private void testDimensionBoundaries(int[] imageDimensions)
	{
		// span dims should match origin dims
		if (dimensionOrigins.length != dimensionSpans.length)
			throw new IllegalArgumentException("testDimensionBoundaries() : dimensionOrigin and dimensionSpan dimension arrays are of differing sizes");

		// origin/span dimensions should match image dimensions
		if (dimensionOrigins.length != imageDimensions.length)
			throw new IllegalArgumentException("testDimensionBoundaries() : dimensionOrigin/dimensionSpan different size than input image");
		
		// make sure origin in a valid range : within bounds of source image
		for (int i = 0; i < dimensionOrigins.length; i++)
			if ((dimensionOrigins[i] < 0) && (dimensionOrigins[i] >= imageDimensions[i]))
				throw new IllegalArgumentException("testDimensionBoundaries() : dimensionOrigins outside bounds of input image at index " + i);

		// make sure span in a valid range : >= 1
		for (int i = 0; i < dimensionSpans.length; i++)
			if (dimensionSpans[i] < 1)
				throw new IllegalArgumentException("testDimensionBoundaries() : dimension span < 1 at index " + i);

		// make sure origin + span within the bounds of the input image
		for (int i = 0; i < dimensionSpans.length; i++)
			if ( (dimensionOrigins[i] + dimensionSpans[i]) > imageDimensions[i] )
				throw new IllegalArgumentException("testDimensionBoundaries() : span length (origin+span) beyond input image boundaries at index " + i);
	}
	
	/**
	 * Multidimensional Snapshot support
	 */
	//public void setImageSnapshot()
	
	public void setSnapshot (Image<T> image, int[] origins, int[] spans)
	{
		this.dimensionOrigins = origins.clone();
		this.dimensionSpans = spans.clone();
		
		testDimensionBoundaries(image.getDimensions());
		
		ImageFactory<T> factory = new ImageFactory<T>(image.createType(),image.getContainerFactory());

	    snapshot = factory.createImage(this.dimensionSpans);
	    	
	     copyFromCursorToCursor( image, snapshot, dimensionOrigins, dimensionSpans, new int[image.getDimensions().length] );
	}
	
	public void restoreData(Image<T> image)
	{
	     copyFromCursorToCursor(snapshot, image, new int[image.getDimensions().length], dimensionSpans, dimensionOrigins );
	}
	
	private void copyFromCursorToCursor(Image<T> sourceImage, Image<T> destinationImage, int[] sourceDimensionOrigins, int[] sourceDimensionSpan, int[] destinationDimensionOrigins )
	{
	    // copy data
	    final LocalizableByDimCursor<T> sourceCursor = sourceImage.createLocalizableByDimCursor();
	    final LocalizableByDimCursor<T> destinationCursor = destinationImage.createLocalizableByDimCursor();
	    final RegionOfInterestCursor<T> sourceROICursor = new RegionOfInterestCursor< T >( sourceCursor, sourceDimensionOrigins, sourceDimensionSpan );
	    final RegionOfInterestCursor<T> destinationROICursor = new RegionOfInterestCursor< T >( destinationCursor, destinationDimensionOrigins, sourceDimensionSpan );
			
	    //iterate over the target data...
	    while( sourceROICursor.hasNext() && destinationROICursor.hasNext() )
	    {

	    	//iterate the cursors
	    	sourceROICursor.fwd();
	    	destinationROICursor.fwd();
	    	
	    	//get the source value
	    	double real = destinationROICursor.getType().getPowerDouble();
	    	double complex = destinationROICursor.getType().getPhaseDouble();
	    	
	    	System.out.println("Source values are " + real + " and " + complex );

	    	//set the destination value
	    	sourceROICursor.getType().setComplexNumber(real, complex);
	    }
	    
	    //close the open cursors
	    sourceROICursor.close( );
	    destinationROICursor.close( );    	
    	sourceCursor.close( );
		destinationCursor.close( );
	}
	
	private String arrayToStr(int[] arr)
	{
		StringBuffer str = new StringBuffer();
		str.append("[");
		for (int i = 0; i < arr.length; i++)
		{
			if (i > 0)
				str.append(":");
			str.append(arr[i] );
		}
		str.append("]");
		
		return str.toString();
	}
	
	@Override
	public String toString()
	{
		StringBuffer str = new StringBuffer();
		str.append( "snapshot(" );
		str.append( arrayToStr(snapshot.getDimensions()) );
		str.append( arrayToStr(dimensionOrigins) );
		str.append( arrayToStr(dimensionSpans) );
		str.append( ")" );
		
		return str.toString();
	}

	public static void main(String[] args)
	{
	    ImageFactory<FloatType> factory = new ImageFactory<FloatType>(new FloatType(),new ArrayContainerFactory());

	    Image<FloatType> image = factory.createImage(new int[] {100, 200, 300});

	    //set two values
	    Cursor<FloatType> cursor = image.createCursor();
	    cursor.fwd();
	    cursor.getType().setComplexNumber(77.77F, 19.99f);
	    cursor.close();
	    
		Cursor<FloatType> cursor5 = image.createCursor();
	    cursor5.fwd();
	    System.out.println("Image data originally is " + cursor5.getType().getRealFloat() );
	    cursor5.close();
	    
	    int[] origin = new int[]{0,0,0};
	    int[] span   = new int[]{10,2,1};
	    
		Snapshot<FloatType> snap = new Snapshot<FloatType>(image, origin, span);
		
		//see if the value was copied to snapshot image
		Cursor<FloatType> cursor4 = snap.snapshot.createCursor();
	    cursor4.fwd();
	    System.out.println("Snap value is " + cursor4.getType().getRealFloat() );
	    cursor4.close();
	    
	    Cursor<FloatType> cursor3 = image.createCursor();
	    cursor3.fwd();
	    cursor3.getType().setReal(2.88f);
	    cursor3.close();
	    
	    //restore
	    snap.restoreData(image);
	    
	    Cursor<FloatType> cursor2 = image.createCursor();
	    cursor2.fwd();
	    System.out.println("Final image value is " + cursor2.getType().getRealFloat() );
	    cursor2.close();
	    
		System.out.println( snap.toString() );
		
		
		
	}
}
