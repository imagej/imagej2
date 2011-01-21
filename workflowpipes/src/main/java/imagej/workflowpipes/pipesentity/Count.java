package imagej.workflowpipes.pipesentity;

public class Count {
	
	private Integer count = 0;
	
	public Count( int count ) {
		this.count = count;
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
