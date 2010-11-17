package imagej2.selection;

public class PositionAxisLessEqualsSelectionFunction implements SelectionFunction
{
	private SelectionFunction lessEquals;
	
	public PositionAxisLessEqualsSelectionFunction(int axisNumber, int value)
	{
		this.lessEquals = new NotSelectionFunction(new PositionAxisGreaterSelectionFunction(axisNumber,value));
	}

	public boolean include(int[] position, double sample)
	{
		return this.lessEquals.include(position, sample);
	}
}

