package pipesentity;

import org.json.JSONObject;

public class Src {
	
	private String moduleid;
	private String id;

	public Src( String id, String moduleid )
	{
		this.id = id;
		this.moduleid = moduleid;
	}
	
	public Src( JSONObject json ) {
		System.out.println("Src constructor " + json.toString() );
	}

	public JSONObject getJSONObject()
	{
		JSONObject json = new JSONObject();
		
		//populate the values
		json.put( "id", id );
		json.put( "moduleid", moduleid );
		
		// create an output module
		JSONObject jsonOutput = new JSONObject();
		
		// populate the output module
		jsonOutput.put( "src", json );
		
		return jsonOutput;
	}
}
