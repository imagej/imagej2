package imagej.process;

public final class Index {

	private Index() {
	  // NB: Prevent instantiation of utility class.
	}

	/** create an index array of length numDims initialized to zeroes */
	public static int[] create(int numDims)
	{
		return new int[numDims];
	}
	
	/** create an index array initialized to passed in values */
	public static int[] create(int[] initialValues)
	{
		return initialValues.clone();
	}
	
	/** create an index array setting the first 2 dims to x & y and the remaining dims populated with passed in values */
	public static int[] create(int x, int y, int[] planePosition)
	{
		if (x < 0)
			throw new IllegalArgumentException("x value must be >= 0");
		
		if (y < 0)
			throw new IllegalArgumentException("y value must be >= 0");

		int[] values = new int[planePosition.length + 2];
		
		values[0] = x;
		values[1] = y;
		
		for (int i = 2; i < values.length; i++)
			values[i] = planePosition[i-2];
		
		return values;
	}
	
	public static boolean isValid(int[] position, int[] origin, int[] span)
	{
		for (int i = 0; i < position.length; i++)
		{
			if (position[i] < origin[i])
				return false;
			
			if (position[i] >= (origin[i] + span[i]))
				return false;
		}
		
		return true;
	}
	
	// incrementing from left to right : not textbook but hacky way to get ImgLibProcessor::duplicate() working 
	public static void increment(int[] position, int[] origin, int[] span)
	{
		int i = 0;

		position[i]++;
			
		// if we're beyond end of this dimension
		while (position[i] >= (origin[i] + span[i]))
		{
			// if this dim is the last then we've gone as far as we can go
			if (i == position.length-1)
			{
				// return a value that isValid() will complain about
				for (int j = 0; j < position.length; j++)
					position[j] = origin[j] + span[j];
				return;
			}
			
			// otherwise set our dim to its origin value and increment the dimension to our right
			position[i] = origin[i];
			position[i+1]++;
			i++;
		}		
	}
	
	/*
	// incrementing from right to left
	public static void increment(int[] position, int[] origin, int[] span)
	{
		int i = position.length - 1;

		position[i]++;
			
		// if we're beyond end of this dimension
		while (position[i] >= (origin[i] + span[i]))
		{
			// if this dim is the first then we've gone as far as we can go
			if (i == 0)
			{
				// return a value that isValid() will complain about
				for (int j = 0; j < position.length; j++)
					position[j] = origin[j] + span[j];
				return;
			}
			
			// otherwise set our dim to its origin value and increment the dimension to our left
			position[i] = origin[i];
			position[i-1]++;
			i--;
		}		
	}
	*/

	// TODO - not thrilled with raster nomenclature. Make more intuitive.
	
	/**
	 * Computes a unique 1-D index corresponding to the given multidimensional
	 * position.
	 * 
	 * @param lengths the maximum value for each positional dimension
	 * @param pos position along each dimensional axis
	 * @return rasterized index value
	 */
	public static int positionToRaster(int[] lengths, int[] pos) {
		int offset = 1;
		int raster = 0;
		for (int i = 0; i < pos.length; i++) {
			raster += offset * pos[i];
			offset *= lengths[i];
		}
		return raster;
	}

	/**
	 * Computes a unique N-D position corresponding to the given rasterized index
	 * value.
	 * 
	 * @param lengths the maximum value at each positional dimension
	 * @param raster rasterized index value
	 * @return position along each dimensional axis
	 */
	public static int[] rasterToPosition(int[] lengths, int raster) {
		return rasterToPosition(lengths, raster, new int[lengths.length]);
	}

	/**
	 * Computes a unique N-D position corresponding to the given rasterized index
	 * value.
	 * 
	 * @param lengths the maximum value at each positional dimension
	 * @param raster rasterized index value
	 * @param pos preallocated position array to populate with the result
	 * @return position along each dimensional axis
	 */
	public static int[] rasterToPosition(int[] lengths, int raster, int[] pos) {
		int offset = 1;
		for (int i = 0; i < pos.length; i++) {
			int offset1 = offset * lengths[i];
			int q = i < pos.length - 1 ? raster % offset1 : raster;
			pos[i] = q / offset;
			raster -= q;
			offset = offset1;
		}
		return pos;
	}

	/**
	 * Computes the number of raster values for a positional array with the given
	 * lengths.
	 */
	public static int getRasterLength(int[] lengths) {
		int len = 1;
		for (int i = 0; i < lengths.length; i++)
			len *= lengths[i];
		return len;
	}

	public static long getSampleNumber(int[] dimensions, int[] indexValue)
	{
		// TODO - make positionToRaster return a long
		
		return Index.positionToRaster(dimensions, indexValue);
	}

	public static int[] getPlanePosition(int[] dimensions, long planeNumber)
	{
		if ((planeNumber < 0) || (planeNumber >= ImageUtils.getTotalPlanes(dimensions)))
			throw new IllegalArgumentException("invalid plane number given");
		
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
			
		int[] planeDim = new int[dimensions.length-2];
		
		for (int i = 0; i < planeDim.length; i++)
			planeDim[i] = dimensions[i+2];
		
		int[] position = new int[planeDim.length];
		
		Index.rasterToPosition(planeDim, (int)planeNumber, position);
		
		return position;
	}

}
