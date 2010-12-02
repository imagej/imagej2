package imagej.dataset;

import imagej.EncodingManager;
import imagej.UserType;

import java.util.ArrayList;

/** creates Datasets made of PlanarDatasets and hierarchical CompositeDatasets */
public class PlanarDatasetFactory implements DatasetFactory
{
	// ************ public interface **************************************************

	public PlanarDatasetFactory() {}
	
	@Override
	public Dataset createDataset(UserType type, int[] dimensions)
	{
		int[][] dimensionList = calcSubDimensions(dimensions);
		
		return makeDataset(type, dimensionList, 0);
	}
	
	// TODO - do something here. Maybe have duplicate() be a method of Dataset.
	@Override
	public Dataset duplicateDataset(Dataset dataset)
	{
		throw new IllegalStateException("unimplemented");
	}

	// ************ private interface **************************************************

	private int[][] calcSubDimensions(int[] dimensions)
	{
		int[][] dimensionList = new int[dimensions.length][];
		
		for (int i = 0; i < dimensions.length; i++)
		{
			int[] subDimension = new int[dimensions.length-i];
			for (int j = 0; j < subDimension.length; j++)
				subDimension[j] = dimensions[j+i];
			dimensionList[i] = subDimension;
		}
		
		return dimensionList;
	}
	
	private Dataset makeDataset(UserType type, int[][] dimensionList, int level)
	{
		int[] currDims = dimensionList[level];;
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
			ArrayList<Dataset> subsets = new ArrayList<Dataset>();
			for (int i = 0; i < currDims[0]; i++)
			{
				Dataset subset = makeDataset(type, dimensionList, level+1);
				subsets.add(subset);
			}
			return new CompositeDataset(type, currDims, subsets);
		}
	}
}
