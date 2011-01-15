package imagej.ij1bridge;

import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import imagej.data.Type;
import imagej.dataset.Dataset;
import imagej.ij1bridge.process.DatasetProcessor;

public class DatasetIJ1ProcessorFactory implements ProcessorFactory
{
	private Dataset dataset;
	private boolean strictlyCompatible;
	
	public DatasetIJ1ProcessorFactory(Dataset dataset, boolean strictlyCompatible)
	{
		this.dataset = dataset;
		this.strictlyCompatible = strictlyCompatible;
	}
	
	@Override
	public ImageProcessor makeProcessor(int[] planePos)
	{
		Type type = this.dataset.getType();
		
		Dataset subset = this.dataset.getSubset(planePos);
		
		int[] dimensions = subset.getDimensions();
		
		if (dimensions.length != 2)
			throw new IllegalArgumentException("Subset of Dataset must be 2-D (given "+dimensions.length+"-D)");
		
		Object plane = subset.getData();
		
		if (plane == null)
			throw new IllegalArgumentException("IJ1 ImageProcessors require a plane of data to work with. Given null plane.");

		if ((type.getNumBitsData() == 8) && (!type.isFloat()) && (type.isUnsigned()))
		{
			return new ByteProcessor(dimensions[0], dimensions[1], (byte[])plane, null);
		}
		else if ((type.getNumBitsData() == 16) && (!type.isFloat()) && (type.isUnsigned()))
		{
			return new ShortProcessor(dimensions[0], dimensions[1], (short[])plane, null);
		}
		else if ((type.getNumBitsData() == 32) && (type.isFloat()))
		{
			return new FloatProcessor(dimensions[0], dimensions[1], (float[])plane, null);
		}
		else if (!strictlyCompatible)
		{
			return new DatasetProcessor(subset);
		}
		
		throw new IllegalArgumentException("cannot find satisfactory processor type for data type "+type.getName()+
				" (require IJ1 processors only = "+this.strictlyCompatible+")");	
	}

}
