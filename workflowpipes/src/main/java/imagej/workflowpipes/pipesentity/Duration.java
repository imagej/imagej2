package imagej.workflowpipes.pipesentity;

import java.io.Serializable;

public class Duration implements Serializable {
    
	private double duration;
	
	public Duration( double duration )
	{
		this.duration = duration;
	}
	
	public String getValue()
	{
		return new Double(duration).toString();
	}
}
