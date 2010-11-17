package imagej2.selection;

import imagej2.selection.SelectionFunction;

public class NotSelectionFunction implements SelectionFunction
{
	private SelectionFunction func1;
	
	public NotSelectionFunction(SelectionFunction func1)
	{
		this.func1 = func1;
	}
	
	public boolean include(int[] position, double sample)
	{
		return ! func1.include(position, sample);
	}
}

