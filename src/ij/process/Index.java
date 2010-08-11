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
		int[] values = new int[planePosition.length + 2];
		values[0] = x;
		values[1] = y;
		for (int i = 2; i < values.length; i++)
			values[i] = planePosition[i-2];
		return values;
	}
}

