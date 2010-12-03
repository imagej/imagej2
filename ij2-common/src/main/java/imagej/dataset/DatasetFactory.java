package imagej.dataset;

import imagej.DataType;

/** the interface that all Dataset factories must implement for creation and duplication of data */
public interface DatasetFactory
{
	/** creates a Dataset according to own layout style of given type and dimensions */ 
	Dataset createDataset(DataType type, int[] dimensions);
	
	/** creates a Dataset according to own layout style but whose shape and data values are copied from a given Dataset */ 
	Dataset duplicateDataset(Dataset dataset);
}
