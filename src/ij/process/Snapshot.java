package ij.process;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class Snapshot<T extends RealType<T>>
{
	Image<T> snapshot;
	
	/*
	 * Return the 2d Image<T> snapshot
	 */
	public Image<T> getImageSnapshot( )
	{
		return snapshot;
	}
	
	/**
	 * Multidimensional Snapshot support
	 */
	//public void setImageSnapshot()
}
