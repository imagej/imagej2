package imagej.selection;

public class ValueLessThanOrEqualsSelectionFunction implements SelectionFunction
{
	private SelectionFunction lessOrEquals;
	
	public ValueLessThanOrEqualsSelectionFunction(double value)
	{
		this.lessOrEquals = new NotSelectionFunction(new ValueGreaterThanSelectionFunction(value));
	}

	public boolean include(int[] position, double sample)
	{
		return lessOrEquals.include(position, sample);
	}
}

