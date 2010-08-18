package ij.process;

import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.cursor.special.RegionOfInterestCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.ComplexType;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.ByteType;
import mpicbg.imglib.type.numeric.integer.IntType;
import mpicbg.imglib.type.numeric.integer.LongType;
import mpicbg.imglib.type.numeric.integer.ShortType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import mpicbg.imglib.type.numeric.integer.UnsignedIntType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;
import mpicbg.imglib.type.numeric.real.DoubleType;
import mpicbg.imglib.type.numeric.real.FloatType;

public class ImageUtils {
	
	public static int[] getDimsBeyondXY(int[] fullDims)
	{
		if (fullDims.length < 2)
			throw new IllegalArgumentException("Image must be at least 2-D");
		
		int[] extraDims = new int[fullDims.length-2];
		
		for (int i = 0; i < extraDims.length; i++)
			extraDims[i] = fullDims[i+2];
		
		return extraDims;
	}
	
	public static long getTotalSamples(int[] dimensions)
	{
		int numDims = dimensions.length;
		
		if (numDims == 0)
			return 0;
		
		long totalSamples = 1;
		
		for (int i = 0; i < numDims; i++)
			totalSamples *= dimensions[i];
		
		return totalSamples;
	}
	
	public static long getTotalPlanes(int[] dimensions)
	{
		int numDims = dimensions.length;
		
		if (numDims < 2)
			return 0;
		
		// else numDims >= 2
		
		int[] sampleSpace = getDimsBeyondXY(dimensions);
		
		return getTotalSamples(sampleSpace);
	}

	/** return an n-dimensional position array populated from a sample number */
	public static int[] getPosition(int[] dimensions, long sampleNumber)
	{
		int numDims = dimensions.length;
		
		if (numDims == 0)
			throw new IllegalArgumentException("getPosition() passed an empty dimensions array");
		
		long totalSamples = getTotalSamples(dimensions);
		
		if ((sampleNumber < 0) || (sampleNumber >= totalSamples))
			throw new IllegalArgumentException("getPosition() passed a sample number out of range");
		
		int[] position = new int[numDims];
		
		for (int dim = 0; dim < numDims; dim++)
		{
			long multiplier = 1;
			for (int j = dim+1; j < numDims; j++)
				multiplier *= dimensions[j];
			
			int thisDim = 0;
			while (sampleNumber >= multiplier)
			{
				sampleNumber -= multiplier;
				thisDim++;
			}
			position[dim] = thisDim;
		}
		
		return position;
	}

	public static int[] getPlanePosition(int[] dimensions, long planeNumber)
	{
		int numDims = dimensions.length;
		
		if (numDims < 2)
			throw new IllegalArgumentException("getPlanePosition() requires at least a 2-D image");
		
		if (numDims == 2)
		{
			if (planeNumber != 0)
				throw new IllegalArgumentException("getPlanePosition() 2-D image can only have 1 plane");
			
			return new int[]{};  // TODO - this is a little scary to do. might need to throw exception and have other places fix the fact
								//    that we have a rows x cols x 1 image
		}
			
		int[] subDimensions = new int[numDims - 2];
		
		for (int i = 0; i < subDimensions.length; i++)
			subDimensions[i] = dimensions[i+2];
		
		return getPosition(subDimensions,planeNumber);
	}

	public static double[] getPlaneData(Image<? extends RealType> image, int w, int h, int[] planePos) {
		  // TODO - use LocalizablePlaneCursor
			// example in ImageJVirtualStack.extractSliceFloat
			final double[] data = new double[w * h];
			final LocalizableByDimCursor<? extends RealType> cursor = image.createLocalizableByDimCursor();
			final int[] pos = Index.create(0,0,planePos);
			int index = 0;
			for (int y=0; y<h; y++) {
				pos[1] = y;
				for (int x=0; x<w; x++) {
					pos[0] = x;
					cursor.setPosition(pos);
					// TODO: better handling of complex types
					data[index++] = cursor.getType().getRealDouble();
				}
			}
			return data;
		}
		
	// TODO: Can we extract these arrays without case logic? Seems difficult...

	public static byte[] getPlaneBytes(Image<ByteType> im, int w, int h, int[] planePos)
	{
		final byte[] data = new byte[w * h];
		final LocalizableByDimCursor<ByteType> cursor = im.createLocalizableByDimCursor();
		final int[] pos = Index.create(0,0,planePos);
		int index = 0;
		for (int y=0; y<h; y++) {
			pos[1] = y;
			for (int x=0; x<w; x++) {
				pos[0] = x;
				cursor.setPosition(pos);
				data[index++] = cursor.getType().get();
			}
		}
		return data;
	}

	public static byte[] getPlaneUnsignedBytes(Image<UnsignedByteType> im, int w, int h, int[] planePos)
	{
		final byte[] data = new byte[w * h];
		final LocalizableByDimCursor<UnsignedByteType> cursor = im.createLocalizableByDimCursor();
		final int[] pos = Index.create(0,0,planePos);
		int index = 0;
		for (int y=0; y<h; y++) {
			pos[1] = y;
			for (int x=0; x<w; x++) {
				pos[0] = x;
				cursor.setPosition(pos);
				data[index++] = (byte) cursor.getType().get();
			}
		}
		return data;
	}

	public static short[] getPlaneShorts(Image<ShortType> im, int w, int h, int[] planePos)
	{
		final short[] data = new short[w * h];
		final LocalizableByDimCursor<ShortType> cursor = im.createLocalizableByDimCursor();
		final int[] pos = Index.create(0,0,planePos);
		int index = 0;
		for (int y=0; y<h; y++) {
			pos[1] = y;
			for (int x=0; x<w; x++) {
				pos[0] = x;
				cursor.setPosition(pos);
				data[index++] = cursor.getType().get();
			}
		}
		return data;
	}

	public static short[] getPlaneUnsignedShorts(Image<UnsignedShortType> im, int w, int h, int[] planePos)
	{
		final short[] data = new short[w * h];
		final LocalizableByDimCursor<UnsignedShortType> cursor = im.createLocalizableByDimCursor();
		final int[] pos = Index.create(0,0,planePos);
		int index = 0;
		for (int y=0; y<h; y++) {
			pos[1] = y;
			for (int x=0; x<w; x++) {
				pos[0] = x;
				cursor.setPosition(pos);
				data[index++] = (short) cursor.getType().get();
			}
		}
		return data;
	}

	public static int[] getPlaneInts(Image<IntType> im, int w, int h, int[] planePos)
	{
		final int[] data = new int[w * h];
		final LocalizableByDimCursor<IntType> cursor = im.createLocalizableByDimCursor();
		final int[] pos = Index.create(0,0,planePos);
		int index = 0;
		for (int y=0; y<h; y++) {
			pos[1] = y;
			for (int x=0; x<w; x++) {
				pos[0] = x;
				cursor.setPosition(pos);
				data[index++] = cursor.getType().get();
			}
		}
		return data;
	}

	public static int[] getPlaneUnsignedInts(Image<UnsignedIntType> im, int w, int h, int[] planePos)
	{
		final int[] data = new int[w * h];
		final LocalizableByDimCursor<UnsignedIntType> cursor = im.createLocalizableByDimCursor();
		final int[] pos = Index.create(0,0,planePos);
		int index = 0;
		for (int y=0; y<h; y++) {
			pos[1] = y;
			for (int x=0; x<w; x++) {
				pos[0] = x;
				cursor.setPosition(pos);
				data[index++] = (int) cursor.getType().get();
			}
		}
		return data;
	}

	public static long[] getPlaneLongs(Image<LongType> im, int w, int h, int[] planePos)
	{
		final long[] data = new long[w * h];
		final LocalizableByDimCursor<LongType> cursor = im.createLocalizableByDimCursor();
		final int[] pos = Index.create(0,0,planePos);
		int index = 0;
		for (int y=0; y<h; y++) {
			pos[1] = y;
			for (int x=0; x<w; x++) {
				pos[0] = x;
				cursor.setPosition(pos);
				data[index++] = cursor.getType().get();
			}
		}
		return data;
	}

	public static float[] getPlaneFloats(Image<FloatType> im, int w, int h, int[] planePos)
	{
		final float[] data = new float[w * h];
		final LocalizableByDimCursor<FloatType> cursor = im.createLocalizableByDimCursor();
		final int[] pos = Index.create(0,0,planePos);
		int index = 0;
		for (int y=0; y<h; y++) {
			pos[1] = y;
			for (int x=0; x<w; x++) {
				pos[0] = x;
				cursor.setPosition(pos);
				data[index++] = cursor.getType().get();
			}
		}
		return data;
	}

	public static double[] getPlaneDoubles(Image<DoubleType> im, int w, int h, int[] planePos)
	{
		final double[] data = new double[w * h];
		final LocalizableByDimCursor<DoubleType> cursor = im.createLocalizableByDimCursor();
		final int[] pos = Index.create(0,0,planePos);
		int index = 0;
		for (int y=0; y<h; y++) {
			pos[1] = y;
			for (int x=0; x<w; x++) {
				pos[0] = x;
				cursor.setPosition(pos);
				data[index++] = cursor.getType().get();
			}
		}
		return data;
	}
	
	public static Object getPlane(Image<? extends RealType> im, int w, int h, int[] planePos)
	{
		Cursor<? extends RealType> cursor = im.createCursor();

		RealType type = cursor.getType();

		cursor.close();

		if (type instanceof ByteType)
			return getPlaneBytes((Image<ByteType>)im,w,h,planePos);

		if (type instanceof UnsignedByteType)
			return getPlaneUnsignedBytes((Image<UnsignedByteType>)im,w,h,planePos);

		if (type instanceof ShortType)
			return getPlaneShorts((Image<ShortType>)im,w,h,planePos);

		if (type instanceof UnsignedShortType)
			return getPlaneUnsignedShorts((Image<UnsignedShortType>)im,w,h,planePos);

		if (type instanceof IntType)
			return getPlaneInts((Image<IntType>)im,w,h,planePos);

		if (type instanceof UnsignedIntType)
			return getPlaneUnsignedInts((Image<UnsignedIntType>)im,w,h,planePos);

		if (type instanceof FloatType)
			return getPlaneFloats((Image<FloatType>)im,w,h,planePos);

		if (type instanceof DoubleType)
			return getPlaneDoubles((Image<DoubleType>)im,w,h,planePos);

		// TODO - longs and complex types

		throw new IllegalArgumentException("getPlane(): unsupported type - "+type.getClass());
	}
	
	/** copies data from one image to another given origins and dimensional spans */
	public static <K extends ComplexType<K>> void copyFromImageToImage(Image<K> sourceImage, Image<K> destinationImage, int[] sourceDimensionOrigins, int[] destinationDimensionOrigins, int[] dimensionSpans)
	{
		// COPY DATA FROM SOURCE IMAGE TO DEST IMAGE:
		
		// create cursors
		final LocalizableByDimCursor<K> sourceCursor = sourceImage.createLocalizableByDimCursor();
		final LocalizableByDimCursor<K> destinationCursor = destinationImage.createLocalizableByDimCursor();
		final RegionOfInterestCursor<K> sourceROICursor = new RegionOfInterestCursor< K >( sourceCursor, sourceDimensionOrigins, dimensionSpans );
		final RegionOfInterestCursor<K> destinationROICursor = new RegionOfInterestCursor< K >( destinationCursor, destinationDimensionOrigins, dimensionSpans );
			
		//iterate over the target data...
		while( sourceROICursor.hasNext() && destinationROICursor.hasNext() )
		{
			//point cursors to current value
			sourceROICursor.fwd();
			destinationROICursor.fwd();
			
			//get the source value
			double real = sourceROICursor.getType().getPowerDouble();
			double image = sourceROICursor.getType().getPhaseDouble();
			
			//System.out.println("Source values are " + real + " and " + complex );
		
			//set the destination value
			destinationROICursor.getType().setComplexNumber(real, image);
		}
		
		//close the open cursors
		sourceROICursor.close( );
		destinationROICursor.close( );    	
		sourceCursor.close( );
		destinationCursor.close( );
	}
	
}
