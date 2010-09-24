package imagej.selection;

public class ModulusSelectionFunction implements SelectionFunction
{
	private int axisNumber;
	private int range;
	private int value;
	private boolean neverCalled;
	
	public ModulusSelectionFunction(int axisNumber, int range, int value)
	{
		this.axisNumber = axisNumber;
		this.range = range;
		this.value = value;

		if (range <= 0)
			throw new IllegalArgumentException();
		
		if ((value < 0) || value >= range)
			throw new IllegalArgumentException();
		
		this.neverCalled = true;
	}

	public boolean include(int[] position, double sample)
	{
		if (neverCalled)
		{
			if (axisNumber > position.length)
				throw new IllegalArgumentException();

			neverCalled = false;
		}
		
		if (position[axisNumber] % this.range == this.value)
			return true;
		
		return false;
	}
}

