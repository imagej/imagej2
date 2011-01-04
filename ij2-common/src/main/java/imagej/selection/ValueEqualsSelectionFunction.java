package imagej.selection;


public class ValueEqualsSelectionFunction implements SelectionFunction
{
	private static final double TOL = 0.0000000001;
	private double value;
	
	public ValueEqualsSelectionFunction(double value)
	{
		this.value = value;
	}
	
	public boolean include(int[] position, double sample)
	{
		return Math.abs(sample - this.value) < TOL;
	}
}

