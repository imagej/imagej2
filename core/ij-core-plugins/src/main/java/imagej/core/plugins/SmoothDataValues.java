package imagej.core.plugins;

import imagej.plugin.Plugin;

/**
 * TODO
 * @author Barry DeZonia
 *
 * @param <T>
 */
@Plugin(
		menuPath = "PureIJ2>Process>Smooth"
)
public class SmoothDataValues extends Neighborhood3x3Operation
{
	public SmoothDataValues()
	{
		setWatcher(new SmoothEdgesWatcher());
	}
	
	private class SmoothEdgesWatcher implements Neighborhood3x3Watcher
	{
		private double sum;
		
		@Override
		public void setup()
		{
		}

		@Override
		public void initializeNeighborhood(int[] position)
		{
			sum = 0;
		}

		@Override
		public void visitLocation(int dx, int dy, double value)
		{
			sum += value;
		}

		@Override
		public double calcOutputValue()
		{
			return sum / 9;
		}
	}
}
