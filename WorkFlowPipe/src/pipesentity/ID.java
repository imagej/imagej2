package pipesentity;

import org.json.JSONArray;
import org.json.JSONObject;

public class ID {
	
	private String id;
	
	public ID( String id )
	{
		this.id = id;
	}

	public ID( JSONObject json ) {
		System.out.println("ID constructor " + json.toString() );
	}

	public ID(JSONArray jsonArray) {
		System.out.println("ID array constructor " + jsonArray.toString() );
	}

	public String getValue() {
		return this.id;
	}

}
