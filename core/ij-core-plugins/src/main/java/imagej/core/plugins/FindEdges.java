package imagej.core.plugins;

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
public class FindEdges extends Neighborhood3x3Operation
{
	public FindEdges()
	{
		setWatcher(new FindEdgesWatcher());
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
}
