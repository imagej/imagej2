package imagej2.selection;

public class SelectAllSelectionFunction implements SelectionFunction
{
	public boolean include(int[] position, double sample)
	{
		return true;
	}
}
