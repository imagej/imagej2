package imagej.dataset;

import imagej.EncodingManager;
import imagej.DataType;
import imagej.process.Index;

import java.util.ArrayList;

/** creates Datasets made of PlanarDatasets and hierarchical CompositeDatasets */
public class PlanarDatasetFactory implements DatasetFactory
{
	// ************ public interface **************************************************

	public PlanarDatasetFactory() {}
	
	@Override
	public Dataset createDataset(DataType type, int[] dimensions)
	{
		if (dimensions.length < 2)
			throw new IllegalArgumentException("this implementation cannot support data whose dimension is less than 2");
			
		int[][] dimensionList = calcSubDimensions(dimensions);
		
		return makeDataset(type, dimensionList, 0);
	}
	
	@Override
	public Dataset duplicateDataset(Dataset dataset)
	{
		DataType type = dataset.getType();
		
		int[] dimensions = dataset.getDimensions();
		
		Dataset newDataset = createDataset(type, dimensions);
		
		int[] position = Index.create(dimensions.length);
		int[] origin = Index.create(dimensions.length);
		int[] span = dimensions;

		// TODO - there are should be faster ways of doing this. do this way for now
		
		while (Index.isValid(position, origin, span))
		{
			double value = dataset.getDouble(position);
			newDataset.setDouble(position, value);
			Index.increment(position, origin, span);
		}
		
		newDataset.setMetaData(dataset.getMetaData());  // TODO - PROBABLY NEED TO CLONE THE METADATA HERE!!!!!!!!!!!
		
		return newDataset;
	}

	// ************ private interface **************************************************

	private int[][] calcSubDimensions(int[] dimensions)
	{
		int[][] dimensionList = new int[dimensions.length][];
		
		for (int i = 0; i < dimensions.length; i++)
		{
			int[] subDimension = new int[dimensions.length-i];
			for (int j = 0; j < subDimension.length; j++)
				subDimension[j] = dimensions[j];
			dimensionList[i] = subDimension;
		}
		
		return dimensionList;
	}
	
	private Dataset makeDataset(DataType type, int[][] dimensionList, int level)
	{
		int[] currDims = dimensionList[level];
		int currDimsLength = currDims.length;
		
		if (currDimsLength < 2)
			throw new IllegalArgumentException("this implementation cannot support data whose dimension is less than 2");
		else if ( currDimsLength == 2)  // make a concrete dataset
		{
			int numElements = (int) ((long)currDims[0] * currDims[1]);
			Object arrayOfData = EncodingManager.allocateCompatibleArray(type, numElements);
			return new PlanarDataset(currDims, type, arrayOfData);
		}
		else  // make a composite dataset
		{
			int thisAxisSize = currDims[currDims.length-1];
			ArrayList<Dataset> subsets = new ArrayList<Dataset>(thisAxisSize);
			for (int i = 0; i < thisAxisSize; i++)
			{
				Dataset subset = makeDataset(type, dimensionList, level+1);
				subsets.add(i, subset);
			}
			return new CompositeDataset(type, currDims, subsets);
		}
	}
}
