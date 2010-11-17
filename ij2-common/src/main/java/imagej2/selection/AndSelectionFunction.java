package imagej2.selection;

import imagej2.selection.SelectionFunction;

public class AndSelectionFunction implements SelectionFunction
{
	private SelectionFunction func1;
	private SelectionFunction func2;
	
	public AndSelectionFunction(SelectionFunction func1, SelectionFunction func2)
	{
		this.func1 = func1;
		this.func2 = func2;
	}
	
	public boolean include(int[] position, double sample)
	{
		return func1.include(position, sample) && func2.include(position, sample);
	}
}

