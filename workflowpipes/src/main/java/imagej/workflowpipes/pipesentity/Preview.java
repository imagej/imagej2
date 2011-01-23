package imagej.workflowpipes.pipesentity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

/**
 * Represents the JSON response for pipes preview call
 * @author rick
 *
 */
public class Preview implements Serializable {
	
	
	private JSONObject statsPua = new JSONObject();
	private HashMap<ID,JSONObject> previewHashMap = new HashMap<ID,JSONObject>();
	private HashMap<ID,Error> errorHashMap = new HashMap<ID,Error>();
	
	
	public Preview()
	{
		
	}

	/**
	 * Add the errors reported by the instance if any
	 * @param moduleID
	 * @param errors
	 */
	public void addErrors( ID moduleID, ArrayList<Error> errors ) {
		
		// if there are errors
		if ( errors.size() > 0)
		{
			// iterate through them and add them
			for(Error error : errors )
			{
				errorHashMap.put( moduleID, error );
			}
		}
		
	}

	public void addPreview( ID id, JSONObject previewJSON ) {
		previewHashMap.put( id, previewJSON );
	}

	/**
	 * @return the entire response message in support of a call to ajax.pipe.preview
	 */
	public JSONObject getJSON() 
	{
		JSONObject output = new JSONObject();
		
		// add ok response
		output.put( "ok", new Integer(1) );
		
		// add the errors
		JSONObject jsonErrors = new JSONObject();
		jsonErrors.put("modules", getModuleErrors() );
		jsonErrors.put("pipes", getPipeErrors() );
		output.put( "errors", jsonErrors  );
		
		// add the preview content
		output.put("preview", getPreviewJSON() );
		
		// add the stats
		JSONObject jsonPua = new JSONObject();
		jsonPua.put( "pua", statsPua );
		output.put( "stats", jsonPua );
		
		return output;
	}

	private String getPipeErrors() {
		JSONObject pipeErrors = new JSONObject();
		
		//TODO: implement pipe error reporting
		
		return null;
	}

	private JSONObject getPreviewJSON() 
	{
		JSONObject previewJSON = new JSONObject();
		
		for( ID id : previewHashMap.keySet() )
		{
			previewJSON.put( id.getValue(), previewHashMap.get( id ) );
		}
		
		return previewJSON;
	}

	// returns the JSONObject
	private JSONObject getModuleErrors() 
	{
		JSONObject jsonErrors = new JSONObject();
		
		// for each error
		for ( ID id : errorHashMap.keySet() )
		{
			// add the error
			jsonErrors.put( id.getValue(), errorHashMap.get( id ).getJSON() );
		}
		
		return jsonErrors;
	}

	public void addStats( String title, JSONObject statsJSON ) {
		statsPua.put( title, statsJSON );
	};
	
	

	

}
