package imagej.core.plugins;

import imagej.core.plugins.Neighborhood3x3Operation.Neighborhood3x3Watcher;
import imagej.model.Dataset;

/**
 * Convolve3x3Operation is used for general 3x3 convolution. It takes a 3x3 kernel as input.
 * It is used by the various Shadow implementations, SharpenDataValues, SmoothDataValues, etc.
 * 
 * @author Barry DeZonia
 *
 * @param <T>
 */
public class Convolve3x3Operation
{
	private Dataset input;
	private double[] kernel;
	private Neighborhood3x3Operation operation; 
	
	public Convolve3x3Operation(Dataset input, double[] kernel)
	{
		this.input = input;
		this.kernel = kernel;
		this.operation = new Neighborhood3x3Operation(input, new ConvolveWatcher());
		
		if (kernel.length != 9)
			throw new IllegalArgumentException("kernel must contain nine elements (shaped 3x3)");
	}
	
	public Dataset run()
	{
		return operation.run();
	}
	
	private class ConvolveWatcher implements Neighborhood3x3Watcher
	{
		private double scale;
		private double sum;
		
		public ConvolveWatcher()
		{
		}
		
		@Override
		public void setup()
		{
			scale = 0;
    		for (int i=0; i<kernel.length; i++)
    			scale += kernel[i];
    		if (scale == 0)
    			scale = 1;
		}

		@Override
		public void initializeNeighborhood(int[] position)
		{
			sum = 0;
		}

		@Override
		public void visitLocation(int dx, int dy, double value)
		{
			int index = (dy+1)*(3) + (dx+1);
			sum += value * kernel[index];
		}

		@Override
		public double calcOutputValue() 
		{
			return sum / scale;
		}
		
	}
}
