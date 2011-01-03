package pipesentity;

import org.json.JSONObject;

public class Tgt {
	
	private String moduleid;
	private String id;

	public Tgt( String id, String moduleid )
	{
		this.id = id;
		this.moduleid = moduleid;
	}
	
	public Tgt( JSONObject json ) {
		System.out.println("Tgt constructor " + json.toString() );
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
		jsonOutput.put( "tgt", json );
		
		return jsonOutput;
	}
}
