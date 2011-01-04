package imagej.selection;


public class NotSelectionFunction implements SelectionFunction
{
	private SelectionFunction func1;
	
	public NotSelectionFunction(SelectionFunction func1)
	{
		this.func1 = func1;
	}
	
	public boolean include(int[] position, double sample)
	{
		return ! this.func1.include(position, sample);
	}
}

