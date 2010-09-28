package imagej.process;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.image.*;

/** an N-dimensional copy of a subset of an Image's data with ability to capture and restore values */
public class Snapshot<T extends RealType<T>>
{
	// ************** Instance variables ********************************************************************
	
	/** The start index in the referenced image that the snapshot copies. */
	private int[] origin;
	
	/** The width of each dimension */
	private int[] span;

	/** Internal multidimensional storage reference. Has same number of dimensions as referenced data set but actual sizes may differ. */
	private Image<T> storage;
	
	
	// ************** Private helper methods ********************************************************************

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
	
	// ************** Public methods ********************************************************************
	
	/** a Snapshot is taken from an image starting at an origin and spanning each dimension */
	public Snapshot(Image<T> image, int[] origin, int[] span)
	{
		copyFromImage(image,origin,span);
	}
	
	/** allow access to snapshot data */ 
	public Image<T> getStorage()
	{
		return this.storage;
	}
	
	/*
	public void setStorage(Image<T> newStorage)
	{
		int[] currDimensions = this.storage.getDimensions();
		int[] newDimensions  = newStorage.getDimensions();
		
		if (currDimensions.length != newDimensions.length)
			throw new IllegalArgumentException("Snapshot::setStorage() passed an Image whose dimensions do not match current storage layout.");
		
		for (int i = 0; i < currDimensions.length; i++)
			if (currDimensions[i] != newDimensions[i])
				throw new IllegalArgumentException("Snapshot::setStorage() passed an Image whose dimensions do not match current storage layout.");
		
		this.storage = newStorage;
	}
	*/
	
	/** take a snapshot of an Image's data from given origin and across each span dimension */
	public void copyFromImage(Image<T> image, int[] origin, int[] span)
	{
		// verify input
		ImageUtils.verifyDimensions(image.getDimensions(),origin,span);
		
		// remember dimensions
		this.origin = origin.clone();
		this.span = span.clone();
		
		// get factory to create a data repository
		ImageFactory<T> factory = new ImageFactory<T>(image.createType(),image.getContainerFactory());

		// create the data repository
		this.storage = factory.createImage(this.span);
			
		// copy the data
		ImageUtils.copyFromImageToImage( image, this.origin, this.span, this.storage, Index.create(image.getDimensions().length), this.span );
	}

	/** paste snapshot data into an image */
	public void pasteIntoImage(Image<T> image)
	{
		// copy from the snapshot to the image
		ImageUtils.copyFromImageToImage(this.storage, Index.create(image.getDimensions().length), this.span, image, this.origin, this.span );
	}
	
	/** encode a snapshot as a String */
	@Override
	public String toString()
	{
		StringBuffer str = new StringBuffer();
		str.append( "snapshot(" );
		str.append( arrayToStr(this.storage.getDimensions()) );
		str.append(",");
		str.append( arrayToStr(this.origin) );
		str.append(",");
		str.append( arrayToStr(this.span) );
		str.append( ")" );
		
		return str.toString();
	}
}
