package ij.process;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.*;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.real.*;
import mpicbg.imglib.container.array.*;
import mpicbg.imglib.image.*;

public class Snapshot<T extends RealType<T>>
{
	private Image<T> snapshot;
	private int[] lowerRange;
	private int[] upperRange;
	
	public Snapshot(Image<T> image, int[] lowerRange, int[] upperRange)
	{
		setSnapshot(image,lowerRange,upperRange);
	}
	
	/*
	 * Return the 2d Image<T> snapshot
	 */
	public Image<T> getImageSnapshot( )
	{
		return snapshot;
	}
	
	private int[] calcActualDimensions(int[] lowerRange, int[] upperRange)
	{
		int length = lowerRange.length;

		int[] actualDims = new int[length];

		for (int i = 0; i < length; i++)
			actualDims[i] = upperRange[i] - lowerRange[i] + 1;

		return actualDims;
	}
	
	/**
	 * Multidimensional Snapshot support
	 */
	//public void setImageSnapshot()
	
	public void setSnapshot (Image<T> image, int[] lowerRange, int[] upperRange)
	{
		if (lowerRange.length != upperRange.length)
			throw new IllegalArgumentException("setSnapshot() : upper and lower range dimension arrays are of differing sizes");
		
		if (lowerRange.length != image.getDimensions().length)
			throw new IllegalArgumentException("setSnapshot() : image and range dimensions are of differing sizes");
		
		for (int i = 0; i < lowerRange.length; i++)
			if (lowerRange[i] > upperRange[i])
				throw new IllegalArgumentException("setSnapshot() : lower range exceeds upper range at dimension #" + i);
		
		this.lowerRange = lowerRange.clone();
		this.upperRange = upperRange.clone();
		
		int[] actualDims = calcActualDimensions(lowerRange,upperRange);

		ImageFactory<T> factory = new ImageFactory<T>(image.createType(),image.getContainerFactory());

	    snapshot = factory.createImage(actualDims);
	}
	
	//public void restoreData(Image<T> image)
	//{
	//}
	
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
		str.append( arrayToStr(lowerRange) );
		str.append( arrayToStr(upperRange) );
		str.append( ")" );
		
		return str.toString();
	}

	public static void main(String[] args)
	{
	    ImageFactory<FloatType> factory = new ImageFactory<FloatType>(new FloatType(),new ArrayContainerFactory());

	    Image<FloatType> image = factory.createImage(new int[] {100, 200, 300});

	    int[] lower = new int[]{0,0,20};
	    int[] upper = new int[]{99,199,20};
	    
		Snapshot<FloatType> snap = new Snapshot<FloatType>(image,lower,upper);
		System.out.println(snap.toString());
	}
}
