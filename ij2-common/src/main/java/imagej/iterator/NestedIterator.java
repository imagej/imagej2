package imagej.iterator;

import imagej.dataset.Dataset;

/** this class does an iteration that is nested like multiple for loops */
public class NestedIterator
{
	// ************ instance variables *********************************************************************************

	private double[] doubleWorkspace;
	private long[] longWorkspace;
	
	// ************ constructor *********************************************************************************

	public NestedIterator(Dataset[] datasets, int[][] origins, int[][] spans, boolean workingInFloats)
	{
		if (workingInFloats)
			this.doubleWorkspace = new double[datasets.length];
		else
			this.longWorkspace = new long[datasets.length];
	}
	
	// ************ public interface *********************************************************************************

	public boolean positionValid()
	{
		// TODO
		return false;
	}
	
	public void loadWorkspace()
	{
		// TODO
	}

	public void incrementPosition()
	{
		// TODO
	}

	public void setLong(int datasetNumber, long value)
	{
		// TODO
	}
	
	public void setDouble(int datasetNumber, double value)
	{
		// TODO
	}

	public long[] getLongWorkspace()
	{
		return this.longWorkspace;
	}

	public double[] getDoubleWorkspace()
	{
		return this.doubleWorkspace;
	}
}
