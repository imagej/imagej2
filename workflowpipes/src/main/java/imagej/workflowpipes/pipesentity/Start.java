package imagej.workflowpipes.pipesentity;

public class Start {
	
	private Long start;
	
	public Start( long start )
	{
		this.start = new Long(start);
	}
	
	public long getValue()
	{
		return start;
	}

}
