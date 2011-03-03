package imagej.core.plugins;

import imagej.plugin.Plugin;

/**
 * TODO
 * @author Barry DeZonia
 *
 * @param <T>
 */
@Plugin(
		menuPath = "PureIJ2>Process>Sharpen"
)
public class SharpenDataValues extends Neighborhood3x3Operation
{
	public SharpenDataValues()
	{
		setWatcher(new SharpenEdgesWatcher());
	}
	
	private class SharpenEdgesWatcher implements Neighborhood3x3Watcher
	{
		private double sum;
		private double scale;
		private int[] weight;
		private int neighIndex;
		
		@Override
		public void setup()
		{
			weight = new int[9];
			for (int i = 0; i < 9; i++)
				weight[i] = -1;
			weight[4] = 12;

			scale = 0;
			for (int i = 0; i < 9; i++)
				scale += weight[i];
			if (scale == 0)
				scale = 1;
		}

		@Override
		public void initializeNeighborhood(int[] position)
		{
			sum = 0;
			neighIndex = 0;
		}

		@Override
		public void visitLocation(int dx, int dy, double value)
		{
			sum += weight[neighIndex++] * value;
		}

		@Override
		public double calcOutputValue()
		{
			return sum / scale;
		}
	}
}
