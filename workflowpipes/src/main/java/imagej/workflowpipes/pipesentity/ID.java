package imagej.workflowpipes.pipesentity;

import java.io.Serializable;
import org.json.JSONArray;
import org.json.JSONObject;

public class ID implements Serializable {
	
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
