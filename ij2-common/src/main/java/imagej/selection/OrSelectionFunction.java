package imagej.selection;


public class OrSelectionFunction implements SelectionFunction
{
	private SelectionFunction func1;
	private SelectionFunction func2;
	
	public OrSelectionFunction(SelectionFunction func1, SelectionFunction func2)
	{
		this.func1 = func1;
		this.func2 = func2;
	}
	
	public boolean include(int[] position, double sample)
	{
		return func1.include(position, sample) || func2.include(position, sample);
	}
}

