package imagej2.selection;

public class PositionEqualsSelectionFunction implements SelectionFunction
{
	private int[] targetPosition;
	
	public PositionEqualsSelectionFunction(int[] position)
	{
		this.targetPosition = position;
	}

	public boolean include(int[] position, double sample)
	{
		return position.equals(this.targetPosition);
	}
}

