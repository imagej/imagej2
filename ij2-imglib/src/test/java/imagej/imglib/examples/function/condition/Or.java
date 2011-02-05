package imagej.imglib.examples.function.condition;

public class Or implements Condition
{
	private Condition condition1, condition2;
	
	public Or(Condition condition1, Condition condition2)
	{
		this.condition1 = condition1;
		this.condition2 = condition2;
	}
	
	@Override
	public boolean isSatisfied(int[] position, double value)
	{
		return condition1.isSatisfied(position, value) || condition2.isSatisfied(position, value); 
	}
}
