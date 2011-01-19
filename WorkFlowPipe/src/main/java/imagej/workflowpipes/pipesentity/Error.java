package imagej.workflowpipes.pipesentity;

import org.json.JSONObject;

/**
 * Relates to the pipes type Errors
 * @author rick
 *
 */
public class Error {

	private Type type;
	private Message message;
	
	public Error( Type type, Message message )
	{
		this.type = type;
		this.message = message;
	}

	public Type getType() {
		return type;
	}

	public Message getMessage() {
		return message;
	}

	public JSONObject getJSON() 
	{
		// create the jsonObject
		JSONObject jsonObject = new JSONObject();
		
		// populate the internals
		jsonObject.put("type", type.getValue() );
		jsonObject.put("message", type.getValue() );
		
		//
		return jsonObject;
	}
	
	
}
