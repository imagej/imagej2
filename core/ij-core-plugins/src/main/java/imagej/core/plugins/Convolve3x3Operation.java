package imagej.core.plugins;

/**
 * Convolve3x3Operation is used for general 3x3 convolution. It takes a 3x3 kernel as input.
 * It is used by the various Shadow implementations, SharpenDataValues, SmoothDataValues, etc.
 * 
 * @author Barry DeZonia
 *
 * @param <T>
 */
public class Convolve3x3Operation extends Neighborhood3x3Operation
{
	private double[] kernel;
	
	public Convolve3x3Operation(double[] kernel)
	{
		if (kernel.length != 9)
			throw new IllegalArgumentException("kernel must contain nine elements (shaped 3x3)");
		
		this.kernel = kernel;
		
		setWatcher(new ConvolveWatcher());
	}
	
	private class ConvolveWatcher implements Neighborhood3x3Watcher
	{
		private double scale;
		private double sum;
		
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
