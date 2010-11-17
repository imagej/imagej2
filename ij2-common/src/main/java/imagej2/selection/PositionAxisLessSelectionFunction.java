package imagej2.selection;

public class PositionAxisLessSelectionFunction implements SelectionFunction
{
	private int axisNumber;
	private int value;
	
	public PositionAxisLessSelectionFunction(int axisNumber, int value)
	{
		this.axisNumber = axisNumber;
		this.value = value;
	}

	public boolean include(int[] position, double sample)
	{
		return position[this.axisNumber] < this.value;
	}
}

