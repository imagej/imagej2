package imagej.dataset;

/** this interface is a helper that Dataset implementations use but whose functionality is not exposed to end users */
public interface RecursiveDataset
{
	/** get data as double using partial index starting from axis. i.e. for int[]{1,2,3} and axis 1 return double using {2,3} as index */
	double getDouble(int[] index, int axis);

	/** set data to double using partial index starting from axis. i.e. for int[]{1,2,3} and axis 1 set double using {2,3} as index */
	void setDouble(int[] index, int axis, double value);
	
	/** get data as long using partial index starting from axis. i.e. for int[]{1,2,3} and axis 1 return long using {2,3} as index */
	long getLong(int[] index, int axis);

	/** set data to long using partial index starting from axis. i.e. for int[]{1,2,3} and axis 1 set long using {2,3} as index */
	void setLong(int[] index, int axis, long value);

	/** get subset using partial index starting from axis. i.e. for int[]{1,2,3} and axis 1 give me subset using {2,3} as index */
	Dataset getSubset(int[] partialIndex, int axis);
}

