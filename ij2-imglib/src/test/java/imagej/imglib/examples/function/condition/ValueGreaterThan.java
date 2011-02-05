package imagej.imglib.examples.function.condition;


public class ValueGreaterThan implements Condition
{
	private double bound;
	
	public ValueGreaterThan(double bound)
	{
		this.bound = bound;
	}
	
	@Override
	public boolean isSatisfied(int[] position, double value)
	{
		return value > bound;
	}
}

