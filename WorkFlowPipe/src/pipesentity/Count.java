package pipesentity;

public class Count {
	
	private Integer count = 0;
	
	public Count()
	{
		
	}
	
	public String getValue()
	{
		return count.toString();
	}
	
	public void incrementCount()
	{
		count++;
	}

}
