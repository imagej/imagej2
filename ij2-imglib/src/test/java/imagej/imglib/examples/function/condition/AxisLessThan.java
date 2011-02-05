package imagej.imglib.examples.function.condition;


public class AxisLessThan implements Condition
{
	private int axis;
	private int bound;
	
	public AxisLessThan(int axis, int bound)
	{
		this.axis = axis;
		this.bound = bound;
	}
	
	@Override
	public boolean isSatisfied(int[] position, double value)
	{
		return position[axis] < bound;
	}
}

