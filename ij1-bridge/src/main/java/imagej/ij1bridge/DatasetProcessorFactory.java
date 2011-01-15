package imagej.ij1bridge;

import ij.process.ImageProcessor;
import imagej.dataset.Dataset;
import imagej.ij1bridge.process.DatasetProcessor;

public class DatasetProcessorFactory implements ProcessorFactory
{
	private Dataset dataset;
	
	public DatasetProcessorFactory(Dataset dataset)
	{
		this.dataset = dataset;
	}
	
	@Override
	public ImageProcessor makeProcessor(int[] planePos)
	{
		Dataset planarSubset = this.dataset.getSubset(planePos);
		
		return new DatasetProcessor(planarSubset);
	}
}
