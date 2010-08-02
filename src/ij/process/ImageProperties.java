package ij.process;

import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class ImageProperties<T extends RealType<T>>
{
	private double backgroundValue;
	
	/** The maximum value contained in the 2D image plane */
	private double maximumPixelValue;
	
	/** The minimum value contained in the 2D image plane */
	private double minimumPixelValue;
	
	/** The number of dimensions beyond two */
	private static int[] extraDimensions;
	
	/** A value used to represent a background measure **/
	public double getBackgroundValue()
	{
		return backgroundValue;
	}
	
	/** Given a input Value and Image array, assigns the background value according to
	 * Generic Type Range Limits. */
	public void setBackgroundValue( double value, Image<T> imageData )
	{
		//Get a cursor
		final LocalizableByDimCursor<T> imageCursor = imageData.createLocalizableByDimCursor( );
     
		backgroundValue = value;
		if (backgroundValue< imageCursor.getType().getMinValue() ) backgroundValue = imageCursor.getType().getMinValue();
		if (backgroundValue>imageCursor.getType().getMaxValue() ) backgroundValue = imageCursor.getType().getMaxValue();
		
		//close the cursor
		imageCursor.close();
	}

	/** Stores a copy of the array extra dimensions */
	public void setExtraDimensions( int[] extraDimensionsInputArray )
	{
		extraDimensions = extraDimensionsInputArray.clone();
	}
	
	/** Returns a copy of the internal array extraDimensions */
	public int[] getExtraDimensions()
	{
		return extraDimensions.clone();
	}
}
