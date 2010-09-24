package imagej.selection;

public class PositionAxisGreaterSelectionFunction implements SelectionFunction
{
	private int axisNumber;
	private int value;
	
	public PositionAxisGreaterSelectionFunction(int axisNumber, int value)
	{
		this.axisNumber = axisNumber;
		this.value = value;
	}

	public boolean include(int[] position, double sample)
	{
		return position[this.axisNumber] > this.value;
	}
}

