package pipesentity;

public class Start {
	
	private Long start;
	
	public Start( long start )
	{
		this.start = new Long(start);
	}
	
	public String getValue()
	{
		return start.toString();
	}

}
