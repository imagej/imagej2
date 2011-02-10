package imagej.workflowpipes.pipesentity;

import java.io.Serializable;

public class Favicon implements Serializable {

	private URL url;
	
	public Favicon( URL url )
	{
		this.url = url;
	}
	
	public String getValue()
	{
		return url.getValue();
	}
}
