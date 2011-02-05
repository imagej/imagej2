package imagej.imglib.examples.function.condition;

public class Not implements Condition
{
	private Condition condition;
	
	public Not(Condition condition)
	{
		this.condition = condition;
	}
	
	@Override
	public boolean isSatisfied(int[] position, double value)
	{
		return ! condition.isSatisfied(position, value); 
	}
	
}
