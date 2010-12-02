package imagej.dataset;


public interface RecursiveDataset
{
	double getDouble(int[] index, int axis);
	void setDouble(int[] index, int axis, double value);
	Dataset getSubset(int[] partialIndex, int axis);
}

