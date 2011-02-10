package imagej.selection;

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
		if (this.neverCalled)
		{
			if (this.origin.length != position.length)
				throw new IllegalArgumentException("position does not have length compatible with subregion size");
			
			this.neverCalled = false;
		}
		
		for (int i = 0; i < this.origin.length; i++)
		{
			int minIndex = this.origin[i];
			
			int maxIndex = minIndex + this.span[i] - 1;
			
			int axisPosition = position[i];
			
			if ((axisPosition < minIndex) || (axisPosition > maxIndex))
				return false;
		}
		
		return true;
	}
}

