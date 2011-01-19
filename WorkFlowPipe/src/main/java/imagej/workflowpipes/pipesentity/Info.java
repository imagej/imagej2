package imagej.workflowpipes.pipesentity;

import org.json.JSONObject;

import imagej.workflowpipes.pipesapi.Module;

public class Info {
	
	private Module pipesModule;
	
	public Info( Module pipesModule )
	{
		this.pipesModule = pipesModule;
	}
	
	public JSONObject getJSONObject()
	{
		//create a new JSON object
		JSONObject json = new JSONObject();
				
		//get the module JSON
		JSONObject moduleJSONObject = pipesModule.getJSONObject();
		
		//add the info layer
		json.put( "info",  moduleJSONObject );
		
		return json;
	}
	
}
