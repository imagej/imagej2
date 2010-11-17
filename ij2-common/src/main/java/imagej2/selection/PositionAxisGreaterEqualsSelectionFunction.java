package imagej2.selection;

public class PositionAxisGreaterEqualsSelectionFunction implements SelectionFunction
{
	private SelectionFunction greaterEquals;
	
	public PositionAxisGreaterEqualsSelectionFunction(int axisNumber, int value)
	{
		this.greaterEquals = new NotSelectionFunction(new PositionAxisLessSelectionFunction(axisNumber,value));
	}

	public boolean include(int[] position, double sample)
	{
		return this.greaterEquals.include(position, sample);
	}
}

