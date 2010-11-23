package imagej.selection;

public class ValueGreaterThanOrEqualsSelectionFunction implements SelectionFunction
{
	private SelectionFunction greaterOrEquals;
	
	public ValueGreaterThanOrEqualsSelectionFunction(double value)
	{
		this.greaterOrEquals = new NotSelectionFunction(new ValueLessThanSelectionFunction(value));
	}

	public boolean include(int[] position, double sample)
	{
		return greaterOrEquals.include(position, sample);
	}
}

