package imagej.core.plugins;

import imagej.core.plugins.Neighborhood3x3Operation.Neighborhood3x3Watcher;
import imagej.model.Dataset;

/**
 * Convolve3x3Operation is used for general 3x3 convolution. It takes a 3x3 kernel as input.
 * Kernel is actually stored as a 1-D array such that {0,1,2,3,4,5,6,7,8} implies this shape:
 * {{0,1,2},{3,4,5},{6,7,8}}. This class is used by the various Shadow implementations,
 * SharpenDataValues, SmoothDataValues, etc.
 * 
 * @author Barry DeZonia
 *
 */
public class Convolve3x3Operation
{
	// ***************  instance variables ***************************************************************

	/** the kernel to convolve an input Dataset by */
	private double[] kernel;
	
	/** the 3x3 operation that will run on the input Dataset and call back this class as needed */
	private Neighborhood3x3Operation operation; 
	
	// ***************  constructor ***************************************************************

	/** constructor. takes an input Dataset and a kernel that will be used to calculate data values. */
	public Convolve3x3Operation(Dataset input, double[] kernel)
	{
		this.kernel = kernel;
		this.operation = new Neighborhood3x3Operation(input, new ConvolveWatcher());
		
		if (kernel.length != 9)
			throw new IllegalArgumentException("kernel must contain nine elements (shaped 3x3)");
	}
	
	// ***************  public interface ***************************************************************

	/** runs the convolution and returns the output Dataset containing the convolved values */
	public Dataset run()
	{
		return operation.run();
	}
	
	// ***************  private interface ***************************************************************

	/** ConvolveWatcher is where the actual convolution value of one output pixel is calculated. The watcher is called from
	 * Neighborhood3x3Operation visiting each pixel in the input image (and all its immediate neighbors) once. ConvolveWatcher
	 * tallies that information and returns apprpriate values as necessary. */
	private class ConvolveWatcher implements Neighborhood3x3Watcher
	{
		private double scale;
		private double sum;
		
		public ConvolveWatcher()
		{
		}

		/** precalculates the kernel scale for use later */
		@Override
		public void setup()
		{
			scale = 0;
    		for (int i=0; i<kernel.length; i++)
    			scale += kernel[i];
    		if (scale == 0)
    			scale = 1;
		}

		/** at each new neighborhood reset it's value sum to 0 */
		@Override
		public void initializeNeighborhood(int[] position)
		{
			sum = 0;
		}

		/** for each pixel visited in the 3x3 neighborhood add the kernel scaled value */
		@Override
		public void visitLocation(int dx, int dy, double value)
		{
			int index = (dy+1)*(3) + (dx+1);
			sum += value * kernel[index];
		}

		/** called after all pixels in neighborhood visited - divide the sum by the kernel scale */
		@Override
		public double calcOutputValue() 
		{
			return sum / scale;
		}
		
	}
}
