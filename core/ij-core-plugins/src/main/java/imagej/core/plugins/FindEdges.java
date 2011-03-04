package imagej.core.plugins;

import imagej.core.plugins.Neighborhood3x3Operation.Neighborhood3x3Watcher;
import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

/**
 * TODO
 * @author Barry DeZonia
 *
 * @param <T>
 */
@Plugin(
		menuPath = "PureIJ2>Process>Find Edges"
)
public class FindEdges implements ImageJPlugin
{
	@Parameter
	private Dataset input;
	
	@Parameter(output=true)
	private Dataset output;

	public FindEdges()
	{
	}
	
	private class FindEdgesWatcher implements Neighborhood3x3Watcher
	{
		private double[] n;
		private int neighIndex;
		
		@Override
		public void setup()
		{
			n = new double[9];
		}

		@Override
		public void initializeNeighborhood(int[] position)
		{
			neighIndex = 0;
		}

		@Override
		public void visitLocation(int dx, int dy, double value)
		{
			n[neighIndex++] = value;
		}

		@Override
		public double calcOutputValue()
		{

            double sum1 = n[0] + 2*n[1] + n[2] - n[6] - 2*n[7] - n[8];
            
            double sum2 = n[0] + 2*n[3] + n[6] - n[2] - 2*n[5] - n[8];
            
            return Math.sqrt(sum1*sum1 + sum2*sum2);
		}
	}

	@Override
	public void run()
	{
		Neighborhood3x3Operation operation = new Neighborhood3x3Operation(input, new FindEdgesWatcher());
		output = operation.run();
	}
}
