package imagej.dataset;

import imagej.data.Type;

/** the interface that all Dataset factories must implement for creation of data */
public interface DatasetFactory
{
	/** creates a Dataset according to own layout style of given type and dimensions */ 
	Dataset createDataset(Type type, int[] dimensions);
}
