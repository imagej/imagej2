package pipesentity;

import org.json.JSONObject;

/**
 * This class represents the JSON structure in the UI
 * 
 * E.g."errors": {
    "modules": {},
    "pipe": null
  }
 * @author rick
 *
 */
public class Errors {
	
	private String modules = "{}";
	private String pipe = "null";
	
	public Errors()
	{
		//used for no errors
	}
	
	public Errors( String modulesValue, String pipeValue )
	{
		this.modules = modulesValue;
		this.pipe = pipeValue;
	}
	
	public JSONObject getJSONObject()
	{
		JSONObject jsonObject = new JSONObject();
		
		jsonObject.put("modules", modules);
		jsonObject.put("pipe", pipe);
		
		JSONObject outputObject = new JSONObject();
		outputObject.put( "errors", jsonObject );
		return outputObject;
	}
	
	@Override
	public String toString()
	{
		return getJSONObject().toString();
	}

}
