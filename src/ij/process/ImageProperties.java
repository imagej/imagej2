package ij.process;

public class ImageProperties
{
	private double backgroundValue;
	
	/** The maximum value contained in the 2D image plane */
	private double maximumPixelValue;
	
	/** The minimum value contained in the 2D image plane */
	private double minimumPixelValue;
	
	/** The dimensional coordinates of this plane within its parent Image<T> */
	private int[] planePosition;
	
	/** A value used to represent a background measure **/
	public double getBackgroundValue()
	{
		return backgroundValue;
	}
	
	/** Given a input Value and Image array, assigns the background value according to
	 * Generic Type Range Limits. */
	/* BDZ - I don't think this is right. ImageJ only does this on ByteType. Not Float or Short. Replaced with next method.
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
	*/
	
	// see notes on previous method
	/** A value used to represent a background measure **/
	public void setBackgroundValue(double value)
	{
		backgroundValue = value;
	}
	
	/** Stores a copy of the plane position within the parent Image<T> */
	public void setPlanePosition( int[] planePosition )
	{
		this.planePosition = planePosition.clone();
	}
	
	/** returns a copy of the plane position within the parent Image<T> */
	public int[] getPlanePosition()
	{
		if (planePosition == null)
			return null;
		
		return planePosition.clone();
	}
}
