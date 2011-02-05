package imagej.imglib.examples.function.condition;


public class ValueLessThan implements Condition
{
	private double bound;
	
	public ValueLessThan(double bound)
	{
		this.bound = bound;
	}
	
	@Override
	public boolean isSatisfied(int[] position, double value)
	{
		return value < bound;
	}
}
