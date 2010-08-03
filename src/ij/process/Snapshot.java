package ij.process;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.real.*;
import mpicbg.imglib.container.array.*;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.cursor.special.RegionOfInterestCursor;
import mpicbg.imglib.image.*;

/** an N-dimensional copy of a subset of an Image's data with ability to capture and restore values */
public class Snapshot<T extends RealType<T>>
{
	/** The start index in the referenced image that the snapshot copies. */
	private int[] dimensionOrigins;
	
	/** The widths of each dimension */
	private int[] dimensionSpans;

	/** Internal multidimensional storage reference. Has same number of dimensions as referenced data set but actual sizes may differ. */
	private Image<T> snapshot;
	
	
	/** a Snapshot is taken from an image starting at an origin and spanning each dimension */
	public Snapshot(Image<T> image, int[] origins, int[] spans)
	{
		copyFromImage(image,origins,spans);
	}
	
	// TODO - not sure why this is needed. Called in imglibProcessor but not good.
	/** allow access to snapshot data */ 
	public Image<T> getImageSnapshot()
	{
		return snapshot;
	}
	
	/** throws an exception if the combination of origins and spans is outside an image's dimensions */
	private void testDimensionBoundaries(int[] imageDimensions, int[] origins, int[] spans)
	{
		// span dims should match origin dims
		if (origins.length != spans.length)
			throw new IllegalArgumentException("testDimensionBoundaries() : origins and spans arrays are of differing sizes");

		// origin/span dimensions should match image dimensions
		if (origins.length != imageDimensions.length)
			throw new IllegalArgumentException("testDimensionBoundaries() : origins/spans different size than input image");
		
		// make sure origin in a valid range : within bounds of source image
		for (int i = 0; i < origins.length; i++)
			if ((origins[i] < 0) || (origins[i] >= imageDimensions[i]))
				throw new IllegalArgumentException("testDimensionBoundaries() : origins outside bounds of input image at index " + i);

		// make sure span in a valid range : >= 1
		for (int i = 0; i < spans.length; i++)
			if (spans[i] < 1)
				throw new IllegalArgumentException("testDimensionBoundaries() : span size < 1 at index " + i);

		// make sure origin + span within the bounds of the input image
		for (int i = 0; i < spans.length; i++)
			if ( (origins[i] + spans[i]) > imageDimensions[i] )
				throw new IllegalArgumentException("testDimensionBoundaries() : span range (origin+span) beyond input image boundaries at index " + i);
	}
	
	/** take a snapshot of an Image's data from given origin and across each span dimension */
	public void copyFromImage(Image<T> image, int[] origins, int[] spans)
	{
		testDimensionBoundaries(image.getDimensions(),origins,spans);
		
		this.dimensionOrigins = origins.clone();
		this.dimensionSpans = spans.clone();
		
		ImageFactory<T> factory = new ImageFactory<T>(image.createType(),image.getContainerFactory());
		
		snapshot = factory.createImage(this.dimensionSpans);
			
		copyFromImageToImage( image, snapshot, dimensionOrigins, new int[image.getDimensions().length], dimensionSpans );
	}

	/** paste snapshot data into an image */
	public void pasteToImage(Image<T> image)
	{
		copyFromImageToImage(snapshot, image, new int[image.getDimensions().length], dimensionOrigins, dimensionSpans );
	}
	
	/** copies data from one image to another given origins and dimensional spans */
	private void copyFromImageToImage(Image<T> sourceImage, Image<T> destinationImage, int[] sourceDimensionOrigins, int[] destinationDimensionOrigins, int[] dimensionSpans)
	{
		// copy data
		final LocalizableByDimCursor<T> sourceCursor = sourceImage.createLocalizableByDimCursor();
		final LocalizableByDimCursor<T> destinationCursor = destinationImage.createLocalizableByDimCursor();
		final RegionOfInterestCursor<T> sourceROICursor = new RegionOfInterestCursor< T >( sourceCursor, sourceDimensionOrigins, dimensionSpans );
		final RegionOfInterestCursor<T> destinationROICursor = new RegionOfInterestCursor< T >( destinationCursor, destinationDimensionOrigins, dimensionSpans );
			
		//iterate over the target data...
		while( sourceROICursor.hasNext() && destinationROICursor.hasNext() )
		{
			//iterate the cursors
			sourceROICursor.fwd();
			destinationROICursor.fwd();
			
			//get the source value
			double real = sourceROICursor.getType().getPowerDouble();
			double complex = sourceROICursor.getType().getPhaseDouble();
			
			//System.out.println("Source values are " + real + " and " + complex );
		
			//set the destination value
			destinationROICursor.getType().setComplexNumber(real, complex);
		}
		
		//close the open cursors
		sourceROICursor.close( );
		destinationROICursor.close( );    	
		sourceCursor.close( );
		destinationCursor.close( );
	}
	
	/** used by toString override */
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
	
	/** encode a snapshot as a String */
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
		
		//see if the value was copied to snapshot image
		Cursor<FloatType> cursor6 = image.createCursor();
		cursor6.fwd();
		System.out.println("Simulated change to image after snapshot. Value is " + cursor6.getType().getRealFloat() );
		cursor6.close();
		
		//restore
		snap.pasteToImage(image);
		
		Cursor<FloatType> cursor2 = image.createCursor();
		cursor2.fwd();
		System.out.println("Final image value is " + cursor2.getType().getRealFloat() );
		cursor2.close();
		
		System.out.println( snap.toString() );
	}
}
