package imagej.selection;

public class ValueLessThanSelectionFunction implements SelectionFunction
{
	private double threshold;
	
	public ValueLessThanSelectionFunction(double threshold)
	{
		this.threshold = threshold;
	}
	
	public boolean include(int[] position, double sample)
	{
		return sample < this.threshold;
	}
}

