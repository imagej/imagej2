package imagej.pipesentity;

import org.json.JSONArray;
import org.json.JSONObject;

public class ID {
	
	private String id;
	
	public ID( String id )
	{
		//System.out.println("ID string constructor " + id );
		this.id = id;
	}

	public String getValue() {
		return this.id;
	}

}
