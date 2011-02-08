package imagej.imglib.examples.function.condition;

import mpicbg.imglib.cursor.LocalizableCursor;
import mpicbg.imglib.type.numeric.RealType;

public class Or<T extends RealType<T>> implements Condition<T>
{
	private Condition<T> condition1, condition2;
	
	public Or(Condition<T> condition1, Condition<T> condition2)
	{
		this.condition1 = condition1;
		this.condition2 = condition2;
	}
	
	@Override
	public boolean isSatisfied(LocalizableCursor<T> cursor, int[] position)
	{
		return condition1.isSatisfied(cursor, position) || condition2.isSatisfied(cursor, position); 
	}
}
