package imagej.workflowpipes.pipesentity;

import java.io.Serializable;

public class URL implements Serializable {

	private String url;
	
	public URL ( String url )
	{
		this.url = url;
	}
	
	public String getValue()
	{
		return url;
	}
}
