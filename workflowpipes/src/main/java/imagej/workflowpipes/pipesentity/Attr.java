package imagej.workflowpipes.pipesentity;

import java.io.Serializable;
import org.json.JSONObject;

/**
 * Maps to pipes entity _attr
 * @author rick
 *
 */
public class Attr implements Serializable {

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
