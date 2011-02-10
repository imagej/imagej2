package imagej.workflowpipes.pipesentity;

import java.io.Serializable;

public class Start implements Serializable {
	
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
