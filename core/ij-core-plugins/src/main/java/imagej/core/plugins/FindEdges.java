package imagej.core.plugins;

import imagej.core.plugins.Neighborhood3x3Operation.Neighborhood3x3Watcher;
import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

/**
 * Runs the Find Edges plugin
 * 
 * @author Barry DeZonia
 *
 */
@Plugin(
		menuPath = "PureIJ2>Process>Find Edges"
)
public class FindEdges implements ImageJPlugin
{
	// ***************  instance variables that are Parameters ***************************************************************

	@Parameter
	private Dataset input;
	
	@Parameter(output=true)
	private Dataset output;

	// ***************  constructor ***************************************************************

	/** default constructor */
	public FindEdges()
	{
	}
	
	// ***************  public interface ***************************************************************

	/** sets the output Dataset to the result of the find edges operation */
	@Override
	public void run()
	{
		Neighborhood3x3Operation operation = new Neighborhood3x3Operation(input, new FindEdgesWatcher());
		output = operation.run();
	}
	
	// ***************  private interface ***************************************************************

	private class FindEdgesWatcher implements Neighborhood3x3Watcher
	{
		/** n - contains a local copy of the 9 values of a 3x3 neighborhood */
		private double[] n;
		
		/** create the local neighborhood variables */
		@Override
		public void setup()
		{
			n = new double[9];
		}

		/** at each new neighborhood start tracking neighbor 0 */
		@Override
		public void initializeNeighborhood(int[] position)
		{
			// nothing to do
		}

		/** every time we visit a location within the neighborhood we update our local copy */
		@Override
		public void visitLocation(int dx, int dy, double value)
		{
			int index = (dy+1)*(3) + (dx+1);
			n[index] = value;
		}

		/** calculates the value of a pixel from the input neighborhood. algorithm taken from IJ1. */
		@Override
		public double calcOutputValue()
		{

            double sum1 = n[0] + 2*n[1] + n[2] - n[6] - 2*n[7] - n[8];
            
            double sum2 = n[0] + 2*n[3] + n[6] - n[2] - 2*n[5] - n[8];
            
            return Math.sqrt(sum1*sum1 + sum2*sum2);
		}
	}

}
