package imagej2.selection;

// TODO : could build up from other pieces. Makes for funky subregions. Maybe not necessary. Wait.
public class SubregionSelectionFunction implements SelectionFunction
{
	private int[] origin, span;
	private boolean neverCalled;
	
	public SubregionSelectionFunction(int[] origin, int[] span)
	{
		this.origin = origin.clone();
		this.span = span.clone();
		
		if (origin.length != span.length)
			throw new IllegalArgumentException("origin and span have incompatible lengths");
		
		this.neverCalled = true;
	}
	
	public boolean include(int[] position, double sample)
	{
		if (neverCalled)
		{
			if (origin.length != position.length)
				throw new IllegalArgumentException("position does not have length compatible with subregion size");
			
			neverCalled = false;
		}
		
		for (int i = 0; i < origin.length; i++)
		{
			int minIndex = origin[i];
			
			int maxIndex = minIndex + span[i] - 1;
			
			int axisPosition = position[i];
			
			if ((axisPosition < minIndex) || (axisPosition > maxIndex))
				return false;
		}
		
		return true;
	}
}

