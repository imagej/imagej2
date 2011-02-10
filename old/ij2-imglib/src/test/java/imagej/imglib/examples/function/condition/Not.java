package imagej.imglib.examples.function.condition;

import mpicbg.imglib.cursor.LocalizableCursor;
import mpicbg.imglib.type.numeric.RealType;

public class Not<T extends RealType<T>> implements Condition<T>
{
	private Condition<T> condition;
	
	public Not(Condition<T> condition)
	{
		this.condition = condition;
	}
	
	@Override
	public boolean isSatisfied(LocalizableCursor<T> cursor, int[] position)
	{
		return ! condition.isSatisfied(cursor, position); 
	}
	
}
