package imagej.selection;

public class ValueGreaterThanSelectionFunction implements SelectionFunction
{
	private double threshold;
	
	public ValueGreaterThanSelectionFunction(double threshold)
	{
		this.threshold = threshold;
	}
	
	public boolean include(int[] position, double sample)
	{
		return sample > this.threshold;
	}
}

