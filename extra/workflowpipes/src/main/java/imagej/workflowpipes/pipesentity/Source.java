package imagej.workflowpipes.pipesentity;

import java.io.Serializable;

public class Source implements Serializable {
	
	private String source;
	
	public Source( String source )
	{
		this.source = source;
	}
	
	public String getSource()
	{
		return this.source;
	}

}
