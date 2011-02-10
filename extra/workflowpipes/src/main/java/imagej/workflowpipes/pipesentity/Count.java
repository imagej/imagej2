package imagej.workflowpipes.pipesentity;

import java.io.Serializable;

public class Count implements Serializable {
	
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
