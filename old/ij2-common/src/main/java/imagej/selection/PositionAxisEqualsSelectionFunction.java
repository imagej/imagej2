package imagej.selection;

public class PositionAxisEqualsSelectionFunction implements SelectionFunction
{
	private int axisNumber;
	private int value;
	
	public PositionAxisEqualsSelectionFunction(int axisNumber, int value)
	{
		this.axisNumber = axisNumber;
		this.value = value;
	}

	public boolean include(int[] position, double sample)
	{
		return position[this.axisNumber] == this.value;
	}
}

