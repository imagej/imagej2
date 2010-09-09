package ij.process;

public class Index {
	
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
}

