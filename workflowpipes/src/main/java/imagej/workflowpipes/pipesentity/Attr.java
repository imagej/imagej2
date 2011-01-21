package imagej.workflowpipes.pipesentity;

import org.json.JSONObject;

/**
 * Maps to pipes entity _attr
 * @author rick
 *
 */
public class Attr {

	private Content content;

	public Attr( Content content )
	{
		this.content = content;
	}
	
	public Object getJSON() 
	{
		JSONObject json = new JSONObject();
		
		json.put("content", content.getJSON() );
		
		return json;
	}

}
