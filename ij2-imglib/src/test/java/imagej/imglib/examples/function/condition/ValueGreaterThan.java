package imagej.imglib.examples.function.condition;

import mpicbg.imglib.cursor.LocalizableCursor;
import mpicbg.imglib.type.numeric.RealType;


public class ValueGreaterThan<T extends RealType<T>> implements Condition<T>
{
	private double bound;
	
	public ValueGreaterThan(double bound)
	{
		this.bound = bound;
	}
	
	@Override
	public boolean isSatisfied(LocalizableCursor<T> cursor, int[] position)
	{
		return cursor.getType().getRealDouble() > bound;
	}
}

