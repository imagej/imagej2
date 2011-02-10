package imagej.dataset;

import imagej.data.Type;

import java.util.ArrayList;

/** creates Datasets made of PlanarDatasets and hierarchical CompositeDatasets */
public class PlanarDatasetFactory implements DatasetFactory
{
	// ************ public interface **************************************************

	public PlanarDatasetFactory() {}
	
	@Override
	public Dataset createDataset(Type type, int[] dimensions)
	{
		if (dimensions.length < 2)
			throw new IllegalArgumentException("this implementation cannot support data whose dimension is less than 2");
			
		int[][] dimensionList = calcSubDimensions(dimensions);
		
		return makeDataset(type, dimensionList, 0);
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
	
	private Dataset makeDataset(Type type, int[][] dimensionList, int level)
	{
		int[] currDims = dimensionList[level];
		int currDimsLength = currDims.length;
		
		if (currDimsLength < 2)
			throw new IllegalArgumentException("this implementation cannot support data whose dimension is less than 2");
		else if ( currDimsLength == 2)  // make a concrete dataset
		{
			long numElements =  (long)currDims[0] * currDims[1];
			Object arrayOfData = type.allocateStorageArray(numElements);
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
