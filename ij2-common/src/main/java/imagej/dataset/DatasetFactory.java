package imagej.dataset;

import imagej.DataType;

/** the interface that all Dataset factories must implement for creation of data */
public interface DatasetFactory
{
	/** creates a Dataset according to own layout style of given type and dimensions */ 
	Dataset createDataset(DataType type, int[] dimensions);
}
