package imagej.imglib.examples.function.condition;

import mpicbg.imglib.cursor.LocalizableCursor;
import mpicbg.imglib.type.numeric.RealType;


public class AxisLessThan<T extends RealType<T>> implements Condition<T>
{
	private int axis;
	private int bound;
	
	public AxisLessThan(int numAxes, int axis, int bound)
	{
		this.axis = axis;
		this.bound = bound;
	}
	
	@Override
	public boolean isSatisfied(LocalizableCursor<T> cursor, int[] position)
	{
		return position[axis] < bound;
	}
}

