package imagej.selection;

/** a SelectionFunction that returns true for any inclusion test. Note that when using SelectionFunctions with iterators: to obtain the best performance
 *  choose setSelectionFunction(null) for the operation rather than use this type of SelectionFunction. This function present for general use elsewhere
 *  in ImageJ.
 */
public class SelectAllSelectionFunction implements SelectionFunction
{
	public boolean include(int[] position, double sample)
	{
		return true;
	}
}
